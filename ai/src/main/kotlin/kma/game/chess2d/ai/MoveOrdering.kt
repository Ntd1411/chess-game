package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.MoveList
import kma.game.chess2d.engine.Piece

/**
 * Thứ tự thử nước.
 *
 * Đây là bước đáng giá nhất trong mục 3.2 và cũng là bước dễ bỏ qua nhất: alpha-beta
 * chỉ cắt được nhánh khi nước tốt được thử trước. Cùng một search, sắp nước tốt
 * lên đầu có thể giảm số node cả một bậc độ lớn — đúng bằng việc đi sâu thêm một
 * tầng miễn phí.
 *
 * Ba nhóm, theo độ tin cậy giảm dần:
 * 1. Nước tốt nhất lần trước (từ transposition table).
 * 2. Ăn quân, xếp theo MVV-LVA: ăn quân TO nhất bằng quân NHỎ nhất trước.
 * 3. Killer move: nước từng cắt được nhánh ở cùng độ sâu này, thường lại cắt được
 *    ở nhánh anh em bên cạnh dù không ăn gì cả.
 */
class MoveOrdering(maxPly: Int) {

    companion object {
        private const val TT_MOVE = 1_000_000
        private const val PROMOTION_BASE = 200_000
        private const val CAPTURE_BASE = 100_000
        private const val KILLER_FIRST = 90_000
        private const val KILLER_SECOND = 80_000

        /** Không phải nước ăn quân — quiescence dùng dấu này để biết chỗ dừng. */
        const val SKIP = Int.MIN_VALUE
    }

    /** Hai killer mỗi độ sâu: nhiều hơn thì lợi ích không bù được chi phí tra cứu. */
    private val killers = Array(maxPly) { IntArray(2) }

    fun clear() {
        for (row in killers) row.fill(0)
    }

    /** Ghi nhậy killer, đẩy nước cũ xuống khe thứ hai. */
    fun rememberKiller(ply: Int, moveRaw: Int) {
        if (ply >= killers.size) return
        val slot = killers[ply]
        if (slot[0] == moveRaw) return
        slot[1] = slot[0]
        slot[0] = moveRaw
    }

    fun score(board: Board, move: Move, ttMoveRaw: Int, ply: Int): Int {
        if (move.raw == ttMoveRaw) return TT_MOVE

        if (move.isCapture) {
            // Bắt tốt qua đường: quân bị ăn không nằm ở ô đến, nên phải xử riêng.
            val victimType =
                if (move.isEnPassant) Piece.PAWN else Piece.typeOf(board.pieceAt(move.to))
            val attackerType = Piece.typeOf(board.pieceAt(move.from))
            var value = CAPTURE_BASE + Evaluation.valueOf(victimType) * 8 -
                Evaluation.valueOf(attackerType)
            if (move.isPromotion) value += Evaluation.valueOf(move.promotionType)
            return value
        }

        if (move.isPromotion) return PROMOTION_BASE + Evaluation.valueOf(move.promotionType)

        if (ply < killers.size) {
            val slot = killers[ply]
            if (move.raw == slot[0]) return KILLER_FIRST
            if (move.raw == slot[1]) return KILLER_SECOND
        }
        return 0
    }

    /**
     * Đưa nước điểm cao nhất trong phần còn lại về vị trí from.
     *
     * Cứ mỗi vòng lại chọn một lần, không sort toàn bộ: phần lớn node bị cắt ngay
     * sau nước đầu tiên, sort cả danh sách là trả tiền cho việc không dùng.
     */
    fun pickBest(moves: MoveList, scores: IntArray, from: Int) {
        var bestIndex = from
        for (index in from + 1 until moves.size) {
            if (scores[index] > scores[bestIndex]) bestIndex = index
        }
        if (bestIndex == from) return
        moves.swap(from, bestIndex)
        val temp = scores[from]
        scores[from] = scores[bestIndex]
        scores[bestIndex] = temp
    }
}
