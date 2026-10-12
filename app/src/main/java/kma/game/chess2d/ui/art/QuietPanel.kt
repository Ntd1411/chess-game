package kma.game.chess2d.ui.art

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Mức nổi của một panel trong hệ giao diện tối giản.
 *
 * - [BASE]: vùng chứa thông tin thường (thống kê, danh sách, cài đặt).
 * - [SUNKEN]: ô con nằm trong panel khác (ô vật phẩm, tab chưa chọn): viền mờ hơn.
 * - [SELECTED]: mục đang chọn / đang nhấn: nền ánh đỏ thẫm, viền vàng sáng. Chỉ trạng thái này
 *   được dùng vàng sáng, nên mắt luôn thấy ngay mục đang chọn.
 */
enum class PanelTone { BASE, SUNKEN, SELECTED }

/** Thông số vẽ của từng mức nổi. Hàm thuần để test được. */
internal data class PanelStyle(
    val fillAlpha: Float,
    val border: Color,
    val borderAlpha: Float,
    val tint: Color?,
    val corners: Boolean,
)

internal fun panelStyle(tone: PanelTone): PanelStyle = when (tone) {
    PanelTone.BASE -> PanelStyle(0.88f, GothicColors.GoldDim, 0.75f, null, corners = true)
    PanelTone.SUNKEN -> PanelStyle(0.7f, GothicColors.GoldDim, 0.4f, null, corners = false)
    PanelTone.SELECTED -> PanelStyle(0.92f, GothicColors.Gold, 0.9f, GothicColors.Crimson, corners = true)
}

/**
 * Panel tối giản vẽ bằng code thay cho khung PNG cầu kỳ: nền tím đen → than đen gần đục (che bớt
 * background ngay tại vùng nội dung), viền vàng tối 2 đơn vị, bốn góc có nét góc vuông nhỏ làm điểm
 * trang trí. Viền mảnh nên gần như toàn bộ diện tích dành cho nội dung.
 *
 * Khung PNG lớn (`ui_panel_main`, `ui_name_plate`…) chỉ còn dành cho trang chủ, nhân vật chính,
 * màn boss và phần thưởng.
 */
@Composable
fun Modifier.quietPanel(tone: PanelTone = PanelTone.BASE): Modifier {
    val unit = LocalDesign.current.unitPx
    val style = panelStyle(tone)
    return this.drawBehind { drawQuietPanel(style, unit) }
}

private fun DrawScope.drawQuietPanel(style: PanelStyle, unit: Float) {
    val radius = CornerRadius(10f * unit)
    val stroke = 2f * unit
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(GothicColors.PanelTop, GothicColors.PanelBottom)),
        cornerRadius = radius,
        alpha = style.fillAlpha,
    )
    style.tint?.let { tint ->
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(tint.copy(alpha = 0.55f), tint.copy(alpha = 0.25f))),
            cornerRadius = radius,
        )
    }
    drawRoundRect(
        color = style.border.copy(alpha = style.borderAlpha),
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = radius,
        style = Stroke(width = stroke),
    )
    if (!style.corners) return
    // Nét góc: hai đoạn ngắn ôm lấy mỗi góc, nằm trong viền 6 đơn vị.
    val inset = 7f * unit
    val arm = minOf(26f * unit, size.minDimension / 4)
    val w = 2.5f * unit
    val color = (if (style.tint != null) GothicColors.Gold else GothicColors.Gold.copy(alpha = 0.7f))
    val l = inset
    val t = inset
    val r = size.width - inset
    val b = size.height - inset
    for ((corner, dx, dy) in listOf(
        Triple(Offset(l, t), 1f, 1f),
        Triple(Offset(r, t), -1f, 1f),
        Triple(Offset(l, b), 1f, -1f),
        Triple(Offset(r, b), -1f, -1f),
    )) {
        drawLine(color, corner, Offset(corner.x + dx * arm, corner.y), strokeWidth = w)
        drawLine(color, corner, Offset(corner.x, corner.y + dy * arm), strokeWidth = w)
    }
}

/** Đường kẻ mảnh vàng tối, mờ dần hai đầu: tách các hàng mà không cần khung. */
@Composable
fun Hairline(modifier: Modifier = Modifier) {
    val d = LocalDesign.current
    Box(
        modifier = modifier.fillMaxWidth().height(d.dp(2)).background(
            Brush.horizontalGradient(
                listOf(Color.Transparent, GothicColors.GoldDim.copy(alpha = 0.8f), Color.Transparent),
            ),
        ),
    )
}

/** Tiêu đề nhóm: chữ vàng cỡ vừa, căn trái, đường kẻ mảnh bên dưới. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    val d = LocalDesign.current
    Column(modifier = modifier.fillMaxWidth().padding(top = d.dp(8), bottom = d.dp(10))) {
        DesignText(
            text = text,
            size = 32,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().height(d.dp(56)),
        )
        Hairline()
    }
}
