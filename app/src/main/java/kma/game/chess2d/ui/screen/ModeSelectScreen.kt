package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Một thẻ chế độ: icon, tên, mô tả và việc cần làm khi chạm. */
private class ModeCard(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val onClick: () -> Unit,
)

/**
 * Chọn chế độ theo bản thiết kế: nền `bg_tower_map`, bốn thẻ `ui_panel_menu` cao 190 cách nhau
 * 215 bắt đầu từ y=230, mỗi thẻ có icon (110x110) bên trái và tên + mô tả bên phải. Cả thẻ là
 * vùng chạm, không dùng nút kéo giãn. Thẻ thứ năm (Máy vs Máy) nối tiếp cùng nhịp.
 */
@Composable
fun ModeSelectScreen(
    onOpenTower: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit,
    onOpenSpectate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        ModeCard(R.drawable.icon_story_mode, R.string.menu_tower, R.string.mode_story_desc, onOpenTower),
        ModeCard(R.drawable.icon_ai_battle, R.string.menu_vs_ai, R.string.mode_ai_desc, onPlayComputer),
        ModeCard(R.drawable.icon_local_multiplayer, R.string.menu_local, R.string.mode_local_desc, onPlayTwoPlayers),
        ModeCard(R.drawable.icon_lan_multiplayer, R.string.menu_lan, R.string.mode_lan_desc, onPlayLan),
        ModeCard(R.drawable.chess_piece_black_king, R.string.spectate_open, R.string.mode_spectate_desc, onOpenSpectate),
    )
    DesignCanvas(background = R.drawable.bg_tower_map, dim = 0.35f, modifier = modifier) {
        SubScreenHeader(title = stringResource(R.string.modes_title), onBack = onBack, backY = 64)
        cards.forEachIndexed { i, card ->
            val y = 230 + i * 215
            SlicedArt(
                res = R.drawable.ui_panel_menu,
                slice = 0.3f,
                modifier = Modifier.at(60, y, 960, 190).clickable(role = Role.Button, onClick = card.onClick),
            )
            ArtImage(card.icon, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.at(100, y + 40, 110, 110))
            Column(modifier = Modifier.at(245, y + 25, 700, 140)) {
                DesignText(
                    text = stringResource(card.title),
                    size = 42,
                    color = GothicColors.Gold,
                    weight = FontWeight.Bold,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    maxLines = 1,
                )
                DesignText(
                    text = stringResource(card.description),
                    size = 28,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    maxLines = 2,
                )
            }
        }
    }
}
