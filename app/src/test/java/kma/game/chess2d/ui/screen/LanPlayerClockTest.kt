package kma.game.chess2d.ui.screen

import kma.game.chess2d.lan.LanUiState
import kma.game.chess2d.net.ClockTimes
import kma.game.chess2d.net.TimeControl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LanPlayerClockTest {
    @Test
    fun clocksFollowYourColorInsteadOfHeaderPosition() {
        val times = ClockTimes(whiteMillis = 123_000L, blackMillis = 45_000L)
        val white = LanUiState(youPlayWhite = true, timeControl = TimeControl.BLITZ)
        assertEquals(123_000L, playerClockMillis(white, times, forYou = true))
        assertEquals(45_000L, playerClockMillis(white, times, forYou = false))
        val black = white.copy(youPlayWhite = false)
        assertEquals(45_000L, playerClockMillis(black, times, forYou = true))
        assertEquals(123_000L, playerClockMillis(black, times, forYou = false))
    }

    @Test
    fun unlimitedGamesDoNotInventACountdown() {
        val state = LanUiState(timeControl = TimeControl.UNLIMITED)
        assertNull(playerClockMillis(state, null, forYou = true))
        assertNull(playerClockMillis(state, null, forYou = false))
    }

    @Test
    fun clockUsesServerSnapshotOrInitialTimeBeforeFirstSnapshot() {
        val initial = LanUiState(timeControl = TimeControl.RAPID)
        assertEquals(TimeControl.RAPID.initialMillis, playerClockMillis(initial, null, forYou = true))
        val state = initial.copy(clock = ClockTimes(120_000L, 90_000L))
        assertEquals(120_000L, playerClockMillis(state, null, forYou = true))
        assertEquals(90_000L, playerClockMillis(state, null, forYou = false))
    }
}
