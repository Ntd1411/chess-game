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
    fun settingsRetainsFiveDistinctScrollableTabs() {
        assertEquals(5, SettingsTab.entries.size)
        assertEquals(5, SettingsTab.entries.map { it.label }.toSet().size)
        assertTrue(SettingsLayout.TAB_MIN_DP >= 48)
    }

    @Test
    fun settingsRowsKeepComfortableTapTargets() {
        assertTrue(SettingsLayout.ROW_MIN_DP in 48..64)
        assertEquals(560, SettingsLayout.MAX_WIDTH_DP)
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
        assertEquals(6, SplashLayout.BAR_HEIGHT_DP)
        assertTrue(SplashLayout.MAX_BAR_WIDTH_DP in 320..560)
    }
}
