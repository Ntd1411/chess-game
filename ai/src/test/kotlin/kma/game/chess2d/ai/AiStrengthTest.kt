package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.MoveGenerator
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AiStrengthTest {
    @Test
    fun defaultSettingsPreserveSeededMoves() {
        for (difficulty in Difficulty.entries) {
            assertEquals(difficulty.depth, AiStrength.from(difficulty).depth)
            assertEquals(difficulty.timeBudgetMillis, AiStrength.from(difficulty).timeBudgetMillis)
        }
        val board = Board.startPosition()
        val implicit = Ai(Random(42)).chooseMove(board, Difficulty.EASY)
        val explicit = Ai(Random(42)).chooseMove(board, Difficulty.EASY, strength = AiStrength.from(Difficulty.EASY))
        assertEquals(implicit.raw, explicit.raw)
    }

    @Test
    fun campaignSettingsOverrideDepthAndPreserveBoard() {
        val board = Board.startPosition()
        val before = board.toFen()
        var deepest = 0
        val move = Ai().chooseMove(
            board, Difficulty.HARD,
            strength = AiStrength(depth = 1, timeBudgetMillis = 500),
            onProgress = { deepest = maxOf(deepest, it.depth) },
        )
        assertEquals(1, deepest)
        assertTrue(MoveGenerator.legalMoves(board).any { it.raw == move.raw })
        assertEquals(before, board.toFen())
    }

    @Test
    fun invalidLimitsAreRejected() {
        assertTrue(runCatching { AiStrength(0, 500) }.isFailure)
        assertTrue(runCatching { AiStrength(2, 0) }.isFailure)
        assertTrue(runCatching { AiStrength(2, 500, 0) }.isFailure)
        assertTrue(runCatching { AiStrength(2, 500, blunderChance = 1.1) }.isFailure)
    }
}
