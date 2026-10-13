package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.campaign.Speaker
import org.junit.Assert.*
import org.junit.Test

class DialoguePresentationTest {
    @Test fun advanceFinishesOnlyAfterLastLine() {
        assertNull(nextDialogueIndex(0, 0))
        assertNull(nextDialogueIndex(0, 1))
        assertEquals(1, nextDialogueIndex(0, 2))
        assertNull(nextDialogueIndex(1, 2))
        assertNull(nextDialogueIndex(9, 2))
    }
    @Test fun everySpeakerRetainsTheCorrectPortrait() {
        assertEquals(R.drawable.portrait_tower_keeper, portraitRes(Speaker.NARRATOR))
        assertEquals(R.drawable.portrait_protagonist, portraitRes(Speaker.TRAVELER))
        assertEquals(R.drawable.portrait_unknown_03, portraitRes(Speaker.PLAYER_48))
        assertEquals(R.drawable.portrait_golden_queen, portraitRes(Speaker.ARIA))
    }
}
