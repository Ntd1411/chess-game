package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.*
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.theme.GothicColors

internal object TowerMapLayout {
    const val NODE_DP = 56
    const val ROW_DP = 96
    fun displayOrder(floors: List<Floor>) = floors.sortedByDescending { it.number }
    fun initialIndex(floors: List<Floor>, number: Int) = displayOrder(floors).indexOfFirst { it.number == number }.coerceAtLeast(0)
    fun canEnter(floor: Floor?, progress: TowerProgress) = floor != null && progress.statusOf(floor.number) != FloorStatus.LOCKED
}

@Composable
fun TowerMapScreen(floors: List<Floor>, progress: TowerProgress, onFloorClick: (Floor) -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var selected by rememberSaveable(progress.currentFloor) { mutableIntStateOf(progress.currentFloor) }
    val floor = floors.firstOrNull { it.number == selected }
    val ordered = remember(floors) { TowerMapLayout.displayOrder(floors) }
    val list = rememberLazyListState(initialFirstVisibleItemIndex = TowerMapLayout.initialIndex(floors, progress.currentFloor))
    LaunchedEffect(progress.currentFloor) { list.scrollToItem(TowerMapLayout.initialIndex(floors, progress.currentFloor)) }
    Box(modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.bg_tower_map, dim = 0.6f)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            SimpleSetupHeader(stringResource(R.string.tower_map_title, progress.currentFloor, TowerCatalog.FLOOR_COUNT), stringResource(R.string.action_back), onBack, titleSize = 16.sp)
            Text(stringResource(R.string.tower_progress, progress.clearedUpTo, TowerCatalog.FLOOR_COUNT), color = GothicColors.Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.weight(1f).widthIn(max = 760.dp).fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyColumn(state = list, modifier = Modifier.weight(0.36f).selectableGroup(), horizontalAlignment = Alignment.CenterHorizontally) {
                    items(ordered, key = { it.number }) { node ->
                        val status = progress.statusOf(node.number)
                        Column(Modifier.fillMaxWidth().heightIn(min = TowerMapLayout.ROW_DP.dp)
                            .selectable(node.number == selected, role = Role.RadioButton, onClick = { selected = node.number }), horizontalAlignment = Alignment.CenterHorizontally) {
                            val active = node.number == selected
                            Box(Modifier.size(TowerMapLayout.NODE_DP.dp).clip(CircleShape)
                                .background(if (active) GothicColors.Gold.copy(alpha = 0.2f) else GothicColors.Ink.copy(alpha = 0.9f))
                                .border(if (active) 2.dp else 1.dp, if (active || status == FloorStatus.CURRENT) GothicColors.Gold else GothicColors.Gold.copy(alpha = 0.35f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(node.number.toString(), color = if (status == FloorStatus.LOCKED) GothicColors.Muted else GothicColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(stringResource(when {
                                status == FloorStatus.LOCKED -> R.string.tower_status_locked
                                status == FloorStatus.CLEARED -> R.string.tower_preview_cleared
                                node.kind == NodeKind.BOSS -> R.string.tower_kind_boss
                                else -> R.string.tower_kind_puzzle
                            }), color = GothicColors.Muted, fontSize = 11.sp)
                            Box(Modifier.width(1.dp).height(16.dp).background(GothicColors.Gold.copy(alpha = 0.25f)))
                        }
                    }
                }
                if (floor != null) key(floor.number) {
                    Column(Modifier.weight(0.64f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        FloorPreview(floor, progress.statusOf(floor.number))
                    }
                }
            }
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SimpleSetupButton(stringResource(R.string.tower_enter), { if (TowerMapLayout.canEnter(floor, progress)) floor?.let(onFloorClick) }, Modifier.fillMaxWidth(), primary = true, enabled = TowerMapLayout.canEnter(floor, progress))
                SimpleSetupButton(stringResource(R.string.action_back), onBack, Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun FloorPreview(floor: Floor, status: FloorStatus) {
    val boss = TowerBosses.character(floor)
    val reward = TowerRewards.forFloor(floor)
    SimpleSetupCard {
        Text(stringResource(R.string.tower_floor_label, floor.number), color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(floor.title, color = GothicColors.Parchment, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Text(stringResource(kindLabelRes(floor.kind)), color = GothicColors.Muted, fontSize = 13.sp)
        if (boss != null) {
            ArtImage(boss.portrait, modifier = Modifier.size(80.dp).clip(CircleShape).align(Alignment.CenterHorizontally))
            Text(stringResource(boss.name), color = GothicColors.Gold, fontSize = 15.sp)
            Text(stringResource(R.string.tower_preview_strength, TowerCatalog.chapterOf(floor.number)), color = GothicColors.Muted, fontSize = 13.sp)
        } else floor.estimatedElo?.let { Text(stringResource(R.string.tower_puzzle_elo, it), color = GothicColors.Gold, fontSize = 14.sp) }
        Text(floor.teaser, color = GothicColors.Parchment, fontStyle = FontStyle.Italic, fontSize = 15.sp)
        Text(stringResource(R.string.tower_preview_rewards), color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(stringResource(R.string.tower_preview_reward_values, reward.gold, reward.crystals, reward.books), color = GothicColors.Parchment, fontSize = 14.sp)
        Relic.entries.firstOrNull { it.unlockFloor == floor.number }?.let {
            Text(stringResource(R.string.tower_preview_relic, stringResource(relicArt(it).name)), color = GothicColors.Gold, fontSize = 14.sp)
        }
        Text(stringResource(when (status) {
            FloorStatus.LOCKED -> R.string.tower_preview_locked
            FloorStatus.CLEARED -> R.string.tower_preview_cleared
            FloorStatus.CURRENT -> R.string.tower_preview_ready
        }), color = GothicColors.Muted, fontSize = 13.sp)
    }
}

internal fun kindLabelRes(kind: NodeKind): Int = when (kind) {
    NodeKind.BATTLE -> R.string.tower_kind_battle
    NodeKind.STORY -> R.string.tower_kind_story
    NodeKind.TREASURE -> R.string.tower_kind_treasure
    NodeKind.PUZZLE -> R.string.tower_kind_puzzle
    NodeKind.BOSS -> R.string.tower_kind_boss
    NodeKind.SECRET -> R.string.tower_kind_secret
}
