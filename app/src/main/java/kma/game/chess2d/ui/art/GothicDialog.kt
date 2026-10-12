package kma.game.chess2d.ui.art

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Hộp thoại gothic thay cho `AlertDialog` của Material: nền `ui_dialog_panel` (hộp lớn, nhiều
 * lựa chọn) hoặc `ui_dialog_small` (hộp xác nhận ngắn) kéo kiểu nine-slice để ornament ở góc
 * không méo, tiêu đề vàng và nội dung xếp dọc.
 *
 * Hộp thoại nằm ngoài [DesignCanvas] nên kích thước tính thẳng bằng dp, không theo khung 1080x1920.
 *
 * @param small dùng khung nhỏ cho câu hỏi Có/Không.
 * @param dismissible `false` thì chạm ra ngoài hay Back không đóng hộp: người dùng phải bấm nút.
 */
@Composable
fun GothicDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    small: Boolean = false,
    dismissible: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = { if (dismissible) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(modifier = modifier.fillMaxWidth(0.9f).widthIn(max = 520.dp)) {
            SlicedArt(
                res = if (small) R.drawable.ui_dialog_small else R.drawable.ui_dialog_panel,
                slice = 0.22f,
                modifier = Modifier.matchParentSize(),
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 30.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        color = GothicColors.Gold,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 8f)),
                    )
                }
                content()
            }
        }
    }
}

/** Chữ thân hộp thoại: giấy da, căn giữa. */
@Composable
fun DialogText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = GothicColors.Parchment,
        fontSize = 17.sp,
        lineHeight = 23.sp,
        textAlign = TextAlign.Center,
        style = TextStyle(shadow = Shadow(Color.Black, blurRadius = 6f)),
        modifier = modifier,
    )
}

/**
 * Câu hỏi Có/Không trên khung `ui_dialog_small`: nút chính `button_primary` bên phải, nút phụ
 * `button_secondary` bên trái. Chạm ra ngoài hay Back đều là chọn nút phụ, trừ khi [dismissible] = false.
 */
@Composable
fun GothicConfirmDialog(
    text: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    title: String? = null,
    dismissible: Boolean = true,
) {
    GothicDialog(onDismiss = onDismiss, title = title, small = true, dismissible = dismissible) {
        DialogText(text)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GothicButton(
                label = dismissLabel,
                onClick = onDismiss,
                art = ButtonArt.SECONDARY,
                modifier = Modifier.weight(1f).height(52.dp),
            )
            GothicButton(
                label = confirmLabel,
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(52.dp),
            )
        }
    }
}
