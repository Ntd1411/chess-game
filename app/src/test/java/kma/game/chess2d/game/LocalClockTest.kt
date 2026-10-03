package kma.game.chess2d.game

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.history.MatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Setup hai người, đồng hồ có tạm dừng/khôi phục, định dạng giờ và luật hết giờ. */
class LocalClockTest {

    private val minute = 60_000L

    // ---- LocalSetup ----

    @Test
    fun `bon muc thoi gian dung theo spec`() {
        assertEquals(listOf(0, 5, 10, 30), LocalTimeControl.entries.map { it.minutes })
        assertFalse(LocalTimeControl.UNLIMITED.limited)
        assertTrue(LocalTimeControl.MIN_5.limited)
        assertEquals(30 * minute, LocalTimeControl.MIN_30.initialMillis)
    }

    @Test
    fun `o ten trong hoac chi co khoang trang thi dung ten mac dinh`() {
        val setup = LocalSetup.of("", "   ", LocalTimeControl.UNLIMITED, "Nguoi choi 1", "Nguoi choi 2")
        assertEquals("Nguoi choi 1", setup.whiteName)
        assertEquals("Nguoi choi 2", setup.blackName)
    }

    @Test
    fun `chi mot o trong thi chi o do dung mac dinh`() {
        val setup = LocalSetup.of("An", "", LocalTimeControl.MIN_10, "P1", "P2")
        assertEquals("An", setup.whiteName)
        assertEquals("P2", setup.blackName)
        assertEquals(LocalTimeControl.MIN_10, setup.time)
    }

    @Test
    fun `ten duoc cat khoang trang va gop khoang trang lien nhau`() {
        assertEquals("Binh An", LocalSetup.cleanName("  Binh    An  "))
    }

    @Test
    fun `ten qua dai bi cat ve gioi han`() {
        val cleaned = LocalSetup.cleanName("x".repeat(100))
        assertEquals(LocalSetup.MAX_NAME_LENGTH, cleaned?.length)
    }

    // ---- LocalClock ----

    @Test
    fun `chua di nuoc nao thi dong ho khong chay`() {
        val clock = LocalClock(5 * minute)
        val times = clock.snapshot(nowMillis = 10 * minute)
        assertEquals(5 * minute, times.whiteMillis)
        assertEquals(5 * minute, times.blackMillis)
        assertNull(clock.flaggedSide(nowMillis = 10 * minute))
    }

