package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LobbyLayoutTest {
    @Test
    fun `man chon che do va chon AI nam gon trong khung`() {
        val cards = (0 until 5).map(ModeSelectLayout::card)
        for (i in 0 until cards.size - 1) assertFalse(cards[i].overlaps(cards[i + 1]))
        assertTrue(cards.last().bottom <= ModeSelectLayout.Ornament.y)
        assertTrue(ModeSelectLayout.Ornament.bottom <= 1920 - LobbyLayout.SAFE_MARGIN)

        val ai = with(AiSelectLayout) { listOf(Name, Stars, Description, Start) }
        for (i in ai.indices) for (j in i + 1 until ai.size) assertFalse(ai[i].overlaps(ai[j]))
        assertTrue(AiSelectLayout.Stars.bottom <= AiSelectLayout.THUMB_Y)
        assertTrue(AiSelectLayout.THUMB_Y + AiSelectLayout.THUMB <= AiSelectLayout.Description.y)
        assertTrue(AiSelectLayout.Start.bottom <= 1920 - LobbyLayout.SAFE_MARGIN)
        assertEquals(1080, AiCarouselX.first() + AiCarouselX.last() + AiSelectLayout.THUMB)
        for (i in 0 until AiCarouselX.size - 1) assertTrue(AiCarouselX[i] + AiSelectLayout.THUMB < AiCarouselX[i + 1])
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
