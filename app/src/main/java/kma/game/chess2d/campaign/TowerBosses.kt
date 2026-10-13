package kma.game.chess2d.campaign

import androidx.annotation.StringRes
import kma.game.chess2d.R
import kma.game.chess2d.ai.AiStrength
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.opponent.AiCharacters

/** Reuse the AI roster; later encounters are stronger rematches, not new characters. */
object TowerBosses {
    private val characterIds = listOf(
        "chess_mage", "dark_knight", "blood_queen", "dark_king",
        "dark_knight", "blood_queen", "hidden",
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

    /** Rematches acknowledge the previous encounter without changing regular AI quotes. */
    @StringRes
    fun greeting(floor: Floor): Int? = character(floor)?.let { opponent ->
        when {
            floor.number == 35 && opponent.id == "dark_knight" -> R.string.tower_knight_rematch_quote
            floor.number == 42 && opponent.id == "blood_queen" -> R.string.tower_queen_rematch_quote
            else -> opponent.quote
        }
    }

    fun strength(floor: Int): AiStrength {
        require(floor in 7..49 && floor % 7 == 0)
        val index = floor / 7 - 1
        return AiStrength(index + 2, budgets[index], choices[index], mistakes[index])
    }
}
