package kma.game.chess2d.game

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kma.game.chess2d.ai.Ai
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.CaptureTally
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.San
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.history.MatchHistoryDatabase
import kma.game.chess2d.history.MatchRecord
import kma.game.chess2d.history.MatchResult
import kma.game.chess2d.net.ClockTimes
import kma.game.chess2d.profile.MatchRecording
import kma.game.chess2d.profile.ProfileDatabase
import kma.game.chess2d.profile.ProfileRepository
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Giữ trạng thái ván đấu và là cầu nối duy nhất giữa giao diện và <code>:engine</code>.
 *
 * Danh sách nước đã đi được lưu vào [SavedStateHandle] thay vì lưu cả bàn cờ: chỉ
 * một <code>IntArray</code> nhỏ mà dựng lại được y nguyên mọi thứ, kể cả quyền nhập
 * thành và cả stack Undo. Nhờ vậy xoay máy hay bị hệ thống giải phóng tiến trình
 * đều không mất ván đang chơi.
 *
 * Ngoài ra, ván còn được ghi xuống đĩa qua [SavedGameStore] để sống qua cả lần tắt app
 * hẳn: [SavedStateHandle] chỉ cứu được trường hợp tiến trình bị thu hồi, còn người
 * chơi tự thoát app thì nó cũng mất theo.
 *
 * Là [AndroidViewModel] chỉ vì cần `Context` của ứng dụng cho DataStore. Không giữ
 * Context của Activity ở đây — đó là rò rỉ bộ nhớ kinh điển khi xoay máy.
 */
