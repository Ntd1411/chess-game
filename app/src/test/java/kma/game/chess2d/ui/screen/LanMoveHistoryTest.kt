package kma.game.chess2d.ui.screen

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Fen
import org.junit.Assert.assertEquals
import org.junit.Test

class LanMoveHistoryTest {
    private fun moves(startFen: String, vararg ucis: String): List<Int> {
        val board = Fen.parse(startFen)
        return ucis.map { uci ->
            val move = Engine.legalMoves(board).first { it.toUci() == uci }
            board.makeMove(move)
            move.raw
        }
    }

    @Test
    fun lanHistoryDisplaysSanIncludingCastling() {
        val raw = moves(Engine.START_FEN, "e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "b5a4", "g8f6", "e1g1")
        assertEquals(listOf("e4", "e5", "Nf3", "Nc6", "Bb5", "a6", "Ba4", "Nf6", "O-O"),
            lanSanMoves(Engine.START_FEN, raw))
        assertEquals(9, raw.size)
    }

    @Test
    fun emptyHistoryAndBlackStartingPositionWork() {
        assertEquals(emptyList<String>(), lanSanMoves(Engine.START_FEN, emptyList()))
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 12"
        val raw = moves(fen, "e7e5", "g1f3")
        assertEquals(listOf("e5", "Nf3"), lanSanMoves(fen, raw))
    }
}
