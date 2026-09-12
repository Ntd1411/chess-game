package kma.game.chess2d.net

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm tra đồng hồ thi đấu (mục 7.2).
 *
 * Mọi mốc thọi gian được truyền vào bằng tay nên test chạy tức thì, không cần chọ
 * một giây thật nào.
 */
class ClockTest {

    @Test
    fun `khong gioi han thi khong tru gio cua ai`() {
        val clock = MatchClock(TimeControl.UNLIMITED)
        clock.start(nowMillis = 0)
        clock.onMovePlayed(nowMillis = 30_000)
        assertEquals(ClockTimes(0, 0), clock.snapshot(nowMillis = 60_000))
        assertNull(clock.flaggedSide(nowMillis = 60_000))
    }

    @Test
    fun `chi tru gio cua ben den luot`() {
        val clock = MatchClock(TimeControl.BLITZ)
        clock.start(nowMillis = 0)
        val times = clock.snapshot(nowMillis = 10_000)
        assertEquals(TimeControl.BLITZ.initialMillis - 10_000, times.whiteMillis)
        assertEquals(TimeControl.BLITZ.initialMillis, times.blackMillis)
    }

    @Test
    fun `di xong mot nuoc thi doi ben bi tru`() {
        val clock = MatchClock(TimeControl.BLITZ)
        clock.start(nowMillis = 0)
        clock.onMovePlayed(nowMillis = 10_000)
        val times = clock.snapshot(nowMillis = 14_000)
        assertEquals(TimeControl.BLITZ.initialMillis - 10_000, times.whiteMillis)
        assertEquals(TimeControl.BLITZ.initialMillis - 4_000, times.blackMillis)
    }

    @Test
    fun `the thuc co tang them thi cong sau moi nuoc`() {
        val clock = MatchClock(TimeControl.RAPID)
        clock.start(nowMillis = 0)
        clock.onMovePlayed(nowMillis = 10_000)
        val times = clock.snapshot(nowMillis = 10_000)
        val expected = TimeControl.RAPID.initialMillis - 10_000 + TimeControl.RAPID.incrementMillis
        assertEquals(expected, times.whiteMillis)
    }

    @Test
    fun `het gio thi bao dung ben`() {
        val clock = MatchClock(TimeControl.BLITZ)
        clock.start(nowMillis = 0)
        val now = TimeControl.BLITZ.initialMillis + 1_000
        assertEquals(true, clock.flaggedSide(nowMillis = now))
        assertEquals(0, clock.snapshot(nowMillis = now).whiteMillis)
    }

    @Test
    fun `dung dong ho thi thoi gian dung lai`() {
        val clock = MatchClock(TimeControl.BLITZ)
        clock.start(nowMillis = 0)
        clock.stop(nowMillis = 5_000)
        val stopped = clock.snapshot(nowMillis = 60_000)
        assertEquals(TimeControl.BLITZ.initialMillis - 5_000, stopped.whiteMillis)
        assertTrue(stopped.blackMillis == TimeControl.BLITZ.initialMillis)
    }
}
