package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.history.MatchRecord
import kma.game.chess2d.history.MatchResult
import org.junit.Assert.*
import org.junit.Test

class HistoryPresentationTest {
    private fun record(moves: String) = MatchRecord(playedAtMillis = 0, mode = GameMode.TWO_PLAYERS.name,
        startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", moves = moves, result = MatchResult.DRAW)

    @Test fun replayIncludesInitialPositionAndEveryLegalMove() {
        val frames = framesOf(record("e2e4 e7e5 g1f3"))
        assertEquals(4, frames.size)
        assertTrue(frames.all { it.size == 32 })
        assertNotEquals(frames[0], frames[1])
        assertNotEquals(frames[1], frames[2])
        assertEquals(1, framesOf(record("")).size)
    }
    @Test fun replayStopsBeforeCorruptOrIllegalMove() {
        assertEquals(framesOf(record("e2e4")), framesOf(record("e2e4 e2e5 g1f3")))
        assertEquals(1, framesOf(record("not-a-move")).size)
    }
    @Test fun resultAndModeMappingsArePreserved() {
        assertEquals("1-0", pgnResultOf(MatchResult.WIN))
        assertEquals("0-1", pgnResultOf(MatchResult.LOSS))
        assertEquals("1/2-1/2", pgnResultOf(MatchResult.DRAW))
        assertEquals(R.string.history_result_win, resultLabel(MatchResult.WIN))
        assertEquals(R.string.history_result_loss, resultLabel(MatchResult.LOSS))
        assertEquals(R.string.history_result_draw, resultLabel(MatchResult.DRAW))
        assertEquals(R.string.history_mode_two_players, modeLabel(GameMode.TWO_PLAYERS.name))
        assertEquals(R.string.history_mode_vs_computer, modeLabel(GameMode.VS_COMPUTER.name))
        assertEquals(R.string.history_mode_lan, modeLabel("LAN"))
    }
}
