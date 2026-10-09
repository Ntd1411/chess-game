package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LobbyLayoutTest {

    @Test
    fun `nut che do chinh to hon nut phu`() {
        assertTrue(LobbyLayout.Tower.h > LobbyLayout.TwoPlayers.h)
        assertTrue(LobbyLayout.VsComputer.h > LobbyLayout.Lan.h)
    }

    @Test
    fun `cac nut khong chong nhau`() {
        val buttons = LobbyLayout.Buttons
        for (i in buttons.indices) {
            for (j in i + 1 until buttons.size) {
                assertFalse("nut $i va $j chong nhau", buttons[i].overlaps(buttons[j]))
            }
        }
    }

    @Test
    fun `nut va icon nam trong vung an toan cua khung 1080x1920`() {
        (LobbyLayout.Buttons + LobbyLayout.IconColumn).forEach { box ->
            assertTrue(box.x >= LobbyLayout.SAFE_MARGIN)
            assertTrue(box.right <= 1080 - LobbyLayout.SAFE_MARGIN)
            assertTrue(box.bottom <= 1920 - LobbyLayout.SAFE_MARGIN)
        }
    }

    @Test
    fun `cot icon khong de len nut`() {
        LobbyLayout.IconColumn.forEach { icon ->
            LobbyLayout.Buttons.forEach { button -> assertFalse(icon.overlaps(button)) }
        }
        assertEquals(5, LobbyLayout.IconColumn.size)
    }

    @Test
    fun `luoi tui do 4x4 nam gon trong panel`() {
        val last = InventoryLayout.COLUMNS * InventoryLayout.ROWS - 1
        assertTrue(InventoryLayout.slotX(0) >= 48)
        assertTrue(InventoryLayout.slotX(last) + InventoryLayout.SLOT <= 48 + 984)
        assertEquals(InventoryLayout.slotX(0), 1080 - (InventoryLayout.slotX(3) + InventoryLayout.SLOT))
        assertTrue(InventoryLayout.slotY(last) + InventoryLayout.SLOT < 1170)
    }

    @Test
    fun `ban do thap tang 1 o duoi cung va cac node khong chong nhau`() {
        assertTrue(TowerMapLayout.centerY(1) > TowerMapLayout.centerY(49))
        assertTrue(TowerMapLayout.centerY(1) + TowerMapLayout.NODE / 2 <= TowerMapLayout.contentHeight)
        for (n in 1 until 49) {
            val dy = TowerMapLayout.centerY(n) - TowerMapLayout.centerY(n + 1)
            assertTrue(dy >= TowerMapLayout.NODE)
        }
        for (n in 1..49) {
            val x = TowerMapLayout.centerX(n)
            assertTrue(x - TowerMapLayout.NODE / 2 >= 48 && x + TowerMapLayout.NODE / 2 <= 1032)
        }
    }
}
