package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

internal fun victorySubtitleRes(floor: Floor): Int = when {
    floor.number == TowerCatalog.FLOOR_COUNT -> R.string.victory_tower_cleared
    floor.kind == NodeKind.TREASURE -> R.string.victory_sub_treasure
    floor.kind == NodeKind.STORY -> R.string.victory_sub_story
    else -> R.string.victory_sub_floor
}

@Composable
fun VictoryScreen(floor: Floor, reward: Reward, nextFloor: Floor?, onContinue: () -> Unit, onLobby: () -> Unit, modifier: Modifier = Modifier) {
    SimpleSetupPage(R.drawable.bg_victory, stringResource(R.string.victory_title), stringResource(R.string.action_back), onContinue, modifier,
        footer = {
            SimpleSetupButton(stringResource(R.string.victory_continue), onContinue, Modifier.fillMaxWidth(), primary = true)
            SimpleSetupButton(stringResource(R.string.result_to_lobby), onLobby, Modifier.fillMaxWidth())
        }) {
        ArtImage(R.drawable.badge_victory, trim = true, modifier = Modifier.size(120.dp).align(androidx.compose.ui.Alignment.CenterHorizontally))
        Text(stringResource(victorySubtitleRes(floor), floor.number, floor.title), color = GothicColors.Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        SimpleSetupCard {
            Text(stringResource(R.string.tower_preview_rewards), color = GothicColors.Gold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.tower_preview_reward_values, reward.gold, reward.crystals, reward.books), color = GothicColors.Parchment, fontSize = 16.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                RewardItem(R.drawable.item_gold_coin, reward.gold)
                RewardItem(R.drawable.item_blue_crystal, reward.crystals)
                RewardItem(R.drawable.item_ancient_book, reward.books)
            }
            if (nextFloor != null) Text(stringResource(R.string.victory_unlocked, nextFloor.number), color = GothicColors.Gold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun RewardItem(@DrawableRes icon: Int, count: Int) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        ArtImage(icon, trim = true, modifier = Modifier.size(48.dp))
        Text(stringResource(R.string.inventory_count, count), color = GothicColors.Parchment, fontSize = 16.sp)
    }
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
    DesignCanvas(background = R.drawable.bg_defeat, dim = 0.2f, modifier = modifier) {
        val d = LocalDesign.current
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
