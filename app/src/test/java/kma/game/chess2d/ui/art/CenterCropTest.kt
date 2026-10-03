package kma.game.chess2d.ui.art

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phép cắt giữa ảnh nền: phủ kín vùng đích, không méo, cắt đều hai bên. */
class CenterCropTest {

    @Test
    fun `vung dich cung ti le thi lay nguyen anh`() {
        val crop = centerCrop(941, 1672, Size(941f, 1672f))
        assertEquals(IntOffset.Zero, crop.srcOffset)
        assertEquals(IntSize(941, 1672), crop.srcSize)
    }

    @Test
    fun `man hinh rong hon anh thi cat bot chieu cao deu tren duoi`() {
        // Ảnh dọc 1000x2000 vào màn vuông 500x500: lấy dải giữa 1000x1000.
        val crop = centerCrop(1000, 2000, Size(500f, 500f))
        assertEquals(IntSize(1000, 1000), crop.srcSize)
        assertEquals(IntOffset(0, 500), crop.srcOffset)
    }

    @Test
    fun `man hinh hep hon anh thi cat bot chieu rong deu hai ben`() {
        // Ảnh ngang 2000x1000 vào màn dọc 500x1000: lấy dải giữa 500x1000.
        val crop = centerCrop(2000, 1000, Size(500f, 1000f))
        assertEquals(IntSize(500, 1000), crop.srcSize)
        assertEquals(IntOffset(750, 0), crop.srcOffset)
    }

    @Test
    fun `vung cat luon nam trong anh va giu ti le cua vung dich`() {
        val target = Size(1080f, 2400f)
        val crop = centerCrop(941, 1672, target)
        assertTrue(crop.srcOffset.x >= 0 && crop.srcOffset.y >= 0)
        assertTrue(crop.srcOffset.x + crop.srcSize.width <= 941)
        assertTrue(crop.srcOffset.y + crop.srcSize.height <= 1672)
        val srcRatio = crop.srcSize.width.toFloat() / crop.srcSize.height
        assertEquals(target.width / target.height, srcRatio, 0.01f)
    }

    @Test
    fun `vung dich rong thi tra ve nguyen anh thay vi chia cho khong`() {
        val crop = centerCrop(941, 1672, Size.Zero)
        assertEquals(IntSize(941, 1672), crop.srcSize)
    }
}
