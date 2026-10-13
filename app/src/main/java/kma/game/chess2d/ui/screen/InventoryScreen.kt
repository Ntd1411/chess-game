package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Relic
import kma.game.chess2d.campaign.Reward
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

internal data class InventoryEntry(
    @DrawableRes val icon: Int,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val count: Int?,
    val unlocked: Boolean,
    val unlockFloor: Int,
)

internal object InventoryLayout {
    const val MIN_SLOT_DP = 72
    const val GAP_DP = 10
    fun columnsFor(widthDp: Float): Int =
        ((widthDp + GAP_DP) / (MIN_SLOT_DP + GAP_DP)).toInt().coerceIn(1, 4)
}

internal fun inventoryEntries(wallet: Reward, clearedUpTo: Int): List<InventoryEntry> = listOf(
    InventoryEntry(R.drawable.item_gold_coin, R.string.item_gold_name, R.string.item_gold_desc, wallet.gold, true, 0),
    InventoryEntry(R.drawable.item_blue_crystal, R.string.item_crystal_name, R.string.item_crystal_desc, wallet.crystals, true, 0),
    InventoryEntry(R.drawable.item_ancient_book, R.string.item_book_name, R.string.item_book_desc, wallet.books, true, 0),
) + Relic.entries.map { relic ->
    val art = relicArt(relic)
    InventoryEntry(art.icon, art.name, art.description, null, relic.isUnlocked(clearedUpTo), relic.unlockFloor)
}

/** Drawn, equal square slots preserve wallet and relic progress without a fixed design canvas. */
@Composable
fun InventoryScreen(wallet: Reward, clearedUpTo: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val entries = inventoryEntries(wallet, clearedUpTo)
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val selectedIndex = selected.coerceIn(entries.indices)
    val current = entries[selectedIndex]
    SimpleSetupPage(background = R.drawable.bg_inventory,
        title = stringResource(R.string.inventory_title), backLabel = stringResource(R.string.action_back),
        onBack = onBack, modifier = modifier,
        footer = { SimpleSetupButton(stringResource(R.string.action_back), onBack, modifier = Modifier.fillMaxWidth()) }) {
        Text(stringResource(R.string.inventory_hint), color = GothicColors.Muted,
            fontFamily = GameFontFamily, fontSize = 14.sp)
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = InventoryLayout.columnsFor(maxWidth.value)
            Column(modifier = Modifier.fillMaxWidth().selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(InventoryLayout.GAP_DP.dp)) {
                entries.withIndex().toList().chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(InventoryLayout.GAP_DP.dp)) {
                        row.forEach { (index, entry) ->
                            InventorySlot(entry, selected = index == selectedIndex,
                                onClick = { selected = index }, modifier = Modifier.weight(1f))
                        }
                        repeat(columns - row.size) { Box(Modifier.weight(1f).aspectRatio(1f)) }
                    }
                }
            }
        }
        SimpleSetupCard {
            Text(if (current.unlocked) stringResource(current.name) else stringResource(R.string.tower_floor_locked_title),
                color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(if (current.unlocked) stringResource(current.description)
                else stringResource(R.string.inventory_locked, current.unlockFloor),
                color = GothicColors.Parchment, fontFamily = GameFontFamily, fontSize = 15.sp, lineHeight = 22.sp)
            current.count?.let {
                Text(stringResource(R.string.inventory_count, it), color = GothicColors.Gold,
                    fontFamily = GameFontFamily, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun InventorySlot(entry: InventoryEntry, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = modifier.aspectRatio(1f).clip(shape)
        .background(if (selected) GothicColors.Gold.copy(alpha = 0.15f) else GothicColors.Ink.copy(alpha = 0.88f))
        .border(if (selected) 2.dp else 1.dp, GothicColors.Gold.copy(alpha = if (selected) 1f else 0.3f), shape)
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        contentAlignment = Alignment.Center) {
        if (entry.unlocked) {
            ArtImage(entry.icon, maxEdge = ArtSizes.PIECE, trim = true,
                contentDescription = stringResource(entry.name), modifier = Modifier.fillMaxSize().padding(14.dp))
            entry.count?.let {
                Text(stringResource(R.string.inventory_count, it),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
                        .background(GothicColors.Ink.copy(alpha = 0.8f), RoundedCornerShape(6.dp)).padding(horizontal = 4.dp),
                    color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 12.sp)
            }
        } else {
            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.inventory_locked, entry.unlockFloor),
                tint = GothicColors.Muted)
        }
    }
}

internal class RelicArt(@DrawableRes val icon: Int, @StringRes val name: Int, @StringRes val description: Int)

internal fun relicArt(relic: Relic): RelicArt = when (relic) {
    Relic.ANCIENT_KEY -> RelicArt(R.drawable.item_ancient_key, R.string.relic_ancient_key_name, R.string.relic_ancient_key_desc)
    Relic.SCROLL -> RelicArt(R.drawable.item_scroll, R.string.relic_scroll_name, R.string.relic_scroll_desc)
    Relic.POTION -> RelicArt(R.drawable.item_potion, R.string.relic_potion_name, R.string.relic_potion_desc)
    Relic.MEMORY_CRYSTAL -> RelicArt(R.drawable.item_memory_crystal, R.string.relic_memory_crystal_name, R.string.relic_memory_crystal_desc)
    Relic.BROKEN_PAWN -> RelicArt(R.drawable.item_broken_pawn_relic, R.string.relic_broken_pawn_name, R.string.relic_broken_pawn_desc)
    Relic.CHESS_FRAGMENT -> RelicArt(R.drawable.item_chess_fragment, R.string.relic_chess_fragment_name, R.string.relic_chess_fragment_desc)
    Relic.WHITE_ROSE -> RelicArt(R.drawable.item_white_rose, R.string.relic_white_rose_name, R.string.relic_white_rose_desc)
    Relic.RED_CRYSTAL -> RelicArt(R.drawable.item_red_crystal, R.string.relic_red_crystal_name, R.string.relic_red_crystal_desc)
    Relic.MAGIC_TOKEN -> RelicArt(R.drawable.item_magic_token, R.string.relic_magic_token_name, R.string.relic_magic_token_desc)
    Relic.BLACK_ROSE -> RelicArt(R.drawable.item_black_rose, R.string.relic_black_rose_name, R.string.relic_black_rose_desc)
    Relic.MAGIC_ORB -> RelicArt(R.drawable.item_magic_orb, R.string.relic_magic_orb_name, R.string.relic_magic_orb_desc)
    Relic.GOLDEN_FRAGMENT -> RelicArt(R.drawable.item_golden_chess_fragment, R.string.relic_golden_fragment_name, R.string.relic_golden_fragment_desc)
    Relic.TOWER_CROWN -> RelicArt(R.drawable.icon_crown, R.string.relic_tower_crown_name, R.string.relic_tower_crown_desc)
}
