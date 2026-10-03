package kma.game.chess2d.lan

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.net.Inet4Address
import java.net.NetworkInterface
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.history.MatchHistoryDatabase
import kma.game.chess2d.history.MatchRecord
import kma.game.chess2d.history.MatchResult
import kma.game.chess2d.net.LanEndpoint
import kma.game.chess2d.net.LanErrorCode
import kma.game.chess2d.net.LanEvent
import kma.game.chess2d.net.LanGameState
import kma.game.chess2d.net.LanGuest
import kma.game.chess2d.net.LanHost
import kma.game.chess2d.net.LanRole
import kma.game.chess2d.net.LanRoomSource
import kma.game.chess2d.net.RoomInfo
import kma.game.chess2d.net.RoomSource
import kma.game.chess2d.net.LanOutcome
import kma.game.chess2d.net.TimeControl
import kma.game.chess2d.profile.MatchRecording
import kma.game.chess2d.profile.ProfileDatabase
import kma.game.chess2d.profile.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Cầu nối duy nhất giữa giao diện và module <code>:net</code>.
 *
 * Phân vai rạch ròi: <code>:net</code> giữ luật chơi và đường truyền, lớp này chỉ làm ba
 * việc — đổi [LanGameState] thành thứ vẽ được, giữ trạng thái chọn quân của riêng màn
 * hình, và chuyển cú chạm thành những lời gọi có sẵn của <code>LanEndpoint</code>.
 *
 * Không lưu vào SavedStateHandle như <code>GameViewModel</code>: một phiên LAN không dựng
 * lại được từ danh sách nước đi, vì socket đã chết cùng tiến trình. Xoay máy không
 * mất ván vì ViewModel sống qua cả configuration change; bị hệ thống giải phóng tiến
 * trình thì phải vào lại phòng — đúng như mọi game LAN khác.
 */
class LanViewModel(application: Application) : AndroidViewModel(application) {

    /** Lịch sử ván đấu (mục 7.3): ván LAN cũng được lưu như ván offline. */
    private val matches = MatchHistoryDatabase.get(application).matches()

    /** Hồ sơ người chơi: ván LAN cũng được cộng thắng/thua/hòa theo phía máy này. */
    private val profile = ProfileRepository(ProfileDatabase.get(application))

    /** Mã ván đang được đo giờ và lúc máy này thấy ván đó lần đầu, để tính thời gian chơi. */
    private var timedGameId: Int? = null
    private var timedGameStartMillis: Long = 0L

    /**
     * Mã ván đã ghi vào lịch sử gần nhất.
     *
     * So theo mã ván chứ không dùng cờ bật/tắt: đấu lại sẽ đổi mã ván, mà trạng thái
     * kết thúc thì có thể được đẩy lên nhiều lần cho cùng một ván.
     */
    private var recordedGameId: Int? = null

    /**
     * Nơi lấy danh sách phòng cho sảnh.
     *
     * Khai báo theo kiểu [RoomSource] chứ không phải `RoomScanner`: ViewModel chỉ cần
     * "một nơi có phòng", không cần biết beacon UDP hay cổng nào. Nhờ vậy luồng sảnh
     * kiểm tra được bằng nguồn giả trong test JVM.
     */
    private val roomSource: RoomSource = LanRoomSource()
    private val mirror = BoardMirror()

    /**
     * Mã phòng do chính máy này mở, `null` khi không mở phòng nào.
     *
     * Beacon là broadcast nên máy mở phòng cũng nghe thấy beacon của chính nó, và
     * phòng đó hiện ra ở cuối danh sách. Bấm vào chỉ có thể lỗi: máy tự nối vào
     * ServerSocket của mình, host thấy "đã có ván" rồi từ chối. Lọc theo mã phòng chứ
     * không lọc theo địa chỉ vì địa chỉ nhận được có thể là IP của một interface khác
     * cùng máy.
     */
    private var hostedRoomId: String? = null

    private var scanJob: Job? = null
    private var sessionJob: Job? = null
    private var endpoint: LanEndpoint? = null

    /**
     * Cách nối lại phòng hiện tại, chỉ có nghĩa với khách.
     *
     * Giữ nguyên hàm đã dùng lúc vào phòng chứ không chỉ giữ địa chỉ: vào bằng phòng
     * tìm thấy và vào bằng địa chỉ gõ tay là hai đường khác nhau, mà [retry] thì phải
     * đi lại đúng đường cũ.
     */
    private var reconnect: (suspend () -> Unit)? = null

