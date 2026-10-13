package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Floor
import kma.game.chess2d.campaign.FloorStatus
import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.Relic
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.campaign.TowerProgress
import kma.game.chess2d.campaign.TowerRewards
import kma.game.chess2d.campaign.TowerBosses
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.screen.SimpleSetupButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Bố trí bản đồ theo đơn vị thiết kế: các tầng xếp zíc-zắc từ dưới lên trong một dải cuộn.
 * Hàm thuần để test kiểm được node không chồng nhau và tầng 1 ở dưới cùng.
 */
internal object TowerMapLayout {
    const val NODE = 150
    const val STEP = 240
    const val TOP_PAD = 330
    const val BOTTOM_PAD = 180

    /** Tâm X lặp theo chu kỳ 4 tầng để đường đi uốn qua lại. */
    const val MAP_WIDTH = 500
    const val PREVIEW_X = 520
    const val PREVIEW_WIDTH = 512
    const val PREVIEW_TOP = 270
    const val PREVIEW_HEIGHT = 1240
    private val CENTER_X = intArrayOf(260, 360, 260, 160)

    /** Chiều cao cả dải bản đồ. */
    val contentHeight: Int = TOP_PAD + (TowerCatalog.FLOOR_COUNT - 1) * STEP + BOTTOM_PAD

    fun centerX(floor: Int): Int = CENTER_X[(floor - 1) % CENTER_X.size]

    /** Tầng cao hơn nằm trên: tầng cuối ở [TOP_PAD], tầng 1 ở đáy. */
    fun centerY(floor: Int): Int = TOP_PAD + (TowerCatalog.FLOOR_COUNT - floor) * STEP
}

/**
 * Bản đồ Tháp Cờ theo bản thiết kế: nền `bg_tower_map`, đường nối `map_floor_connection` vẽ
 * trước (nằm dưới) node, node theo trạng thái (`map_floor_node` / `_complete` / `_locked` /
 * `_boss` + `badge_boss`), quầng `map_floor_current_glow` dưới tầng hiện tại, và nút Vào tầng.
 *
 * Tap a node to preview it; press Enter to play. Locked floors can be previewed,
 * but cannot be entered.
 */
