package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.campaign.TowerProgress
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
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
    private val CENTER_X = intArrayOf(495, 725, 495, 265)

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
 * Chạm node để chọn tầng (đánh dấu bằng `map_crown_marker`), bấm Vào tầng để chơi. Tầng khóa
 * không chọn được và giấu tên.
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

        Box(modifier = Modifier.at(0, MAP_TOP, 1080, MAP_HEIGHT).verticalScroll(scroll)) {
            Box(modifier = Modifier.fillMaxWidth().height(d.dp(TowerMapLayout.contentHeight))) {
                ArtImage(R.drawable.map_tower_emblem, maxEdge = ArtSizes.PIECE, trim = true,
                    modifier = Modifier.at(400, 20, 280, 200))
                // Đường nối vẽ trước: luôn nằm sau node.
                for (n in 1 until floors.size) {
                    Connection(from = n, to = n + 1, open = progress.statusOf(n) == FloorStatus.CLEARED)
                }
                floors.forEach { floor ->
                    FloorNode(
                        floor = floor,
                        status = progress.statusOf(floor.number),
                        isSelected = floor.number == selected,
                        onClick = { selected = floor.number },
                    )
                }
            }
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
        GothicButton(
            label = stringResource(R.string.tower_enter),
            onClick = { selectedFloor?.let(onFloorClick) },
            enabled = selectedFloor != null && progress.statusOf(selected) != FloorStatus.LOCKED,
            fontSize = d.sp(40),
            modifier = Modifier.at(150, 1650, 780, 110),
        )
    }
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
        stringResource(R.string.tower_status_locked)
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
            // Tầng khóa không chạm được.
            .then(if (status == FloorStatus.LOCKED) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick)),
    )
    if (status != FloorStatus.LOCKED) {
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
