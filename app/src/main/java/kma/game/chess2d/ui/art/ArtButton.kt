package kma.game.chess2d.ui.art

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Bộ ảnh trạng thái của một loại nút. Mỗi lúc chỉ vẽ **một** trạng thái; `hover` không dùng vì
 * màn hình cảm ứng không có trạng thái rê chuột.
 */
enum class ButtonArt(
    @DrawableRes val normal: Int,
    @DrawableRes val pressed: Int,
    @DrawableRes val disabled: Int,
) {
    PRIMARY(R.drawable.button_primary_normal, R.drawable.button_primary_pressed, R.drawable.button_primary_disabled),
    SECONDARY(
        R.drawable.button_secondary_normal,
        R.drawable.button_secondary_pressed,
        R.drawable.button_secondary_disabled,
    ),
    SMALL(R.drawable.button_small_normal, R.drawable.button_small_pressed, R.drawable.button_small_disabled),
    CHOICE(R.drawable.button_choice_normal, R.drawable.button_choice_pressed, R.drawable.button_choice_disabled),
    ;

    /** Ảnh cần vẽ cho trạng thái hiện tại: tắt ưu tiên hơn nhấn. */
    @DrawableRes
    fun resFor(isEnabled: Boolean, isPressed: Boolean): Int = when {
        !isEnabled -> disabled
        isPressed -> pressed
        else -> normal
    }
}

/** Gameplay buttons with one artwork use press scaling and disabled alpha; other buttons retain their state images. */
enum class BattleButton(
    @DrawableRes val normal: Int,
    @DrawableRes val pressed: Int = normal,
    @DrawableRes val disabled: Int = normal,
) {
    UNDO(R.drawable.game_play_button_undo),
    HINT(R.drawable.battle_button_hint),
    MOVE(R.drawable.battle_button_move),
    MENU(R.drawable.game_play_button_menu),
    CONTINUE(R.drawable.game_play_button_continue),
    RESTART(R.drawable.game_play_button_replay),
    SETTINGS(R.drawable.game_play_button_settings),
    FLIP(R.drawable.battle_button_flip_normal, R.drawable.battle_button_flip_pressed, R.drawable.battle_button_flip_disabled),
    LEVEL(R.drawable.game_play_button_level),
    MAP(R.drawable.battle_button_map_normal, R.drawable.battle_button_map_pressed, R.drawable.battle_button_map_disabled),
    RESIGN(R.drawable.game_play_button_resign),
    DRAW(R.drawable.game_play_button_draw),
    REMATCH(R.drawable.game_play_button_replay),
    SYNC(R.drawable.battle_button_sync_normal, R.drawable.battle_button_sync_pressed, R.drawable.battle_button_sync_disabled),
    SHARE(
        R.drawable.battle_button_share_normal,
        R.drawable.battle_button_share_pressed,
        R.drawable.battle_button_share_disabled,
    ),
    LEAVE(R.drawable.game_play_button_exit),
    ;

    /** Chỉ có một ảnh cho mọi trạng thái. */
    val singleState: Boolean get() = normal == pressed && normal == disabled

    /** Ảnh cần vẽ cho trạng thái hiện tại: tắt ưu tiên hơn nhấn. */
    @DrawableRes
    fun resFor(isEnabled: Boolean, isPressed: Boolean): Int = when {
        !isEnabled -> disabled
        isPressed -> pressed
        else -> normal
    }
}

/**
 * Nút gothic vẽ từ bộ ảnh `button_*` với chữ đè ở giữa.
 *
 * Ảnh nút là khung 3:1 nhưng khung thiết kế thường tới 7:1, nên ảnh được vẽ kiểu nine-slice
 * ([SlicedArt]): hai đầu ornament giữ đúng tỉ lệ, phần thân giãn ngang. Cỡ chữ và lề chữ tính
 * theo chiều cao nút nếu bên gọi không truyền, nên chữ không đè lên hai đầu nút.
 */
@Composable
fun GothicButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    art: ButtonArt = ButtonArt.PRIMARY,
    enabled: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    textColor: Color = if (art == ButtonArt.PRIMARY) GothicColors.Gold else GothicColors.Parchment,
    maxLines: Int = 1,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "buttonScale")

    BoxWithConstraints(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        SlicedArt(
            res = art.resFor(enabled, pressed),
            slice = 0.5f,
            modifier = Modifier.fillMaxSize(),
        )
        val size = if (fontSize != TextUnit.Unspecified) {
            fontSize
        } else {
            with(LocalDensity.current) { (maxHeight * 0.30f).toSp() }
        }
        Text(
            text = label,
            color = if (enabled) textColor else textColor.copy(alpha = 0.5f),
            fontSize = size,
            lineHeight = size * 1.1f,
            fontFamily = GameFontFamily,
            fontWeight = FontWeight.Bold,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 6f)),
            modifier = Modifier.padding(horizontal = maxHeight * 0.55f),
        )
    }
}
