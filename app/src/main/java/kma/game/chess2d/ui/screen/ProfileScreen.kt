package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import kma.game.chess2d.ui.art.Hairline
import kma.game.chess2d.ui.art.quietPanel
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
import androidx.compose.ui.tooling.preview.Preview
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

/** Bố cục Hồ sơ theo đơn vị thiết kế 1080x1920. Hàm thuần để test không chồng lấn. */
internal object ProfileLayout {
    val Character = LobbyLayout.Box(300, 170, 480, 500)
    val NamePlate = LobbyLayout.Box(90, 680, 900, 252)
    val LevelPanel = LobbyLayout.Box(90, 950, 900, 190)
    val StatsPanel = LobbyLayout.Box(90, 1200, 900, 760)

    const val STAT_ROWS = 4
    const val STAT_PAD = 30

    /** Ô thống kê thứ [index] (2 cột x 4 hàng) nằm trọn trong [StatsPanel], cách viền [STAT_PAD]. */
    fun statCell(index: Int): LobbyLayout.Box {
        val p = StatsPanel
        val w = (p.w - 2 * STAT_PAD) / 2
        val h = (p.h - 2 * STAT_PAD) / STAT_ROWS
        return LobbyLayout.Box(p.x + STAT_PAD + (index % 2) * w, p.y + STAT_PAD + (index / 2) * h, w, h)
    }
}

/**
 * Màn Hồ sơ: nhân vật toàn thân và bảng tên `ui_name_plate` (khung cầu kỳ dành cho nhân vật
 * chính), rồi hai panel tối giản: cấp + thanh lên cấp, và bảng thống kê 2x4. Mỗi ô thống kê có
 * nhãn nhỏ màu phụ phía trên và số lớn phía dưới, ngăn bằng đường kẻ mảnh nên viền không bao giờ
 * cắt qua chữ.
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

    DesignCanvas(background = R.drawable.bg_profile, dim = 0.3f, scrim = true, modifier = modifier) {
        SubScreenHeader(title = stringResource(R.string.profile_title), onBack = onBack)

        val ch = ProfileLayout.Character
        ArtImage(
            res = R.drawable.character_protagonist_fullbody,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(ch.x, ch.y, ch.w, ch.h),
        )
        val plate = ProfileLayout.NamePlate
        SlicedArt(res = R.drawable.ui_name_plate, slice = 0.5f, modifier = Modifier.at(plate.x, plate.y, plate.w, plate.h))
        DesignText(
            text = name.ifBlank { stringResource(R.string.match_player_one) },
            size = 40,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(plate.x + 60, plate.y, plate.w - 120, plate.h),
        )

        // Cấp: nhãn trái, tiến độ phải, thanh ở giữa, gợi ý nhỏ ở dưới.
        val lv = ProfileLayout.LevelPanel
        Box(modifier = Modifier.at(lv.x, lv.y, lv.w, lv.h).quietPanel())
        DesignText(
            text = stringResource(R.string.profile_level, stats.level),
            size = 36,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.at(lv.x + 40, lv.y + 18, 420, 60),
        )
        DesignText(
            text = stringResource(R.string.profile_progress, into, total),
            size = 28,
            color = GothicColors.Muted,
            align = TextAlign.End,
            alignment = Alignment.CenterEnd,
            maxLines = 1,
            modifier = Modifier.at(lv.x + 460, lv.y + 18, 400, 60),
        )
        ProgressBar(fraction = into.toFloat() / total, modifier = Modifier.at(lv.x + 36, lv.y + 86, lv.w - 72, 40))
        DesignText(
            text = stringResource(R.string.profile_level_hint, total),
            size = 24,
            color = GothicColors.Muted,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.at(lv.x + 40, lv.y + 132, lv.w - 80, 44),
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

    val p = ProfileLayout.StatsPanel
    Box(modifier = Modifier.at(p.x, p.y, p.w, p.h).quietPanel())
    // Đường kẻ ngăn cột và ngăn hàng, nằm giữa các ô nên không cắt qua nhãn hay số.
    val first = ProfileLayout.statCell(0)
    Box(
        modifier = Modifier.at(first.right - 1, p.y + 50, 2, p.h - 100)
            .background(GothicColors.GoldDim.copy(alpha = 0.45f)),
    )
    for (row in 1 until ProfileLayout.STAT_ROWS) {
        val c = ProfileLayout.statCell(row * 2)
        Hairline(modifier = Modifier.at(p.x + 50, c.y - 1, p.w - 100, 2))
    }
    cells.forEachIndexed { i, (label, value) ->
        val c = ProfileLayout.statCell(i)
        Column(
            modifier = Modifier.at(c.x, c.y, c.w, c.h).padding(horizontal = LocalDesign.current.dp(30)),
            verticalArrangement = Arrangement.Center,
        ) {
            DesignText(
                text = label,
                size = 26,
                color = GothicColors.Muted,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(46)),
            )
            DesignText(
                text = value,
                size = 44,
                weight = FontWeight.Bold,
                color = GothicColors.Parchment,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(70)),
            )
        }
    }
}

// ================================================================================================
// Preview Functions
// ================================================================================================

/** Mock data for previews */
private object ProfilePreviewData {
    val beginnerPlayer = PlayerStats(
        wins = 2,
        losses = 5,
        draws = 1,
        checkmates = 1,
        highestFloor = 6,
        playSeconds = 1800L
    )

    val experiencedPlayer = PlayerStats(
        wins = 30,
        losses = 43,
        draws = 15,
        checkmates = 18,
        highestFloor = 28,
        playSeconds = 86400L
    )

    val veteranPlayer = PlayerStats(
        wins = 72,
        losses = 156,
        draws = 42,
        checkmates = 45,
        highestFloor = 50,
        playSeconds = 360000L
    )
}

@Preview(name = "Beginner Player", showBackground = true)
@Composable
private fun ProfileScreenPreview_Beginner() {
    ProfileScreen(
        name = "ChessNovice",
        stats = ProfilePreviewData.beginnerPlayer,
        onBack = {}
    )
}

@Preview(name = "Experienced Player", showBackground = true)
@Composable
private fun ProfileScreenPreview_Experienced() {
    ProfileScreen(
        name = "TacticalMaster",
        stats = ProfilePreviewData.experiencedPlayer,
        onBack = {}
    )
}

@Preview(name = "Veteran Player", showBackground = true)
@Composable
private fun ProfileScreenPreview_Veteran() {
    ProfileScreen(
        name = "GrandmasterLegend",
        stats = ProfilePreviewData.veteranPlayer,
        onBack = {}
    )
}
