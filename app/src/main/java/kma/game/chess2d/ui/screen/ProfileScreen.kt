package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.profile.PlayTime
import kma.game.chess2d.profile.PlayerStats
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn Hồ sơ trên khung thiết kế 1080x1920: nhân vật toàn thân ở giữa, tên trên `ui_name_plate`,
 * cấp và thanh tiến độ lên cấp (`loading_bar_frame/fill`), rồi bảng thống kê hai cột trong
 * `ui_panel_main`.
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
    val total = PlayerStats.GAMES_PER_LEVEL
    val into = stats.pointsIntoLevel

    DesignCanvas(background = R.drawable.bg_profile, dim = 0.35f, modifier = modifier) {
        SubScreenHeader(title = stringResource(R.string.profile_title), onBack = onBack)

        ArtImage(
            res = R.drawable.character_protagonist_fullbody,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(190, 180, 700, 690),
        )
        SlicedArt(res = R.drawable.ui_name_plate, slice = 0.5f, modifier = Modifier.at(200, 880, 680, 90))
        DesignText(
            text = name.ifBlank { stringResource(R.string.match_player_one) },
            size = 42,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(250, 880, 580, 90),
        )
        DesignText(
            text = stringResource(R.string.profile_level, stats.level) + " · " +
                stringResource(R.string.profile_progress, into, total),
            size = 32,
            maxLines = 1,
            modifier = Modifier.at(150, 980, 780, 56),
        )
        ProgressBar(fraction = into.toFloat() / total, modifier = Modifier.at(210, 1045, 660, 48))
        DesignText(
            text = stringResource(R.string.profile_level_hint, total),
            size = 24,
            color = GothicColors.Parchment.copy(alpha = 0.7f),
            maxLines = 1,
            modifier = Modifier.at(100, 1100, 880, 50),
        )

        StatsPanel(stats = stats)
    }
}

/** Thanh tiến độ: ruột `loading_bar_fill` cắt theo [fraction] trong khung `loading_bar_frame`. */
@Composable
private fun ProgressBar(fraction: Float, modifier: Modifier) {
    val d = LocalDesign.current
    Box(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = d.dp(14), vertical = d.dp(10)),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (fraction > 0f) {
                SlicedArt(
                    res = R.drawable.loading_bar_fill,
                    slice = 0.5f,
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)),
                )
            }
        }
        SlicedArt(res = R.drawable.loading_bar_frame, slice = 0.5f, modifier = Modifier.fillMaxSize())
    }
}

/** Bảng thống kê hai cột: thắng/thua/hòa, tỉ lệ thắng, chiếu hết, tầng cao nhất, thời gian chơi. */
@Composable
private fun StatsPanel(stats: PlayerStats) {
    val winRate = stats.winRatePercent?.let { stringResource(R.string.profile_win_rate_value, it) }
        ?: stringResource(R.string.profile_win_rate_none)
    val playTime = PlayTime.of(stats.playSeconds)
    val playTimeText = when (playTime.unit) {
        PlayTime.Unit.HOURS_MINUTES ->
            stringResource(R.string.profile_time_hm, playTime.hours, playTime.minutes)
        PlayTime.Unit.MINUTES -> stringResource(R.string.profile_time_m, playTime.minutes)
        PlayTime.Unit.SECONDS -> stringResource(R.string.profile_time_s, playTime.seconds)
    }
    val cells = listOf(
        stringResource(R.string.profile_wins) to stats.wins.toString(),
        stringResource(R.string.profile_losses) to stats.losses.toString(),
        stringResource(R.string.profile_draws) to stats.draws.toString(),
        stringResource(R.string.profile_total_games) to stats.totalGames.toString(),
        stringResource(R.string.profile_win_rate) to winRate,
        stringResource(R.string.profile_checkmates) to stats.checkmates.toString(),
        stringResource(R.string.profile_highest_floor) to stringResource(
            R.string.profile_highest_floor_value,
            stats.highestFloor,
            TowerCatalog.FLOOR_COUNT,
        ),
        stringResource(R.string.profile_play_time) to playTimeText,
    )

    SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(60, 1170, 960, 650))
    cells.forEachIndexed { i, (label, value) ->
        val x = if (i % 2 == 0) 120 else 560
        val y = 1215 + (i / 2) * 145
        Column(modifier = Modifier.at(x, y, 400, 130)) {
            DesignText(
                text = label,
                size = 28,
                color = GothicColors.Gold,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(52)),
            )
            DesignText(
                text = value,
                size = 40,
                weight = FontWeight.Bold,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(70)),
            )
        }
    }
}
