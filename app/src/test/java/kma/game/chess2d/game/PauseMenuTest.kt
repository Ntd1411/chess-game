package kma.game.chess2d.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Luật chọn mục của Pause Menu. */
class PauseMenuTest {

    @Test
    fun `moi che do co du bon muc theo dung thu tu spec`() {
        for (mode in GameMode.entries) {
            assertEquals(
                mode.name,
                listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.SETTINGS, PauseItem.LEAVE),
                PauseMenu.itemsFor(mode),
            )
        }
    }

    @Test
    fun `tiep tuc luon dung dau de la lua chon de bam nhat`() {
        for (mode in GameMode.entries) {
            assertEquals(mode.name, PauseItem.RESUME, PauseMenu.itemsFor(mode).first())
        }
    }

    @Test
    fun `moi che do deu co muc roi tran`() {
        for (mode in GameMode.entries) {
            assertTrue(mode.name, PauseItem.LEAVE in PauseMenu.itemsFor(mode))
        }
    }

    @Test
    fun `khong co muc nao bi lap`() {
        for (mode in GameMode.entries) {
            val items = PauseMenu.itemsFor(mode)
            assertEquals(mode.name, items.size, items.toSet().size)
        }
    }

    @Test
    fun `van chien dich co du bon muc theo dung thu tu spec`() {
        assertEquals(
            listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.SETTINGS, PauseItem.LEAVE),
            PauseMenu.itemsForCampaign(),
        )
    }
}
