package kma.game.chess2d.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Luật chọn mục của Pause Menu theo chế độ chơi. */
class PauseMenuTest {

    @Test
    fun `dau may co du bon muc theo dung thu tu spec`() {
        assertEquals(
            listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.SETTINGS, PauseItem.LEAVE),
            PauseMenu.itemsFor(GameMode.VS_COMPUTER),
        )
    }

    @Test
    fun `hai nguoi an muc cai dat vi khong co gi de chinh`() {
        val items = PauseMenu.itemsFor(GameMode.TWO_PLAYERS)
        assertFalse(PauseItem.SETTINGS in items)
        assertEquals(listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.LEAVE), items)
    }

    @Test
    fun `moi che do deu co tiep tuc va roi tran`() {
        for (mode in GameMode.entries) {
            val items = PauseMenu.itemsFor(mode)
            assertTrue(mode.name, PauseItem.RESUME in items)
            assertTrue(mode.name, PauseItem.LEAVE in items)
            // Tiếp tục luôn đứng đầu để là lựa chọn dễ bấm nhất.
            assertEquals(mode.name, PauseItem.RESUME, items.first())
        }
    }

    @Test
    fun `khong co muc nao bi lap`() {
        for (mode in GameMode.entries) {
            val items = PauseMenu.itemsFor(mode)
            assertEquals(mode.name, items.size, items.toSet().size)
        }
    }
}
