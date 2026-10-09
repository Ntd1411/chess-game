package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GothicColors
import kotlin.math.min

/** Khung thiết kế chung của mọi màn: 1080 x 1920 đơn vị, toạ độ lấy thẳng từ bản thiết kế. */
object Design {
    const val WIDTH = 1080f
    const val HEIGHT = 1920f

    /** Số px mỗi đơn vị thiết kế để cả khung 1080x1920 lọt vào vùng [width]x[height] px. Hàm thuần. */
    fun unitPx(width: Float, height: Float): Float = min(width / WIDTH, height / HEIGHT)
}

/** Đổi đơn vị thiết kế sang dp/sp trên máy hiện tại. */
@Immutable
class DesignScale(val unitPx: Float, private val density: Float, private val fontScale: Float) {
    fun dp(value: Number): Dp = (value.toFloat() * unitPx / density).dp

    /** Cỡ chữ theo đơn vị thiết kế; chia fontScale để chữ giữ đúng tỉ lệ với khung panel. */
    fun sp(value: Number): TextUnit = (value.toFloat() * unitPx / density / fontScale).sp
}

val LocalDesign = staticCompositionLocalOf { DesignScale(1f, 1f, 1f) }

/**
 * Nền phủ kín màn hình (Crop, không méo) cộng một khung thiết kế 1080x1920 được co vừa vùng an
 * toàn và căn giữa. Con đặt vị trí bằng [at] theo đúng toạ độ trong bản thiết kế, nên bố cục
 * giữ nguyên trên mọi máy; máy dài hơn 16:9 chỉ lộ thêm nền ở trên/dưới.
 *
 * @param dim độ phủ tối nền để chữ đọc được.
 * @param backLayer lớp vẽ ngay trên nền nhưng dưới khung thiết kế (hiệu ứng phủ tràn mép).
 */
@Composable
fun DesignCanvas(
    @DrawableRes background: Int,
    modifier: Modifier = Modifier,
    dim: Float = 0.25f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(res = background, dim = dim, maxEdge = ArtSizes.SCREEN)
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            contentAlignment = Alignment.Center,
        ) {
            val density = LocalDensity.current
            val scale = DesignScale(
                unitPx = Design.unitPx(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat()),
                density = density.density,
                fontScale = density.fontScale,
            )
            CompositionLocalProvider(LocalDesign provides scale) {
                Box(modifier = Modifier.size(scale.dp(Design.WIDTH), scale.dp(Design.HEIGHT)), content = content)
            }
        }
    }
}

/** Đặt phần tử vào khung (x, y, w, h) tính theo đơn vị thiết kế 1080x1920. */
@Composable
fun Modifier.at(x: Number, y: Number, w: Number, h: Number): Modifier {
    val d = LocalDesign.current
    return this.offset(d.dp(x), d.dp(y)).size(d.dp(w), d.dp(h))
}

/**
 * Chữ đặt trong một khung thiết kế, có bóng đen nhẹ để đọc được trên nền ảnh.
 *
 * @param size cỡ chữ theo đơn vị thiết kế (không phải sp).
 */
@Composable
fun DesignText(
    text: String,
    size: Number,
    modifier: Modifier = Modifier,
    color: Color = GothicColors.Parchment,
    weight: FontWeight = FontWeight.Normal,
    align: TextAlign = TextAlign.Center,
    alignment: Alignment = Alignment.Center,
    maxLines: Int = Int.MAX_VALUE,
    italic: Boolean = false,
) {
    val d = LocalDesign.current
    Box(modifier = modifier, contentAlignment = alignment) {
        Text(
            text = text,
            color = color,
            fontSize = d.sp(size),
            lineHeight = d.sp(size.toFloat() * 1.25f),
            fontWeight = weight,
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            textAlign = align,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 8f)),
        )
    }
}

/** Tiêu đề màn: chữ vàng đậm, căn giữa khung. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier, size: Number = 56) {
    DesignText(
        text = text,
        size = size,
        modifier = modifier,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        maxLines = 1,
    )
}

/** Đầu màn phụ chung của bản thiết kế: nút back (48,60,88,88) và tiêu đề (180,60,720,100). */
@Composable
fun SubScreenHeader(title: String, onBack: () -> Unit, backY: Int = 60) {
    BackIcon(onClick = onBack, modifier = Modifier.at(48, backY, 88, 88))
    ScreenTitle(text = title, modifier = Modifier.at(180, 60, 720, 100))
}

/** Nút quay lại dùng đúng `icon_back.png`, không tạo lại icon. */
@Composable
fun BackIcon(onClick: () -> Unit, modifier: Modifier = Modifier, description: String? = null) {
    ArtImage(
        res = R.drawable.icon_back,
        maxEdge = ArtSizes.ICON,
        trim = true,
        contentDescription = description ?: stringResource(R.string.action_back),
        modifier = modifier.clip(CircleShape).clickable(role = Role.Button, onClick = onClick),
    )
}
