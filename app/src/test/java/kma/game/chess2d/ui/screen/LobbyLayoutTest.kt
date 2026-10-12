package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LobbyLayoutTest {

    @Test
    fun `the che do chinh to hon the phu`() {
        assertTrue(LobbyLayout.Tower.h > LobbyLayout.TwoPlayers.h)
        assertTrue(LobbyLayout.VsComputer.h > LobbyLayout.Lan.h)
        assertTrue(LobbyLayout.Tower.w > LobbyLayout.Spectate.w)
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
        val header = with(LobbyLayout) { listOf(Avatar, Name, XpBar, GoldPill, CrystalPill) }
        (LobbyLayout.Buttons + LobbyLayout.SideButtons + header).forEach { box ->
            assertTrue(box.x >= LobbyLayout.SAFE_MARGIN)
            assertTrue(box.right <= 1080 - LobbyLayout.SAFE_MARGIN)
            assertTrue(box.bottom <= 1920 - LobbyLayout.SAFE_MARGIN)
        }
    }

    @Test
    fun `nut tron hai ben khong de len the va khong de nhau`() {
        val sides = LobbyLayout.SideButtons
        assertEquals(6, sides.size)
        sides.forEach { icon ->
            LobbyLayout.Buttons.forEach { button -> assertFalse(icon.overlaps(button)) }
        }
        for (i in sides.indices) for (j in i + 1 until sides.size) assertFalse(sides[i].overlaps(sides[j]))
        // Hai ô tiền tệ không đè tên người chơi.
        assertFalse(LobbyLayout.GoldPill.overlaps(LobbyLayout.Name))
        assertFalse(LobbyLayout.GoldPill.overlaps(LobbyLayout.CrystalPill))
    }

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
