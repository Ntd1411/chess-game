package kma.game.chess2d.ui.screen

import java.io.File
import javax.imageio.ImageIO
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseMenuArtworkTest {
    @Test
    fun gameplayPauseRowsUseThePreloadedSettingLineArtwork() {
        assertEquals(R.drawable.setting_line, PauseMenuLayout.rowArt)
        assertTrue(ArtAssets.preload.any { it.res == PauseMenuLayout.rowArt })
        assertFalse(File("src/main/res/drawable/setting_line.png").exists())
        val image = requireNotNull(ImageIO.read(File("src/main/res/drawable/setting_line.webp")))
        assertTrue(image.colorModel.hasAlpha())
        assertTrue((image.getRGB(768, 480) ushr 24) > 24)
    }

    @Test
    fun pauseRowsAreCompactButKeepComfortableTapTargetsAndBalancedInsets() {
        assertTrue(PauseMenuLayout.ROW_HEIGHT_DP >= 48)
        assertTrue(PauseMenuLayout.ROW_HEIGHT_DP < 58)
        assertTrue(PauseMenuLayout.ROW_GAP_DP in 4..10)
        assertTrue(PauseMenuLayout.ICON_SIZE_DP < PauseMenuLayout.ROW_HEIGHT_DP)
        assertTrue(PauseMenuLayout.LABEL_INSET_DP > PauseMenuLayout.ICON_INSET_DP + PauseMenuLayout.ICON_SIZE_DP)
    }

    @Test
    fun settingsPanelNoLongerUsesTheGameplayStripOrCompactRowLayout() {
        assertEquals(200, SettingsLayout.ROW_H)
        val source = File("src/main/java/kma/game/chess2d/ui/screen/SettingsPanel.kt").readText()
        assertFalse(source.contains("R.drawable.setting_line"))
    }
}
