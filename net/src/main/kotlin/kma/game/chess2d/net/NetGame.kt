package kma.game.chess2d.net

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Rules

/**
 * Ván đấu nhìn từ tầng mạng: một bàn cờ của [Engine] cộng danh sách nước đã đi.
 *
 * Đây là chỗ hiện thực nguyên tắc quan trọng nhất của mục 5.5: **cả hai bên đều tự
 * validate bằng engine của mình**, không bao giờ áp dụng một nước đi chỉ vì đầu bên
 * kia nói thế. Một client bị sửa để gửi nước sai luật không làm lệch được bàn cờ
 * của bên còn lại.
 *
 * Lớp này KHÔNG thread-safe: session chỉ gọi nó từ đúng một luồng đọc.
 */
class NetGame(startFen: String = Engine.START_FEN) {

    var startFen: String = startFen
        private set

    private var board: Board = Engine.fromFen(startFen)
    private val moves = ArrayList<Move>()

    /** Số nước đã đi. Cũng là chỉ số ply của nước sắp đi, dùng để chống lệch. */
    val ply: Int get() = moves.size

    val whiteToMove: Boolean get() = board.whiteToMove

    val status: GameStatus get() = Rules.status(board)

    val isOver: Boolean get() = Rules.isGameOver(board)

    fun rawMoves(): List<Int> = moves.map { it.raw }

    fun fen(): String = board.toFen()

    fun legalMoves(): List<Move> = Engine.legalMoves(board)

    /** Ảnh chụp gửi được qua dây. */
    fun toSync(guestPlaysWhite: Boolean, gameId: Int): StateSync =
        StateSync(
            startFen = startFen,
            moves = rawMoves(),
            guestPlaysWhite = guestPlaysWhite,
            gameId = gameId,
        )

    /**
     * Kiểm tra một nước đi trước khi áp dụng.
     *
     * @param byWhite bên gửi nước này cầm quân Trắng hay Đen. Phải xét: "đi đúng luật"
     *        và "đúng lượt của anh" là hai chuyện khác nhau, và cả hai đều phải chặn.
     * @return null nếu hợp lệ, ngược lại là lý do từ chối (tiếng Anh, dùng cho log và giao thức).
     */
    fun rejectionReason(raw: Int, byWhite: Boolean, atPly: Int): String? = when {
        isOver -> "game is already over"
        atPly != ply -> "ply mismatch: expected $ply but got $atPly"
        byWhite != board.whiteToMove -> "not your turn"
        legalMoves().none { it.raw == raw } -> "illegal move: ${Move(raw).toUci()}"
        else -> null
    }

    /**
     * Áp dụng nước đi sau khi đã chắc nó hợp lệ.
     *
     * Vẫn tự lọc lại một lần nữa thay vì tin người gọi: đây là hàng rào cuối cùng trước
     * khi một nước sai luật lọt vào bàn cờ.
     */
    fun apply(raw: Int): Boolean {
        val move = legalMoves().firstOrNull { it.raw == raw } ?: return false
        board.makeMove(move)
        moves.add(move)
        return true
    }

    /**
     * Dụng lại toàn bộ ván từ một [StateSync] nhận được.
     *
     * Đi lại từng nước qua engine chứ không nhảy thẳng tới thế cờ cuối, và vẫn lọc
     * hợp lệ từng nước. Nhờ vậy một gói sync bị sửa hoặc của phiên bản khác sẽ bị
     * chặn ngay tại đây, thay vì tạo ra một bàn cờ vô nghĩa.
     *
     * @return false nếu không đi lại được; lúc đó ván cũ được giữ nguyên, không bị phá dở.
     */
    fun restore(sync: StateSync): Boolean {
        val rebuilt = runCatching { Engine.fromFen(sync.startFen) }.getOrNull() ?: return false
        val replayed = ArrayList<Move>(sync.moves.size)
        for (raw in sync.moves) {
            val move = Engine.legalMoves(rebuilt).firstOrNull { it.raw == raw } ?: return false
            rebuilt.makeMove(move)
            replayed.add(move)
        }
        startFen = sync.startFen
        board = rebuilt
        moves.clear()
        moves.addAll(replayed)
        return true
    }

    fun reset(startFen: String = Engine.START_FEN) {
        this.startFen = startFen
        board = Engine.fromFen(startFen)
        moves.clear()
    }
}