    /** Trạng thái chọn quân là chuyện riêng của máy này, không gửi qua mạng. */
    private var selectedSquare = Squares.NONE
    private var pendingPromotion: PendingPromotion? = null

    private val _uiState = MutableStateFlow(
        LanUiState(
            lobby = LanLobbyUiState(localName = defaultLocalName()),
            localAddresses = localIpv4Addresses(),
        ),
    )
    val uiState: StateFlow<LanUiState> = _uiState.asStateFlow()

    init {
        startScan()
    }

    // --------------------------------------------------------------- sảnh chờ

    /** Bắt đầu nghe beacon. Gọi lại nhiều lần không sao: lần thứ hai bị bỏ qua. */
    fun startScan() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
            launch { roomSource.discover() }
            roomSource.rooms.collect { rooms ->
                _uiState.update { it.copy(lobby = it.lobby.copy(rooms = arrangeRooms(rooms))) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        roomSource.clear()
        _uiState.update { it.copy(lobby = it.lobby.copy(rooms = emptyList())) }
    }

    fun setLocalName(name: String) {
        _uiState.update { it.copy(lobby = it.lobby.copy(localName = name)) }
    }

    fun setManualAddress(address: String) {
        _uiState.update { it.copy(lobby = it.lobby.copy(manualAddress = address)) }
    }

    /**
     * Chọn thể thức thời gian cho phòng sắp mở.
     *
     * Chỉ nhận khi còn ở sảnh: phòng đã mở thì khách có thể đã nhận thể thức cũ qua
     * `Welcome`, đổi nửa đường sẽ làm hai máy hiển thị hai con số khác nhau.
     */
    fun setTimeControl(control: TimeControl) {
        if (_uiState.value.phase != LanPhase.LOBBY) return
        _uiState.update { it.copy(lobby = it.lobby.copy(timeControl = control)) }
    }

    /**
     * Mở phòng: vừa lắng nghe TCP vừa phát beacon, và làm trọng tài của ván.
     *
     * **Không đổi màn hình.** Pha mới là [LanPhase.HOSTING], tức vẫn ở sảnh: người mở
     * phòng đứng đó chạm thẻ "Phòng của bạn", đọc địa chỉ cho người kia, và vẫn thấy
     * các phòng khác trong mạng. Chỉ khi bắt tay xong (xem [LanEvent.Connected]) màn hình
     * mới chuyển sang bàn cờ.
     */
    fun host() {
        val host = LanHost(
            localName = currentName(),
            timeControl = _uiState.value.lobby.timeControl,
        )
        hostedRoomId = host.roomId
        startSession(host, LanPhase.HOSTING) {
            host.run { port ->
                _uiState.update { it.copy(hostPort = port) }
            }
        }
    }

    /**
     * Huỷ phòng vừa mở, khi chưa ai vào.
     *
     * Tách hẳn khỏi [leave] chứ không dùng chung: [leave] là "rời một ván đang chơi" nên
     * nó gửi Bye cho đối thủ và phải hỏi xác nhận trước; còn ở đây chưa có đối thủ nào
     * để gửi và cũng không có gì để mất, nên bấm là đóng ngay. Huỷ job là đủ để đóng
     * cả ServerSocket và tắt advertiser, vì `LanHost.run` dọn chúng trong khối finally.
     */
    fun cancelHosting() {
        if (_uiState.value.phase != LanPhase.HOSTING) return
        val job = sessionJob
        hostedRoomId = null
        endpoint = null
        reconnect = null
        sessionJob = null
        viewModelScope.launch { job?.cancelAndJoin() }
        _uiState.update {
            it.copy(
                phase = LanPhase.LOBBY,
                role = null,
                connected = false,
                waitingForOpponent = false,
                hostPort = 0,
                opponentName = "",
                offerRetry = false,
                notice = null,
                board = LanBoardUiState(),
            )
        }
        startScan()
    }

    fun join(room: RoomInfo) {
        // Chặn ngay tại đây thay vì để host từ chối: đỡ một vòng bắt tay và thông báo
        // cũng rõ hơn, vì beacon đã nói sẵn phiên bản giao thức của phòng đó.
        if (!room.compatible) {
            notify(LanNoticeKind.VERSION_MISMATCH, "room ${room.hostName} speaks v${room.protocolVersion}")
            return
        }
        val guest = LanGuest(localName = currentName())
        // Vào bằng địa chỉ + cổng lấy từ [RoomInfo]: phòng được định danh bằng mã phòng,
        // còn địa chỉ chỉ là dữ liệu để nối, nên lần nào vào cũng dùng địa chỉ mới nhất
        // mà beacon vừa báo.
        startSession(guest) { guest.run(room.address, room.port) }
    }

    /**
     * Vào phòng bằng địa chỉ gõ tay.
     *
     * Không phải tính năng cho vui: nhiều mạng Wi-Fi công cộng bật AP isolation hoặc
     * chặn broadcast, lúc đó danh sách phòng luôn rỗng dù hai máy đều ở cùng mạng.
     */
    fun joinManual() {
        val raw = _uiState.value.lobby.manualAddress.trim()
        val host = raw.substringBefore(':').trim()
        val port = raw.substringAfter(':', "").trim().toIntOrNull()
        if (host.isEmpty() || port == null || port !in 1..65_535) {
            notify(LanNoticeKind.BAD_ADDRESS, raw)
            return
        }
        val guest = LanGuest(localName = currentName())
        startSession(guest) { guest.run(host, port) }
    }

    // ------------------------------------------------------------------- phiên

    /**
     * @param phase pha màn hình sau khi mở phiên: mở phòng thì vẫn ở sảnh
     *        ([LanPhase.HOSTING]), còn vào phòng người khác thì sang thẳng bàn cờ.
     */
    private fun startSession(
        target: LanEndpoint,
        phase: LanPhase = LanPhase.SESSION,
        block: suspend () -> Unit,
    ) {
        sessionJob?.cancel()
        endpoint = target
        reconnect = block.takeIf { target.role == LanRole.GUEST }
        selectedSquare = Squares.NONE
        pendingPromotion = null
        _uiState.update {
            it.copy(
                phase = phase,
                role = target.role,
                connected = false,
                waitingForOpponent = target.role == LanRole.HOST,
                hostPort = 0,
                opponentName = "",
                offerRetry = false,
                notice = null,
                board = LanBoardUiState(),
            )
        }

        sessionJob = viewModelScope.launch {
            launch { target.state.collect(::onNetworkState) }
            launch { target.events.collect(::onNetworkEvent) }
            block()
        }
    }

    /**
     * Rời phòng và về sảnh chờ. Giống nhau cho cả hai vai.
     *
     * Rời phòng không đồng nghĩa với đóng phòng: bên còn lại sẽ giữ phòng tiếp (xem
     * [onOpponentLeft]), nên phòng chỉ thực sự biến mất khi cả hai đều đã rời.
     */
    fun leave() {
        val leaving = endpoint
        val job = sessionJob
        hostedRoomId = null
        endpoint = null
        reconnect = null
        sessionJob = null
        viewModelScope.launch {
            // Gửi Bye trước khi hủy job, để đối thủ biết là mình chủ động rời chứ không
            // phải mất mạng — hai việc này hiển thị khác nhau bên máy đối thủ.
            leaving?.leave("user left the room")
            job?.cancelAndJoin()
        }
        _uiState.update {
            it.copy(
                phase = LanPhase.LOBBY,
                role = null,
                connected = false,
                waitingForOpponent = false,
                hostPort = 0,
                offerRetry = false,
                board = LanBoardUiState(),
            )
        }
        startScan()
    }

    /**
     * Thử nối lại bằng tay sau khi tầng mạng đã bỏ cuộc.
     *
     * Dùng lại chính [LanGuest] cũ chứ không tạo mới: vé nối lại nằm trong đó, nên nếu
     * host vẫn giữ ván thì vào lại là chơi tiếp đúng thế cờ đang dở, không phải bắt
     * đầu ván mới.
     */
    fun retry() {
        val guest = endpoint as? LanGuest ?: return
        val again = reconnect ?: return
        startSession(guest, block = again)
    }

    fun resign() = endpoint?.resign() ?: Unit

    fun offerDraw() = endpoint?.offerDraw() ?: Unit

    fun respondDraw(accepted: Boolean) = endpoint?.respondDraw(accepted) ?: Unit

    fun offerRematch() = endpoint?.offerRematch() ?: Unit

    fun respondRematch(accepted: Boolean) = endpoint?.respondRematch(accepted) ?: Unit

    fun requestSync() = endpoint?.requestSync("user asked") ?: Unit

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    // ---------------------------------------------------------------- bàn cờ

    /**
     * Người chơi chạm một ô.
     *
     * Giống <code>GameViewModel</code> nhưng có thêm một chặn: không phải lượt mình thì
     * không nhận chạm. Chặn ở đây chỉ để giao diện đỡ hẫng; trọng tài vẫn kiểm tra
     * lại mọi nước, nên một bản bị sửa cũng không đi được hai nước liền.
     */
    fun onSquareTap(square: Int) {
        val state = _uiState.value
        if (!state.yourTurn || pendingPromotion != null) return

        if (selectedSquare == Squares.NONE) {
            selectIfOwnPiece(square, state.youPlayWhite)
            publish()
            return
        }
        if (square == selectedSquare) {
            selectedSquare = Squares.NONE
            publish()
            return
        }

        val moves = mirror.legalMoves.filter { it.from == selectedSquare && it.to == square }
        when {
            moves.isEmpty() -> selectIfOwnPiece(square, state.youPlayWhite)
            moves.size == 1 -> submit(moves.first())
            // Nhiều nước cùng ô đi và ô đến thì chỉ có thể là phong cấp.
            else -> pendingPromotion =
                PendingPromotion(selectedSquare, square, mirror.whiteToMove, moves)
        }
        publish()
    }

    fun onPromotionChosen(move: Move) {
        pendingPromotion = null
        submit(move)
        publish()
    }

    fun onPromotionDismissed() {
        pendingPromotion = null
        publish()
    }

    private fun submit(move: Move) {
        selectedSquare = Squares.NONE
        // submitMove tự áp dụng nước tại máy rồi mới gửi đi, nên quân đi ngay không
        // chờ mạng. Nếu trọng tài từ chối, [LanEvent.MoveRejected] sẽ tới sau và bàn cờ
        // được đồng bộ lại.
        endpoint?.submitMove(move.raw)
    }

    private fun selectIfOwnPiece(square: Int, youPlayWhite: Boolean) {
        val ownPiece = mirror.isOwnPiece(square, youPlayWhite)
        selectedSquare =
            if (ownPiece && mirror.legalMoves.any { it.from == square }) square else Squares.NONE
    }

    // ------------------------------------------------------- tín hiệu từ :net

    private fun onNetworkState(state: LanGameState) {
        // Ván mới (lần đầu, hoặc đấu lại đổi mã ván) thì bắt đầu đo giờ từ lúc này.
        if (timedGameId != state.gameId) {
            timedGameId = state.gameId
            timedGameStartMillis = System.currentTimeMillis()
        }
        mirror.sync(state.startFen, state.moves, state.gameId)
        // Đối thủ vừa đi, hoặc ván vừa được đồng bộ lại: ô đang chọn không còn ý nghĩa.
        if (!state.yourTurn) {
            selectedSquare = Squares.NONE
            pendingPromotion = null
        }
        publish(state)
        recordIfFinished(state)
    }

    /**
     * Ghi ván LAN vừa kết thúc vào lịch sử.
     *
     * Kết quả tính theo phía người chơi này chứ không theo Bên Trắng: cùng một ván thì
     * hai máy phải ghi ra hai kết quả ngược nhau.
     *
     * Chỉ lưu FEN đầu + dãy nước UCI, giống ván offline, để xem lại bằng engine.
     */
    private fun recordIfFinished(state: LanGameState) {
        if (!state.finished || recordedGameId == state.gameId) return
        val winnerWhite: Boolean? = when {
            // Đầu hàng: bên đầu hàng là bên thua.
            state.outcome == LanOutcome.RESIGNATION -> state.resignedByWhite?.not()
            // Hết giờ: bên rụng cờ là bên thua.
            state.outcome == LanOutcome.TIMEOUT -> state.flaggedWhite?.not()
            state.outcome == LanOutcome.DRAW_AGREED -> null
            // Chiếu hết: bên đến lượt là bên bị thua.
            state.status == GameStatus.CHECKMATE -> !state.whiteToMove
            else -> null
        }
        val result = when (winnerWhite) {
            null -> MatchResult.DRAW
            state.youPlayWhite -> MatchResult.WIN
            else -> MatchResult.LOSS
        }

        recordedGameId = state.gameId
        val record = MatchRecord(
            playedAtMillis = System.currentTimeMillis(),
            mode = MODE_LAN,
            opponent = state.opponentName,
            startFen = state.startFen,
            moves = state.moves.joinToString(" ") { Move(it).toUci() },
            result = result,
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) { matches.insert(record) }
        }

        // Chỉ chiếu hết thật sự mới tính checkmate: đầu hàng/hết giờ/hòa thỏa thuận thì không.
        val byCheckmate = result == MatchResult.WIN &&
            state.outcome != LanOutcome.RESIGNATION &&
            state.outcome != LanOutcome.TIMEOUT &&
            state.status == GameStatus.CHECKMATE
        val seconds = MatchRecording.elapsedSeconds(timedGameStartMillis, System.currentTimeMillis())
        viewModelScope.launch {
            withContext(Dispatchers.IO + NonCancellable) {
                profile.recordMatch(result, byCheckmate, seconds)
            }
        }
    }

