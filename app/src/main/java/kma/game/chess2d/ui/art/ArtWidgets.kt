package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role

/**
 * Thanh tiến độ vẽ từ một cặp ảnh khung + ruột **cùng kích thước canvas** (`loading_bar_*`,
 * `bar_player_xp_*`). Hai ảnh vẽ nguyên canvas, không cắt viền trong suốt, nên ruột luôn khớp
 * đúng chỗ trong khung như bản vẽ; ruột được cắt ngang theo [fraction] chứ không bị ép dẹt.
 *
 * Khung nên có cùng tỉ lệ với canvas ảnh (ví dụ 3:1), thanh thật nằm ở giữa khung.
 *
 * @param fillStart, fillEnd vị trí (0..1 theo chiều ngang canvas) nơi ruột bắt đầu và kết thúc.
 * @param fillOnTop vẽ ruột đè lên khung (khung kiểu "ống rỗng") thay vì nằm dưới khung viền.
 */
@Composable
fun ArtBar(
    @DrawableRes frame: Int,
    @DrawableRes fill: Int,
    fraction: Float,
    modifier: Modifier = Modifier,
    fillStart: Float = 0f,
    fillEnd: Float = 1f,
    fillOnTop: Boolean = false,
) {
    val f = fraction.coerceIn(0f, 1f)
    val right = fillStart + f * (fillEnd - fillStart)
    Box(modifier = modifier) {
        if (fillOnTop) {
            ArtImage(res = frame, maxEdge = ArtSizes.BUTTON, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        }
        if (f > 0f) {
            ArtImage(
                res = fill,
                maxEdge = ArtSizes.BUTTON,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize().drawWithContent {
                    clipRect(right = size.width * right) { this@drawWithContent.drawContent() }
                },
            )
        }
        if (!fillOnTop) {
            ArtImage(res = frame, maxEdge = ArtSizes.BUTTON, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Vùng chạm có hiệu ứng nhấn (thu nhỏ nhẹ) cho thẻ/nút vẽ bằng ảnh chỉ có một trạng thái. Tắt thì
 * mờ đi và không nhận chạm.
 */
@Composable
fun ArtPressable(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    disabledAlpha: Float = 0.45f,
    content: @Composable BoxScope.(pressed: Boolean) -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.96f else 1f, label = "pressScale")
    Box(
        modifier = modifier
            .scale(scale)
            .alpha(if (enabled) 1f else disabledAlpha)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        content(pressed)
    }
}
