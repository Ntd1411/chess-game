package kma.game.chess2d.ui.screen

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelDialogPresentationTest {
    @Test
    fun difficultyPanelIncludesAllThreeLevelsInOrder() {
        assertEquals(listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD), LevelDialogLayout.levels)
        assertEquals(R.string.difficulty_easy, labelOf(Difficulty.EASY))
        assertEquals(R.string.difficulty_medium, labelOf(Difficulty.MEDIUM))
        assertEquals(R.string.difficulty_hard, labelOf(Difficulty.HARD))
    }

    @Test
    fun drawnOptionsHaveComfortableTapTargets() {
        assertTrue(LevelDialogLayout.OPTION_MIN_HEIGHT_DP >= 48)
        assertTrue(LevelDialogLayout.MAX_WIDTH_DP in 320..440)
    }

    @Test
    fun panelUsesSelectableDrawnRowsWithoutBitmapControls() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/LevelDialog.kt").readText()
        assertTrue(source.contains("selectableGroup()"))
        assertTrue(source.contains("Role.RadioButton"))
        assertFalse(source.contains("GothicButton"))
        assertFalse(source.contains("ArtImage"))
    }
}
