package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoveStripTest {
    @Test
    fun labelsKeepMoveNumbersAndAnUnpairedWhiteMove() {
        assertEquals(listOf("1. e4", "e5", "2. Nf3"), moveStripLabels(listOf("e4", "e5", "Nf3")))
        assertEquals(emptyList<String>(), moveStripLabels(emptyList()))
    }

    @Test
    fun labelsHandleBlackStartingFromACustomPosition() {
        assertEquals(listOf("12... e5", "13. Nf3", "Nc6", "14. Bb5"),
            moveStripLabels(listOf("e5", "Nf3", "Nc6", "Bb5"), 12, false))
    }

    @Test
    fun newMovesDoNotInterruptBrowsingOlderMoves() {
        assertNull(moveStripScrollTarget(21, null, 20, 4, null))
        assertEquals(20, moveStripScrollTarget(21, null, 20, 19, null))
        assertEquals(20, moveStripScrollTarget(21, null, 0, -1, null))
    }

    @Test
    fun reviewUndoAndReturnToLiveSelectTheCorrectScrollTarget() {
        assertEquals(4, moveStripScrollTarget(20, 4, 20, 19, null))
        assertEquals(19, moveStripScrollTarget(20, null, 20, 4, 4))
        assertEquals(9, moveStripScrollTarget(10, null, 20, 19, null))
        assertEquals(19, moveStripScrollTarget(20, 99, 20, 4, null))
        assertNull(moveStripScrollTarget(0, null, 20, 19, 4))
    }
}
