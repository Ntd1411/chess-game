package kma.game.chess2d.profile

import kma.game.chess2d.history.MatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Dữ liệu hiển thị của màn Hồ sơ: tiến độ cấp, tỉ lệ thắng, định dạng thời gian (thuần). */
class ProfileDisplayTest {

    @Test
    fun `diem tien trien la thang cong tang da vuot`() {
        val s = PlayerStats(wins = 3, highestFloor = 4)
        assertEquals(7, s.progressPoints)
    }

    @Test
    fun `diem trong cap luon nho hon so diem moi cap`() {
        for (wins in 0..40) {
            val s = PlayerStats(wins = wins, highestFloor = 3)
            assertTrue(s.pointsIntoLevel in 0 until PlayerStats.GAMES_PER_LEVEL)
        }
    }

    @Test
    fun `du diem thi len cap va diem trong cap ve khong`() {
        val s = PlayerStats(wins = PlayerStats.GAMES_PER_LEVEL)
        assertEquals(2, s.level)
        assertEquals(0, s.pointsIntoLevel)
    }

    @Test
    fun `chua choi van nao thi chua co ti le thang`() {
        assertNull(PlayerStats().winRatePercent)
    }

    @Test
    fun `ti le thang lam tron xuong`() {
        val s = PlayerStats(wins = 1, losses = 2)
        assertEquals(33, s.winRatePercent)
    }

    @Test
    fun `thang toan bo la mot tram phan tram`() {
        assertEquals(100, PlayerStats(wins = 4).winRatePercent)
    }

    @Test
    fun `thang them van khong lam ti le thang vuot qua mot tram`() {
        val s = PlayerStats().withMatch(MatchResult.WIN, byCheckmate = false, seconds = 1)
        assertEquals(100, s.winRatePercent)
    }

    @Test
    fun `duoi mot phut hien bang giay`() {
        val t = PlayTime.of(42)
        assertEquals(PlayTime.Unit.SECONDS, t.unit)
        assertEquals(42, t.seconds)
    }

    @Test
    fun `tu mot phut den duoi mot gio hien bang phut`() {
        val t = PlayTime.of(125)
        assertEquals(PlayTime.Unit.MINUTES, t.unit)
        assertEquals(2, t.minutes)
    }

    @Test
    fun `tu mot gio tro len hien gio va phut`() {
        val t = PlayTime.of(3 * 3600 + 25 * 60 + 7)
        assertEquals(PlayTime.Unit.HOURS_MINUTES, t.unit)
        assertEquals(3L, t.hours)
        assertEquals(25, t.minutes)
    }

    @Test
    fun `dung mot gio la gio khong phut`() {
        val t = PlayTime.of(3600)
        assertEquals(PlayTime.Unit.HOURS_MINUTES, t.unit)
        assertEquals(1L, t.hours)
        assertEquals(0, t.minutes)
    }

    @Test
    fun `thoi gian am coi nhu khong`() {
        val t = PlayTime.of(-100)
        assertEquals(PlayTime.Unit.SECONDS, t.unit)
        assertEquals(0, t.seconds)
    }
}
