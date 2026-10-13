package kma.game.chess2d.ui.screen

import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashAssetAlignmentTest {
    @Test
    fun fillFitsFrameOpeningOnSharedCanvas() {
        val dir = File("src/main/res/drawable")
        val frame = ImageIO.read(File(dir, "splash_loading_frame.png"))
        val fill = ImageIO.read(File(dir, "splash_loading_fill.png"))
        assertEquals(1536, frame.width)
        assertEquals(1024, frame.height)
        assertEquals(frame.width, fill.width)
        assertEquals(frame.height, fill.height)

        var left = fill.width
        var right = -1
        var top = fill.height
        var bottom = -1
        for (y in 0 until fill.height) for (x in 0 until fill.width) {
            if ((fill.getRGB(x, y) ushr 24) > 128) {
                left = minOf(left, x)
                right = maxOf(right, x)
                top = minOf(top, y)
                bottom = maxOf(bottom, y)
            }
        }
        assertTrue("Fill must be visible", right >= left)
        assertTrue("Fill must stay inside rounded ends", left >= 166 && right <= 1370)
        assertTrue("Fill must stay between horizontal borders", top >= 458 && bottom <= 555)
        assertEquals(768f, (left + right + 1) / 2f, 1f)
        assertEquals(507f, (top + bottom + 1) / 2f, 1f)
        assertTrue("Progress clipping must begin before visible fill", SplashLayout.FILL_START * fill.width <= left)
        assertTrue("Full progress must reveal all fill", SplashLayout.FILL_END * fill.width >= right + 1)

        // Away from the central ornament, the fill must occupy only the hollow interior.
        for (x in listOf(400, 1100)) for (y in top..bottom) {
            if ((fill.getRGB(x, y) ushr 24) > 128) {
                assertTrue("Fill overlaps frame at $x,$y", (frame.getRGB(x, y) ushr 24) <= 128)
            }
        }
    }
}
