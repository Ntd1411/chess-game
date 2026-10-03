package kma.game.chess2d.campaign

import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move
import kma.game.chess2d.puzzle.PuzzleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Luật thắng/thua của một tầng, chạy trên engine thật và AI thật (cấp Dễ/Vừa cho nhanh).
 */
class CampaignMatchTest {

    private val puzzles = PuzzleCatalog.parse(
        """
        Chiếu hết hàng cuối|6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1|a1a8
        Ba nước|6k1/8/8/8/8/8/8/R5K1 w - - 0 1|a1a2 g8f8 a2a3
        """.trimIndent(),
    )

    private fun floor(goal: FloorGoal, kind: NodeKind, fen: String? = null) =
        Floor(number = 1, kind = kind, title = "tầng thử", goal = goal, startFen = fen)

    private fun move(fen: String, uci: String): Move =
        Engine.legalMoves(Engine.fromFen(fen)).first { it.toUci() == uci }

    @Test
    fun `chieu het may thi thang tang danh AI`() {
        val fen = "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1"
        val match = CampaignMatch(floor(FloorGoal.DefeatAi(Difficulty.EASY), NodeKind.BATTLE, fen), puzzles)

        assertTrue(match.play(move(fen, "a1a8")))
        assertEquals(FloorResult.WON, match.result)
        assertNull(match.lossReason)
    }

    @Test
    fun `van hoa thi thua o tang danh AI`() {
        // Hậu f1 lên f7: Đen hết nước mà không bị chiếu.
        val fen = "7k/8/6K1/8/8/8/8/5Q2 w - - 0 1"
        val match = CampaignMatch(floor(FloorGoal.DefeatAi(Difficulty.EASY), NodeKind.BATTLE, fen), puzzles)

        assertTrue(match.play(move(fen, "f1f7")))
        assertEquals(FloorResult.LOST, match.result)
        assertEquals(LossReason.DRAW, match.lossReason)
    }

    @Test
    fun `bi chieu het thi thua`() {
        // Trắng đi Vua vào góc thì Xe Đen chiếu hết ngay.
        val fen = "r5k1/5ppp/8/8/8/8/5PPP/6K1 w - - 0 1"
        val match = CampaignMatch(floor(FloorGoal.DefeatAi(Difficulty.MEDIUM), NodeKind.BATTLE, fen), puzzles)

        assertTrue(match.play(move(fen, "g1h1")))
        assertTrue(match.needsAiReply)
        assertEquals(FloorResult.ONGOING, match.result)

        assertNotNull(match.aiStep())
        assertEquals(FloorResult.LOST, match.result)
        assertEquals(LossReason.CHECKMATE, match.lossReason)
    }

    @Test
    fun `moi di 1 tren 2 nuoc song sot thi chua thang`() {
        val fen = "4k3/8/8/8/8/8/8/4K2R w - - 0 1"
        val goal = FloorGoal.SurviveMoves(moves = 2, difficulty = Difficulty.EASY)
        val match = CampaignMatch(floor(goal, NodeKind.SECRET, fen), puzzles, seed = 7)

        assertTrue(match.play(move(fen, "h1h2")))
        assertNotNull(match.aiStep())
        // Mới đi 1/2 nước nên chưa qua.
        assertEquals(FloorResult.ONGOING, match.result)
        assertEquals(1, match.playerMoves)
    }

    @Test
    fun `song sot chua thang khi may chua dap lai nuoc cuoi va thang ngay sau do`() {
        val fen = "4k3/8/8/8/8/8/8/4K2R w - - 0 1"
        val goal = FloorGoal.SurviveMoves(moves = 1, difficulty = Difficulty.EASY)
        val match = CampaignMatch(floor(goal, NodeKind.SECRET, fen), puzzles, seed = 7)

        assertTrue(match.play(move(fen, "h1h2")))
        // Máy chưa đáp lại nên chưa tính là sống sót.
        assertEquals(FloorResult.ONGOING, match.result)
        match.aiStep()
        assertEquals(FloorResult.WON, match.result)
    }

