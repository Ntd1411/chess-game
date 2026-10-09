package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Relic
import kma.game.chess2d.campaign.Reward
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Một ô trong túi: ảnh icon nhỏ (`item_*`, không phải `prop_*`), tên, mô tả, số lượng. */
private class InventoryEntry(
    @DrawableRes val icon: Int,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val count: Int?,
    val unlocked: Boolean,
    val unlockFloor: Int,
)

/** Lưới 4x4 của bản thiết kế: ô 205, khe 22, căn giữa panel (48..1032). Hàm thuần để test. */
internal object InventoryLayout {
    const val COLUMNS = 4
    const val ROWS = 4
    const val SLOT = 205
    const val GAP = 22
    const val TOP = 240
    val LEFT = 48 + (984 - (COLUMNS * SLOT + (COLUMNS - 1) * GAP)) / 2

    fun slotX(index: Int): Int = LEFT + (index % COLUMNS) * (SLOT + GAP)
    fun slotY(index: Int): Int = TOP + (index / COLUMNS) * (SLOT + GAP)
}

/**
 * Túi đồ theo bản thiết kế: nền `bg_inventory`, panel lớn, lưới 4x4 `ui_inventory_slot`, ô khóa dùng
 * `ui_inventory_slot_locked`, mô tả món đang chọn trong `ui_tooltip`.
 *
 * Ba ô đầu là vàng, pha lê và sách cổ (suy ra từ thưởng các tầng đã qua), sau đó là các di vật
 * mở theo tầng ([Relic]).
 */
@Composable
fun InventoryScreen(
    wallet: Reward,
    clearedUpTo: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = listOf(
        InventoryEntry(R.drawable.item_gold_coin, R.string.item_gold_name, R.string.item_gold_desc, wallet.gold, true, 0),
        InventoryEntry(R.drawable.item_blue_crystal, R.string.item_crystal_name, R.string.item_crystal_desc, wallet.crystals, true, 0),
        InventoryEntry(R.drawable.item_ancient_book, R.string.item_book_name, R.string.item_book_desc, wallet.books, true, 0),
    ) + Relic.entries.map { relic ->
        val art = relicArt(relic)
        InventoryEntry(art.icon, art.name, art.description, null, relic.isUnlocked(clearedUpTo), relic.unlockFloor)
    }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    DesignCanvas(background = R.drawable.bg_inventory, dim = 0.3f, modifier = modifier) {
        val d = LocalDesign.current
        SubScreenHeader(title = stringResource(R.string.inventory_title), onBack = onBack)
        SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(48, 190, 984, 1500))

        entries.take(InventoryLayout.COLUMNS * InventoryLayout.ROWS).forEachIndexed { i, entry ->
            val x = InventoryLayout.slotX(i)
            val y = InventoryLayout.slotY(i)
            Box(
                modifier = Modifier.at(x, y, InventoryLayout.SLOT, InventoryLayout.SLOT)
                    .clickable(role = Role.Button) { selected = i },
                contentAlignment = Alignment.Center,
            ) {
                if (entry.unlocked) {
                    ArtImage(R.drawable.ui_inventory_slot, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.fillMaxSize())
                    ArtImage(
                        res = entry.icon,
                        maxEdge = ArtSizes.PIECE,
                        trim = true,
                        contentDescription = stringResource(entry.name),
                        modifier = Modifier.fillMaxSize().padding(d.dp(45)),
                    )
                    if (entry.count != null) {
                        DesignText(
                            text = stringResource(R.string.inventory_count, entry.count),
                            size = 30,
                            weight = FontWeight.Bold,
                            color = GothicColors.Gold,
                            alignment = Alignment.BottomEnd,
                            maxLines = 1,
                            modifier = Modifier.fillMaxSize().padding(end = d.dp(28), bottom = d.dp(18)),
                        )
                    }
                } else {
                    ArtImage(R.drawable.ui_inventory_slot_locked, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.fillMaxSize())
                }
                if (i == selected) {
                    ArtImage(R.drawable.fx_move_highlight, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.fillMaxSize())
                }
            }
        }

        val current = entries.getOrNull(selected)
        SlicedArt(res = R.drawable.ui_tooltip, slice = 0.5f, modifier = Modifier.at(90, 1170, 900, 300))
        if (current != null) {
            Column(
                modifier = Modifier.at(90, 1170, 900, 300).padding(horizontal = d.dp(120), vertical = d.dp(40)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DesignText(
                    text = if (current.unlocked) stringResource(current.name) else stringResource(R.string.tower_floor_locked_title),
                    size = 40,
                    color = GothicColors.Gold,
                    weight = FontWeight.Bold,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                DesignText(
                    text = if (current.unlocked) {
                        stringResource(current.description)
                    } else {
                        stringResource(R.string.inventory_locked, current.unlockFloor)
                    },
                    size = 28,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        DesignText(
            text = stringResource(R.string.inventory_hint),
            size = 28,
            italic = true,
            align = TextAlign.Center,
            modifier = Modifier.at(90, 1500, 900, 80),
        )
    }
}

private class RelicArt(@DrawableRes val icon: Int, @StringRes val name: Int, @StringRes val description: Int)

private fun relicArt(relic: Relic): RelicArt = when (relic) {
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