    private fun onNetworkEvent(event: LanEvent) {
        when (event) {
            // Đây là chỗ duy nhất đưa màn hình sang bàn cờ: chỉ khi đã bắt tay xong thì
            // bàn cờ mới có nghĩa. Người mở phòng đi qua đúng đường này, không được
            // chuyển màn ngay lúc bấm Mở phòng.
            is LanEvent.Connected -> _uiState.update {
                it.copy(
                    phase = LanPhase.SESSION,
                    waitingForOpponent = false,
                    offerRetry = false,
                    notice = null,
                )
            }

            is LanEvent.MoveRejected -> notify(LanNoticeKind.MOVE_REJECTED, event.reason)

            LanEvent.Resynced -> notify(LanNoticeKind.RESYNCED)

            is LanEvent.OpponentLeft -> onOpponentLeft(event.reason)

            is LanEvent.Disconnected -> {
                // canRetry ở đây nghĩa là "tầng mạng đang tự thử lại". Hết đường rồi thì mới
                // đến lượt người dùng quyết định, nên nút thử lại chỉ hiện đúng lúc đó — và
                // chỉ với khách, vì host không có địa chỉ nào để gọi.
                _uiState.update {
                    it.copy(offerRetry = !event.canRetry && it.role == LanRole.GUEST)
                }
                notify(
                    if (event.canRetry) LanNoticeKind.DISCONNECTED_RETRYING else LanNoticeKind.DISCONNECTED_FINAL,
                    event.reason,
                )
            }

            is LanEvent.Failed -> notify(
                when (event.code) {
                    LanErrorCode.VERSION_MISMATCH -> LanNoticeKind.VERSION_MISMATCH
                    LanErrorCode.ROOM_BUSY -> LanNoticeKind.ROOM_BUSY
                    else -> LanNoticeKind.BAD_HANDSHAKE
                },
                event.detail,
            )

            is LanEvent.DrawSettled, is LanEvent.RematchSettled -> Unit // Trạng thái đã nói đủ.
        }
    }

