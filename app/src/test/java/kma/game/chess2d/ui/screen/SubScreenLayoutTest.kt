package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SubScreenLayoutTest {

    @Test
    fun settingsTabsFollowTheDesignRows() {
        assertEquals(listOf(300, 440, 580), (0..2).map(SettingsLayout::tabY))
        // Mọi tab nằm trong panel (200..1620).
        val last = SettingsLayout.tabY(SettingsTab.entries.size - 1) + SettingsLayout.TAB_H
        assertTrue(last <= 1620)
    }

    @Test
    fun settingsColumnsMatchTheDesign() {
        assertEquals(390, SettingsLayout.CONTENT_X)
        assertEquals(760, SettingsLayout.CONTROL_X)
        // Control kết thúc trong vùng nội dung và trong panel (48..1032).
        assertTrue(SettingsLayout.CONTROL_X + SettingsLayout.CONTROL_W <= SettingsLayout.CONTENT_X + SettingsLayout.CONTENT_W)
        assertTrue(SettingsLayout.CONTENT_X + SettingsLayout.CONTENT_W <= 1032)
        assertTrue(SettingsLayout.TAB_X + SettingsLayout.TAB_W < SettingsLayout.CONTENT_X)
    }

    @Test
    fun lanLobbyBoxesDoNotOverlap() {
        val boxes = with(LanLobbyLayout) { listOf(CodePanel, YouSlot, OpponentSlot, Start, Leave) }
        for (i in boxes.indices) for (j in i + 1 until boxes.size) {
            assertTrue("$i vs $j", !boxes[i].overlaps(boxes[j]))
        }
        // Nút sao chép nằm trong ô địa chỉ; danh sách phòng kết thúc trước nút chính.
        assertTrue(LanLobbyLayout.CodePanel.overlaps(LanLobbyLayout.CopyIcon))
        assertTrue(LanLobbyLayout.ROOMS_BOTTOM <= LanLobbyLayout.Start.y)
        assertTrue(LanLobbyLayout.roomsTop(true) >= LanLobbyLayout.OpponentSlot.bottom)
    }

    @Test
    fun journalGridIsCenteredThreeColumns() {
        assertEquals(listOf(120, 410, 700), (0..2).map(JournalLayout::cellX))
        assertEquals(1080, JournalLayout.cellX(0) + JournalLayout.cellX(2) + JournalLayout.CELL)
        assertEquals(0, JournalLayout.rows(0))
        assertEquals(1, JournalLayout.rows(3))
        assertEquals(3, JournalLayout.rows(7))
    }
}
