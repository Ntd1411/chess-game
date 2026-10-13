package kma.game.chess2d.ui.art

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Ô nhập chữ gothic: nền `ui_text_field_normal` (đang gõ thì `ui_text_field_focused`) kéo kiểu
 * nine-slice, chữ giấy da, con trỏ vàng.
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
    var focused by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        SlicedArt(
            res = if (focused) R.drawable.ui_text_field_focused else R.drawable.ui_text_field_normal,
            slice = 0.5f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = d.dp(56)),
            contentAlignment = Alignment.Center,
        ) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = GothicColors.Parchment.copy(alpha = 0.5f),
                    fontSize = d.sp(fontSize),
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                enabled = enabled,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(GothicColors.Gold),
                textStyle = TextStyle(
                    fontFamily = GameFontFamily,
                    color = GothicColors.Parchment,
                    fontSize = d.sp(fontSize),
                    textAlign = TextAlign.Center,
                ),
                modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
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

@Preview(widthDp = 400, heightDp = 200, showBackground = true)
@Composable
private fun GothicTextFieldPreview() {
    val d = LocalDesign.current
    Column(
        modifier = Modifier.fillMaxSize().padding(d.dp(20)),
        verticalArrangement = Arrangement.spacedBy(d.dp(20)),
    ) {
        var text1 by remember { mutableStateOf("") }
        GothicTextField(
            value = text1,
            onValueChange = { text1 = it },
            placeholder = "Enter name...",
            modifier = Modifier.fillMaxWidth().height(d.dp(100)),
        )
        
        var text2 by remember { mutableStateOf("Player 1") }
        GothicTextField(
            value = text2,
            onValueChange = { text2 = it },
            placeholder = "Enter name...",
            modifier = Modifier.fillMaxWidth().height(d.dp(100)),
        )
    }
}

@Preview(widthDp = 300, heightDp = 150, showBackground = true)
@Composable
private fun GothicTogglePreview() {
    val d = LocalDesign.current
    Row(
        modifier = Modifier.fillMaxSize().padding(d.dp(20)),
        horizontalArrangement = Arrangement.spacedBy(d.dp(30)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var checked1 by remember { mutableStateOf(false) }
        GothicToggle(
            checked = checked1,
            onCheckedChange = { checked1 = it },
            modifier = Modifier.width(d.dp(120)).height(d.dp(60))  // Cách 1: Resize với modifier
        )
        
        var checked2 by remember { mutableStateOf(true) }
        GothicToggle(
            checked = checked2,
            onCheckedChange = { checked2 = it },
            modifier = Modifier.width(d.dp(80)).height(d.dp(40))  // Nhỏ hơn
        )
    }
}

@Preview(widthDp = 200, heightDp = 150, showBackground = true)
@Composable
private fun GothicRadioPreview() {
    val d = LocalDesign.current
    Row(
        modifier = Modifier.fillMaxSize().padding(d.dp(20)),
        horizontalArrangement = Arrangement.spacedBy(d.dp(30)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var selected by remember { mutableStateOf(0) }
        GothicRadio(
            selected = selected == 0,
            onClick = { selected = 0 },
            modifier = Modifier.size(d.dp(60))  // Cách 1: Resize với modifier (60x60)
        )
        
        GothicRadio(
            selected = selected == 1,
            onClick = { selected = 1 },
            modifier = Modifier.size(d.dp(40))  // Nhỏ hơn (40x40)
        )
    }
}
