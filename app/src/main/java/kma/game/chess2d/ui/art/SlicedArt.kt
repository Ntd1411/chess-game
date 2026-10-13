package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Vẽ một khung/panel/nút PNG vào hộp có tỉ lệ bất kỳ mà **không làm méo ornament ở góc** (nine-slice).
 *
 * Bộ asset gothic vẽ panel theo khung 3:4 và nút theo khung 3:1, trong khi thiết kế cần thẻ 5:1,
 * khung đối thủ 6.5:1 hay nút 7:1. Kéo giãn cả ảnh sẽ bóp méo hoa văn hai đầu; ngược lại đặt Fit
 * thì ảnh chỉ chiếm một mẩu giữa hộp. Ở đây ảnh (đã bỏ lề trong suốt) được chia 3x3: bốn góc giữ đúng
 * tỉ lệ, bốn cạnh chỉ giãn theo một chiều, ô giữa giãn cả hai chiều.
 *
 * @param slice độ rộng vùng góc trên ảnh nguồn, tính theo tỉ lệ cạnh ngắn của vùng có nội dung
 *        (0 < slice <= 0.5). Nút dùng 0.5 để hai đầu nút là hai "góc" cao bằng cả nút.
 * @param corner cạnh vùng góc trên màn hình; `null` thì lấy `slice * cạnh ngắn của hộp` nên khung
 *        co giãn cùng hộp. Luôn bị chặn để hai góc không chồng lên nhau.
 */
@Composable
fun SlicedArt(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    slice: Float = 0.25f,
    corner: Dp? = null,
    maxEdge: Int = ArtSizes.BUTTON,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
) {
    val art = rememberTrimmedArt(res, maxEdge)
    Box(
        modifier = modifier.drawBehind {
            if (art != null) {
                val cornerPx = corner?.toPx() ?: (min(size.width, size.height) * slice)
                drawNineSlice(art, slice, cornerPx, alpha, colorFilter)
            }
        },
    )
}

/** Các mốc chia 3x3 (4 mốc mỗi chiều) trên ảnh nguồn và trên màn hình. Hàm thuần để test được. */
internal data class SliceGrid(val srcX: IntArray, val srcY: IntArray, val dstX: IntArray, val dstY: IntArray)

internal fun sliceGrid(src: IntRect, slice: Float, dstWidth: Float, dstHeight: Float, cornerPx: Float): SliceGrid {
    val inset = (min(src.width, src.height) * slice.coerceIn(0.01f, 0.5f)).roundToInt().coerceAtLeast(1)
    val c = cornerPx.coerceAtMost(min(dstWidth, dstHeight) / 2f).coerceAtLeast(0f)
    val w = dstWidth.roundToInt()
    val h = dstHeight.roundToInt()
    val ci = c.roundToInt()
    return SliceGrid(
        srcX = intArrayOf(src.left, src.left + inset, src.right - inset, src.right),
        srcY = intArrayOf(src.top, src.top + inset, src.bottom - inset, src.bottom),
        dstX = intArrayOf(0, ci, w - ci, w),
        dstY = intArrayOf(0, ci, h - ci, h),
    )
}

private fun DrawScope.drawNineSlice(
    art: TrimmedArt,
    slice: Float,
    cornerPx: Float,
    alpha: Float,
    colorFilter: ColorFilter?,
) {
    val grid = sliceGrid(art.bounds, slice, size.width, size.height, cornerPx)
    for (row in 0 until 3) {
        for (col in 0 until 3) {
            val sw = grid.srcX[col + 1] - grid.srcX[col]
            val sh = grid.srcY[row + 1] - grid.srcY[row]
            val dw = grid.dstX[col + 1] - grid.dstX[col]
            val dh = grid.dstY[row + 1] - grid.dstY[row]
            if (sw <= 0 || sh <= 0 || dw <= 0 || dh <= 0) continue
            drawImage(
                image = art.image,
                srcOffset = IntOffset(grid.srcX[col], grid.srcY[row]),
                srcSize = IntSize(sw, sh),
                dstOffset = IntOffset(grid.dstX[col], grid.dstY[row]),
                dstSize = IntSize(dw, dh),
                alpha = alpha,
                colorFilter = colorFilter,
                filterQuality = FilterQuality.Medium,
            )
        }
    }
}

@Preview(widthDp = 400, heightDp = 300, showBackground = true)
@Composable
private fun SlicedArtButtonPreview() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Button-style: slice=0.5 (hai đầu là "góc" cao bằng nút)
        SlicedArt(
            res = R.drawable.toggle_on,
            slice = 0.5f,
            modifier = Modifier.width(200.dp).height(60.dp),
        )
        
        SlicedArt(
            res = R.drawable.toggle_on,
            slice = 0.5f,
            modifier = Modifier.width(300.dp).height(80.dp),  // Lớn hơn
        )
        
        SlicedArt(
            res = R.drawable.toggle_on,
            slice = 0.5f,
            corner = 25.dp,  // Fixed corner size
            modifier = Modifier.width(250.dp).height(50.dp),
        )
    }
}

@Preview(widthDp = 400, heightDp = 350, showBackground = true)
@Composable
private fun SlicedArtPanelPreview() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Panel-style: slice=0.25 (góc nhỏ hơn)
        SlicedArt(
            res = R.drawable.ui_panel_main,
            slice = 0.25f,
            modifier = Modifier.width(250.dp).height(150.dp),
        )
        
        SlicedArt(
            res = R.drawable.ui_panel_main,
            slice = 0.25f,
            modifier = Modifier.width(300.dp).height(200.dp),  // Lớn hơn
        )
        
        SlicedArt(
            res = R.drawable.ui_panel_main,
            slice = 0.25f,
            corner = 30.dp,  // Fixed corner
            modifier = Modifier.width(280.dp).height(120.dp),
        )
    }
}
