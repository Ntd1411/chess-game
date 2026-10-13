package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.GothicDialog

/**
 * Pause Menu: hộp `ui_dialog_panel` phủ lên bàn cờ, gồm Tiếp tục / Khởi động lại / Cài đặt / Rời trận,
 * mỗi mục có icon riêng (`icon_resume`, `icon_restart`, `icon_settings`, `icon_leave`).
 *
 * Dùng Dialog nên hệ thống tự vẽ lớp mờ phía sau và chặn chạm xuống bàn cờ. Chạm ra ngoài hay
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
    GothicDialog(
        onDismiss = { onItem(PauseItem.RESUME) },
        title = stringResource(R.string.pause_title),
        modifier = modifier,
    ) {
        for (item in items) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ArtImage(iconFor(item), maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.size(44.dp))
                // Tiếp tục là hành động chính (nút vàng); các mục còn lại là nút phụ.
                GothicButton(
                    label = stringResource(labelFor(item)),
                    onClick = { onItem(item) },
                    art = if (item == PauseItem.RESUME) ButtonArt.PRIMARY else ButtonArt.SECONDARY,
                    modifier = Modifier.weight(1f).height(58.dp),
                )
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

/** Icon đầu dòng của từng mục. */
internal fun iconFor(item: PauseItem): Int = when (item) {
    PauseItem.RESUME -> R.drawable.icon_resume
    PauseItem.RESTART -> R.drawable.game_play_button_replay
    PauseItem.SETTINGS -> R.drawable.game_play_button_settings
    PauseItem.LEAVE -> R.drawable.game_play_button_exit
}