@Composable
fun TowerMapScreen(
    floors: List<Floor>,
    progress: TowerProgress,
    onFloorClick: (Floor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by rememberSaveable(progress.currentFloor) { mutableIntStateOf(progress.currentFloor) }
    TowerMapScreenContent(
        floors = floors,
        progress = progress,
        selected = selected,
        onSelectFloor = { selected = it },
        onFloorClick = onFloorClick,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun TowerMapScreenContent(
    floors: List<Floor>,
    progress: TowerProgress,
    selected: Int,
    onSelectFloor: (Int) -> Unit,
    onFloorClick: (Floor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedFloor = floors.getOrNull(selected - 1)
    val density = LocalDensity.current
    val scroll = rememberScrollState()

    DesignCanvas(background = R.drawable.bg_tower_map, dim = 0.3f, modifier = modifier) {
        // Đọc tỷ lệ bên trong DesignCanvas: bên ngoài chỉ có giá trị mặc định 1:1.
        val d = LocalDesign.current

        // Cuộn tới tầng hiện tại, đặt nó khoảng giữa dải bản đồ.
        LaunchedEffect(progress.currentFloor, d.unitPx) {
            val target = TowerMapLayout.centerY(progress.currentFloor) - MAP_HEIGHT / 2
            scroll.scrollTo(with(density) { d.dp(target.coerceAtLeast(0)).toPx().roundToInt() })
        }

        ArtImage(R.drawable.decor_map_vignette, maxEdge = ArtSizes.BOARD, alpha = 0.6f,
            modifier = Modifier.at(0, 0, 1080, 1920))

        Box(modifier = Modifier.at(0, MAP_TOP, TowerMapLayout.MAP_WIDTH, MAP_HEIGHT).verticalScroll(scroll)) {
            Box(modifier = Modifier.fillMaxWidth().height(d.dp(TowerMapLayout.contentHeight))) {
                ArtImage(R.drawable.map_tower_emblem, maxEdge = ArtSizes.PIECE, trim = true,
                    modifier = Modifier.at(100, 20, 280, 200))
                // Đường nối vẽ trước: luôn nằm sau node.
                for (n in 1 until floors.size) {
                    Connection(from = n, to = n + 1, open = progress.statusOf(n) == FloorStatus.CLEARED)
                }
                floors.forEach { floor ->
                    FloorNode(
                        floor = floor,
                        status = progress.statusOf(floor.number),
                        isSelected = floor.number == selected,
                        onClick = { onSelectFloor(floor.number) },
                    )
                }
            }
        }

        selectedFloor?.let { floor ->
            FloorPreview(
                floor = floor,
                status = progress.statusOf(floor.number),
                modifier = Modifier.at(TowerMapLayout.PREVIEW_X, TowerMapLayout.PREVIEW_TOP,
                    TowerMapLayout.PREVIEW_WIDTH, TowerMapLayout.PREVIEW_HEIGHT),
            )
        }

        ArtImage(R.drawable.decor_map_bottom_ornament, maxEdge = ArtSizes.PIECE, trim = true,
            modifier = Modifier.at(340, 1600, 400, 50))
        SubScreenHeader(
            title = stringResource(R.string.tower_map_title, progress.currentFloor, TowerCatalog.FLOOR_COUNT),
            onBack = onBack,
        )
        DesignText(
            text = selectedFloor?.let { floorCaption(it) }
                ?: stringResource(R.string.tower_progress, progress.clearedUpTo, TowerCatalog.FLOOR_COUNT),
            size = 30,
            maxLines = 1,
            modifier = Modifier.at(170, 150, 740, 50),
        )
        SimpleSetupButton(
            label = stringResource(R.string.tower_enter),
            primary = selectedFloor != null && progress.statusOf(selected) != FloorStatus.LOCKED,
            modifier = Modifier.at(150, 1650, 780, 110),
            onClick = { selectedFloor?.let(onFloorClick) },
        )
    }
}

/** Fixed rectangular preview beside the scrollable map; rewards stay visible. */
@Composable
private fun FloorPreview(floor: Floor, status: FloorStatus, modifier: Modifier = Modifier) {
    val d = LocalDesign.current
    val detailScroll = rememberScrollState()
    LaunchedEffect(floor.number) { detailScroll.scrollTo(0) }
    val boss = TowerBosses.character(floor)
    val reward = TowerRewards.forFloor(floor)
    Column(
        modifier = modifier.background(GothicColors.Ink.copy(alpha = 0.94f))
            .border(d.dp(3), GothicColors.Gold.copy(alpha = 0.7f)).padding(d.dp(20)),
        verticalArrangement = Arrangement.spacedBy(d.dp(16)),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(detailScroll),
            verticalArrangement = Arrangement.spacedBy(d.dp(16)),
        ) {
            DesignText(stringResource(R.string.tower_floor_label, floor.number), 32,
                color = GothicColors.Gold, weight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            DesignText(floor.title, 38, weight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            if (boss != null) {
                PreviewText(stringResource(boss.name), colorGold = true)
                ArtImage(boss.portrait, maxEdge = ArtSizes.PIECE,
                    modifier = Modifier.align(Alignment.CenterHorizontally).at(0, 0, 120, 120))
            } else {
                floor.estimatedElo?.let {
                    PreviewText(stringResource(R.string.tower_puzzle_elo, it), colorGold = true)
                }
            }
            PreviewText(floor.teaser, italic = true)
            if (boss != null) {
                PreviewText(stringResource(R.string.tower_preview_strength, TowerCatalog.chapterOf(floor.number)))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(d.dp(8))) {
            DesignText(stringResource(R.string.tower_preview_rewards), 30,
                modifier = Modifier.fillMaxWidth(), weight = FontWeight.Bold, color = GothicColors.Gold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(d.dp(24), Alignment.CenterHorizontally),
            ) {
                RewardItem(R.drawable.item_gold_coin, reward.gold)
                RewardItem(R.drawable.item_red_crystal, reward.crystals)
                RewardItem(R.drawable.item_ancient_book, reward.books)
            }
            Relic.entries.firstOrNull { it.unlockFloor == floor.number }?.let { relic ->
                DesignText(stringResource(R.string.tower_preview_relic, stringResource(relicArt(relic).name)),
                    28, modifier = Modifier.fillMaxWidth(), color = GothicColors.Gold)
            }
            DesignText(stringResource(when (status) {
                FloorStatus.LOCKED -> R.string.tower_preview_locked
                FloorStatus.CLEARED -> R.string.tower_preview_cleared
                FloorStatus.CURRENT -> R.string.tower_preview_ready
            }), 28, modifier = Modifier.fillMaxWidth(),
                color = if (status == FloorStatus.LOCKED) GothicColors.Parchment else GothicColors.Gold)
        }
    }
}

@Composable
private fun RewardItem(iconRes: Int, quantity: Int) {
    val d = LocalDesign.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(d.dp(12)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtImage(iconRes, maxEdge = ArtSizes.PIECE, modifier = Modifier.size(d.dp(80)))
        DesignText("$quantity", 64, color = GothicColors.Parchment, weight = FontWeight.Bold)
    }
}

@Composable
private fun PreviewText(text: String, colorGold: Boolean = false, italic: Boolean = false) {
    DesignText(text, 36, modifier = Modifier.fillMaxWidth(), align = TextAlign.Start,
        alignment = Alignment.CenterStart, italic = italic,
        color = if (colorGold) GothicColors.Gold else GothicColors.Parchment)
}

/** "Tầng N · tên · loại" của tầng đang chọn. */
@Composable
private fun floorCaption(floor: Floor): String =
    stringResource(R.string.tower_floor_label, floor.number) + " · " + floor.title + " · " +
        stringResource(kindLabelRes(floor.kind))

/** Một đoạn xích `map_floor_connection` xoay theo hướng giữa hai tâm node. */
@Composable
private fun Connection(from: Int, to: Int, open: Boolean) {
    val x1 = TowerMapLayout.centerX(from).toFloat()
    val y1 = TowerMapLayout.centerY(from).toFloat()
    val x2 = TowerMapLayout.centerX(to).toFloat()
    val y2 = TowerMapLayout.centerY(to).toFloat()
    val length = hypot(x2 - x1, y2 - y1)
    val angle = Math.toDegrees(atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())).toFloat()
    val midX = (x1 + x2) / 2
    val midY = (y1 + y2) / 2
    ArtImage(
        res = R.drawable.map_floor_connection,
        maxEdge = ArtSizes.PIECE,
        trim = true,
        alpha = if (open) 1f else 0.45f,
        // Giữ đúng tỉ lệ ảnh (~3.85:1 sau khi bỏ lề), không kéo méo xích.
        modifier = Modifier.at(midX - length / 2, midY - length / CHAIN_RATIO / 2, length, length / CHAIN_RATIO)
            .rotate(angle),
    )
}

@Composable
private fun FloorNode(floor: Floor, status: FloorStatus, isSelected: Boolean, onClick: () -> Unit) {
    val cx = TowerMapLayout.centerX(floor.number)
    val cy = TowerMapLayout.centerY(floor.number)
    val half = TowerMapLayout.NODE / 2
    val isBoss = floor.kind == NodeKind.BOSS

    if (status == FloorStatus.CURRENT) {
        val transition = rememberInfiniteTransition(label = "tower-glow")
        val pulse by transition.animateFloat(
            initialValue = 0.55f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
            label = "tower-glow-alpha",
        )
        ArtImage(R.drawable.map_floor_current_glow, maxEdge = ArtSizes.PIECE, trim = true, alpha = pulse,
            modifier = Modifier.at(cx - 105, cy - 105, 210, 210))
    }

    val nodeRes = when {
        status == FloorStatus.CLEARED -> R.drawable.map_floor_node_complete
        status == FloorStatus.LOCKED -> R.drawable.map_floor_node_locked
        isBoss -> R.drawable.map_floor_node_boss
        else -> R.drawable.map_floor_node
    }
    val description = if (status == FloorStatus.LOCKED) {
        stringResource(R.string.tower_floor_label, floor.number) + " · " + stringResource(R.string.tower_status_locked)
    } else {
        stringResource(R.string.tower_floor_label, floor.number)
    }
    ArtImage(
        res = nodeRes,
        maxEdge = ArtSizes.PIECE,
        trim = true,
        contentDescription = description,
        modifier = Modifier.at(cx - half, cy - half, TowerMapLayout.NODE, TowerMapLayout.NODE)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
    )
    run {
        DesignText(
            text = floor.number.toString(),
            size = 40,
            weight = FontWeight.Bold,
            color = if (status == FloorStatus.CURRENT) GothicColors.Gold else GothicColors.Parchment,
            maxLines = 1,
            modifier = Modifier.at(cx - 50, cy - 30, 100, 60),
        )
    }
    if (isBoss) {
        ArtImage(R.drawable.badge_boss, maxEdge = ArtSizes.PIECE, trim = true,
            modifier = Modifier.at(cx - 60, cy - half - 46, 120, 60))
    }
    if (isSelected) {
        ArtImage(R.drawable.map_crown_marker, maxEdge = ArtSizes.ICON, trim = true,
            modifier = Modifier.at(cx + half - 30, cy - half - 40, 80, 80))
    }
}

private fun kindLabelRes(kind: NodeKind): Int = when (kind) {
    NodeKind.BATTLE -> R.string.tower_kind_battle
    NodeKind.STORY -> R.string.tower_kind_story
    NodeKind.TREASURE -> R.string.tower_kind_treasure
    NodeKind.PUZZLE -> R.string.tower_kind_puzzle
    NodeKind.BOSS -> R.string.tower_kind_boss
    NodeKind.SECRET -> R.string.tower_kind_secret
}

/** Dải bản đồ cuộn được nằm giữa tiêu đề và nút Vào tầng. */
private const val MAP_TOP = 210
private const val MAP_HEIGHT = 1390
private const val CHAIN_RATIO = 3.85f

// Preview functions for Android Studio
@Preview(name = "Floor Preview - Boss Floor", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun FloorPreviewBossPreview() {
    val sampleFloor = TowerCatalog.build(puzzleCount = 0)[20] // Floor 21 - boss floor
    FloorPreview(
        floor = sampleFloor,
        status = FloorStatus.CURRENT,
        modifier = Modifier.size(400.dp, 800.dp),
    )
}

@Preview(name = "Floor Preview - Cleared", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun FloorPreviewClearedPreview() {
    val sampleFloor = TowerCatalog.build(puzzleCount = 0)[4] // Floor 5
    FloorPreview(
        floor = sampleFloor,
        status = FloorStatus.CLEARED,
        modifier = Modifier.size(400.dp, 800.dp),
    )
}

@Preview(name = "Floor Preview - Locked", showBackground = true, widthDp = 400, heightDp = 800)
@Composable
private fun FloorPreviewLockedPreview() {
    val sampleFloor = TowerCatalog.build(puzzleCount = 0)[29] // Floor 30
    FloorPreview(
        floor = sampleFloor,
        status = FloorStatus.LOCKED,
        modifier = Modifier.size(400.dp, 800.dp),
    )
}
