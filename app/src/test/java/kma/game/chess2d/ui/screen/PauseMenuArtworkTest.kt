package kma.game.chess2d.ui.screen

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.game.PauseItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseMenuArtworkTest {
    @Test
    fun pauseActionsKeepTheirLabelsAfterRedesign() {
        assertEquals(R.string.pause_resume, labelFor(PauseItem.RESUME))
        assertEquals(R.string.pause_restart, labelFor(PauseItem.RESTART))
        assertEquals(R.string.pause_settings, labelFor(PauseItem.SETTINGS))
        assertEquals(R.string.pause_leave, labelFor(PauseItem.LEAVE))
        assertEquals(4, PauseItem.entries.map(::labelFor).toSet().size)
    }

    @Test
    fun pauseRowsKeepComfortableTapTargetsAndIconSpacing() {
        assertTrue(PauseMenuLayout.ROW_HEIGHT_DP >= 48)
        assertTrue(PauseMenuLayout.ROW_HEIGHT_DP < 58)
        assertTrue(PauseMenuLayout.ROW_GAP_DP in 4..10)
        assertTrue(PauseMenuLayout.ICON_SIZE_DP < PauseMenuLayout.ROW_HEIGHT_DP)
        assertTrue(PauseMenuLayout.ICON_SIZE_DP + 2 * PauseMenuLayout.ROW_PADDING_DP <= PauseMenuLayout.ROW_HEIGHT_DP)
        assertTrue(PauseMenuLayout.MAX_WIDTH_DP in 320..440)
    }

    @Test
    fun pauseWindowUsesDrawnControlsInsteadOfDecorativeArtwork() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/PauseMenuOverlay.kt").readText()
        assertFalse(source.contains("SlicedArt"))
        assertFalse(source.contains("ArtImage"))
        assertFalse(source.contains("GothicDialog"))
    }

    @Test
    fun settingsPanelRemainsIndependentOfTheGameplayMenu() {
        assertEquals(56, SettingsLayout.ROW_MIN_DP)
        val source = File("src/main/java/kma/game/chess2d/ui/screen/SettingsPanel.kt").readText()
        assertFalse(source.contains("R.drawable.setting_line"))
    }
}
