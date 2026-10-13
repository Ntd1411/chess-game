package kma.game.chess2d.campaign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TowerRewardsTest {

    private val floors = TowerCatalog.build(puzzleCount = 42)

    @Test
    fun `chua qua tang nao thi vi rong`() {
        assertEquals(Reward(), TowerRewards.total(floors, 0))
    }

    @Test
    fun `tong la cong don thuong tung tang da qua`() {
        val expected = floors.take(10).map(TowerRewards::forFloor).reduce(Reward::plus)
        assertEquals(expected, TowerRewards.total(floors, 10))
    }

    @Test
    fun `vi khong giam khi qua them tang`() {
        var previous = Reward()
        for (n in 1..TowerCatalog.FLOOR_COUNT) {
            val now = TowerRewards.total(floors, n)
            assertTrue(now.gold > previous.gold)
            assertTrue(now.crystals >= previous.crystals && now.books >= previous.books)
            previous = now
        }
    }

    @Test
    fun `chi kho bau bi mat va trum cho pha le`() {
        floors.forEach { floor ->
            val gives = TowerRewards.forFloor(floor).crystals > 0
            val expected = floor.kind == NodeKind.TREASURE || floor.kind == NodeKind.SECRET || floor.kind == NodeKind.BOSS
            assertEquals("tang ${floor.number}", expected, gives)
        }
    }

    @Test
    fun `di vat mo theo tang va tang mo khong trung nhau`() {
        assertFalse(Relic.ANCIENT_KEY.isUnlocked(Relic.ANCIENT_KEY.unlockFloor - 1))
        assertTrue(Relic.ANCIENT_KEY.isUnlocked(Relic.ANCIENT_KEY.unlockFloor))
        assertEquals(Relic.entries.size, Relic.entries.map { it.unlockFloor }.toSet().size)
        assertTrue(Relic.entries.all { it.unlockFloor in 1..TowerCatalog.FLOOR_COUNT })
        // 3 o tien te + di vat vua dung luoi 4x4.
        assertEquals(16, 3 + Relic.entries.size)
    }
}
