package kma.game.chess2d.settings

import java.util.Locale

/** Local full-content unlock, not authentication or a server-issued purchase. */
object UnlockCode {
    const val FULL_GAME = "TOWER49"
    fun matches(input: String): Boolean = input.trim().uppercase(Locale.ROOT) == FULL_GAME
}

enum class UnlockResult { UNLOCKED, ALREADY_UNLOCKED, INVALID_CODE }