    /**
     * Đối thủ chủ động rời phòng.
     *
     * Phòng không chết theo người rời: host thì ở lại chờ người tiếp theo, còn khách
     * thì **tự lên làm chủ phòng** — mở một phòng ngay trên máy mình rồi chờ. Nhờ vậy
     * chỉ khi cả hai đều rời thì phòng mới biến mất khỏi danh sách.
     *
     * Ván cũ không được giữ lại: người vào sau là người khác, tiếp tục thế cờ của hai
     * người trước là vô nghĩa.
     */
    private fun onOpponentLeft(reason: String) {
        if (_uiState.value.role == LanRole.HOST) {
            // Phòng vẫn mở, nhưng không còn ván nào để vẽ: về đúng pha chờ người vào ở sảnh
            // thay vì để người mở phòng ngồi nhìn một bàn cờ đã bị xóa.
            _uiState.update {
                it.copy(phase = LanPhase.HOSTING, waitingForOpponent = true, connected = false)
            }
            notify(LanNoticeKind.OPPONENT_LEFT, reason)
            return
        }
        // [host] gọi startSession, mà startSession hủy đúng cái job đang chạy hàm này. Không
        // sao: phần còn lại của startSession không có điểm suspend nào nên vẫn chạy hết, và
        // job mới được mở trong viewModelScope nên không chết theo job cũ.
        host()
        notify(LanNoticeKind.BECAME_HOST, reason)
    }

