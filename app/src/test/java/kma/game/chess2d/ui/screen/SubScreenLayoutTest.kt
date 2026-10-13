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
    fun journalGridIsCenteredThreeColumns() {
        assertEquals(listOf(80, 395, 710), (0..2).map(JournalLayout::cellX))
        assertEquals(1080, JournalLayout.cellX(0) + JournalLayout.cellX(2) + JournalLayout.CELL)
        assertEquals(0, JournalLayout.rows(0))
        assertEquals(1, JournalLayout.rows(3))
        assertEquals(3, JournalLayout.rows(7))
    }

    @Test
    fun profileBlocksDoNotOverlap() {
        val blocks = with(ProfileLayout) { listOf(Character, NamePlate, LevelPanel, StatsPanel) }
        for (i in blocks.indices) for (j in i + 1 until blocks.size) {
            assertTrue("$i vs $j", !blocks[i].overlaps(blocks[j]))
        }
        assertTrue(ProfileLayout.StatsPanel.bottom <= 1872)
    }

    @Test
    fun profileStatCellsStayInsidePanelWithoutOverlap() {
        val panel = ProfileLayout.StatsPanel
        val cells = (0 until ProfileLayout.STAT_ROWS * 2).map(ProfileLayout::statCell)
        for (c in cells) {
            assertTrue(c.x >= panel.x && c.y >= panel.y && c.right <= panel.right && c.bottom <= panel.bottom)
        }
        for (i in cells.indices) for (j in i + 1 until cells.size) {
            assertTrue("$i vs $j", !cells[i].overlaps(cells[j]))
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
