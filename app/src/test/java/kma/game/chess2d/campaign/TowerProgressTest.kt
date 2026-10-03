package kma.game.chess2d.campaign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Luật mở khóa tầng: thắng tầng N thì mở tầng N+1, không nhảy cóc, không lùi.
 *
 * Kiểm lớp thuần [TowerProgress]; phần lưu DataStore chỉ bọc lại lớp này.
 */
class TowerProgressTest {

    @Test
    fun `luc dau chi tang 1 la tang hien tai`() {
        val progress = TowerProgress()
        assertEquals(FloorStatus.CURRENT, progress.statusOf(1))
        assertEquals(FloorStatus.LOCKED, progress.statusOf(2))
        assertEquals(1, progress.currentFloor)
    }

    @Test
    fun `thang tang N thi mo khoa tang N cong 1`() {
        val progress = TowerProgress().withCleared(1)
        assertEquals(FloorStatus.CLEARED, progress.statusOf(1))
        assertEquals(FloorStatus.CURRENT, progress.statusOf(2))
        assertEquals(FloorStatus.LOCKED, progress.statusOf(3))
        assertEquals(2, progress.currentFloor)
    }

    @Test
    fun `qua lien tiep nhieu tang`() {
        var progress = TowerProgress()
        for (floor in 1..10) progress = progress.withCleared(floor)
        assertEquals(10, progress.clearedUpTo)
        assertEquals(11, progress.currentFloor)
    }

    @Test
    fun `khong nhay coc qua tang con khoa`() {
        val progress = TowerProgress().withCleared(5)
        assertEquals(0, progress.clearedUpTo)
    }

    @Test
    fun `choi lai tang cu khong lam tien do lui`() {
        val progress = TowerProgress(clearedUpTo = 8).withCleared(3)
        assertEquals(8, progress.clearedUpTo)
    }

    @Test
    fun `bao qua cung tang hien tai hai lan khong tien them`() {
        val once = TowerProgress().withCleared(1)
        assertEquals(once, once.withCleared(1))
    }

    @Test
    fun `qua tang cuoi thi ca thap duoc chinh phuc`() {
        val progress = TowerProgress(clearedUpTo = 48).withCleared(49)
        assertTrue(progress.isTowerCleared)
        assertEquals(49, progress.currentFloor)
        assertEquals(FloorStatus.CLEARED, progress.statusOf(49))
    }

    @Test
    fun `khong the qua tang vuot qua 49`() {
        val done = TowerProgress(clearedUpTo = 49)
        assertEquals(done, done.withCleared(50))
        assertFalse(TowerProgress(clearedUpTo = 48).isTowerCleared)
    }

    @Test
    fun `tien do ngoai khoang thi bao loi`() {
        val tooHigh = runCatching { TowerProgress(clearedUpTo = 50) }.exceptionOrNull()
        val negative = runCatching { TowerProgress(clearedUpTo = -1) }.exceptionOrNull()
        assertTrue("$tooHigh", tooHigh is IllegalArgumentException)
        assertTrue("$negative", negative is IllegalArgumentException)
    }
}