    private fun notify(kind: LanNoticeKind, detail: String = "") {
        _uiState.update { it.copy(notice = LanNotice(kind, detail)) }
    }

    // ---------------------------------------------------------------- phát ra

    /** Dùng lại trạng thái mạng gần nhất khi chỉ có việc chọn quân thay đổi. */
    private fun publish() {
        _uiState.update { it.copy(board = boardState(it.youPlayWhite)) }
    }

    private fun publish(state: LanGameState) {
        _uiState.update {
            it.copy(
                connected = state.connected,
                waitingForOpponent = it.role == LanRole.HOST && !state.connected,
                opponentName = state.opponentName,
                youPlayWhite = state.youPlayWhite,
                whiteToMove = state.whiteToMove,
                yourTurn = state.yourTurn,
                ply = state.ply,
                startFen = state.startFen,
                moves = state.moves,
                status = state.status,
                finished = state.finished,
                outcome = state.outcome,
                resignedByWhite = state.resignedByWhite,
                timeControl = state.timeControl,
                clock = state.clock,
                flaggedWhite = state.flaggedWhite,
                latencyMillis = state.latencyMillis,
                opponentOffersDraw = state.opponentOffersDraw,
                waitingDrawReply = state.waitingDrawReply,
                opponentOffersRematch = state.opponentOffersRematch,
                waitingRematchReply = state.waitingRematchReply,
                board = boardState(state.youPlayWhite),
            )
        }
    }