    @Test
    fun `cau do dung chuoi nuoc thi thang`() {
        val match = CampaignMatch(floor(FloorGoal.SolvePuzzle(0), NodeKind.PUZZLE), puzzles)
        val fen = puzzles[0].startFen

        assertTrue(match.play(move(fen, "a1a8")))
        assertEquals(FloorResult.WON, match.result)
        assertEquals(0, match.mistakes)
    }

    @Test
    fun `cau do di sai thi khong di va khong thua`() {
        val match = CampaignMatch(floor(FloorGoal.SolvePuzzle(0), NodeKind.PUZZLE), puzzles)
        val fen = puzzles[0].startFen

        assertFalse(match.play(move(fen, "a1a7")))
        assertEquals(1, match.mistakes)
        assertEquals(FloorResult.ONGOING, match.result)
        assertTrue(match.snapshot().game.sanMoves.isEmpty())

        // Vẫn giải được sau khi đi sai.
        assertTrue(match.play(move(fen, "a1a8")))
        assertEquals(FloorResult.WON, match.result)
    }

    @Test
    fun `cau do nhieu nuoc may tu dap lai theo kich ban`() {
        val match = CampaignMatch(floor(FloorGoal.SolvePuzzle(1), NodeKind.PUZZLE), puzzles)
        val fen = puzzles[1].startFen

        assertTrue(match.play(move(fen, "a1a2")))
        // Đen đã đáp g8f8 theo kịch bản, lại tới lượt Trắng, và không cần máy nghĩ.
        val state = match.snapshot().game
        assertEquals(2, state.sanMoves.size)
        assertTrue(state.whiteToMove)
        assertFalse(match.needsAiReply)
        assertEquals(FloorResult.ONGOING, match.result)

        val afterReply = Engine.fromFen(fen).also { board ->
            board.makeMove(move(fen, "a1a2"))
            board.makeMove(Engine.legalMoves(board).first { it.toUci() == "g8f8" })
        }
        assertTrue(match.play(Engine.legalMoves(afterReply).first { it.toUci() == "a2a3" }))
        assertEquals(FloorResult.WON, match.result)
    }

    @Test
    fun `nuoc phi luat bi tu choi`() {
        val fen = "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1"
        val match = CampaignMatch(floor(FloorGoal.DefeatAi(Difficulty.EASY), NodeKind.BATTLE, fen), puzzles)

        // Nước hợp lệ của một thế khác nên không có trong danh sách nước của thế này.
        val foreign = move(Engine.START_FEN, "e2e4")
        assertFalse(match.play(foreign))
        assertEquals(0, match.playerMoves)
    }

    @Test
    fun `tang chi co thoai hoac nhan khong dung duoc de danh`() {
        val claim = Floor(1, NodeKind.TREASURE, "kho báu", FloorGoal.Claim)
        val failed = runCatching { CampaignMatch(claim, puzzles) }
        assertTrue(failed.isFailure)
    }

    @Test
    fun `moi tang co van co the dung duoc ma khong loi`() {
        val allFloors = TowerCatalog.build(puzzles.size)
        for (floor in allFloors) {
            if (floor.goal is FloorGoal.Claim) continue
            val match = CampaignMatch(floor, puzzles)
            assertEquals("tầng ${floor.number}", FloorResult.ONGOING, match.result)
            assertTrue("tầng ${floor.number}", match.snapshot().game.pieces.isNotEmpty())
        }
    }

    @Test
    fun `chon quan va hop phong cap qua cham o`() {
        val fen = "8/P6k/8/8/8/8/8/7K w - - 0 1"
        val match = CampaignMatch(floor(FloorGoal.DefeatAi(Difficulty.EASY), NodeKind.BATTLE, fen), puzzles)
        val a7 = 48
        val a8 = 56

        assertFalse(match.onSquareTap(a7))
        assertEquals(a7, match.snapshot().game.selectedSquare)
        assertTrue(match.snapshot().game.legalTargets.contains(a8))

        // Bốn nước phong cấp cùng ô đến: phải hỏi người chơi chứ không tự chọn.
        assertFalse(match.onSquareTap(a8))
        val pending = match.snapshot().game.pendingPromotion
        assertNotNull(pending)
        assertEquals(4, pending!!.options.size)
    }
}
