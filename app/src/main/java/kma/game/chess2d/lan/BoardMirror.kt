package kma.game.chess2d.lan

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.PieceOnBoard

/**
 * Bàn cờ phía giao diện, dựng lại từ ảnh chụp mà tầng mạng đẩy lên.
 *
 * Tầng mạng chỉ gửi lên "FEN đầu ván + danh sách nước đã đi". Giao diện lại cần quân
 * cờ, nước hợp lệ, ô vua bị chiếu. Lớp này là chỗ đổi từ cái thứ nhất sang cái thứ hai.
 *
 * Không đọc trực tiếp bàn cờ bên trong <code>LanEndpoint</code> vì bàn cờ đó bị luồng đọc
 * socket sửa đổi liên tục; giao diện đọc nó là đọc chéo luồng lên một đối tượng không
 * thread-safe. Dựng một bàn cờ riêng ở đây vừa an toàn vừa rẻ.
 *
 * Điểm tinh tế: [sync] cố gắng đi thêm phần nước còn thiếu thay vì dựng lại từ đầu.
 * Dựng lại từ đầu sẽ cấp id mới cho tất cả quân, và Compose sẽ hiểu là "32 quân cũ
 * biến mất, 32 quân mới xuất hiện" — mọi chuyển động biến thành nhảy giật. Chỉ khi
 * ván thực sự khác (đấu lại, hoặc bị trọng tài bắt đồng bộ lại) mới dựng lại.
 */
internal class BoardMirror {

    private var startFen: String = Engine.START_FEN
    private var gameId: Int = Int.MIN_VALUE
    private var board: Board = Engine.newGame()
    private val applied = ArrayList<Move>()

    /** id của quân đang đứng ở từng ô, [NO_PIECE_ID] là ô trống. */
    private var squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }
    private var nextPieceId = 0

    var legalMoves: List<Move> = emptyList()
        private set

    val whiteToMove: Boolean get() = board.whiteToMove

    val lastMove: Move? get() = applied.lastOrNull()

    /** Đưa bàn cờ này về đúng trạng thái mà tầng mạng vừa báo. */
    fun sync(startFen: String, moves: List<Int>, gameId: Int) {
        if (!canExtend(startFen, moves, gameId)) rebuild(startFen, gameId)

        for (index in applied.size until moves.size) {
            val raw = moves[index]
            // Vẫn lọc qua engine: một nước không hợp lệ ở đây nghĩa là ảnh chụp bị lệch,
            // và dừng lại vẫn tốt hơn là vẽ ra một bàn cờ vô nghĩa.
            val move = Engine.legalMoves(board).firstOrNull { it.raw == raw } ?: break
            applyOne(move)
        }

        legalMoves = Engine.legalMoves(board)
    }

    fun pieces(): List<PieceOnBoard> = (0 until Squares.COUNT).mapNotNull { square ->
        val piece = board.pieceAt(square)
        if (piece == Piece.NONE) null else PieceOnBoard(squareIds[square], piece, square)
    }

    fun checkedKingSquare(): Int =
        if (board.isInCheck()) board.kingSquare(board.whiteToMove) else Squares.NONE

    fun status() = Rules.status(board)

    fun isOwnPiece(square: Int, white: Boolean): Boolean {
        val piece = board.pieceAt(square)
        return piece != Piece.NONE && Piece.isColor(piece, white)
    }

    fun targetsFrom(square: Int): Set<Int> {
        if (square == Squares.NONE) return emptySet()
        // Phong cấp sinh bốn nước cùng ô đến, Set tự gộp lại thành một gợi ý.
        return legalMoves.filter { it.from == square }.mapTo(HashSet()) { it.to }
    }

    /** Ảnh chụp mới có phải là phần tiếp theo của ván đang vẽ hay không. */
    private fun canExtend(startFen: String, moves: List<Int>, gameId: Int): Boolean =
        startFen == this.startFen &&
            gameId == this.gameId &&
            moves.size >= applied.size &&
            applied.indices.all { moves[it] == applied[it].raw }

    private fun rebuild(startFen: String, gameId: Int) {
        this.startFen = startFen
        this.gameId = gameId
        board = runCatching { Engine.fromFen(startFen) }.getOrElse { Engine.newGame() }
        applied.clear()
        squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }
        for (square in 0 until Squares.COUNT) {
            if (board.pieceAt(square) != Piece.NONE) squareIds[square] = nextPieceId++
        }
    }

    /** Đi một nước và cập nhật bản đồ id đúng như những gì makeMove vừa làm. */
    private fun applyOne(move: Move) {
        val movingWhite = board.whiteToMove
        board.makeMove(move)

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

        applied.add(move)
    }

    private companion object {
        const val NO_PIECE_ID = -1
    }
}
