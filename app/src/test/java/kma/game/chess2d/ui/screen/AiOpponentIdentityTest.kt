package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.SavedGame
import kma.game.chess2d.opponent.AiCharacters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AiOpponentIdentityTest {
    @Test
    fun eachSelectedAiKeepsItsOwnPortraitInGameplay() {
        AiCharacters.all.forEach { character ->
            val state = GameUiState(mode = GameMode.VS_COMPUTER,
                difficulty = character.difficulty, aiCharacterId = character.id)
            assertEquals(character.id, character.portrait, opponentPortrait(state))
        }
    }

    @Test
    fun changingSearchDifficultyDoesNotChangeSelectedPortrait() {
        val state = GameUiState(mode = GameMode.VS_COMPUTER,
            difficulty = Difficulty.EASY, aiCharacterId = "golden_queen")
        assertEquals(R.drawable.portrait_golden_queen, opponentPortrait(state))
    }

    @Test
    fun oldSavesWithoutIdentityUseTheLegacyDifficultyFallback() {
        val old = SavedGame(Engine.START_FEN, listOf("e2e4"), GameMode.VS_COMPUTER, Difficulty.HARD)
        assertNull(old.aiCharacterId)
        val state = GameUiState(mode = old.mode, difficulty = old.difficulty, aiCharacterId = old.aiCharacterId)
        assertEquals(R.drawable.portrait_ai_blood_queen, opponentPortrait(state))
        assertEquals("silent_bishop", old.copy(aiCharacterId = "silent_bishop").aiCharacterId)
    }

    @Test
    fun localMultiplayerNeverUsesAnAiPortrait() {
        assertEquals(R.drawable.portrait_player_two,
            opponentPortrait(GameUiState(mode = GameMode.TWO_PLAYERS, aiCharacterId = "golden_queen")))
    }
}
