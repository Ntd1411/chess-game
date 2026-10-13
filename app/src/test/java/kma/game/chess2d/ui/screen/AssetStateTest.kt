package kma.game.chess2d.ui.screen

import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.ui.art.BattleButton
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AssetStateTest {

    @Test
    fun battleButtonPicksDisabledOverPressed() {
        val b = BattleButton.RESIGN
        assertEquals(b.normal, b.resFor(isEnabled = true, isPressed = false))
        assertEquals(b.pressed, b.resFor(isEnabled = true, isPressed = true))
        assertEquals(b.disabled, b.resFor(isEnabled = false, isPressed = true))
    }

    @Test
    fun singleArtworkButtonsUseSharedPressAndDisabledFeedback() {
        val single = BattleButton.entries.filter { it.singleState }.toSet()
        assertEquals(setOf(
            BattleButton.UNDO, BattleButton.HINT, BattleButton.MOVE, BattleButton.MENU,
            BattleButton.RESTART, BattleButton.SETTINGS, BattleButton.RESIGN,
            BattleButton.DRAW, BattleButton.REMATCH, BattleButton.LEAVE,
        ), single)
        for (b in BattleButton.entries - single) {
            assertEquals(3, setOf(b.normal, b.pressed, b.disabled).size)
        }
    }

    @Test
    fun resultBannerFollowsYourSide() {
        assertNull(ResultBanner.of(over = false, winnerWhite = true, youWhite = true))
        assertEquals(ResultBanner.DRAW, ResultBanner.of(over = true, winnerWhite = null, youWhite = false))
        assertEquals(ResultBanner.WIN, ResultBanner.of(over = true, winnerWhite = false, youWhite = false))
        assertEquals(ResultBanner.LOSS, ResultBanner.of(over = true, winnerWhite = true, youWhite = false))
    }

    @Test
    fun everyPauseItemHasItsOwnIcon() {
        val icons = PauseItem.entries.map { iconFor(it) }
        assertEquals(icons.size, icons.toSet().size)
        assertTrue(icons.all { it != 0 })
        assertFalse(icons.isEmpty())
    }
}
