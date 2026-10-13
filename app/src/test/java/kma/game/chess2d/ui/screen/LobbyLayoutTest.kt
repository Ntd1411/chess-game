package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LobbyLayoutTest {
    @Test
    fun `preview o ben canh ban do khong che node hay nut vao tang`() {
        val floors = kma.game.chess2d.campaign.TowerCatalog.build(200)
        val progress = kma.game.chess2d.campaign.TowerProgress()
        assertTrue(TowerMapLayout.canEnter(floors.first(), progress))
        assertFalse(TowerMapLayout.canEnter(floors[1], progress))
        assertFalse(TowerMapLayout.canEnter(null, progress))
        assertTrue(TowerMapLayout.NODE_DP >= 48)
    }

    @Test
    fun `man chon che do nam gon trong khung`() {
        assertEquals(listOf("TOWER", "AI", "LOCAL", "LAN", "SPECTATE"), ModeChoice.entries.map { it.name })
        assertEquals(5, ModeChoice.entries.map { it.title }.toSet().size)
        assertEquals(5, ModeChoice.entries.map { it.description }.toSet().size)

    }

    @Test
    fun inventoryGridFitsAvailableWidth() {
        for (width in 72..560) {
            val columns = InventoryLayout.columnsFor(width.toFloat())
            assertTrue(columns in 1..4)
            assertTrue(columns * InventoryLayout.MIN_SLOT_DP + (columns - 1) * InventoryLayout.GAP_DP <= width)
        }
        assertEquals(3, InventoryLayout.columnsFor(288f))
        assertEquals(4, InventoryLayout.columnsFor(320f))
    }

    @Test
    fun `ban do thap tang 1 o duoi cung va cac node khong chong nhau`() {
        val floors = kma.game.chess2d.campaign.TowerCatalog.build(200)
        assertEquals((49 downTo 1).toList(), TowerMapLayout.displayOrder(floors).map { it.number })
        for (number in 1..49) assertEquals(49 - number, TowerMapLayout.initialIndex(floors, number))
        assertTrue(TowerMapLayout.ROW_DP > TowerMapLayout.NODE_DP)
    }
}
