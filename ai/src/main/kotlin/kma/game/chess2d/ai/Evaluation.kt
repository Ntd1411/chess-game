package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Squares

/**
 * Hàm lượng giá: chấm điểm một thế cờ tĩnh, đơn vị centipawn (100 = một Tốt).
 *
 * Hai thành phần, đúng thứ tự quan trọng ở mục 3.2:
 * - Chất: quân nhiều hơn thì mạnh hơn. Riêng cái này đã đủ để AI biết ăn quân.
 * - Vị trí (piece-square table): Mã ở trung tâm hơn Mã ở góc, Tốt càng tiến càng quý.
 *   Không có phần này thì AI đi những nước vô nghĩa khi không có gì để ăn.
 *
 * Hai bảng cho mỗi quân (khai/trung cuộc và tàn cuộc) rồi nội suy theo lượng quân
 * còn lại — gọi là tapered eval. Cần thiết vì vua đầu ván phải trốn sau hàng Tốt,
 * còn tàn cuộc phải xung ra trung tâm; dùng một bảng duy nhất thì sai ở một đầu.
 *
 * Bảng được viết theo thứ tự nhìn thấy trên bàn: ô đầu tiên là a8, ô cuối là h1.
 */
object Evaluation {

    const val PAWN = 100
    const val KNIGHT = 320
    const val BISHOP = 330
    const val ROOK = 500
    const val QUEEN = 900

    /** Tra theo Piece.typeOf, nên phải đúng thứ tự NONE, Tốt, Mã, Tượng, Xe, Hậu, Vua. */
    private val VALUES = intArrayOf(0, PAWN, KNIGHT, BISHOP, ROOK, QUEEN, 0)

    /**
     * Trọng số "độ còn khai cuộc". Tổng tả cả quân lúc đầu ván đúng bằng MAX_PHASE.
     * Tốt và Vua không tính: chúng không bao giờ rời bàn.
     */
    private val PHASE_WEIGHTS = intArrayOf(0, 0, 1, 1, 2, 4, 0)

    /** 4 Mã + 4 Tượng + 4 Xe×2 + 2 Hậu×4 = 24. */
    private const val MAX_PHASE = 24

