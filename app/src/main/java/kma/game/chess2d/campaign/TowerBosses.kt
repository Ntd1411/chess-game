package kma.game.chess2d.campaign

import androidx.annotation.StringRes
import kma.game.chess2d.ai.AiStrength
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.opponent.AiCharacters

/** Seven distinct AI guardians, one for each chapter, with increasing search limits. */
object TowerBosses {
    private val characterIds = listOf(
        "chess_mage", "dark_knight", "blood_queen", "dark_king",
        "silent_bishop", "golden_queen", "hidden",
    )
    private val budgets = listOf(500L, 900L, 1_500L, 2_200L, 3_000L, 4_000L, 5_000L)
    private val choices = listOf(3, 3, 2, 2, 1, 1, 1)
    private val mistakes = listOf(0.20, 0.12, 0.06, 0.03, 0.0, 0.0, 0.0)

    fun characterId(floor: Int): String {
        require(floor in 7..49 && floor % 7 == 0)
        return characterIds[floor / 7 - 1]
    }

    fun character(floor: Floor): AiCharacter? = floor.bossId?.let { id ->
        AiCharacters.all.first { it.id == id }
    }

    /** Each guardian introduces their own personality without recycled rematch dialogue. */
    @StringRes
    fun greeting(floor: Floor): Int? = character(floor)?.quote

    fun strength(floor: Int): AiStrength {
        require(floor in 7..49 && floor % 7 == 0)
        val index = floor / 7 - 1
        return AiStrength(index + 2, budgets[index], choices[index], mistakes[index])
    }
}
