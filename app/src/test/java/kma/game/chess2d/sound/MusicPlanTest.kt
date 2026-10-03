package kma.game.chess2d.sound

import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.TowerStage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MusicPlanTest {

    @Test
    fun `ban do va thoai dung nhac menu`() {
        for (stage in listOf(TowerStage.MAP, TowerStage.INTRO, TowerStage.EPILOGUE)) {
            assertEquals(MusicTrack.MENU, MusicPlan.forTowerStage(stage, NodeKind.BATTLE))
        }
    }

    @Test
    fun `van dau thuong dung nhac chien dau`() {
        assertEquals(MusicTrack.BATTLE, MusicPlan.forTowerStage(TowerStage.BATTLE, NodeKind.BATTLE))
    }

    @Test
    fun `tang trum dung nhac rieng`() {
        assertEquals(MusicTrack.BOSS, MusicPlan.forTowerStage(TowerStage.BATTLE, NodeKind.BOSS))
    }

    @Test
    fun `thang thua phat doan bao ket qua`() {
        assertEquals(MusicTrack.VICTORY, MusicPlan.forTowerStage(TowerStage.VICTORY, NodeKind.BOSS))
        assertEquals(MusicTrack.DEFEAT, MusicPlan.forTowerStage(TowerStage.DEFEAT, NodeKind.BATTLE))
    }

    @Test
    fun `doan bao ket qua khong lap va noi tiep nhac menu`() {
        for (track in listOf(MusicTrack.VICTORY, MusicTrack.DEFEAT)) {
            assertFalse(track.loops)
            assertEquals(MusicTrack.MENU, track.next)
        }
    }

    @Test
    fun `nhac nen lap mai va khong co ban noi tiep`() {
        for (track in listOf(MusicTrack.MENU, MusicTrack.BATTLE, MusicTrack.BOSS)) {
            assertTrue(track.loops)
            assertNull(track.next)
        }
    }

    @Test
    fun `ten tep khop voi tep trong res raw`() {
        assertEquals(
            listOf("music_menu", "music_battle", "music_boss", "victory", "defeat"),
            MusicTrack.entries.map { it.resourceName },
        )
    }
}
