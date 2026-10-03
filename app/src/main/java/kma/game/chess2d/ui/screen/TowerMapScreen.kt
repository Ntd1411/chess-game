package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Floor
import kma.game.chess2d.campaign.FloorStatus
import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.campaign.TowerProgress
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Bản đồ Tháp Cờ: các tầng xếp theo đường đi từ dưới lên, tầng 1 ở chân tháp.
 *
 * Tầng chưa mở bị khóa và giấu tên, tầng đang chinh phục phát sáng vàng, tầng đã qua
 * vào chơi lại được. Màn này chỉ hiển thị và báo chạm qua [onFloorClick]; việc vào
 * thoại/ván đấu thuộc phía gọi.
 */
@Composable
fun TowerMapScreen(
    floors: List<Floor>,
    progress: TowerProgress,
    onFloorClick: (Floor) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_tower_map),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Phủ tối nhẹ để chữ và node luôn đọc được trên mọi vùng của ảnh nền.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)))

        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            MapHeader(progress = progress, onBack = onBack)

            val listState = rememberLazyListState()
            // reverseLayout: phần tử 0 (tầng 1) nằm sát đáy. Cuộn tới một tầng dưới tầng
            // hiện tại để người chơi thấy cả đoạn đường vừa đi lẫn đoạn sắp tới.
            LaunchedEffect(progress.currentFloor) {
                listState.scrollToItem((progress.currentFloor - 2).coerceAtLeast(0))
            }

            LazyColumn(
                state = listState,
                reverseLayout = true,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 24.dp),
                modifier = Modifier.weight(1f).fillMaxWidth(),
            ) {
                items(floors, key = { it.number }) { floor ->
                    val status = progress.statusOf(floor.number)
                    FloorRow(
                        floor = floor,
                        status = status,
                        isTop = floor.number == TowerCatalog.FLOOR_COUNT,
                        onClick = { onFloorClick(floor) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MapHeader(progress: TowerProgress, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.icon_back),
            contentDescription = stringResource(R.string.tower_back),
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
        )
        Text(
            text = stringResource(
                R.string.tower_map_title,
                progress.currentFloor,
                TowerCatalog.FLOOR_COUNT,
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GothicColors.Gold,
        )
    }
}

/**
 * Một tầng: node tròn kèm tên. Đoạn nối phía trên node dẫn lên tầng kế (tầng cao hơn
 * nằm trên màn hình), sáng vàng khi tầng này đã qua vì lúc đó đường lên đã mở.
 */
@Composable
private fun FloorRow(floor: Floor, status: FloorStatus, isTop: Boolean, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isTop) {
            val connector = if (status == FloorStatus.CLEARED) GothicColors.Gold else GothicColors.Locked
            Box(
                modifier = Modifier
                    .padding(start = 31.dp)
                    .width(3.dp)
                    .height(24.dp)
                    .background(connector),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FloorNode(floor = floor, status = status, onClick = onClick)
            FloorLabel(floor = floor, status = status)
        }
    }
}

@Composable
private fun FloorNode(floor: Floor, status: FloorStatus, onClick: () -> Unit) {
    // Luôn dựng transition cho đơn giản; chỉ tầng hiện tại mới dùng giá trị nhấp nháy.
    val transition = rememberInfiniteTransition(label = "tower-glow")
    val pulse by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 900), RepeatMode.Reverse),
        label = "tower-glow-alpha",
    )
    val ring = when (status) {
        FloorStatus.CURRENT -> GothicColors.Gold.copy(alpha = pulse)
        FloorStatus.CLEARED -> GothicColors.Gold.copy(alpha = 0.6f)
        FloorStatus.LOCKED -> GothicColors.Locked
    }
    val kindLabel = stringResource(kindLabelRes(floor.kind))
    val description = if (status == FloorStatus.LOCKED) {
        stringResource(R.string.tower_status_locked)
    } else {
        kindLabel
    }

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(GothicColors.Ink.copy(alpha = 0.85f))
            .border(if (status == FloorStatus.CURRENT) 4.dp else 2.dp, ring, CircleShape)
            // Tầng khóa không chạm được: không có onClick thì cũng không có hiệu ứng nhấn.
            .then(if (status == FloorStatus.LOCKED) Modifier else Modifier.clickable(onClick = onClick)),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(
                if (status == FloorStatus.LOCKED) R.drawable.icon_locked else kindIconRes(floor.kind),
            ),
            contentDescription = description,
            modifier = Modifier.size(36.dp),
        )
    }
}

@Composable
private fun FloorLabel(floor: Floor, status: FloorStatus) {
    val locked = status == FloorStatus.LOCKED
    Column {
        Text(
            text = stringResource(R.string.tower_floor_label, floor.number),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (status == FloorStatus.CURRENT) GothicColors.Gold else GothicColors.Parchment,
        )
        // Tầng chưa mở giấu cả tên lẫn loại để giữ bí ẩn, nhất là tầng bí mật.
        Text(
            text = if (locked) stringResource(R.string.tower_floor_locked_title) else floor.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (locked) GothicColors.Locked else GothicColors.Parchment,
        )
        if (!locked) {
            Text(
                text = stringResource(kindLabelRes(floor.kind)),
                style = MaterialTheme.typography.bodySmall,
                color = GothicColors.Parchment.copy(alpha = 0.7f),
            )
        }
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

/** Icon dùng lại từ bộ asset đã có trong `res/drawable`. */
private fun kindIconRes(kind: NodeKind): Int = when (kind) {
    NodeKind.BATTLE -> R.drawable.piece_black_knight
    NodeKind.STORY -> R.drawable.icon_journal
    NodeKind.TREASURE -> R.drawable.item_golden_chess_fragment
    NodeKind.PUZZLE -> R.drawable.item_magic_chess_token
    NodeKind.BOSS -> R.drawable.item_tower_crown
    NodeKind.SECRET -> R.drawable.icon_memory
}
