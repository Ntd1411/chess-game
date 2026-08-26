package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Fen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluationTest {

    /** Thế ban đầu đối xứng hoàn toàn, mọi thành phần phải triệt tiêu về 0. */
    @Test
    fun startPositionIsBalanced() {
        assertEquals(0, Evaluation.evaluate(Board.startPosition()))
    }

    /**
     * Cùng một thế cờ, đổi bên đến lượt thì điểm phải đảo dấu.
     *
     * Đây là quy ước sống còn của negamax. Sai chỗ này thì AI chọn đúng nước TỆ nhất
     * mỗi khi đến lượt Đen, mà vẫn chạy trơn tru không báo lỗi gì.
     */
    @Test
    fun scoreIsFromSideToMovePerspective() {
        val whiteToMove = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/8/4KQ2 w - - 0 1"))
        val blackToMove = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/8/4KQ2 b - - 0 1"))
        assertTrue("hơn Hậu mà không dương: $whiteToMove", whiteToMove > 0)
        assertTrue("thiếu Hậu mà không âm: $blackToMove", blackToMove < 0)
        assertEquals(whiteToMove, -blackToMove)
    }

    /** Hơn hẳn một Hậu phải nặng hơn hơn một Tốt, không để bảng vị trí lấn át chất. */
    @Test
    fun materialDominatesPlacement() {
        val extraQueen = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/8/4KQ2 w - - 0 1"))
        val extraPawn = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"))
        assertTrue(extraQueen > extraPawn)
        assertTrue(extraPawn > 0)
    }

    /** Tốt càng gần đích càng quý — không có tính chất này thì AI không bao giờ tiến Tốt. */
    @Test
    fun advancedPawnBeatsHomePawn() {
        val advanced = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/4P3/8/4K3 w - - 0 1"))
        val home = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"))
        assertTrue("tiến $advanced không hơn tại chỗ $home", advanced > home)
    }

    /** Bảng vị trí phải đối xứng gương giữa hai bên: Mã góc của Trắng = Mã góc của Đen. */
    @Test
    fun placementIsMirroredBetweenColors() {
        val whiteKnightCorner = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/8/N3K3 w - - 0 1"))
        val blackKnightCorner = Evaluation.evaluate(Fen.parse("n3k3/8/8/8/8/8/8/4K3 b - - 0 1"))
        assertEquals(whiteKnightCorner, blackKnightCorner)
    }

    /** Mã trung tâm phải ăn điểm hơn Mã nằm góc bàn. */
    @Test
    fun centralKnightBeatsCornerKnight() {
        val central = Evaluation.evaluate(Fen.parse("4k3/8/8/3N4/8/8/8/4K3 w - - 0 1"))
        val corner = Evaluation.evaluate(Fen.parse("4k3/8/8/8/8/8/8/N3K3 w - - 0 1"))
        assertTrue("trung tâm $central không hơn góc $corner", central > corner)
    }
}
