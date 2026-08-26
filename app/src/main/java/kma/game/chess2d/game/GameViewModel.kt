package kma.game.chess2d.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kma.game.chess2d.ai.Ai
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.Squares
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Giữ trạng thái ván đấu và là cầu nối duy nhất giữa giao diện và <code>:engine</code>.
 *
 * Danh sách nước đã đi được lưu vào [SavedStateHandle] thay vì lưu cả bàn cờ: chỉ
 * một <code>IntArray</code> nhỏ mà dụng lại được y nguyên mọi thứ, kể cả quyền nhập
 * thành và cả stack Undo. Nhờ vậy xoay máy hay bị hệ thống giải phóng tiến trình
 * đều không mất ván đang chơi.
 */
class GameViewModel(private val savedState: SavedStateHandle) : ViewModel() {

    private var board = Engine.newGame()
    private val playedMoves = mutableListOf<Move>()

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

    /** Lượt nghĩ đang chạy. Giữ lại để hủy được khi người chơi đổi ý. */
    private var aiJob: Job? = null
    private var aiThinking = false

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        resetBoard()
        mode = savedState.get<String>(KEY_MODE)?.let { runCatching { GameMode.valueOf(it) }.getOrNull() }
            ?: GameMode.TWO_PLAYERS
        difficulty = savedState.get<String>(KEY_DIFFICULTY)?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
            ?: Difficulty.MEDIUM
        // Đi lại toàn bộ ván cũ nếu có. Các nước này đã từng hợp lệ nên không cần lọc lại.
        savedState.get<IntArray>(KEY_PLAYED_MOVES)?.forEach { raw -> applyMove(Move(raw)) }
        publish()
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
        if (pendingPromotion != null || Rules.isGameOver(board)) return
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

    fun setMode(newMode: GameMode) {
        if (mode == newMode) return
        cancelAiTurn()
        mode = newMode
        savedState[KEY_MODE] = newMode.name
        publish()
        maybeStartAiTurn()
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

    fun undo() {
        cancelAiTurn()
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
     *    tới 5 giây, đủ để Android dỡng hẳn ANR nếu chặn main thread.
     * 2. Máy nghĩ trên một BẢN SAO của bàn cờ. Search đi và hoàn nguyên hàng triệu
     *    nước; nếu dùng chung bàn cờ với giao diện thì UI sẽ đọc phải những thế cờ
     *    nửa vời đang thử dở.
     * 3. Bản sao được dụng bằng cách đi lại cả ván chứ không phải copy từ FEN: có
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

    private fun replayOnFreshBoard(): Board {
        val copy = Engine.newGame()
        for (move in playedMoves) copy.makeMove(move)
        return copy
    }

    private fun undoOneMove() {
        board.unmakeMove()
        playedMoves.removeAt(playedMoves.lastIndex)
        squareIds = idHistory.removeLast()
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

        playedMoves.add(move)
        saveMoves()
    }

    private fun resetBoard() {
        board = Engine.newGame()
        playedMoves.clear()
        idHistory.clear()
        nextPieceId = 0
        squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }
        for (square in 0 until Squares.COUNT) {
            if (board.pieceAt(square) != Piece.NONE) squareIds[square] = nextPieceId++
        }
        selectedSquare = Squares.NONE
        pendingPromotion = null
    }

    private fun saveMoves() {
        savedState[KEY_PLAYED_MOVES] = IntArray(playedMoves.size) { playedMoves[it].raw }
    }

    private fun publish() {
        legalMoves = Engine.legalMoves(board)
        val lastMove = playedMoves.lastOrNull()
        _uiState.value = GameUiState(
            pieces = currentPieces(),
            whiteToMove = board.whiteToMove,
            status = Rules.status(board),
            selectedSquare = selectedSquare,
            legalTargets = targetsFrom(selectedSquare),
            lastMoveFrom = lastMove?.from ?: Squares.NONE,
            lastMoveTo = lastMove?.to ?: Squares.NONE,
            checkedKingSquare = if (board.isInCheck()) board.kingSquare(board.whiteToMove) else Squares.NONE,
            canUndo = board.canUndo(),
            pendingPromotion = pendingPromotion,
            mode = mode,
            difficulty = difficulty,
            aiThinking = aiThinking,
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
        const val NO_PIECE_ID = -1

        /** Máy cầm Đen, người chơi đi trước. */
        const val AI_PLAYS_WHITE = false
    }
}
