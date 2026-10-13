package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.campaign.TowerCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class FloorResultPresentationTest {
    @Test fun defeatRetainsBothLossReasons() {
        assertEquals(R.string.defeat_reason_checkmate, defeatReasonRes(kma.game.chess2d.campaign.LossReason.CHECKMATE))
        assertEquals(R.string.defeat_reason_draw, defeatReasonRes(kma.game.chess2d.campaign.LossReason.DRAW))
    }
    @Test fun victoryPreservesFloorAndFinalTowerMessages() {
        val floors = TowerCatalog.build(200)
        assertEquals(R.string.victory_sub_floor, victorySubtitleRes(floors.first()))
        assertEquals(R.string.victory_tower_cleared, victorySubtitleRes(floors.last()))
    }
}
