package kma.game.chess2d.profile

import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.FloorGoal
import kma.game.chess2d.campaign.FloorResult
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.history.MatchResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Luật ghi ván vào Hồ sơ (thuần, không cần Room). */
class MatchRecordingTest {

    @Test
    fun `chi che do dau may duoc tinh vao ho so`() {
        assertTrue(MatchRecording.countsForProfile(GameMode.VS_COMPUTER))
        assertFalse(MatchRecording.countsForProfile(GameMode.TWO_PLAYERS))
    }

    @Test
    fun `chua biet luc bat dau thi thoi gian la khong`() {
        assertEquals(0L, MatchRecording.elapsedSeconds(null, 5_000_000L))
    }

    @Test
    fun `thoi gian tinh bang giay va lam tron xuong`() {
        assertEquals(90L, MatchRecording.elapsedSeconds(1_000L, 91_999L))
    }

    @Test
    fun `dong ho lui khong ra so am`() {
        assertEquals(0L, MatchRecording.elapsedSeconds(10_000L, 1_000L))
    }

    @Test
    fun `van bo quen qua dem bi cat tran`() {
        val twelveHours = 12 * 60 * 60 * 1000L
        assertEquals(MatchRecording.MAX_SECONDS_PER_MATCH, MatchRecording.elapsedSeconds(0L, twelveHours))
    }

    @Test
    fun `van chien dich chua xong thi khong co ket qua`() {
        val goal = FloorGoal.DefeatAi(Difficulty.EASY)
        assertNull(MatchRecording.campaignOutcome(goal, FloorResult.ONGOING, null))
    }

    @Test
    fun `thang tang danh may la thang bang chieu het`() {
        val out = MatchRecording.campaignOutcome(FloorGoal.DefeatAi(Difficulty.HARD), FloorResult.WON, null)
        assertEquals(MatchRecording.Outcome(MatchResult.WIN, byCheckmate = true), out)
    }

    @Test
    fun `thang cau do hoac song sot khong tinh checkmate`() {
        val puzzle = MatchRecording.campaignOutcome(FloorGoal.SolvePuzzle(0), FloorResult.WON, null)
        val survive = MatchRecording.campaignOutcome(
            FloorGoal.SurviveMoves(10, Difficulty.EASY),
            FloorResult.WON,
            null,
        )
        assertEquals(MatchResult.WIN, puzzle?.result)
        assertFalse(puzzle!!.byCheckmate)
        assertEquals(MatchResult.WIN, survive?.result)
        assertFalse(survive!!.byCheckmate)
    }

    @Test
    fun `bi chieu het la thua con hoa khi can thang la hoa`() {
        val goal = FloorGoal.DefeatAi(Difficulty.EASY)
        val mated = MatchRecording.campaignOutcome(goal, FloorResult.LOST, LossReason.CHECKMATE)
        val drawn = MatchRecording.campaignOutcome(goal, FloorResult.LOST, LossReason.DRAW)
        assertEquals(MatchResult.LOSS, mated?.result)
        assertEquals(MatchResult.DRAW, drawn?.result)
        assertFalse(mated!!.byCheckmate)
    }

    @Test
    fun `ket qua khong ro ly do thua mac dinh la thua`() {
        val out = MatchRecording.campaignOutcome(FloorGoal.DefeatAi(Difficulty.EASY), FloorResult.LOST, null)
        assertEquals(MatchResult.LOSS, out?.result)
    }
}