    private val PAWN_MG = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        50, 50, 50, 50, 50, 50, 50, 50,
        10, 10, 20, 30, 30, 20, 10, 10,
        5, 5, 10, 25, 25, 10, 5, 5,
        0, 0, 0, 20, 20, 0, 0, 0,
        5, -5, -10, 0, 0, -10, -5, 5,
        5, 10, 10, -20, -20, 10, 10, 5,
        0, 0, 0, 0, 0, 0, 0, 0,
    )

    /** Tàn cuộc: Tốt chỉ có một việc là tiến, nên thưởng theo hàng là đủ. */
    private val PAWN_EG = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        90, 90, 90, 90, 90, 90, 90, 90,
        50, 50, 50, 50, 50, 50, 50, 50,
        30, 30, 30, 30, 30, 30, 30, 30,
        15, 15, 15, 15, 15, 15, 15, 15,
        5, 5, 5, 5, 5, 5, 5, 5,
        0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0,
    )

    private val KNIGHT_TABLE = intArrayOf(
        -50, -40, -30, -30, -30, -30, -40, -50,
        -40, -20, 0, 0, 0, 0, -20, -40,
        -30, 0, 10, 15, 15, 10, 0, -30,
        -30, 5, 15, 20, 20, 15, 5, -30,
        -30, 0, 15, 20, 20, 15, 0, -30,
        -30, 5, 10, 15, 15, 10, 5, -30,
        -40, -20, 0, 5, 5, 0, -20, -40,
        -50, -40, -30, -30, -30, -30, -40, -50,
    )

    private val BISHOP_TABLE = intArrayOf(
        -20, -10, -10, -10, -10, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 10, 10, 5, 0, -10,
        -10, 5, 5, 10, 10, 5, 5, -10,
        -10, 0, 10, 10, 10, 10, 0, -10,
        -10, 10, 10, 10, 10, 10, 10, -10,
        -10, 5, 0, 0, 0, 0, 5, -10,
        -20, -10, -10, -10, -10, -10, -10, -20,
    )

    private val ROOK_TABLE = intArrayOf(
        0, 0, 0, 0, 0, 0, 0, 0,
        5, 10, 10, 10, 10, 10, 10, 5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        -5, 0, 0, 0, 0, 0, 0, -5,
        0, 0, 0, 5, 5, 0, 0, 0,
    )

    private val QUEEN_TABLE = intArrayOf(
        -20, -10, -10, -5, -5, -10, -10, -20,
        -10, 0, 0, 0, 0, 0, 0, -10,
        -10, 0, 5, 5, 5, 5, 0, -10,
        -5, 0, 5, 5, 5, 5, 0, -5,
        0, 0, 5, 5, 5, 5, 0, -5,
        -10, 5, 5, 5, 5, 5, 0, -10,
        -10, 0, 5, 0, 0, 0, 0, -10,
        -20, -10, -10, -5, -5, -10, -10, -20,
    )

    /** Khai cuộc: vua trốn về góc sau khi nhập thành, tuyệt đối tránh trung tâm. */
    private val KING_MG = intArrayOf(
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -30, -40, -40, -50, -50, -40, -40, -30,
        -20, -30, -30, -40, -40, -30, -30, -20,
        -10, -20, -20, -20, -20, -20, -20, -10,
        20, 20, 0, 0, 0, 0, 20, 20,
        20, 30, 10, 0, 0, 10, 30, 20,
    )

    /** Tàn cuộc: đảo ngược hoàn toàn — vua phải ra trung tâm mới mong thắng. */
    private val KING_EG = intArrayOf(
        -50, -40, -30, -20, -20, -30, -40, -50,
        -30, -20, -10, 0, 0, -10, -20, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 30, 40, 40, 30, -10, -30,
        -30, -10, 20, 30, 30, 20, -10, -30,
        -30, -30, 0, 0, 0, 0, -30, -30,
        -50, -30, -30, -30, -30, -30, -30, -50,
    )

    private val EMPTY_TABLE = IntArray(Squares.COUNT)

    private val MIDDLEGAME_TABLES = arrayOf(
        EMPTY_TABLE, PAWN_MG, KNIGHT_TABLE, BISHOP_TABLE, ROOK_TABLE, QUEEN_TABLE, KING_MG,
    )

    private val ENDGAME_TABLES = arrayOf(
        EMPTY_TABLE, PAWN_EG, KNIGHT_TABLE, BISHOP_TABLE, ROOK_TABLE, QUEEN_TABLE, KING_EG,
    )

    fun valueOf(type: Int): Int = VALUES[type]

    /**
     * Điểm theo góc nhìn của BÊN ĐẾN LƯỢT, không phải của Trắng.
     *
     * Đây là quy ước bắt buộc của negamax: mỗi tầng chỉ việc đảo dấu điểm của tầng
     * dưới. Lỡ trả về điểm theo góc nhìn Trắng thì AI sẽ tự điêm những nước tệ nhất
     * mỗi khi đến lượt Đen.
     */
    fun evaluate(board: Board): Int {
        var middlegame = 0
        var endgame = 0
        var phase = 0

        for (square in 0 until Squares.COUNT) {
            val piece = board.squares[square]
            if (piece == Piece.NONE) continue
            val type = Piece.typeOf(piece)
            val white = Piece.isWhite(piece)
            // Bảng viết từ góc nhìn Đen (a8 ở đầu), nên quân Trắng phải lật hàng: xor 56.
            val index = if (white) square xor 56 else square
            val sign = if (white) 1 else -1
            val material = VALUES[type]
            middlegame += sign * (material + MIDDLEGAME_TABLES[type][index])
            endgame += sign * (material + ENDGAME_TABLES[type][index])
            phase += PHASE_WEIGHTS[type]
        }

        // Phong cấp có thể tạo ra nhiều Hậu hơn lúc đầu ván, nên phải chặn trần.
        val clamped = if (phase > MAX_PHASE) MAX_PHASE else phase
        val score = (middlegame * clamped + endgame * (MAX_PHASE - clamped)) / MAX_PHASE
        return if (board.whiteToMove) score else -score
    }
}
