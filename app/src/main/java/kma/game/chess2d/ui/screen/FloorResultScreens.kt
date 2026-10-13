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

internal fun defeatReasonRes(reason: LossReason): Int = when (reason) {
    LossReason.CHECKMATE -> R.string.defeat_reason_checkmate
    LossReason.DRAW -> R.string.defeat_reason_draw
}

@Composable
fun DefeatScreen(floor: Floor, reason: LossReason, onRetry: () -> Unit, onLobby: () -> Unit, modifier: Modifier = Modifier) {
    SimpleSetupPage(R.drawable.bg_defeat, stringResource(R.string.defeat_title), stringResource(R.string.action_back), onLobby, modifier,
        footer = {
            SimpleSetupButton(stringResource(R.string.defeat_retry), onRetry, Modifier.fillMaxWidth(), primary = true)
            SimpleSetupButton(stringResource(R.string.result_to_lobby), onLobby, Modifier.fillMaxWidth())
        }) {
        ArtImage(R.drawable.badge_defeat, trim = true, modifier = Modifier.size(120.dp).align(androidx.compose.ui.Alignment.CenterHorizontally))
        SimpleSetupCard {
            Text(stringResource(R.string.defeat_floor, floor.number), color = GothicColors.Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(floor.title, color = GothicColors.Muted, fontSize = 15.sp)
            Text(stringResource(defeatReasonRes(reason)), color = GothicColors.Parchment, fontSize = 18.sp)
            Text(stringResource(R.string.defeat_encourage), color = GothicColors.Muted, fontSize = 15.sp)
        }
    }
}
