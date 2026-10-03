package kma.game.chess2d.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.profile.PlayTime
import kma.game.chess2d.profile.PlayerStats
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn Hồ sơ: ảnh nhân vật ở giữa, tên và cấp, thanh tiến độ lên cấp, rồi bảng thống kê.
 *
 * Chỉ có phần "Thông tin" (thống kê). Các tab Trang phục/Kỹ năng/Ký ức trong spec chưa có hệ
 * thống nào đằng sau nên không dựng: một tab rỗng là ngõ cụt.
 *
 * @param stats thống kê hiện tại; mọi số liệu đọc thẳng từ đây, màn không tự tính lại.
 */
@Composable
fun ProfileScreen(
    name: String,
    stats: PlayerStats,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_main_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Phủ tối để chữ và bảng thống kê đọc được trên mọi vùng của ảnh nền.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = stringResource(R.string.profile_back),
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                )
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = GothicColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Phần dưới cuộn được: máy nhỏ hoặc chữ lớn không được cắt mất bảng thống kê.
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.char_protagonist_full_body),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.height(240.dp).fillMaxWidth(),
                )
                Text(
                    text = name.ifBlank { stringResource(R.string.match_player_one) },
                    style = MaterialTheme.typography.headlineSmall,
                    color = GothicColors.Parchment,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                LevelBlock(stats = stats)
                StatsCard(stats = stats)
            }
        }
    }
}

/** Cấp hiện tại và thanh tiến độ tới cấp kế tiếp. */
@Composable
private fun LevelBlock(stats: PlayerStats) {
    val total = PlayerStats.GAMES_PER_LEVEL
    val into = stats.pointsIntoLevel
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.profile_level, stats.level),
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Gold,
            fontWeight = FontWeight.Bold,
        )
        // Thanh tự vẽ bằng hai Box: tránh phụ thuộc vào chữ ký của LinearProgressIndicator.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(GothicColors.Locked),
        ) {
            if (into > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = into.toFloat() / total)
                        .background(GothicColors.Gold),
                )
            }
        }
        Text(
            text = stringResource(R.string.profile_progress, into, total),
            style = MaterialTheme.typography.labelMedium,
            color = GothicColors.Parchment,
        )
        Text(
            text = stringResource(R.string.profile_level_hint, total),
            style = MaterialTheme.typography.labelSmall,
            color = GothicColors.Parchment.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
        )
    }
}

/** Bảng thống kê hai cột: thắng/thua/hòa, tỉ lệ thắng, chiếu hết, tầng cao nhất, thời gian chơi. */
@Composable
private fun StatsCard(stats: PlayerStats) {
    val winRate = stats.winRatePercent?.let { stringResource(R.string.profile_win_rate_value, it) }
        ?: stringResource(R.string.profile_win_rate_none)
    val playTime = PlayTime.of(stats.playSeconds)
    val playTimeText = when (playTime.unit) {
        PlayTime.Unit.HOURS_MINUTES ->
            stringResource(R.string.profile_time_hm, playTime.hours, playTime.minutes)
        PlayTime.Unit.MINUTES -> stringResource(R.string.profile_time_m, playTime.minutes)
        PlayTime.Unit.SECONDS -> stringResource(R.string.profile_time_s, playTime.seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StatPair(
            left = stringResource(R.string.profile_wins) to stats.wins.toString(),
            right = stringResource(R.string.profile_losses) to stats.losses.toString(),
        )
        StatPair(
            left = stringResource(R.string.profile_draws) to stats.draws.toString(),
            right = stringResource(R.string.profile_total_games) to stats.totalGames.toString(),
        )
        StatPair(
            left = stringResource(R.string.profile_win_rate) to winRate,
            right = stringResource(R.string.profile_checkmates) to stats.checkmates.toString(),
        )
        StatPair(
            left = stringResource(R.string.profile_highest_floor) to
                stringResource(
                    R.string.profile_highest_floor_value,
                    stats.highestFloor,
                    TowerCatalog.FLOOR_COUNT,
                ),
            right = stringResource(R.string.profile_play_time) to playTimeText,
        )
    }
}

/** Một hàng hai ô nhãn/giá trị, chia đôi chiều ngang. */
@Composable
private fun StatPair(left: Pair<String, String>, right: Pair<String, String>) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCell(label = left.first, value = left.second, modifier = Modifier.weight(1f))
        StatCell(label = right.first, value = right.second, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GothicColors.Gold,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Parchment,
        )
    }
}
