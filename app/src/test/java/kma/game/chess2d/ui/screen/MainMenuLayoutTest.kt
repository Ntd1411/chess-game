package kma.game.chess2d.ui.screen

import java.io.File
import kma.game.chess2d.R
import org.junit.Assert.*
import org.junit.Test

class MainMenuLayoutTest {
    @Test fun shortcutsUseTwoColumnsOnPhonesAndThreeOnWideScreens() {
        for (width in listOf(240f, 288f, 320f, 412f)) assertEquals(2, MainMenuLayout.columnsFor(width))
        for (width in listOf(480f, 600f, 1024f)) assertEquals(3, MainMenuLayout.columnsFor(width))
        for (width in listOf(288f, 560f)) assertTrue(MainMenuShortcut.entries.chunked(MainMenuLayout.columnsFor(width)).all { it.size == MainMenuLayout.columnsFor(width) })
    }
    @Test fun shopReplacesAchievementsWithoutLosingAnyShortcut() {
        assertEquals(listOf("PROFILE", "JOURNAL", "INVENTORY", "SHOP", "HISTORY", "SETTINGS"), MainMenuShortcut.entries.map { it.name })
        assertEquals(R.string.shop_title, MainMenuShortcut.SHOP.label)
        assertEquals(6, MainMenuShortcut.entries.map { it.label }.toSet().size)
    }
    @Test fun menuUsesDrawnControlsAndRetainsBackgroundAndAvatar() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/MenuScreen.kt").readText()
        assertFalse(source.contains("ArtPressable"))
        assertFalse(source.contains("main_menu_achievements"))
        assertFalse(source.contains("main_menu_header_frame"))
        assertTrue(source.contains("main_menu_background"))
        assertTrue(source.contains("main_menu_avatar"))
        assertTrue(source.contains("safeDrawingPadding"))
        assertTrue(source.contains("verticalScroll"))
    }
}
