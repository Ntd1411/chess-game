package kma.game.chess2d.spectate

import kma.game.chess2d.ai.Difficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hai AI đấu nhau bằng đúng lớp mà màn Máy vs Máy dùng.
 *
 * Chỉ dùng cấp Dễ để test chạy nhanh; luật đi và bản đồ id quân không phụ thuộc cấp độ.
 */
class SpectateMatchTest {

    private fun easyMatch(seed: Int, maxPlies: Int = 60) =
        SpectateMatch(Difficulty.EASY, Difficulty.EASY, seed = seed, maxPlies = maxPlies)

    @Test
    fun `van moi bat dau o the khoi dau voi 32 quan`() {
        val match = easyMatch(seed = 1)
        val snapshot = match.snapshot()
        assertEquals(0, match.ply)
        assertFalse(match.isOver)
        assertEquals(32, snapshot.pieces.size)
        assertTrue(snapshot.whiteToMove)
    }

    @Test
    fun `hai AI di den khi het van va khong vuot tran so nuoc`() {
        for (seed in 1..5) {
            val match = easyMatch(seed)
            var guard = 0
            while (match.step() != null) {
                guard++
                assertTrue("van khong dung sau ${match.maxPlies} nuoc", guard <= match.maxPlies)
            }
            assertTrue(match.isOver)
            assertTrue(match.ply <= match.maxPlies)
            // Mỗi nước đã đi đều được ghi SAN.
            assertEquals(match.ply, match.snapshot().sanMoves.size)
        }
    }

    @Test
    fun `cung hat giong cho ra cung mot van`() {
        fun play(seed: Int): List<String> {
            val match = easyMatch(seed, maxPlies = 40)
            while (match.step() != null) Unit
            return match.playedMoves.map { it.toUci() }
        }
        assertEquals(play(seed = 7), play(seed = 7))
    }

    @Test
    fun `cham tran so nuoc thi van dung va coi la hoa`() {
        val match = easyMatch(seed = 3, maxPlies = 6)
        repeat(6) { assertNotNull(match.step()) }
        // Sáu nửa nước đầu không thể kết thúc theo luật cờ, nên chỉ có thể dừng vì chạm trần.
        assertFalse(match.endedByRules)
        assertTrue(match.hitMoveLimit)
        assertTrue(match.isOver)
        assertNull(match.step())
        assertEquals(6, match.ply)
    }

    @Test
    fun `luot nghi bi huy thi nuoc khong duoc ap dung`() {
        val match = easyMatch(seed = 2)
        assertNull(match.step(isActive = { false }))
        assertEquals(0, match.ply)
        assertEquals(32, match.snapshot().pieces.size)
    }

    @Test
    fun `id quan duy nhat va khop so quan tren ban sau tung nuoc`() {
        for (seed in 1..5) {
            val match = easyMatch(seed, maxPlies = 120)
            val startIds = match.snapshot().pieces.map { it.id }.toSet()
            while (match.step() != null) {
                val pieces = match.snapshot().pieces
                val ids = pieces.map { it.id }
                assertEquals("id bi trung o nuoc ${match.ply}", ids.size, ids.toSet().size)
                assertTrue("xuat hien id la o nuoc ${match.ply}", startIds.containsAll(ids))
                assertEquals(pieces.size, pieces.map { it.square }.toSet().size)
            }
        }
    }

    @Test
    fun `quan di khong bi bat giu nguyen id de animate duoc`() {
        val match = easyMatch(seed = 4)
        val before = match.snapshot().pieces.associateBy { it.square }
        val move = match.step()!!
        val after = match.snapshot().pieces
        // Nước đầu của Trắng không thể là nước ăn quân: quân vừa đi phải mang id cũ.
        val moved = after.single { it.square == move.to }
        assertEquals(before.getValue(move.from).id, moved.id)
    }
}
