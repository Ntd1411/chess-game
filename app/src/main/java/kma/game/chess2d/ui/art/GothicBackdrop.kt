package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kma.game.chess2d.ui.theme.GothicColors
import kotlin.math.roundToInt

/**
 * Nền gothic cho cả màn hình, dưới dạng một Modifier để gắn thẳng vào Column/Box gốc của màn
 * mà không phải bọc thêm một tầng bố cục.
 *
 * Thứ tự là cố ý: nền vẽ trên toàn bộ vùng `fillMaxSize` (tràn mép, edge-to-edge) rồi mới đến
 * padding thanh trạng thái/thanh điều hướng, nên nội dung đặt sau Modifier này luôn nằm trong
 * vùng an toàn còn nền thì không lộ dải trống. Đặt nó **trước** `verticalScroll` để ảnh nền đứng
 * yên khi nội dung cuộn.
 *
 * @param res ảnh nền của màn (`screen_*`), nạp qua [ArtCache] cùng cỡ với [ArtBackdrop].
 * @param dim độ phủ tối (0..1) để thẻ và chữ luôn đọc được trên vùng sáng của ảnh.
 */
@Composable
fun Modifier.gothicBackdrop(@DrawableRes res: Int, dim: Float = 0.55f): Modifier {
    val image = rememberArt(res, ArtSizes.SCREEN)
    return this
        .fillMaxSize()
        .drawBehind {
            drawRect(GothicColors.Ink)
            if (image != null) {
                val crop = centerCrop(image.width, image.height, size)
                drawImage(
                    image = image,
                    srcOffset = crop.srcOffset,
                    srcSize = crop.srcSize,
                    dstSize = IntSize(size.width.roundToInt(), size.height.roundToInt()),
                    filterQuality = FilterQuality.Medium,
                )
            }
            if (dim > 0f) drawRect(Color.Black.copy(alpha = dim))
        }
        .statusBarsPadding()
        .navigationBarsPadding()
}

/** Phần ảnh nguồn được lấy để phủ kín vùng đích mà không méo (cắt đều hai bên). */
internal data class CropRect(val srcOffset: IntOffset, val srcSize: IntSize)

/** Tính vùng cắt giữa của ảnh [imageWidth]x[imageHeight] để phủ kín [target]. Hàm thuần để test được. */
internal fun centerCrop(imageWidth: Int, imageHeight: Int, target: Size): CropRect {
    if (target.width <= 0f || target.height <= 0f) {
        return CropRect(IntOffset.Zero, IntSize(imageWidth, imageHeight))
    }
    val scale = maxOf(target.width / imageWidth, target.height / imageHeight)
    val srcWidth = (target.width / scale).roundToInt().coerceIn(1, imageWidth)
    val srcHeight = (target.height / scale).roundToInt().coerceIn(1, imageHeight)
    return CropRect(
        srcOffset = IntOffset((imageWidth - srcWidth) / 2, (imageHeight - srcHeight) / 2),
        srcSize = IntSize(srcWidth, srcHeight),
    )
}