@OptIn(FlowPreview::class)
class GameViewModel(
    application: Application,
    private val savedState: SavedStateHandle,
) : AndroidViewModel(application) {

    private val store = SavedGameStore(application)

    /** Lịch sử ván đấu (mục 7.3). Chỉ ghi đúng một dòng khi ván kết thúc. */
    private val matches = MatchHistoryDatabase.get(application).matches()

    /** Hồ sơ người chơi: chỉ ván đấu máy được cộng vào đây (xem [MatchRecording.countsForProfile]). */
    private val profile = ProfileRepository(ProfileDatabase.get(application))

    /**
     * Ván hiện tại đã được cộng vào Hồ sơ hay chưa.
     *
     * Khác [recordedInHistory] ở chỗ **không** được reset khi Undo: thắng rồi Undo rồi thắng
     * lại không được tính hai thắng. Chỉ ván mới ([resetBoard]) mới mở khóa lại.
     */
    private var recordedInProfile = false

    /** Lúc đi nước đầu tiên của ván, để tính thời gian chơi; `null` khi chưa đi nước nào. */
    private var firstMoveAtMillis: Long? = null

    /** Đang đi lại ván cũ từ SavedStateHandle: các nước này đã được tính từ trước, không ghi lại. */
    private var replaying = false

    /**
     * Ván hiện tại đã được ghi vào lịch sử hay chưa.
     *
     * Cần cờ này vì sau khi hết ván người chơi vẫn có thể bấm Undo rồi đi lại, và
     * một ván chỉ nên nằm một lần trong lịch sử.
     */
    private var recordedInHistory = false

    /**
     * Hàng chờ ghi đĩa. `null` nghĩa là "xoá ván đã lưu".
     *
     * Phải debounce vì mỗi nước đi, mỗi lần Undo đều muốn ghi; ở chế độ hai người đi
     * nhanh thì đó là hàng chục lần ghi đĩa trong vài giây. Chỉ cần trạng thái **cuối**
     * đúng là đủ, nên dùng [collectLatest] để lần ghi cũ bị buông khi có yêu cầu mới.
     */
    private val saveRequests = MutableSharedFlow<SavedGame?>(extraBufferCapacity = 16)

    private var board = Engine.newGame()
    private val playedMoves = mutableListOf<Move>()

    /**
     * Ký hiệu SAN của từng nước đã đi, song song với [playedMoves].
     *
     * Phải sinh **ngay lúc đi** và giữ lại, vì SAN phụ thuộc thế cờ trước nước đó;
     * để đến lúc vẽ danh sách mới tính thì phải đi lại cả ván cho mỗi lần vẽ.
     */
    private val sanMoves = mutableListOf<String>()

    /** id của quân đang đứng ở từng ô, [NO_PIECE_ID] là ô trống. */
    private var squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }

    /** Ảnh chụp bản đồ id trước từng nước đi, để Undo trả lại đúng định danh cũ. */
    private val idHistory = ArrayDeque<IntArray>()
    private var nextPieceId = 0

    private var legalMoves: List<Move> = emptyList()
    private var selectedSquare = Squares.NONE
    private var pendingPromotion: PendingPromotion? = null

    private val ai = Ai()
    private var mode = GameMode.TWO_PLAYERS
    private var difficulty = Difficulty.MEDIUM

    /** Tên hai người và thể thức thời gian của ván hai người hiện tại. Đấu máy không dùng. */
    private var setup = LocalSetup(whiteName = "", blackName = "")

    /** Đồng hồ của ván hai người có giờ; `null` khi không giới hạn hoặc đang đấu máy. */
    private var clock: LocalClock? = null

    /** Khác `null` khi ván đã kết thúc vì hết giờ. */
    private var timeout: TimeoutResult? = null

    /** Màn hình đang yêu cầu dừng đồng hồ (mở Pause/Cài đặt, hoặc app xuống nền). */
    private var clockPaused = false

    /** Vòng cập nhật đồng hồ và phát hiện hết giờ. Chỉ chạy khi [clock] khác `null`. */
    private var tickJob: Job? = null

    private val _clockTimes = MutableStateFlow<ClockTimes?>(null)

    /**
     * Giờ còn lại đã làm tròn lên tới giây, `null` khi ván không bấm giờ. Tách khỏi [uiState] để
     * mỗi giây đổi không phải dựng lại danh sách quân và thống kê quân bị bắt.
     */
    val clockTimes: StateFlow<ClockTimes?> = _clockTimes.asStateFlow()

    /** Lượt nghĩ đang chạy. Giữ lại để hủy được khi người chơi đổi ý. */
    private var aiJob: Job? = null
    private var aiThinking = false

    /**
     * Đang xem lại thế cờ sau nước thứ (reviewPly + 1), `null` là đang ở thế hiện tại.
     *
     * Bàn cờ thật [board] **không** bị hoàn nguyên khi xem lại: thế cũ được dựng
     * trên một bàn riêng để vẽ. Nhờ vậy thoát xem lại là về đúng ván đang chơi, không
     * có cách nào làm mất những nước đã đi.
     */
    private var reviewPly: Int? = null

    /** Tiếng cần phát cho nước đi gần nhất. */
    private var soundCue: SoundCue? = null

    /** Đếm số lần yêu cầu phát tiếng, để hai tiếng giống nhau liền nhau vẫn khác cue. */
    private var soundSerial = 0

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        // Giờ và kết quả hết giờ phải đọc TRƯỚC khi dựng lại ván: lúc đi lại các nước cũ, saveMoves() ghi
        // đè giờ bằng đồng hồ mới tinh vào savedState, mất sạch số liệu cần khôi phục.
        val savedWhiteMillis = savedState.get<Long>(KEY_CLOCK_WHITE)
        val savedBlackMillis = savedState.get<Long>(KEY_CLOCK_BLACK)
        val savedTimeoutWhite = savedState.get<Boolean>(KEY_TIMEOUT_WHITE)
        val savedTimeoutDrawn = savedState.get<Boolean>(KEY_TIMEOUT_DRAWN) ?: false
        mode = savedState.get<String>(KEY_MODE)?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
            ?: GameMode.TWO_PLAYERS
        difficulty = savedState.get<String>(KEY_DIFFICULTY)?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
            ?: Difficulty.MEDIUM
        setup = LocalSetup(
            whiteName = savedState.get<String>(KEY_WHITE_NAME).orEmpty(),
            blackName = savedState.get<String>(KEY_BLACK_NAME).orEmpty(),
            time = savedState.get<String>(KEY_TIME_CONTROL)
                ?.let { runCatching { LocalTimeControl.valueOf(it) }.getOrNull() }
                ?: LocalTimeControl.UNLIMITED,
        )
        // Sau khi mode và setup đã đúng: resetBoard() dựng đồng hồ theo chúng.
        resetBoard()
        // Đi lại toàn bộ ván cũ nếu có. Các nước này đã từng hợp lệ nên không cần lọc lại.
        savedState.get<IntArray>(KEY_PLAYED_MOVES)?.let { raw ->
            replaying = true
            try {
                raw.forEach { applyMove(Move(it)) }
            } finally {
                replaying = false
            }
        }
        if (savedTimeoutWhite != null && clock != null) {
            // Ván đã kết thúc vì hết giờ trước khi tiến trình bị thu hồi: dựng lại kết quả, không ghi lịch sử lại.
            // resetBoard() đã xoá hai khóa này khỏi savedState nên phải ghi lại, không thì lần thu hồi sau mất kết quả.
            timeout = TimeoutResult(whiteFlagged = savedTimeoutWhite, drawn = savedTimeoutDrawn)
            savedState[KEY_TIMEOUT_WHITE] = savedTimeoutWhite
            savedState[KEY_TIMEOUT_DRAWN] = savedTimeoutDrawn
            recordedInHistory = true
            recordedInProfile = true
            if (savedWhiteMillis != null && savedBlackMillis != null) {
                restoreClock(savedWhiteMillis, savedBlackMillis)
            } else {
                stopClock()
            }
        } else if (savedWhiteMillis != null && savedBlackMillis != null) {
            restoreClock(savedWhiteMillis, savedBlackMillis)
        }
        publish()
        // Vòng ghi đĩa chạy suốt đời ViewModel. Đặt trên [Dispatchers.IO] vì đây là I/O thật.
        viewModelScope.launch {
            saveRequests.debounce(SAVE_DEBOUNCE_MILLIS).collectLatest { game ->
                withContext(Dispatchers.IO) {
                    if (game == null) store.clear() else store.save(game)
                }
            }
        }
        // Xoay máy đúng lúc đến lượt máy thì phải nghĩ lại: lượt nghĩ cũ đã chết cùng
        // ViewModel trước đó.
        maybeStartAiTurn()
    }

    /**
     * Người chơi chạm vào một ô. Mọi tương tác bàn cờ đi qua đúng hàm này.
     *
     * Nước đi luôn được đối chiếu với danh sách nước hợp lệ do engine sinh ra, nên
     * không có đường nào để giao diện thực hiện một nước sai luật.
     */
    fun onSquareTap(square: Int) {
        // Đang xem lại thì bàn cờ chỉ để ngắm: đi tại đây sẽ là đi từ thế cũ, không khớp
        // với thế hiện tại mà người chơi đang thấy.
        if (reviewPly != null) return
        if (pendingPromotion != null || gameFinished()) return
        // Không cho đi hộ máy, và không nhận chạm trong lúc máy đang nghĩ.
        if (isComputerTurn()) return

        if (selectedSquare == Squares.NONE) {
            selectIfOwnPiece(square)
            publish()
            return
        }
        if (square == selectedSquare) {
            selectedSquare = Squares.NONE
            publish()
            return
        }

        val moves = legalMoves.filter { it.from == selectedSquare && it.to == square }
        when {
            // Không đi được tới đó: coi như người chơi định chọn quân khác.
            moves.isEmpty() -> selectIfOwnPiece(square)
            moves.size == 1 -> {
                selectedSquare = Squares.NONE
                applyMove(moves.first())
            }
            // Nhiều nước cùng ô đi và ô đến thì chỉ có thể là phong cấp.
            else -> pendingPromotion =
                PendingPromotion(selectedSquare, square, board.whiteToMove, moves)
        }
        publish()
        maybeStartAiTurn()
    }

    fun onPromotionChosen(move: Move) {
        pendingPromotion = null
        selectedSquare = Squares.NONE
        applyMove(move)
        publish()
        maybeStartAiTurn()
    }

    /** Đóng hộp thoại mà không phong cấp: nước đi chưa hề được thực hiện nên không cần hoàn nguyên. */
    fun onPromotionDismissed() {
        pendingPromotion = null
        publish()
    }

    /**
     * Đổi chế độ chơi.
     *
     * Đổi chế độ là **bắt đầu một ván khác**, nên bàn cờ được dọn sạch. Trước đây
     * chỉ đổi biến [mode] mà giữ nguyên dãy nước đã đi, nên ván đấu máy đang dở bị mang
     * sang chế độ hai người và ngược lại — hai chế độ trông như lẫn vào nhau.
     *
     * ViewModel này dùng chung cho cả hai chế độ vì nó gắn vào Activity, nên dọn ở đây
     * là chỗ duy nhất chắc chắn chạy đúng một lần cho mỗi lần đổi chế độ.
     */
    fun setMode(newMode: GameMode) {
        if (mode == newMode) return
        cancelAiTurn()
        mode = newMode
        savedState[KEY_MODE] = newMode.name
        ai.newGame()
        resetBoard()
        saveMoves()
        publish()
        maybeStartAiTurn()
    }

    /**
     * Mở một ván hai người **mới** theo [newSetup]. Gọi khi người chơi bấm BẮT ĐẦU ở màn setup.
     *
     * Luôn dựng ván mới dù chế độ đang là hai người sẵn: [setMode] bỏ qua khi chế độ không đổi, nên nếu chỉ
     * dựa vào nó thì ván dở trước đó sẽ bị dùng lại với tên và thời gian cũ.
     *
     * [token] chống mở trùng: màn hình dựng lại khi xoay máy sẽ gọi lại hàm này với cùng token, và token đã
     * xử lý được nhớ trong [SavedStateHandle] nên lần gọi thứ hai bị bỏ qua.
     */
    fun startLocalGame(newSetup: LocalSetup, token: Int) {
        if (savedState.get<Int>(KEY_START_TOKEN) == token) return
        savedState[KEY_START_TOKEN] = token
        cancelAiTurn()
        reviewPly = null
        mode = GameMode.TWO_PLAYERS
        savedState[KEY_MODE] = mode.name
        applySetup(newSetup)
        ai.newGame()
        resetBoard()
        saveMoves()
        publish()
    }

    /**
     * Giao diện báo cần dừng hay chạy lại đồng hồ (mở/đóng Pause hay Cài đặt, app xuống nền/trở lại).
     * Ván không bấm giờ thì không có gì thay đổi ngoài việc nhớ cờ này cho ván sau.
     */
    fun setClockPaused(paused: Boolean) {
        if (clockPaused == paused) return
        clockPaused = paused
        val running = clock ?: return
        val now = nowMillis()
        if (paused) running.pause(now) else running.resume(now)
        syncClock()
        // Lưu giờ còn lại ngay: thoát app lúc đang dừng không được làm mất phần giờ đã trôi.
        if (paused) {
            persistClock()
            requestSave()
        }
    }

    fun setDifficulty(newDifficulty: Difficulty) {
        if (difficulty == newDifficulty) return
        // Đổi cấp giữa lúc máy đang nghĩ thì bỏ lượt nghĩ đó và tính lại theo cấp mới.
        cancelAiTurn()
        difficulty = newDifficulty
        savedState[KEY_DIFFICULTY] = newDifficulty.name
        publish()
        maybeStartAiTurn()
    }

    /**
     * Dựng lại ván đã lưu trên đĩa. Gọi khi người chơi chọn "Tiếp tục" ở sảnh.
     *
     * Chỉ chạy đúng một lần cho mỗi ViewModel: xoay máy sẽ gọi lại hàm này, mà lúc đó
     * ván đã được [SavedStateHandle] giữ sẵn — đi lại lần nữa sẽ nối đuôi nước đi thành
     * một ván lạ không ai chơi.
     *
     * Nước đi đọc từ đĩa **vẫn phải qua engine duyệt**: file có thể cũ, có thể bị sửa
     * tay, nên gặp nước không hợp lệ thì dừng ở đó và giữ phần đã đi được, thay vì
     * để một thế cờ sai luật luồn vào bàn cờ.
     */
    fun resumeSavedGame() {
        if (savedState.get<Boolean>(KEY_RESUMED) == true) return
        savedState[KEY_RESUMED] = true
        viewModelScope.launch {
            val game = withContext(Dispatchers.IO) { store.saved.first() } ?: return@launch
            // Hiện app luôn mở ván từ thế chuẩn. Ván lưu với thế đầu khác (sau này mới có,
            // ví dụ từ câu đố) thì chưa dựng lại được nên bỏ qua cho an toàn.
            if (game.startFen != Engine.START_FEN) return@launch

            cancelAiTurn()
            ai.newGame()
            // Chế độ, cấp độ và setup phải đặt TRƯỚC resetBoard(): nó dựng đồng hồ theo chúng.
            mode = game.mode
            savedState[KEY_MODE] = game.mode.name
            difficulty = game.difficulty
            savedState[KEY_DIFFICULTY] = game.difficulty.name
            applySetup(LocalSetup(game.whiteName, game.blackName, game.timeControl))
            resetBoard()
            // Đi lại nước cũ không được chạy đồng hồ: giờ còn lại lấy từ bản lưu bên dưới.
            replaying = true
            try {
                for (uci in game.uciMoves) {
                    val move = Engine.legalMoves(board).firstOrNull { it.toUci() == uci } ?: break
                    applyMove(move)
                }
            } finally {
                replaying = false
            }
            if (clock != null) restoreClock(game.whiteMillis, game.blackMillis)
            publish()
            maybeStartAiTurn()
        }
    }

    /**
     * Chuyển sang xem lại thế cờ sau nước thứ [ply] (đếm từ 0), `null` để về hiện tại.
     *
     * Bỏ chọn quân và đóng hộp phong cấp khi vào chế độ xem lại: những thứ đó thuộc
     * thế hiện tại, để lại trên một thế cờ khác chỉ gây hiểu nhầm.
     */
    fun reviewAt(ply: Int?) {
        val target = ply?.coerceIn(0, playedMoves.lastIndex.coerceAtLeast(0))
            ?.takeIf { playedMoves.isNotEmpty() }
        if (reviewPly == target) return
        reviewPly = target
        selectedSquare = Squares.NONE
        pendingPromotion = null
        publish()
    }

    fun undo() {
        // Ván có giờ không cho Đi lại: hoàn nguyên một nước thì phải trả lại giờ đã trừ, và người chơi
        // sẽ lợi dụng để đi lại miễn phí giờ. Giao diện cũng đã khóa nút này (xem canUndo trong publish).
        if (clock != null) return
        cancelAiTurn()
        reviewPly = null
        if (!board.canUndo()) return
        undoOneMove()
        // Ở chế độ đấu máy, một lần bấm phải trả về đúng lượt của người chơi. Hoàn nguyên
        // đúng một nước sẽ trả về lượt máy, và máy lập tức đi lại — nhìn như Undo hỏng.
        if (mode == GameMode.VS_COMPUTER && board.whiteToMove == AI_PLAYS_WHITE && board.canUndo()) {
            undoOneMove()
        }
        saveMoves()
        publish()
    }

    fun newGame() {
        cancelAiTurn()
        reviewPly = null
        // Xóa bộ nhớ của AI: điểm của ván cũ không còn ý nghĩa với ván mới.
        ai.newGame()
        resetBoard()
        saveMoves()
        publish()
        maybeStartAiTurn()
    }

    /**
     * Giao lượt cho máy nếu đúng lúc.
     *
     * Ba điểm đáng chú ý, đây cũng là ba cái bẫy được ghi ở mục 5.4:
     *
     * 1. Nghĩ trên [Dispatchers.Default], KHÔNG trên main thread. Search cấp Khó chạy
     *    tới 5 giây, đủ để Android báo ANR nếu chặn main thread.
     * 2. Máy nghĩ trên một BẢN SAO của bàn cờ. Search đi và hoàn nguyên hàng triệu
     *    nước; nếu dùng chung bàn cờ với giao diện thì UI sẽ đọc phải những thế cờ
     *    nửa vời đang thử dở.
     * 3. Bản sao được dựng bằng cách đi lại cả ván chứ không phải copy từ FEN: có
     *    vậy AI mới thấy được lịch sử lặp thế để tính luật hòa ba lần lặp.
     */
    private fun maybeStartAiTurn() {
        if (!isComputerTurn()) return
        if (aiJob?.isActive == true) return

        val snapshot = replayOnFreshBoard()
        val chosenDifficulty = difficulty
        aiThinking = true
        publish()

        aiJob = viewModelScope.launch {
            val move = withContext(Dispatchers.Default) {
                val job = coroutineContext[Job]
                ai.chooseMove(
                    board = snapshot,
                    difficulty = chosenDifficulty,
                    // Đây là điểm hủy thực sự: rời màn hình thì viewModelScope bị hủy, job
                    // thành không còn active, và search tự dừng thay vì chạy tiếp trong nền.
                    isActive = { job?.isActive != false },
                )
            }

            aiThinking = false
            // Kiểm tra lại một lần nữa: ván có thể đã khác nếu người chơi bấm Undo hoặc
            // Ván mới đúng lúc máy vừa nghĩ xong.
            if (move != Move.NONE && legalMoves.any { it.raw == move.raw }) {
                applyMove(move)
            }
            publish()
        }
    }

    private fun cancelAiTurn() {
        aiJob?.cancel()
        aiJob = null
        aiThinking = false
    }

    private fun isComputerTurn(): Boolean =
        mode == GameMode.VS_COMPUTER &&
            board.whiteToMove == AI_PLAYS_WHITE &&
            !Rules.isGameOver(board)

    /**
     * Dựng một bàn cờ mới rồi đi lại [plies] nước đầu tiên của ván.
     *
     * Mặc định đi lại toàn bộ ván (bản sao cho AI). Truyền số nhỏ hơn để lấy một thế
     * cờ giữa ván khi xem lại.
     */
    private fun replayOnFreshBoard(plies: Int = playedMoves.size): Board {
        val copy = Engine.newGame()
        for (index in 0 until plies) copy.makeMove(playedMoves[index])
        return copy
    }

    /**
     * Danh sách quân trên một bàn cờ bất kỳ, dùng khi xem lại.
     *
     * Ở đây lấy luôn số ô làm id: bản đồ id thật chỉ đúng cho thế hiện tại, mà xem
     * lại thì không cần định danh liên tục — nhảy từ thế này sang thế khác không phải
     * là một nước đi để mà chạy hoạt ảnh.
     */
    private fun piecesOf(shownBoard: Board): List<PieceOnBoard> =
        (0 until Squares.COUNT).mapNotNull { square ->
            val piece = shownBoard.pieceAt(square)
            if (piece == Piece.NONE) null else PieceOnBoard(square, piece, square)
        }

    private fun undoOneMove() {
        board.unmakeMove()
        playedMoves.removeAt(playedMoves.lastIndex)
        sanMoves.removeAt(sanMoves.lastIndex)
        squareIds = idHistory.removeLast()
        // Đã hoàn nguyên thì ván lại đang chơi; lần kết thúc sau đáng được ghi tiếp.
        recordedInHistory = false
        selectedSquare = Squares.NONE
        pendingPromotion = null
    }

    private fun selectIfOwnPiece(square: Int) {
        val piece = board.pieceAt(square)
        val ownPiece = piece != Piece.NONE && Piece.isColor(piece, board.whiteToMove)
        // Chỉ chọn khi quân đó thực sự còn nước đi, tránh highlight một ô rồi không làm gì.
        selectedSquare = if (ownPiece && legalMoves.any { it.from == square }) square else Squares.NONE
    }

    private fun applyMove(move: Move) {
        val movingWhite = board.whiteToMove
        // Sinh SAN trước khi đi: sau makeMove thì không còn biết quân nào khác cũng
        // đi tới được ô đó, mà đó là thứ quyết định có phải viết thêm ô đi hay không.
        val san = San.of(board, move, legalMoves.ifEmpty { Engine.legalMoves(board) })
        // Đi một nước mới thì đương nhiên là thôi xem lại: người chơi cần thấy nước vừa đi.
        reviewPly = null
        idHistory.addLast(squareIds.copyOf())
        board.makeMove(move)

        // Bản đồ id phải phản chiếu đúng những gì makeMove vừa làm trên bàn cờ.
        if (move.isEnPassant) {
            // Quân bị ăn không nằm ở ô đến, nên phải xóa riêng.
            squareIds[if (movingWhite) move.to - 8 else move.to + 8] = NO_PIECE_ID
        }
        squareIds[move.to] = squareIds[move.from]
        squareIds[move.from] = NO_PIECE_ID
        if (move.isCastle) {
            val rookFrom = if (move.flag == Move.CASTLE_KING) move.to + 1 else move.to - 2
            val rookTo = if (move.flag == Move.CASTLE_KING) move.to - 1 else move.to + 1
            squareIds[rookTo] = squareIds[rookFrom]
            squareIds[rookFrom] = NO_PIECE_ID
        }

        if (playedMoves.isEmpty()) firstMoveAtMillis = System.currentTimeMillis()
        playedMoves.add(move)
        sanMoves.add(san)
        // Đi lại nước cũ (xoay máy, dựng lại từ đĩa) không phải nước mới: không được trừ giờ.
        if (!replaying) advanceClock()
        noteSound(move)
        saveMoves()
        recordIfFinished()
    }

    /**
     * Ghi ván vừa kết thúc vào lịch sử.
     *
     * Chỉ lưu FEN đầu + dãy nước UCI chứ không lưu thế cờ: xem lại thì đi lại bằng
     * engine, vừa nhẹ vừa không bao giờ lệch với luật hiện tại.
     *
     * Kết quả ghi theo góc nhìn Bên Trắng, cũng chính là góc nhìn người chơi khi đấu máy
     * vì máy luôn cầm Đen ([AI_PLAYS_WHITE]).
     */
    private fun recordIfFinished() {
        if (recordedInHistory) return
        val status = Rules.status(board)
        val result = when (status) {
            // Hết ván bởi chiếu hết: bên đến lượt chính là bên bị thua.
            GameStatus.CHECKMATE -> if (board.whiteToMove) MatchResult.LOSS else MatchResult.WIN
            GameStatus.STALEMATE,
            GameStatus.DRAW_FIFTY_MOVES,
            GameStatus.DRAW_REPETITION,
            GameStatus.DRAW_INSUFFICIENT_MATERIAL -> MatchResult.DRAW
            // Ván chưa xong thì chưa có gì để ghi.
            GameStatus.ONGOING, GameStatus.CHECK -> return
        }

        // Dựng lại một ván đã xong (khôi phục sau khi tiến trình bị thu hồi) không phải ván mới kết thúc: nó
        // đã nằm trong lịch sử và Hồ sơ từ trước, ghi lại sẽ thành bản trùng.
        if (replaying) {
            recordedInHistory = true
            recordedInProfile = true
            return
        }
        insertHistory(result)
        recordInProfile(result, byCheckmate = status == GameStatus.CHECKMATE)
    }

    /**
     * Ghi một dòng vào lịch sử cho ván vừa kết thúc, dù kết thúc bằng chiếu hết, hòa hay hết giờ.
     *
     * Ván hai người lưu tên hai bên vào cột `opponent` (dạng "Tên 1 vs Tên 2") để lịch sử biết ai đã đấu với ai.
     * Chạy trên [NonCancellable] vì người chơi hay thoát màn ngay sau khi ván kết thúc; hủy viewModelScope
     * giữa chừng không được làm mất ván đã chơi.
     */
    private fun insertHistory(result: String) {
        recordedInHistory = true
        val record = MatchRecord(
            playedAtMillis = System.currentTimeMillis(),
            mode = mode.name,
            difficulty = if (mode == GameMode.VS_COMPUTER) difficulty.name else null,
            opponent = if (mode == GameMode.TWO_PLAYERS) playersLabel() else "",
            startFen = Engine.START_FEN,
            moves = playedMoves.joinToString(" ") { it.toUci() },
            result = result,
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO + NonCancellable) { matches.insert(record) }
        }
    }

    /** "Tên Trắng vs Tên Đen", hoặc rỗng khi chưa có tên nào. */
    private fun playersLabel(): String =
        if (setup.whiteName.isBlank() && setup.blackName.isBlank()) {
            ""
        } else {
            "${setup.whiteName} vs ${setup.blackName}"
        }

    /**
     * Cộng ván vừa xong vào Hồ sơ (một lần cho mỗi ván, và chỉ với chế độ đấu máy).
     *
     * Chạy trên [NonCancellable] vì người chơi hay thoát màn ngay sau khi
     * chiếu hết; hủy viewModelScope giữa chừng không được làm mất ván thắng.
     */
    private fun recordInProfile(result: String, byCheckmate: Boolean) {
        if (recordedInProfile || !MatchRecording.countsForProfile(mode)) return
        recordedInProfile = true
        // Đi lại ván cũ (xoay máy sau khi ván đã xong) không phải ván mới kết thúc: đã cộng từ lần trước.
        if (replaying) return
        val seconds = MatchRecording.elapsedSeconds(firstMoveAtMillis, System.currentTimeMillis())
        viewModelScope.launch {
            withContext(Dispatchers.IO + NonCancellable) {
                profile.recordMatch(result, byCheckmate && result == MatchResult.WIN, seconds)
            }
        }
    }

    /**
     * Chọn tiếng cho nước vừa đi.
     *
     * Thứ tự ưu tiên theo mức "quan trọng" với người chơi: hết ván đè lên chiếu, chiếu
     * đè lên ăn quân. Một nước vừa ăn quân vừa chiếu hết thì chỉ nên nghe một tiếng,
     * và tiếng đáng nghe là tiếng kết thúc ván.
     *
     * Gọi ngay trong [applyMove] vì lúc này mới còn biết nước đi là gì; [publish] chỉ
     * thấy bàn cờ sau nước đi nên không phân biệt được ăn quân hay không.
     */
    private fun noteSound(move: Move) {
        val sound = when {
            Rules.isGameOver(board) -> MoveSound.GAME_END
            board.isInCheck() -> MoveSound.CHECK
            move.isCapture || move.isEnPassant -> MoveSound.CAPTURE
            else -> MoveSound.MOVE
        }
        soundCue = SoundCue(++soundSerial, sound)
    }

    /**
     * Đẩy trạng thái hiện tại vào hàng chờ ghi đĩa.
     *
     * Ván đã xong hoặc chưa đi nước nào thì xoá luôn bản lưu: không có gì để "tiếp
     * tục", và câu hỏi ở sảnh cũng không nên hiện nữa.
     */
    private fun requestSave() {
        val finished = gameFinished()
        val snapshot = if (finished || playedMoves.isEmpty()) {
            null
        } else {
            val twoPlayers = mode == GameMode.TWO_PLAYERS
            val times = clock?.snapshot(nowMillis())
            SavedGame(
                startFen = Engine.START_FEN,
                uciMoves = playedMoves.map { it.toUci() },
                mode = mode,
                difficulty = difficulty,
                whiteName = if (twoPlayers) setup.whiteName else "",
                blackName = if (twoPlayers) setup.blackName else "",
                timeControl = if (twoPlayers) setup.time else LocalTimeControl.UNLIMITED,
                whiteMillis = times?.whiteMillis ?: 0L,
                blackMillis = times?.blackMillis ?: 0L,
            )
        }
        saveRequests.tryEmit(snapshot)
    }

    private fun resetBoard() {
        board = Engine.newGame()
        playedMoves.clear()
        sanMoves.clear()
        idHistory.clear()
        nextPieceId = 0
        squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }
        for (square in 0 until Squares.COUNT) {
            if (board.pieceAt(square) != Piece.NONE) squareIds[square] = nextPieceId++
        }
        selectedSquare = Squares.NONE
        pendingPromotion = null
        recordedInHistory = false
        recordedInProfile = false
        firstMoveAtMillis = null
        // Ván mới thì không còn nước nào để phát tiếng, kể cả tiếng của ván trước.
        soundCue = null
        // Ván mới xóa kết quả hết giờ cũ và dựng đồng hồ mới theo chế độ và setup hiện tại.
        timeout = null
        savedState.remove<Boolean>(KEY_TIMEOUT_WHITE)
        savedState.remove<Boolean>(KEY_TIMEOUT_DRAWN)
        rebuildClock()
    }

    /** Đặt tên và thể thức thời gian cho ván hai người và nhớ vào [SavedStateHandle]. Chưa dựng đồng hồ. */
    private fun applySetup(newSetup: LocalSetup) {
        setup = newSetup
        savedState[KEY_WHITE_NAME] = newSetup.whiteName
        savedState[KEY_BLACK_NAME] = newSetup.blackName
        savedState[KEY_TIME_CONTROL] = newSetup.time.name
    }

    private fun saveMoves() {
        savedState[KEY_PLAYED_MOVES] = IntArray(playedMoves.size) { playedMoves[it].raw }
        persistClock()
        requestSave()
    }

    // ---- Đồng hồ ván hai người ----

    /** Thời gian đơn điệu, không nhảy khi đổi giờ hệ thống. */
    private fun nowMillis(): Long = SystemClock.elapsedRealtime()

    /** Ván đã xong: chiếu hết, hòa theo luật, hoặc hết giờ. */
    private fun gameFinished(): Boolean = timeout != null || Rules.isGameOver(board)

    /**
     * Dựng đồng hồ mới cho ván vừa reset: chỉ ván hai người có giờ mới có đồng hồ.
     * Nếu màn hình đang yêu cầu dừng (ví dụ bấm Khởi động lại trong Pause Menu) thì đồng hồ mới cũng đang dừng.
     */
    private fun rebuildClock() {
        tickJob?.cancel()
        tickJob = null
        clock = if (mode == GameMode.TWO_PLAYERS && setup.time.limited) {
            LocalClock(setup.time.initialMillis).also { if (clockPaused) it.pause(nowMillis()) }
        } else {
            null
        }
        syncClock()
        if (clock != null) startTicker()
    }

    /**
     * Thay đồng hồ bằng bản khôi phục từ giờ còn lại đã lưu (đĩa hoặc savedState). Đồng hồ khôi phục bắt đầu
     * ở trạng thái dừng, chỉ chạy lại nếu màn hình không yêu cầu dừng, để thời gian app bị tắt không bị tính.
     */
    private fun restoreClock(whiteMillis: Long, blackMillis: Long) {
        if (clock == null) return
        val restored = LocalClock.restore(
            initialMillis = setup.time.initialMillis,
            whiteMillis = whiteMillis,
            blackMillis = blackMillis,
            whiteToMove = board.whiteToMove,
            started = playedMoves.isNotEmpty(),
        )
        if (gameFinished()) {
            restored.stop(nowMillis())
            // Ván đã xong thì không cần vòng cập nhật nữa.
            tickJob?.cancel()
            tickJob = null
        } else if (!clockPaused) {
            restored.resume(nowMillis())
        }
        clock = restored
        persistClock()
        syncClock()
    }

    /** Ghi nhận nước vừa đi: trừ giờ, chuyển bên, và dừng hẳn nếu nước đó kết thúc ván. */
    private fun advanceClock() {
        val running = clock ?: return
        val now = nowMillis()
        running.onMovePlayed(now)
        if (Rules.isGameOver(board)) stopClock() else syncClock()
    }

    /** Dừng hẳn đồng hồ và vòng cập nhật (ván đã xong). Giờ còn lại vẫn hiện đúng con số cuối. */
    private fun stopClock() {
        clock?.stop(nowMillis())
        tickJob?.cancel()
        tickJob = null
        syncClock()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MILLIS)
                onTick()
            }
        }
    }

    private fun onTick() {
        val running = clock ?: return
        syncClock()
        if (clockPaused || gameFinished()) return
        val whiteFlagged = running.flaggedSide(nowMillis()) ?: return
        onFlag(whiteFlagged)
    }

    /** Đẩy giờ còn lại (làm tròn lên tới giây) ra [clockTimes]; StateFlow tự bỏ qua nếu con số không đổi. */
    private fun syncClock() {
        _clockTimes.value = clock?.snapshot(nowMillis())?.let {
            ClockTimes(
                whiteMillis = ClockFormat.roundUpToSecond(it.whiteMillis),
                blackMillis = ClockFormat.roundUpToSecond(it.blackMillis),
            )
        }
    }

    /** Ghi giờ còn lại vào [SavedStateHandle] để khôi phục được nếu tiến trình bị thu hồi. */
    private fun persistClock() {
        val times = clock?.snapshot(nowMillis())
        if (times == null) {
            savedState.remove<Long>(KEY_CLOCK_WHITE)
            savedState.remove<Long>(KEY_CLOCK_BLACK)
        } else {
            savedState[KEY_CLOCK_WHITE] = times.whiteMillis
            savedState[KEY_CLOCK_BLACK] = times.blackMillis
        }
    }

    /**
     * Một bên hết giờ: kết thúc ván, phát tiếng hết ván, ghi lịch sử và xoá bản lưu trên đĩa.
     *
     * Kết quả theo [TimeoutRule]: bên còn lại thắng, trừ khi không đủ quân để chiếu hết thì hòa.
     */
    private fun onFlag(whiteFlagged: Boolean) {
        val result = TimeoutRule.resultForWhite(board, whiteFlagged)
        val flagged = TimeoutResult(whiteFlagged = whiteFlagged, drawn = result == MatchResult.DRAW)
        timeout = flagged
        savedState[KEY_TIMEOUT_WHITE] = flagged.whiteFlagged
        savedState[KEY_TIMEOUT_DRAWN] = flagged.drawn
        stopClock()
        selectedSquare = Squares.NONE
        pendingPromotion = null
        soundCue = SoundCue(++soundSerial, MoveSound.GAME_END)
        if (!recordedInHistory) insertHistory(result)
        // Ván đã xong nên requestSave() sẽ xoá bản lưu: không còn gì để "tiếp tục".
        requestSave()
        publish()
    }

    private fun publish() {
        legalMoves = Engine.legalMoves(board)
        val review = reviewPly
        // Khi xem lại, mọi thứ vẽ trên bàn cờ dựng riêng cho thế cũ; còn trạng thái ván
        // (lượt ai, kết quả, Undo) vẫn là của thế hiện tại — xem lại không đổi ván.
        val shownBoard = if (review == null) board else replayOnFreshBoard(review + 1)
        val shownMove = if (review == null) playedMoves.lastOrNull() else playedMoves[review]
        // Thống kê quân bị bắt tính lại từ danh sách nước đi, và tính đúng tới nước đang
        // xem: xem lại giữa ván mà vẫn hiện hàng quân của thế cuối thì sai với bàn cờ.
        val tally = CaptureTally.of(
            startFen = Engine.START_FEN,
            moves = playedMoves,
            plies = review?.plus(1) ?: playedMoves.size,
        )
        _uiState.value = GameUiState(
            pieces = if (review == null) currentPieces() else piecesOf(shownBoard),
            whiteToMove = board.whiteToMove,
            status = Rules.status(board),
            selectedSquare = selectedSquare,
            legalTargets = targetsFrom(selectedSquare),
            lastMoveFrom = shownMove?.from ?: Squares.NONE,
            lastMoveTo = shownMove?.to ?: Squares.NONE,
            checkedKingSquare = if (shownBoard.isInCheck()) {
                shownBoard.kingSquare(shownBoard.whiteToMove)
            } else {
                Squares.NONE
            },
            canUndo = board.canUndo() && clock == null,
            pendingPromotion = pendingPromotion,
            mode = mode,
            difficulty = difficulty,
            aiThinking = aiThinking,
            soundCue = soundCue,
            sanMoves = sanMoves.toList(),
            reviewPly = review,
            takenFromWhite = tally.takenFromWhite,
            takenFromBlack = tally.takenFromBlack,
            materialBalance = tally.materialBalance,
            whiteName = if (mode == GameMode.TWO_PLAYERS) setup.whiteName else "",
            blackName = if (mode == GameMode.TWO_PLAYERS) setup.blackName else "",
            timeLimited = clock != null,
            timeout = timeout,
        )
    }

    private fun currentPieces(): List<PieceOnBoard> = (0 until Squares.COUNT).mapNotNull { square ->
        val piece = board.pieceAt(square)
        if (piece == Piece.NONE) null else PieceOnBoard(squareIds[square], piece, square)
    }

    private fun targetsFrom(square: Int): Set<Int> {
        if (square == Squares.NONE) return emptySet()
        // Phong cấp sinh bốn nước cùng ô đến, Set tự gộp lại thành một gợi ý.
        return legalMoves.filter { it.from == square }.mapTo(HashSet()) { it.to }
    }

    private companion object {
        const val KEY_PLAYED_MOVES = "playedMoves"
        const val KEY_MODE = "gameMode"
        const val KEY_DIFFICULTY = "difficulty"
        const val KEY_RESUMED = "resumedFromDisk"
        const val KEY_START_TOKEN = "localStartToken"
        const val KEY_WHITE_NAME = "localWhiteName"
        const val KEY_BLACK_NAME = "localBlackName"
        const val KEY_TIME_CONTROL = "localTimeControl"
        const val KEY_CLOCK_WHITE = "clockWhiteMillis"
        const val KEY_CLOCK_BLACK = "clockBlackMillis"
        const val KEY_TIMEOUT_WHITE = "timeoutWhiteFlagged"
        const val KEY_TIMEOUT_DRAWN = "timeoutDrawn"
        const val NO_PIECE_ID = -1

        /**
         * Nhịp cập nhật đồng hồ. 250 ms đủ dày để phát hiện hết giờ trễ không quá một phần tư giây, và con số
         * hiển thị chỉ đổi mỗi giây nên giao diện không phải vẽ lại nhịp này.
         */
        const val TICK_MILLIS = 250L

        /**
         * Chờ lặng một nhịp rồi mới ghi đĩa.
         *
         * Nửa giây đủ ngắn để thoát app ngay sau khi đi vẫn kịp lưu, và đủ dài để gộp
         * một chuỗi nước đi nhanh thành một lần ghi.
         */
        const val SAVE_DEBOUNCE_MILLIS = 500L

        /** Máy cầm Đen, người chơi đi trước. */
        const val AI_PLAYS_WHITE = false
    }
}
