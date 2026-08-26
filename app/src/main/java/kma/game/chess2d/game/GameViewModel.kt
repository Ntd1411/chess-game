package kma.game.chess2d.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.Squares
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Giữ trạng thái ván đấu và là cầu nối duy nhất giữa giao diện và <code>:engine</code>.
 *
 * Danh sách nước đã đi được lưu vào [SavedStateHandle] thay vì lưu cả bàn cờ: chỉ
 * một <code>IntArray</code> nhỏ mà dựng lại được y nguyên mọi thứ, kể cả quyền nhập
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

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        resetBoard()
        // Đi lại toàn bộ ván cũ nếu có. Các nước này đã từng hợp lệ nên không cần lọc lại.
        savedState.get<IntArray>(KEY_PLAYED_MOVES)?.forEach { raw -> applyMove(Move(raw)) }
        publish()
    }

    /**
     * Người chơi chạm vào một ô. Mọi tương tác bàn cờ đi qua đúng hàm này.
     *
     * Nước đi luôn được đối chiếu với danh sách nước hợp lệ do engine sinh ra, nên
     * không có đường nào để giao diện thực hiện một nước sai luật.
     */
    fun onSquareTap(square: Int) {
        if (pendingPromotion != null || Rules.isGameOver(board)) return

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
    }

    fun onPromotionChosen(move: Move) {
        pendingPromotion = null
        selectedSquare = Squares.NONE
        applyMove(move)
        publish()
    }

    /** Đóng hộp thoại mà không phong cấp: nước đi chưa hề được thực hiện nên không cần hoàn nguyên. */
    fun onPromotionDismissed() {
        pendingPromotion = null
        publish()
    }

    fun undo() {
        if (!board.canUndo()) return
        board.unmakeMove()
        playedMoves.removeAt(playedMoves.lastIndex)
        squareIds = idHistory.removeLast()
        selectedSquare = Squares.NONE
        pendingPromotion = null
        saveMoves()
        publish()
    }

    fun newGame() {
        resetBoard()
        saveMoves()
        publish()
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
        const val NO_PIECE_ID = -1
    }
}
