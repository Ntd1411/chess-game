package kma.game.chess2d.net

import kma.game.chess2d.engine.Engine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phần "trọng tài" của tầng mạng, test được hoàn toàn không cần socket. */
class NetGameTest {

    private fun NetGame.rawOf(uci: String): Int =
        legalMoves().first { it.toUci() == uci }.raw

    @Test
    fun `fresh game starts at the initial position`() {
        val game = NetGame()
        assertEquals(0, game.ply)
        assertTrue(game.whiteToMove)
        assertFalse(game.isOver)
        assertEquals(20, game.legalMoves().size)
        assertEquals(Engine.START_FEN, game.fen())
    }

    @Test
    fun `applying a legal move advances the game`() {
        val game = NetGame()
        assertTrue(game.apply(game.rawOf("e2e4")))
        assertEquals(1, game.ply)
        assertFalse(game.whiteToMove)
    }

    @Test
    fun `rejection reason covers the three ways a move can be wrong`() {
        val game = NetGame()
        val e2e4 = game.rawOf("e2e4")

        // Đúng luật, đúng lượt, đúng ply.
        assertNull(game.rejectionReason(e2e4, byWhite = true, atPly = 0))

        // Sai lượt: Đen không được đi nước đầu tiên.
        assertNotNull(game.rejectionReason(e2e4, byWhite = false, atPly = 0))

        // Lệch ply: dấu hiệu hai bàn cờ đã khác nhau, phải chặn trước khi áp dụng.
        assertNotNull(game.rejectionReason(e2e4, byWhite = true, atPly = 5))

        // Sai luật: raw không thuộc tập nước hợp lệ.
        assertNotNull(game.rejectionReason(raw = 0, byWhite = true, atPly = 0))
        assertFalse(game.apply(0))
        assertEquals(0, game.ply)
    }

    @Test
    fun `sync round trip rebuilds the same position`() {
        val original = NetGame()
        for (uci in listOf("e2e4", "e7e5", "g1f3", "b8c6")) {
            assertTrue(original.apply(original.rawOf(uci)))
        }
        val sync = original.toSync(guestPlaysWhite = true, gameId = 3)

        val restored = NetGame()
        assertTrue(restored.restore(sync))
        assertEquals(original.fen(), restored.fen())
        assertEquals(original.rawMoves(), restored.rawMoves())
    }

    @Test
    fun `a tampered sync is refused and leaves the game untouched`() {
        val game = NetGame()
        assertTrue(game.apply(game.rawOf("e2e4")))
        val before = game.fen()

        val tampered = StateSync(
            startFen = Engine.START_FEN,
            // 999_999 không phải một nước đi hợp lệ nào: đi lại phải thất bại.
            moves = listOf(999_999),
            guestPlaysWhite = false,
            gameId = 1,
        )
        assertFalse(game.restore(tampered))
        assertEquals(before, game.fen())
        assertEquals(1, game.ply)
    }

    @Test
    fun `reset returns to a clean game`() {
        val game = NetGame()
        assertTrue(game.apply(game.rawOf("d2d4")))
        game.reset()
        assertEquals(0, game.ply)
        assertEquals(Engine.START_FEN, game.fen())
    }
}
