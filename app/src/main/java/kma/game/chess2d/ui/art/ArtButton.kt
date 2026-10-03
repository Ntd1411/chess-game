package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Nút bấm vẽ bằng ảnh "bảng" gothic (viền vàng, lõi đá obsidian) với chữ đè ở giữa.
 *
 * Ảnh luôn được vẽ Fit giữ đúng tỉ lệ gốc, không kéo giãn, vì hai đầu nút có ornament.
 * Vùng chạm là toàn bộ hộp của nút nên chiều cao do bên gọi quyết định (>= 48dp cho nút nhỏ).
 * Khi nhấn: nút thu nhẹ, và đổi sang [pressedRes] nếu có (ảnh pressed phải có nền trong suốt),
 * nếu không thì làm tối ảnh thường.
 */
@Composable
fun PlateButton(
    label: String,
    onClick: () -> Unit,
    @DrawableRes normalRes: Int,
    modifier: Modifier = Modifier,
    @DrawableRes pressedRes: Int? = null,
    fontSize: TextUnit = 18.sp,
    maxLines: Int = 1,
    textColor: Color = GothicColors.Gold,
    textPadding: Dp = 22.dp,
    maxEdge: Int = ArtSizes.BUTTON,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "plateScale")

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        val usePressedAsset = pressed && pressedRes != null
        ArtImage(
            res = if (usePressedAsset) pressedRes!! else normalRes,
            maxEdge = maxEdge,
            contentScale = ContentScale.Fit,
            colorFilter = if (pressed && !usePressedAsset) {
                ColorFilter.tint(Color(0xFFB8B8B8), BlendMode.Modulate)
            } else {
                null
            },
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            maxLines = maxLines,
            textAlign = TextAlign.Center,
            lineHeight = fontSize * 1.1f,
            style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 6f)),
            modifier = Modifier.padding(horizontal = textPadding),
        )
    }
}