    private fun boardState(youPlayWhite: Boolean): LanBoardUiState {
        val lastMove = mirror.lastMove
        return LanBoardUiState(
            pieces = mirror.pieces(),
            selectedSquare = selectedSquare,
            legalTargets = mirror.targetsFrom(selectedSquare),
            lastMoveFrom = lastMove?.from ?: Squares.NONE,
            lastMoveTo = lastMove?.to ?: Squares.NONE,
            checkedKingSquare = mirror.checkedKingSquare(),
            pendingPromotion = pendingPromotion,
            flipped = !youPlayWhite,
        )
    }

    /**
     * Dọn danh sách phòng trước khi đưa lên sảnh: bỏ phòng của chính mình và **cố định
     * thứ tự**.
     *
     * Beacon về mỗi giây một lần cho mỗi phòng, và trước đây phòng vừa nghe thấy được
     * đẩy xuống cuối danh sách, nên có ba bốn phòng là danh sách nhảy liên tục và
     * không ai bấm nổi vào phòng mình muốn. Sắp theo tên rồi theo mã phòng thì thứ tự
     * chỉ đổi khi có phòng mới xuất hiện hoặc phòng cũ tắt.
     */
    private fun arrangeRooms(rooms: List<RoomInfo>): List<RoomInfo> = rooms
        .filterNot { it.roomId == hostedRoomId }
        .sortedWith(compareBy({ it.hostName.lowercase() }, { it.roomId }))

    private fun currentName(): String =
        _uiState.value.lobby.localName.trim().ifEmpty { defaultLocalName() }

    private companion object {
        /** Chế độ ghi trong lịch sử cho ván đánh qua mạng LAN. */
        const val MODE_LAN = "LAN"
    }

    override fun onCleared() {
        // ViewModel chết thì socket và beacon phải chết theo, không để sót một ván "ảo"
        // vẫn hiện trong danh sách phòng của máy khác. viewModelScope tự hủy cả hai job.
        super.onCleared()
    }
}

/** Tên máy hiện cho đối thủ thấy. */
private fun defaultLocalName(): String = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Android"

/**
 * Địa chỉ IPv4 của máy, để hiện cho người dùng đọc cho máy đối diện gõ tay khi
 * discovery bị mạng chặn.
 */
private fun localIpv4Addresses(): List<String> = runCatching {
    NetworkInterface.getNetworkInterfaces()
        .asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .mapNotNull { it.hostAddress }
        .toList()
}.getOrDefault(emptyList())
