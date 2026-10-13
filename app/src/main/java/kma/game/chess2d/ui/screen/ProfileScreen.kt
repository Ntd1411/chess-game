package kma.game.chess2d.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.profile.PlayTime
import kma.game.chess2d.profile.PlayerStats
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.theme.GothicColors

internal enum class ProfileMetric(@StringRes val label: Int) {
    WINS(R.string.profile_wins), LOSSES(R.string.profile_losses), DRAWS(R.string.profile_draws),
    TOTAL(R.string.profile_total_games), WIN_RATE(R.string.profile_win_rate),
    CHECKMATES(R.string.profile_checkmates), FLOOR(R.string.profile_highest_floor), TIME(R.string.profile_play_time),
}

internal fun profileLevelFraction(stats: PlayerStats): Float =
    stats.pointsIntoLevel.toFloat() / PlayerStats.GAMES_PER_LEVEL

@Composable
fun ProfileScreen(name: String, stats: PlayerStats, onBack: () -> Unit, modifier: Modifier = Modifier) {
    SimpleSetupPage(
        background = R.drawable.bg_profile, title = stringResource(R.string.profile_title),
        backLabel = stringResource(R.string.action_back), onBack = onBack, modifier = modifier,
        footer = { SimpleSetupButton(stringResource(R.string.profile_back), onBack, Modifier.fillMaxWidth()) },
    ) {
        SimpleSetupCard {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                ArtImage(res = R.drawable.portrait_protagonist, trim = true, modifier = Modifier.size(88.dp).clip(CircleShape))
                Text(name.ifBlank { stringResource(R.string.match_player_one) }, color = GothicColors.Gold,
                    fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
        }
        SimpleSetupCard {
            Text(stringResource(R.string.profile_level, stats.level), color = GothicColors.Gold, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.profile_progress, stats.pointsIntoLevel, PlayerStats.GAMES_PER_LEVEL), color = GothicColors.Parchment)
            LinearProgressIndicator(progress = { profileLevelFraction(stats) }, color = GothicColors.Gold,
                trackColor = GothicColors.Gold.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)))
            Text(stringResource(R.string.profile_level_hint, PlayerStats.GAMES_PER_LEVEL), color = GothicColors.Muted, fontSize = 13.sp)
        }
        val time = PlayTime.of(stats.playSeconds)
        val values = listOf(stats.wins.toString(), stats.losses.toString(), stats.draws.toString(), stats.totalGames.toString(),
            stats.winRatePercent?.let { stringResource(R.string.profile_win_rate_value, it) } ?: stringResource(R.string.profile_win_rate_none),
            stats.checkmates.toString(), stringResource(R.string.profile_highest_floor_value, stats.highestFloor, TowerCatalog.FLOOR_COUNT),
            when (time.unit) {
                PlayTime.Unit.HOURS_MINUTES -> stringResource(R.string.profile_time_hm, time.hours, time.minutes)
                PlayTime.Unit.MINUTES -> stringResource(R.string.profile_time_m, time.minutes)
                PlayTime.Unit.SECONDS -> stringResource(R.string.profile_time_s, time.seconds)
            })
        ProfileMetric.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { metric ->
                    SimpleSetupCard(Modifier.weight(1f)) {
                        Text(stringResource(metric.label), color = GothicColors.Muted, fontSize = 13.sp, minLines = 2, maxLines = 2)
                        Text(values[metric.ordinal], color = GothicColors.Parchment, fontSize = 22.sp, fontWeight = FontWeight.Bold, minLines = 2, maxLines = 2)
                    }
                }
            }
        }
    }
}
