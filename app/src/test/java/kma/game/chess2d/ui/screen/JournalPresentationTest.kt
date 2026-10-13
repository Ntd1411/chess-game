package kma.game.chess2d.ui.screen

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.profile.Journal
import kma.game.chess2d.profile.JournalState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JournalPresentationTest {
    @Test
    fun unmetCharactersHideNamesAndPortraitsForEveryAi() {
        for (character in AiCharacters.all) {
            for (state in listOf(JournalState.UNKNOWN, JournalState.LOCKED)) {
                val page = Journal.Page(character, state, null)
                assertEquals(R.string.ai_hidden_name, journalNameRes(page))
                assertNull(journalPortraitRes(page))
            }
        }
    }

    @Test
    fun metCharactersUseTheirOwnNamesAndPortraits() {
        AiCharacters.all.forEach { character ->
            val page = Journal.Page(character, JournalState.MET, null)
            assertEquals(character.name, journalNameRes(page))
            assertEquals(character.portrait, journalPortraitRes(page))
        }
    }

    @Test
    fun eachJournalStateKeepsItsExistingLabel() {
        assertEquals(R.string.journal_state_met, journalStateLabel(JournalState.MET))
        assertEquals(R.string.journal_state_unknown, journalStateLabel(JournalState.UNKNOWN))
        assertEquals(R.string.journal_state_locked, journalStateLabel(JournalState.LOCKED))
    }

    @Test
    fun everyCardReservesTheSameNameAndFooterLineCounts() {
        assertEquals(2, JournalLayout.NAME_LINES)
        assertEquals(1, JournalLayout.FOOTER_LINES)
        val source = File("src/main/java/kma/game/chess2d/ui/screen/JournalScreen.kt").readText()
        assertTrue(source.contains("minLines = JournalLayout.NAME_LINES, maxLines = JournalLayout.NAME_LINES"))
        assertTrue(source.contains("minLines = JournalLayout.FOOTER_LINES, maxLines = JournalLayout.FOOTER_LINES"))
    }

    @Test
    fun unlockCaptionIsOnlyPopulatedForLockedCards() {
        AiCharacters.all.forEach { character ->
            assertEquals(character.unlockFloor, journalUnlockFloor(Journal.Page(character, JournalState.LOCKED, null)))
            assertNull(journalUnlockFloor(Journal.Page(character, JournalState.MET, null)))
            assertNull(journalUnlockFloor(Journal.Page(character, JournalState.UNKNOWN, null)))
        }
    }

    @Test
    fun responsiveGridFitsAllSupportedWidths() {
        for (width in 144..560) {
            val columns = JournalLayout.columnsFor(width.toFloat())
            assertTrue(columns in 1..3)
            assertTrue(columns * JournalLayout.MIN_CELL_DP + (columns - 1) * JournalLayout.GAP_DP <= width)
        }
        assertEquals(1, JournalLayout.columnsFor(100f))
        assertEquals(2, JournalLayout.columnsFor(320f))
        assertEquals(3, JournalLayout.columnsFor(560f))
    }
}
