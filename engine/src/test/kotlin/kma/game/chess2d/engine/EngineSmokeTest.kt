package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test rong cua Phase 0: chi de chung minh JUnit tren JVM chay duoc
 * ma khong can emulator. Phase 1 se them PerftTest, FenTest, luat dac biet.
 */
class EngineSmokeTest {
    @Test
    fun boardHas64Squares() {
        assertEquals(64, Engine.BOARD_SIZE)
    }

    @Test
    fun startFenIsPresent() {
        assertEquals("w", Engine.START_FEN.split(" ")[1])
    }
}
