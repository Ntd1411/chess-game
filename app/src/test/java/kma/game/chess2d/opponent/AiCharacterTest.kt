package kma.game.chess2d.opponent

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.TowerBosses
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiCharacterTest {
    private val characters = AiCharacters.all

    @Test
    fun sevenCharactersHaveDistinctIdsAndPortraits() {
        assertEquals(7, characters.size)
        assertEquals(7, characters.map { it.id }.toSet().size)
        assertEquals(7, characters.map { it.portrait }.toSet().size)
        for (character in characters) {
            assertNotEquals(0, character.name)
            assertNotEquals(0, character.portrait)
            assertNotEquals(0, character.style)
            assertNotEquals(0, character.personality)
            assertNotEquals(0, character.quote)
        }
        assertEquals(R.drawable.portrait_silent_bishop, characters.single { it.id == "silent_bishop" }.portrait)
        assertEquals(R.drawable.portrait_golden_queen, characters.single { it.id == "golden_queen" }.portrait)
    }

    @Test
    fun onlyFirstThreeCharactersAreInitiallyUnlocked() {
        assertEquals(listOf("chess_mage", "dark_knight", "blood_queen"),
            characters.filter { it.isUnlocked(0) }.map { it.id })
        assertEquals(listOf(28, 35, 42, 49), characters.drop(3).map { it.unlockFloor })
        assertTrue(AiCharacters.default.isUnlocked(0))
    }

    @Test
    fun laterCharactersUnlockOnlyAfterDefeatingTheirOwnFloor() {
        characters.drop(3).forEach { character ->
            val floor = requireNotNull(character.unlockFloor)
            assertEquals(character.id, TowerBosses.characterId(floor))
            assertFalse(character.id, character.isUnlocked(floor - 1))
            assertTrue(character.id, character.isUnlocked(floor))
        }
        for (cleared in 0..49) {
            assertEquals(3 + listOf(28, 35, 42, 49).count { it <= cleared },
                characters.count { it.isUnlocked(cleared) })
        }
    }

    @Test
    fun displayedStrengthUsesRealDifficulty() {
        characters.forEach {
            assertEquals(it.difficulty.ordinal + 1, it.stars)
            assertTrue(it.stars in 1..3)
            assertEquals(it.difficulty.timeBudgetMillis / 1000f, it.thinkSeconds, 0f)
        }
        val levels = characters.map { it.difficulty.ordinal }
        assertEquals(levels.sorted(), levels)
    }

    @Test
    fun sharedHardDifficultyDoesNotCollapseCharacterIdentity() {
        characters.forEach {
            assertEquals(it, AiCharacters.resolve(it.id, it.difficulty))
        }
        assertEquals("silent_bishop", AiCharacters.resolve("silent_bishop", Difficulty.MEDIUM).id)
        assertEquals("golden_queen", AiCharacters.resolve("golden_queen", Difficulty.HARD).id)
        assertEquals("blood_queen", AiCharacters.resolve(null, Difficulty.HARD).id)
        assertEquals("dark_knight", AiCharacters.resolve("missing-old-id", Difficulty.MEDIUM).id)
    }

    @Test
    fun selectionUsesPortraitsInsteadOfFullBodyArtwork() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/AiSelectionScreen.kt").readText()
        assertTrue(source.contains("res = current.portrait"))
        assertFalse(source.contains("fullBody"))
        assertFalse(source.contains("character_ai_blood_queen"))
    }
}
