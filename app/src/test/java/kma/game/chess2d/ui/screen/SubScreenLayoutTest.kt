package kma.game.chess2d.ui.screen

import kma.game.chess2d.ui.art.PanelTone
import kma.game.chess2d.ui.art.panelStyle
import kma.game.chess2d.ui.theme.GothicColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SubScreenLayoutTest {

    @Test
    fun settingsTabsFitOneRowAboveContent() {
        assertEquals(listOf(90, 272, 454), (0..2).map(SettingsLayout::tabX))
        val last = SettingsLayout.tabX(SettingsTab.entries.size - 1) + SettingsLayout.TAB_W
        assertTrue(last <= SettingsLayout.CONTENT_X + SettingsLayout.CONTENT_W)
        assertTrue(SettingsLayout.TAB_Y + SettingsLayout.TAB_H <= SettingsLayout.CONTENT_Y)
    }

    @Test
    fun settingsContentStaysInsideSafeArea() {
        assertTrue(SettingsLayout.CONTENT_X >= 48)
        assertTrue(SettingsLayout.CONTENT_X + SettingsLayout.CONTENT_W <= 1032)
        assertTrue(SettingsLayout.CONTENT_Y + SettingsLayout.CONTENT_H <= SettingsLayout.RESTORE_Y)
    }

    @Test
    fun journalGridAdaptsToAvailableWidth() {
        assertEquals(1, JournalLayout.columnsFor(144f))
        assertEquals(2, JournalLayout.columnsFor(320f))
        assertEquals(3, JournalLayout.columnsFor(560f))
        assertEquals(3, JournalLayout.columnsFor(1000f))
        for (width in 144..560) {
            val columns = JournalLayout.columnsFor(width.toFloat())
            assertTrue(columns * JournalLayout.MIN_CELL_DP + (columns - 1) * JournalLayout.GAP_DP <= width)
        }
    }

    @Test
    fun profileRetainsEightDistinctMetricsInFourRows() {
        assertEquals(8, ProfileMetric.entries.size)
        assertEquals(8, ProfileMetric.entries.map { it.label }.toSet().size)
        assertTrue(ProfileMetric.entries.chunked(2).all { it.size == 2 })
    }

    @Test
    fun profileProgressUsesRealLevelPoints() {
        for (points in 0..30) {
            val stats = kma.game.chess2d.profile.PlayerStats(wins = points)
            assertEquals((points % 5) / 5f, profileLevelFraction(stats), 0.0001f)
        }
    }

    @Test
    fun onlySelectedPanelUsesBrightGoldAndTint() {
        val selected = panelStyle(PanelTone.SELECTED)
        assertEquals(GothicColors.Gold, selected.border)
        assertEquals(GothicColors.Crimson, selected.tint)
        for (tone in listOf(PanelTone.BASE, PanelTone.SUNKEN)) {
            assertNull(panelStyle(tone).tint)
            assertEquals(GothicColors.GoldDim, panelStyle(tone).border)
        }
    }

    @Test
    fun splashBarIsCenteredInLowerScreen() {
        with(SplashLayout) {
            assertEquals(1080, Bar.x * 2 + Bar.w)
            // Khung nhìn thấy (32–63% chiều cao canvas) nằm trong vùng an toàn phía dưới.
            assertTrue(Bar.y + Bar.h * 0.32f > 1920 * 0.75f)
            assertTrue(Bar.y + Bar.h * 0.63f <= 1920 - LobbyLayout.SAFE_MARGIN)
            assertTrue(FILL_START < FILL_END)
        }
    }
}
