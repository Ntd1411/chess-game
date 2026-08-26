package kma.game.chess2d.ai

import kma.game.chess2d.engine.Engine

/**
 * Placeholder cua Phase 0. Phase 3 se hien thuc:
 * negamax + alpha-beta -> move ordering -> iterative deepening
 * -> quiescence -> transposition table -> evaluation.
 */
enum class Difficulty(val depth: Int, val timeBudgetMillis: Long) {
    EASY(2, 500),
    MEDIUM(4, 2_000),
    HARD(6, 5_000),
}

object Ai {
    fun describe(difficulty: Difficulty): String =
        "${difficulty.name}: depth=${difficulty.depth}, " +
            "budget=${difficulty.timeBudgetMillis}ms, board=${Engine.BOARD_SIZE}"
}
