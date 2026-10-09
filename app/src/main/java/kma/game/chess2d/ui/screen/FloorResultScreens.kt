package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Floor
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.Reward
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn Chiến thắng theo bản thiết kế: nền `bg_victory`, ánh sáng phía sau huy hiệu, ornament,
 * tiêu đề lớn, panel phần thưởng (vàng, pha lê, sách cổ) và hai nút Tiếp tục / Về sảnh.
 *
 * Hiệu ứng sáng và hạt chỉ nằm phía trên panel, không đè lên vật phẩm hay chữ.
 *
 * @param reward thưởng của tầng ([kma.game.chess2d.campaign.TowerRewards.forFloor]).
 * @param nextFloor tầng vừa mở; `null` ở tầng cuối.
 * @param onContinue về bản đồ, nơi tầng kế đang sáng sẵn nút Vào tầng.
 */
@Composable
fun VictoryScreen(
    floor: Floor,
    reward: Reward,
    nextFloor: Floor?,
    onContinue: () -> Unit,
    onLobby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFinal = floor.number == TowerCatalog.FLOOR_COUNT
    val subtitle = when {
        isFinal -> stringResource(R.string.victory_tower_cleared)
        floor.kind == NodeKind.TREASURE -> stringResource(R.string.victory_sub_treasure)
        floor.kind == NodeKind.STORY -> stringResource(R.string.victory_sub_story)
        else -> stringResource(R.string.victory_sub_floor, floor.number, floor.title)
    }
    val d = LocalDesign.current

    DesignCanvas(background = R.drawable.bg_victory, dim = 0.15f, modifier = modifier) {
        ArtImage(R.drawable.fx_victory_particles, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.5f,
            modifier = Modifier.at(40, 120, 1000, 640))
        ArtImage(R.drawable.decor_victory_light, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.85f,
            modifier = Modifier.at(140, 200, 800, 700))
        ArtImage(R.drawable.decor_victory_particles, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.5f,
            modifier = Modifier.at(200, 220, 680, 480))
        ArtImage(R.drawable.badge_victory, maxEdge = ArtSizes.BUTTON, trim = true,
            modifier = Modifier.at(340, 300, 400, 300))
        ArtImage(R.drawable.decor_victory_ornament, maxEdge = ArtSizes.BUTTON, trim = true,
            modifier = Modifier.at(140, 600, 800, 140))
        DesignText(
            text = stringResource(R.string.victory_title),
            size = 104,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(100, 760, 880, 140),
        )
        DesignText(subtitle, size = 32, maxLines = 2, modifier = Modifier.at(100, 900, 880, 80))

        SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(100, 980, 880, 360))
        RewardItem(R.drawable.item_gold_coin, reward.gold, x = 230)
        RewardItem(R.drawable.item_blue_crystal, reward.crystals, x = 490)
        RewardItem(R.drawable.item_ancient_book, reward.books, x = 750)
        if (nextFloor != null) {
            DesignText(
                text = stringResource(R.string.victory_unlocked, nextFloor.number),
                size = 30,
                color = GothicColors.Gold,
                maxLines = 1,
                modifier = Modifier.at(140, 1250, 800, 60),
            )
        }

        GothicButton(
            label = stringResource(R.string.victory_continue),
            onClick = onContinue,
            fontSize = d.sp(40),
            modifier = Modifier.at(150, 1430, 780, 110),
        )
        GothicButton(
            label = stringResource(R.string.result_to_lobby),
            onClick = onLobby,
            art = ButtonArt.SECONDARY,
            fontSize = d.sp(36),
            modifier = Modifier.at(150, 1560, 780, 100),
        )
    }
}

/** Một vật phẩm thưởng 100x100 ở y=1080 và số lượng ngay bên dưới. */
@Composable
private fun RewardItem(@DrawableRes icon: Int, count: Int, x: Int) {
    ArtImage(icon, maxEdge = ArtSizes.PIECE, trim = true, alpha = if (count > 0) 1f else 0.35f,
        modifier = Modifier.at(x, 1080, 100, 100))
    DesignText(
        text = stringResource(R.string.inventory_count, count),
        size = 34,
        weight = FontWeight.Bold,
        color = if (count > 0) GothicColors.Gold else GothicColors.Parchment.copy(alpha = 0.5f),
        maxLines = 1,
        modifier = Modifier.at(x - 40, 1185, 180, 56),
    )
}

/**
 * Màn Thất bại theo bản thiết kế: nền `bg_defeat`, khói mờ ở giữa, ornament, tiêu đề, panel lý do
 * thua và hai nút Thử lại / Về sảnh. Chỉ dùng một lớp hạt (`decor_defeat_particles`) cho đỡ rối.
 */
@Composable
fun DefeatScreen(
    floor: Floor,
    reason: LossReason,
    onRetry: () -> Unit,
    onLobby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val d = LocalDesign.current
    DesignCanvas(background = R.drawable.bg_defeat, dim = 0.2f, modifier = modifier) {
        ArtImage(R.drawable.decor_defeat_smoke, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.3f,
            modifier = Modifier.at(140, 360, 800, 1060))
        ArtImage(R.drawable.decor_defeat_particles, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.45f,
            modifier = Modifier.at(0, 120, 300, 420))
        ArtImage(R.drawable.decor_defeat_particles, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.45f,
            modifier = Modifier.at(780, 1680, 300, 220))
        ArtImage(R.drawable.decor_defeat_ornament, maxEdge = ArtSizes.BUTTON, trim = true,
            modifier = Modifier.at(140, 650, 800, 140))
        DesignText(
            text = stringResource(R.string.defeat_title),
            size = 104,
            color = GothicColors.Blood,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(100, 790, 880, 140),
        )

        SlicedArt(res = R.drawable.ui_panel_sub, slice = 0.2f, modifier = Modifier.at(100, 980, 880, 230))
        Column(
            modifier = Modifier.at(100, 980, 880, 230).padding(horizontal = d.dp(70), vertical = d.dp(28)),
            verticalArrangement = Arrangement.spacedBy(d.dp(4)),
        ) {
            DesignText(stringResource(R.string.defeat_floor, floor.number), size = 32, color = GothicColors.Gold,
                weight = FontWeight.Bold, maxLines = 1, modifier = Modifier.fillMaxWidth())
            DesignText(
                text = stringResource(
                    when (reason) {
                        LossReason.CHECKMATE -> R.string.defeat_reason_checkmate
                        LossReason.DRAW -> R.string.defeat_reason_draw
                    },
                ),
                size = 28,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            DesignText(stringResource(R.string.defeat_encourage), size = 26, italic = true, maxLines = 2,
                color = GothicColors.Parchment.copy(alpha = 0.8f), modifier = Modifier.fillMaxWidth())
        }

        GothicButton(
            label = stringResource(R.string.defeat_retry),
            onClick = onRetry,
            fontSize = d.sp(40),
            modifier = Modifier.at(150, 1320, 780, 110),
        )
        GothicButton(
            label = stringResource(R.string.result_to_lobby),
            onClick = onLobby,
            art = ButtonArt.SECONDARY,
            fontSize = d.sp(36),
            modifier = Modifier.at(150, 1450, 780, 100),
        )
    }
}
