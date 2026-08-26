package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Fen
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.MoveGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {

    /**
     * Hết nước trong một nước: Ra8 chiếu bí. Chọn thế này vì Xe ở a1 không bị quân
     * nào với tới, nên đáp án là duy nhất và không thể nhầm.
     */
    @Test
    fun findsMateInOne() {
        val board = Fen.parse("6k1/5ppp/8/8/8/8/5PPP/R5K1 w - - 0 1")
        val result = Search().search(board, depth = 3, timeBudgetMillis = 5_000)
        assertEquals("a1a8", result.bestMove.toUci())
        assertTrue("điểm ${result.score} không phải điểm hết nước", result.score > Search.MATE_THRESHOLD)
    }

    /** Ăn Hậu bỏ không — phép thử tối thiểu của phần lượng giá chất. */
    @Test
    fun takesTheFreeQueen() {
        val board = Fen.parse("4k3/8/8/8/8/8/3q4/4K3 w - - 0 1")
        val result = Search().search(board, depth = 4, timeBudgetMillis = 5_000)
        assertEquals("e1d2", result.bestMove.toUci())
    }

    /**
     * Không đứt lưỡi ở nước ăn quân đầu tiên: ăn Tốt bằng Hậu ở đây mất Hậu ngay,
     * không có quiescence thì search tưởng mình vừa lời một Tốt.
     */
    @Test
    fun doesNotGrabAPoisonedPawn() {
        val board = Fen.parse("4k3/8/8/8/8/2p5/1p6/Q3K3 w - - 0 1")
        val result = Search().search(board, depth = 3, timeBudgetMillis = 5_000)
        assertFalse(
            "ăn Tốt b2 là mất Hậu: ${result.bestMove.toUci()}",
            result.bestMove.toUci() == "a1b2",
        )
    }

    /** Hết nước thì không có gì để đi, phải trả về Move.NONE thay vì nổ. */
    @Test
    fun returnsNoMoveWhenGameIsOver() {
        val board = Fen.parse("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
        val result = Search().search(board, depth = 4, timeBudgetMillis = 1_000)
        assertEquals(Move.NONE, result.bestMove)
    }

    /** Đi sâu thêm phải không bao giờ làm AI đi nước phi luật. */
    @Test
    fun bestMoveIsAlwaysLegal() {
        val board = Fen.parse(Fen.KIWIPETE)
        val legal = MoveGenerator.legalMoves(board).map { it.raw }.toSet()
        for (depth in 1..5) {
            val result = Search().search(board, depth = depth, timeBudgetMillis = 5_000)
            assertTrue("độ sâu $depth cho nước lạ", result.bestMove.raw in legal)
        }
    }

    /**
     * Trần thời gian phải thắng độ sâu. Đây chính là thứ giữ cho cấp Khó không treo
     * máy ở những thế cờ rối: xin độ sâu 20 nhưng chỉ cho 300ms.
     */
    @Test
    fun respectsTimeBudget() {
        val board = Fen.parse(Fen.KIWIPETE)
        val startedAt = System.nanoTime()
        val result = Search().search(board, depth = 20, timeBudgetMillis = 300)
        val elapsed = (System.nanoTime() - startedAt) / 1_000_000L
        assertTrue("mất ${elapsed}ms cho ngân sách 300ms", elapsed < 2_000)
        assertTrue("không trả về nước nào", result.bestMove != Move.NONE)
    }

    /** Vòn đào sâu dở dang bị bỏ, nhưng độ sâu hoàn thành phải được báo đúng. */
    @Test
    fun reportsProgressForEachCompletedDepth() {
        val board = Board.startPosition()
        val depths = ArrayList<Int>()
        val result = Search().search(
            board = board,
            depth = 4,
            timeBudgetMillis = 10_000,
            onProgress = { depths.add(it.depth) },
        )
        assertEquals(listOf(1, 2, 3, 4), depths)
        assertEquals(4, result.depth)
        assertTrue(result.completed)
    }

    /**
     * Điểm hủy phải thực sự dừng được lượt nghĩ — yêu cầu ở mục 5.4 về việc thoát
     * màn hình là luồng nghĩ phải chết theo, không chạy tiếp trong nền.
     */
    @Test
    fun stopsWhenCancelled() {
        val board = Fen.parse(Fen.KIWIPETE)
        val startedAt = System.nanoTime()
        val result = Search().search(
            board = board,
            depth = 20,
            timeBudgetMillis = 60_000,
            isActive = { false },
        )
        val elapsed = (System.nanoTime() - startedAt) / 1_000_000L
        assertTrue("bị hủy nhưng chạy ${elapsed}ms", elapsed < 2_000)
        // Vẫn phải trả về một nước hợp lệ để bên gọi không phải xử lý trường hợp rỗng.
        val legal = MoveGenerator.legalMoves(board).map { it.raw }.toSet()
        assertTrue(result.bestMove.raw in legal)
    }

    /** Bàn cờ phải được trả nguyên trạng sau khi nghĩ: search đi và hoàn nguyên hàng triệu nước. */
    @Test
    fun leavesBoardUnchanged() {
        val board = Fen.parse(Fen.KIWIPETE)
        val fenBefore = board.toFen()
        val keyBefore = board.hashKey
        Search().search(board, depth = 4, timeBudgetMillis = 5_000)
        assertEquals(fenBefore, board.toFen())
        assertEquals(keyBefore, board.hashKey)
    }

    /** Cấp Dễ cần điểm chính xác cho mọi nước gốc để chọn trong nhóm ba nước tốt nhất. */
    @Test
    fun exactRootScoresRankAllMoves() {
        val board = Fen.parse("4k3/8/8/8/8/8/3q4/4K3 w - - 0 1")
        val result = Search().search(
            board = board,
            depth = 3,
            timeBudgetMillis = 5_000,
            exactRootScores = true,
        )
        assertEquals(MoveGenerator.legalMoves(board).size, result.rootMoves.size)
        // Đã xếp giảm dần.
        val scores = result.rootMoves.map { it.score }
        assertEquals(scores.sortedDescending(), scores)
        assertEquals("e1d2", result.rootMoves[0].move.toUci())
    }
}
