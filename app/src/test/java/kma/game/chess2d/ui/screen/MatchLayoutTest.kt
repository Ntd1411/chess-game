package kma.game.chess2d.ui.screen

import kma.game.chess2d.engine.Piece
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MatchLayoutTest {

    @Test
    fun fourActionsMatchTheDesignSlots() {
        // Bản thiết kế: tâm nút ở 270/450/630/810, nút rộng 110.
        assertEquals(listOf(215, 395, 575, 755), actionSlotsX(4))
    }

    @Test
    fun actionsStayCenteredAndInsideTheScreen() {
        for (count in 1..6) {
            val xs = actionSlotsX(count)
            assertEquals(count, xs.size)
            assertTrue(xs.first() >= 0)
            assertTrue(xs.last() + 110 <= 1080)
            assertEquals(1080, xs.first() + xs.last() + 110)
        }
        assertEquals(emptyList<Int>(), actionSlotsX(0))
    }

    @Test
    fun hpFallsWithLostMaterial() {
        assertEquals(1f, MatchHp.fromLost(emptyList()), 0.0001f)
        val queen = Piece.of(Piece.QUEEN, false)
        assertEquals(1f - 9f / 39f, MatchHp.fromLost(listOf(queen)), 0.0001f)
        val everything = List(8) { Piece.of(Piece.PAWN, true) } + List(4) { Piece.of(Piece.QUEEN, true) }
        assertEquals(0f, MatchHp.fromLost(everything), 0.0001f)
    }
}
