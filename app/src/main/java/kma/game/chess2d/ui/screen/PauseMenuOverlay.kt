package kma.game.chess2d.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kma.game.chess2d.R
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Pause Menu: lớp phủ làm mờ bàn cờ phía sau, gồm Tiếp tục / Khởi động lại / Cài đặt / Rời trận.
 *
 * Dùng [Dialog] nên hệ thống tự vẽ lớp mờ phía sau và chặn chạm xuống bàn cờ. Chạm ra ngoài hay
 * bấm Back đều là "Tiếp tục", không bao giờ là rời trận: rời trận phải bấm đúng nút.
 *
 * @param items các mục cần hiện, do `PauseMenu.itemsFor` quyết định.
 * @param onItem được gọi khi bấm một mục; phía gọi tự đóng overlay nếu cần.
 */
@Composable
fun PauseMenuOverlay(
    items: List<PauseItem>,
    onItem: (PauseItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = { onItem(PauseItem.RESUME) }) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = GothicColors.Ink,
            border = BorderStroke(2.dp, GothicColors.Gold),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.pause_title),
                    color = GothicColors.Gold,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                for (item in items) {
                    val label = stringResource(labelFor(item))
                    // Tiếp tục là hành động chính (nền vàng); các mục còn lại là viền.
                    if (item == PauseItem.RESUME) {
                        Button(
                            onClick = { onItem(item) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GothicColors.Gold,
                                contentColor = GothicColors.Ink,
                            ),
                        ) { Text(label) }
                    } else {
                        OutlinedButton(
                            onClick = { onItem(item) },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                            border = BorderStroke(
                                1.dp,
                                if (item == PauseItem.LEAVE) GothicColors.Blood else GothicColors.Gold,
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = GothicColors.Parchment,
                            ),
                        ) { Text(label) }
                    }
                }
            }
        }
    }
}

private fun labelFor(item: PauseItem): Int = when (item) {
    PauseItem.RESUME -> R.string.pause_resume
    PauseItem.RESTART -> R.string.pause_restart
    PauseItem.SETTINGS -> R.string.pause_settings
    PauseItem.LEAVE -> R.string.pause_leave
}
