package kma.game.chess2d.ui.screen

import kma.game.chess2d.campaign.*
import kma.game.chess2d.opponent.AiCharacters
import org.junit.Assert.*
import org.junit.Test

class UnlockProgressTest {
    @Test fun fullAccessOpensFloorsWithoutFabricatingWinsOrCompletion() {
        val progress = TowerProgress(clearedUpTo = 2, allUnlocked = true)
        for (floor in 1..49) assertNotEquals(FloorStatus.LOCKED, progress.statusOf(floor))
        assertEquals(FloorStatus.CLEARED, progress.statusOf(2))
        assertEquals(FloorStatus.CURRENT, progress.statusOf(49))
        assertEquals(2, progress.clearedUpTo)
        assertEquals(3, progress.currentFloor)
        assertFalse(progress.isTowerCleared)
        assertEquals(FloorStatus.LOCKED, progress.statusOf(50))
    }
    @Test fun accessDoesNotGrantRewardsOrSkipTheRealProgressCounter() {
        val floors = TowerCatalog.build(200)
        val normal = TowerProgress(2)
        val unlocked = normal.copy(allUnlocked = true)
        assertEquals(TowerRewards.total(floors, normal.clearedUpTo), TowerRewards.total(floors, unlocked.clearedUpTo))
        assertEquals(3, unlocked.withCleared(3).clearedUpTo)
        assertTrue(unlocked.withCleared(3).allUnlocked)
        assertEquals(2, unlocked.withCleared(49).clearedUpTo)
        assertEquals(FloorStatus.LOCKED, normal.statusOf(49))
    }
    @Test fun effectiveAccessOpensAllAiAndRelics() {
        val progress = TowerProgress(allUnlocked = true)
        assertEquals(49, progress.unlockLevel)
        assertTrue(AiCharacters.all.all { it.isUnlocked(progress.unlockLevel) })
        assertTrue(Relic.entries.all { it.isUnlocked(progress.unlockLevel) })
    }
}
