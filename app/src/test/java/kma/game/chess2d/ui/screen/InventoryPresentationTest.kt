package kma.game.chess2d.ui.screen

import kma.game.chess2d.campaign.Relic
import kma.game.chess2d.campaign.Reward
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InventoryPresentationTest {
    @Test
    fun walletCountsAndAllRelicsRemainInStableOrder() {
        val entries = inventoryEntries(Reward(gold = 120, crystals = 9, books = 3), 0)
        assertEquals(3 + Relic.entries.size, entries.size)
        assertEquals(listOf(120, 9, 3), entries.take(3).map { it.count })
        assertTrue(entries.take(3).all { it.unlocked })
        assertEquals(Relic.entries.map { it.unlockFloor }, entries.drop(3).map { it.unlockFloor })
        assertFalse(entries.drop(3).any { it.unlocked })
    }

    @Test
    fun eachRelicStillUnlocksAtItsOriginalFloor() {
        Relic.entries.forEachIndexed { index, relic ->
            assertFalse(inventoryEntries(Reward(), relic.unlockFloor - 1)[index + 3].unlocked)
            assertTrue(inventoryEntries(Reward(), relic.unlockFloor)[index + 3].unlocked)
            assertEquals(relicArt(relic).icon, inventoryEntries(Reward(), 49)[index + 3].icon)
        }
    }
}
