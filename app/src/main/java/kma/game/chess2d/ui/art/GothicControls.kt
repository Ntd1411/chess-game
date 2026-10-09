package kma.game.chess2d.ui.art

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Ô nhập chữ gothic: nền `ui_label_plate` kéo kiểu nine-slice, chữ giấy da, con trỏ vàng.
 * Thay cho `OutlinedTextField` của Material để ô nhập cùng bộ với panel và nút.
 *
 * @param fontSize cỡ chữ theo đơn vị thiết kế.
 */
@Composable
fun GothicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    fontSize: Number = 34,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    enabled: Boolean = true,
) {
    val d = LocalDesign.current
    Box(modifier = modifier) {
        SlicedArt(res = R.drawable.ui_label_plate, slice = 0.5f, modifier = Modifier.fillMaxSize())
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = d.dp(56)),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = GothicColors.Parchment.copy(alpha = 0.5f),
                    fontSize = d.sp(fontSize),
                    maxLines = 1,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = enabled,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(GothicColors.Gold),
                textStyle = TextStyle(color = GothicColors.Parchment, fontSize = d.sp(fontSize)),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Công tắc vẽ bằng `toggle_on`/`toggle_off` (khung thiết kế khoảng 100x50). */
@Composable
fun GothicToggle(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    ArtImage(
        res = if (checked) R.drawable.toggle_on else R.drawable.toggle_off,
        maxEdge = ArtSizes.ICON,
        trim = true,
        modifier = modifier.toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange),
    )
}

/** Nút chọn một trong nhiều vẽ bằng `radio_selected`/`radio_unselected` (48x48). */
@Composable
fun GothicRadio(selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ArtImage(
        res = if (selected) R.drawable.radio_selected else R.drawable.radio_unselected,
        maxEdge = ArtSizes.ICON,
        trim = true,
        modifier = modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}
