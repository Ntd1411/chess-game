package kma.game.chess2d.profile

import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.campaign.TowerCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/** Dựng trang Nhật ký từ danh sách nhân vật và bản ghi đã gặp (thuần, không cần Room). */
class JournalPagesTest {

    private val all = AiCharacters.all

    private fun entry(id: String, times: Int = 1) =
        JournalEntry(npcId = id, firstMetAtMillis = 1_000L, timesMet = times)

    @Test
    fun `trang giu dung thu tu va du so nhan vat`() {
        val pages = Journal.pages(all, emptyMap(), clearedUpTo = 0)
        assertEquals(all.map { it.id }, pages.map { it.character.id })
    }

    @Test
    fun `chua gap ai thi nhan vat mo san la chua ro va nhan vat an la khoa`() {
        val pages = Journal.pages(all, emptyMap(), clearedUpTo = 0)
        val hidden = pages.single { it.character.id == "hidden" }
        assertEquals(JournalState.LOCKED, hidden.state)
        pages.filter { it.character.id != "hidden" }.forEach {
            assertEquals(JournalState.UNKNOWN, it.state)
        }
    }

    @Test
    fun `chi nhan vat da gap moi co lich su gap`() {
        val pages = Journal.pages(all, mapOf("chess_mage" to entry("chess_mage", times = 3)), clearedUpTo = 0)
        val met = pages.single { it.character.id == "chess_mage" }
        assertEquals(JournalState.MET, met.state)
        assertEquals(3, met.entry?.timesMet)
        pages.filter { it.character.id != "chess_mage" }.forEach { assertNull(it.entry) }
    }

    @Test
    fun `vuot het thap thi nhan vat an chuyen sang chua ro`() {
        val pages = Journal.pages(all, emptyMap(), clearedUpTo = TowerCatalog.FLOOR_COUNT)
        assertEquals(JournalState.UNKNOWN, pages.single { it.character.id == "hidden" }.state)
    }

    @Test
    fun `gap nhan vat an thi la da gap du chua mo theo tien do`() {
        // Bản ghi đã gặp luôn thắng điều kiện mở, giống Journal.stateOf.
        val pages = Journal.pages(all, mapOf("hidden" to entry("hidden")), clearedUpTo = 0)
        val hidden = pages.single { it.character.id == "hidden" }
        assertEquals(JournalState.MET, hidden.state)
        assertNotNull(hidden.entry)
    }

    @Test
    fun `ban ghi cua nhan vat khong ton tai bi bo qua`() {
        val pages = Journal.pages(all, mapOf("ghost" to entry("ghost")), clearedUpTo = 0)
        assertEquals(0, Journal.metCount(pages))
        assertEquals(all.size, pages.size)
    }

    @Test
    fun `dem dung so nhan vat da gap`() {
        val entries = mapOf("chess_mage" to entry("chess_mage"), "dark_knight" to entry("dark_knight"))
        val pages = Journal.pages(all, entries, clearedUpTo = 0)
        assertEquals(2, Journal.metCount(pages))
    }
}
