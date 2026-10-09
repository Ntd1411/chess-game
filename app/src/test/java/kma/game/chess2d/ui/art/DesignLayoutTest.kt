package kma.game.chess2d.ui.art

import androidx.compose.ui.unit.IntRect
import kma.game.chess2d.R
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DesignLayoutTest {

    @Test
    fun `khung 1080x1920 vua khit man hinh dung ti le`() {
        assertEquals(1f, Design.unitPx(1080f, 1920f), 1e-6f)
        assertEquals(2f, Design.unitPx(2160f, 3840f), 1e-6f)
    }

    @Test
    fun `man dai hon 16x9 bi gioi han boi chieu ngang`() {
        // 1080x2400: chiều ngang quyết định, khung không tràn ra ngoài.
        assertEquals(1f, Design.unitPx(1080f, 2400f), 1e-6f)
        // Máy tính bảng ngang: chiều dọc quyết định.
        assertEquals(0.5f, Design.unitPx(2560f, 960f), 1e-6f)
    }

    @Test
    fun `nine slice giu goc vuong va chan goc khong chong nhau`() {
        val grid = sliceGrid(IntRect(10, 20, 310, 120), slice = 0.5f, dstWidth = 700f, dstHeight = 100f, cornerPx = 50f)
        assertArrayEquals(intArrayOf(10, 60, 260, 310), grid.srcX)
        assertArrayEquals(intArrayOf(20, 70, 70, 120), grid.srcY)
        assertArrayEquals(intArrayOf(0, 50, 650, 700), grid.dstX)
        assertArrayEquals(intArrayOf(0, 50, 50, 100), grid.dstY)

        val clamped = sliceGrid(IntRect(0, 0, 100, 100), slice = 0.25f, dstWidth = 60f, dstHeight = 40f, cornerPx = 500f)
        assertArrayEquals(intArrayOf(0, 20, 40, 60), clamped.dstX)
        assertArrayEquals(intArrayOf(0, 20, 20, 40), clamped.dstY)
    }

    @Test
    fun `nut chi chon mot trang thai, tat uu tien hon nhan`() {
        assertEquals(R.drawable.button_primary_normal, ButtonArt.PRIMARY.resFor(isEnabled = true, isPressed = false))
        assertEquals(R.drawable.button_primary_pressed, ButtonArt.PRIMARY.resFor(isEnabled = true, isPressed = true))
        assertEquals(R.drawable.button_small_disabled, ButtonArt.SMALL.resFor(isEnabled = false, isPressed = true))
    }
}
