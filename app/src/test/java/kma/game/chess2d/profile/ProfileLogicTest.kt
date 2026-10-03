package kma.game.chess2d.profile

import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.history.MatchResult
import kma.game.chess2d.opponent.AiCharacters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Luật cộng thống kê Hồ sơ và suy ra trạng thái Nhật ký (thuần, không cần Room). */
class ProfileLogicTest {

    @Test
    fun `thong ke ban dau toan so khong va cap 1`() {
        val s = PlayerStats()
        assertEquals(0, s.totalGames)
        assertEquals(0, s.highestFloor)
        assertEquals(1, s.level)
    }

    @Test
    fun `thang bang chieu het cong ca thang va checkmate`() {
        val s = PlayerStats().withMatch(MatchResult.WIN, byCheckmate = true, seconds = 90)
        assertEquals(1, s.wins)
        assertEquals(1, s.checkmates)
        assertEquals(90L, s.playSeconds)
        assertEquals(1, s.totalGames)
    }

    @Test
    fun `thang khong phai chieu het thi khong tinh checkmate`() {
        // Vd. đối thủ bỏ cuộc hoặc hết giờ.
        val s = PlayerStats().withMatch(MatchResult.WIN, byCheckmate = false, seconds = 10)
        assertEquals(1, s.wins)
        assertEquals(0, s.checkmates)
    }

    @Test
    fun `thua va hoa chi cong dung o cua minh`() {
        val s = PlayerStats()
            .withMatch(MatchResult.LOSS, byCheckmate = false, seconds = 30)
            .withMatch(MatchResult.DRAW, byCheckmate = false, seconds = 20)
        assertEquals(1, s.losses)
        assertEquals(1, s.draws)
        assertEquals(0, s.wins)
        assertEquals(50L, s.playSeconds)
        assertEquals(2, s.totalGames)
    }

    @Test
    fun `thua khong bao gio tinh checkmate du co co`() {
        val s = PlayerStats().withMatch(MatchResult.LOSS, byCheckmate = true, seconds = 5)
        assertEquals(0, s.checkmates)
    }

    @Test
    fun `thoi gian am bi cat ve khong`() {
        val s = PlayerStats().withMatch(MatchResult.WIN, byCheckmate = false, seconds = -50)
        assertEquals(0L, s.playSeconds)
    }

    @Test
    fun `ket qua la bi bo qua`() {
        val s = PlayerStats()
        assertSame(s, s.withMatch("???", byCheckmate = true, seconds = 10))
    }

    @Test
    fun `tang cao nhat chi tang khong bao gio tut`() {
        val s = PlayerStats().withFloorCleared(7).withFloorCleared(3)
        assertEquals(7, s.highestFloor)
        assertEquals(12, s.withFloorCleared(12).highestFloor)
    }

    @Test
    fun `cap len theo thang cong tang da vuot`() {
        // 5 điểm tiến triển = lên 1 cấp; thắng và tầng cùng được tính.
        val s = PlayerStats(wins = 2, highestFloor = 3)
        assertEquals(2, s.level)
        assertEquals(1, PlayerStats(wins = 2, highestFloor = 2).level)
        // Thua nhiều không làm tụt cấp.
        assertEquals(1, PlayerStats(losses = 100).level)
    }

    @Test
    fun `gap lai nhan vat giu lan dau va tang so lan`() {
        val first = JournalEntry("dark_king", firstMetAtMillis = 1000L)
        val again = first.metAgain()
        assertEquals(1000L, again.firstMetAtMillis)
        assertEquals(2, again.timesMet)
    }

    @Test
    fun `nhan vat da gap la MET du chua mo theo thap`() {
        val hidden = AiCharacters.all.last()
        assertEquals(JournalState.MET, Journal.stateOf(hidden, setOf(hidden.id), clearedUpTo = 0))
    }

    @Test
    fun `nhan vat mo san ma chua gap la UNKNOWN`() {
        val mage = AiCharacters.all.first()
        assertEquals(JournalState.UNKNOWN, Journal.stateOf(mage, emptySet(), clearedUpTo = 0))
    }

    @Test
    fun `nhan vat an bi khoa cho den khi het thap`() {
        val hidden = AiCharacters.all.last()
        val last = TowerCatalog.FLOOR_COUNT
        assertEquals(JournalState.LOCKED, Journal.stateOf(hidden, emptySet(), clearedUpTo = last - 1))
        assertEquals(JournalState.UNKNOWN, Journal.stateOf(hidden, emptySet(), clearedUpTo = last))
    }

    @Test
    fun `nhat ky va man chon doi thu dung chung dieu kien mo`() {
        for (character in AiCharacters.all) {
            for (cleared in listOf(0, 10, TowerCatalog.FLOOR_COUNT)) {
                val locked = Journal.stateOf(character, emptySet(), cleared) == JournalState.LOCKED
                assertTrue(character.id, locked == !character.isUnlocked(cleared))
            }
        }
    }
}