    @Test
    fun `nuoc dau cua Trang khong ton gio va gio cua Den bat dau dem ngay sau do`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 1_000)
        val times = clock.snapshot(nowMillis = 1_000 + 20_000)
        assertEquals(5 * minute, times.whiteMillis)
        assertEquals(5 * minute - 20_000, times.blackMillis)
    }

    @Test
    fun `moi nuoc di tru gio da nghi cua ben vua di`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 0) // Trang di nuoc dau
        clock.onMovePlayed(nowMillis = 10_000) // Den nghi 10 giay
        clock.onMovePlayed(nowMillis = 25_000) // Trang nghi 15 giay
        val times = clock.snapshot(nowMillis = 25_000)
        assertEquals(5 * minute - 15_000, times.whiteMillis)
        assertEquals(5 * minute - 10_000, times.blackMillis)
    }

    @Test
    fun `tam dung dung phan gio dang troi va khong tru them`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 0)
        clock.pause(nowMillis = 4_000)
        val during = clock.snapshot(nowMillis = 999_000)
        assertEquals(5 * minute - 4_000, during.blackMillis)
    }

    @Test
    fun `tiep tuc sau tam dung chi tru tu luc tiep tuc`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 0)
        clock.pause(nowMillis = 4_000)
        clock.resume(nowMillis = 100_000)
        val times = clock.snapshot(nowMillis = 103_000)
        assertEquals(5 * minute - 7_000, times.blackMillis)
    }

    @Test
    fun `tam dung hai lan lien tiep khong tru hai lan`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 0)
        clock.pause(nowMillis = 4_000)
        clock.pause(nowMillis = 50_000)
        assertEquals(5 * minute - 4_000, clock.snapshot(nowMillis = 60_000).blackMillis)
    }

    @Test
    fun `het gio thi ben do bi bao va giay con lai khong am`() {
        val clock = LocalClock(minute)
        clock.onMovePlayed(nowMillis = 0)
        assertNull(clock.flaggedSide(nowMillis = minute - 1))
        assertEquals(false, clock.flaggedSide(nowMillis = minute)) // Den het gio
        assertEquals(0L, clock.snapshot(nowMillis = 10 * minute).blackMillis)
    }

    @Test
    fun `Trang het gio duoc bao la true`() {
        val clock = LocalClock(minute)
        clock.onMovePlayed(nowMillis = 0)
        clock.onMovePlayed(nowMillis = 1_000)
        assertEquals(true, clock.flaggedSide(nowMillis = 1_000 + minute))
    }

    @Test
    fun `dung han thi dong ho dung lai va nuoc di sau do bi bo qua`() {
        val clock = LocalClock(5 * minute)
        clock.onMovePlayed(nowMillis = 0)
        clock.stop(nowMillis = 3_000)
        clock.onMovePlayed(nowMillis = 9_000)
        clock.resume(nowMillis = 9_000)
        val times = clock.snapshot(nowMillis = 500_000)
        assertEquals(5 * minute - 3_000, times.blackMillis)
        assertEquals(5 * minute, times.whiteMillis)
    }

    @Test
    fun `khoi phuc tu gio da luu thi tam dung cho den khi tiep tuc`() {
        val clock = LocalClock.restore(
            initialMillis = 5 * minute,
            whiteMillis = 4 * minute,
            blackMillis = 3 * minute,
            whiteToMove = true,
            started = true,
        )
        // Chua resume: thoi gian ung dung bi tat khong bi tinh.
        assertEquals(4 * minute, clock.snapshot(nowMillis = 99 * minute).whiteMillis)
        clock.resume(nowMillis = 100 * minute)
        val times = clock.snapshot(nowMillis = 100 * minute + 5_000)
        assertEquals(4 * minute - 5_000, times.whiteMillis)
        assertEquals(3 * minute, times.blackMillis)
    }

    @Test
    fun `khoi phuc ke ca khi gio luu vuot muc ban dau thi bi chan o muc ban dau`() {
        val clock = LocalClock.restore(minute, whiteMillis = 99 * minute, blackMillis = -5, whiteToMove = true, started = true)
        val times = clock.snapshot(nowMillis = 0)
        assertEquals(minute, times.whiteMillis)
        assertEquals(0L, times.blackMillis)
    }

    @Test
    fun `khoi phuc khi chua co nuoc nao thi tiep tuc van chua chay`() {
        val clock = LocalClock.restore(minute, minute, minute, whiteToMove = true, started = false)
        clock.resume(nowMillis = 0)
        assertEquals(minute, clock.snapshot(nowMillis = 30_000).whiteMillis)
    }

    // ---- ClockFormat ----

    @Test
    fun `dinh dang mm ss lam tron len toi giay`() {
        assertEquals("05:00", ClockFormat.mmss(5 * minute))
        // Con 299,999 giay van hien 05:00 (lam tron LEN), chi xuong 04:59 khi da troi qua tron mot giay.
        assertEquals("05:00", ClockFormat.mmss(5 * minute - 1))
        assertEquals("04:59", ClockFormat.mmss(5 * minute - 1_000))
        assertEquals("00:01", ClockFormat.mmss(400))
        assertEquals("00:00", ClockFormat.mmss(0))
        assertEquals("30:00", ClockFormat.mmss(30 * minute))
    }

    @Test
    fun `gio am duoc xem nhu het gio`() {
        assertEquals("00:00", ClockFormat.mmss(-1234))
        assertEquals(0L, ClockFormat.roundUpToSecond(-1))
    }

    // ---- TimeoutRule ----

    @Test
    fun `het gio o the cuoc chuan thi ben con lai thang`() {
        val board = Engine.newGame()
        assertEquals(MatchResult.LOSS, TimeoutRule.resultForWhite(board, whiteFlagged = true))
        assertEquals(MatchResult.WIN, TimeoutRule.resultForWhite(board, whiteFlagged = false))
    }

    @Test
    fun `ben con lai chi co Vua thi het gio la hoa`() {
        val board = Engine.fromFen("8/8/8/4k3/8/8/4Q3/4K3 w - - 0 1")
        // Den chi con Vua nen Den khong the chieu het: Trang het gio thi hoa.
        assertEquals(MatchResult.DRAW, TimeoutRule.resultForWhite(board, whiteFlagged = true))
        // Trang con Hau nen Den het gio thi Trang thang.
        assertEquals(MatchResult.WIN, TimeoutRule.resultForWhite(board, whiteFlagged = false))
    }

    @Test
    fun `Vua va mot quan nhe chong Vua tran thi het gio la hoa`() {
        val board = Engine.fromFen("8/8/8/4k3/8/8/4B3/4K3 w - - 0 1")
        assertEquals(MatchResult.DRAW, TimeoutRule.resultForWhite(board, whiteFlagged = false))
    }

    @Test
    fun `Vua va mot quan nhe nhung doi phuong con tot thi van co the chieu het`() {
        val board = Engine.fromFen("8/8/8/4k3/4p3/8/4B3/4K3 w - - 0 1")
        assertEquals(MatchResult.WIN, TimeoutRule.resultForWhite(board, whiteFlagged = false))
    }

    @Test
    fun `Vua va hai quan nhe van co the chieu het`() {
        val board = Engine.fromFen("8/8/8/4k3/8/8/3NN3/4K3 w - - 0 1")
        assertTrue(TimeoutRule.canMate(board, white = true))
    }

    @Test
    fun `chi con mot tot cung la du de chieu het`() {
        val board = Engine.fromFen("8/8/8/4k3/8/8/4P3/4K3 w - - 0 1")
        assertTrue(TimeoutRule.canMate(board, white = true))
        assertFalse(TimeoutRule.canMate(board, white = false))
    }
}
