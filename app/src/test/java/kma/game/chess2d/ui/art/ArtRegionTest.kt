package kma.game.chess2d.ui.art

import androidx.compose.ui.unit.IntRect
import org.junit.Assert.assertEquals
import org.junit.Test

class ArtRegionTest {
    @Test
    fun sourceCropMapsIntoSampledBitmapWithoutLayoutOffsets() {
        val region = ArtRegion(12, 301, 1500, 368, 1536, 1024)
        assertEquals(IntRect(12, 301, 1512, 669), region.sampledBounds(1536, 1024))
        assertEquals(IntRect(6, 151, 756, 335), region.sampledBounds(768, 512))
        assertEquals(IntRect(3, 75, 378, 167), region.sampledBounds(384, 256))
    }

    @Test
    fun smallestCropAlwaysKeepsOnePixelInsideBitmap() {
        val region = ArtRegion(1535, 1023, 1, 1, 1536, 1024)
        assertEquals(IntRect(0, 0, 1, 1), region.sampledBounds(1, 1))
        assertEquals(IntRect(3, 3, 4, 4), region.sampledBounds(4, 4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsCropOutsideSourceCanvas() {
        ArtRegion(12, 301, 1536, 368, 1536, 1024)
    }
}
