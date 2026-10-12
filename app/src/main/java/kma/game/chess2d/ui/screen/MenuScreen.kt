package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.PanelTone
import kma.game.chess2d.ui.art.quietPanel
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Khung bố trí sảnh chính theo bản thiết kế 1080x1920 (đơn vị thiết kế, không phải dp).
 * Tách ra để test kiểm được: nút không chồng nhau và nằm trong vùng an toàn.
 */
internal object LobbyLayout {
    data class Box(val x: Int, val y: Int, val w: Int, val h: Int) {
        val right: Int get() = x + w
        val bottom: Int get() = y + h
        fun overlaps(o: Box): Boolean = x < o.right && o.x < right && y < o.bottom && o.y < bottom
    }

    val Tower = Box(140, 1240, 800, 112)
    val VsComputer = Box(140, 1365, 800, 108)
    val TwoPlayers = Box(140, 1487, 390, 100)
    val Lan = Box(550, 1487, 390, 100)
    val Buttons = listOf(Tower, VsComputer, TwoPlayers, Lan)

    /** Cột icon bên phải: Nhật ký, Túi đồ, Cài đặt, Lịch sử, Chọn chế độ. */
    val IconColumn = listOf(260, 380, 500, 620, 740).map { Box(900, it, 100, 100) }

    /** Thanh nền tối gom năm icon thành một nhóm thay vì rải rời trên nền. */
    val IconRail = Box(884, 244, 132, 612)

    /** Ví (vàng + pha lê) gom trong một ô nhỏ ở góc phải. */
    val Wallet = Box(670, 52, 230, 142)

    const val SAFE_MARGIN = 48
}

/**
 * Sảnh chính theo bản thiết kế: avatar + tên + cấp ở góc trái, vàng và pha lê ở góc phải,
 * nhân vật toàn thân ở giữa (chạm để mở Hồ sơ), cột icon bên phải và bốn nút chế độ ở dưới.
 *
 * Máy vs Máy nằm trong màn Chọn chế độ (icon menu ở cột phải) để sảnh chỉ có đúng bốn nút
 * như thiết kế.
 */
@Composable
fun MenuScreen(
    name: String,
    level: Int,
    gold: Int,
    crystals: Int,
    onNameChange: (String) -> Unit,
    onOpenTower: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenInventory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val profileLabel = stringResource(R.string.profile_open)

    DesignCanvas(background = R.drawable.bg_main_menu, dim = 0.15f, modifier = modifier) {
        // Chỉ giữ nến hai bên, nằm ngoài vùng nút (nút từ x=140 đến 940); bỏ hoa văn trang trí để
        // sảnh bớt rối.
        ArtImage(R.drawable.decor_main_menu_candle, maxEdge = ArtSizes.PIECE, trim = true,
            modifier = Modifier.at(20, 1010, 100, 200))
        ArtImage(R.drawable.decor_main_menu_candle, maxEdge = ArtSizes.PIECE, trim = true,
            modifier = Modifier.at(960, 1010, 100, 200))

        // Hạt sáng phía sau nhân vật, không phủ chữ.
        ArtImage(R.drawable.decor_main_menu_particles, maxEdge = ArtSizes.BOARD, trim = true,
            alpha = 0.6f, modifier = Modifier.at(220, 340, 640, 820))
        ArtImage(
            res = R.drawable.character_protagonist_fullbody,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            contentDescription = profileLabel,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(160, 300, 760, 900).clickable(role = Role.Button, onClick = onOpenProfile),
        )

        // Avatar + tên + cấp (chạm để mở Hồ sơ).
        Box(modifier = Modifier.at(48, 56, 112, 112).clip(CircleShape).clickable(role = Role.Button, onClick = onOpenProfile)) {
            PortraitInFrame(R.drawable.portrait_protagonist, contentDescription = profileLabel)
        }
        Column(modifier = Modifier.at(180, 58, 330, 100).clickable(onClick = onOpenProfile)) {
            DesignText(
                text = name.ifBlank { stringResource(R.string.match_player_one) },
                size = 40,
                weight = FontWeight.Bold,
                color = GothicColors.Gold,
                align = TextAlign.Start,
                maxLines = 1,
            )
            DesignText(
                text = stringResource(R.string.lobby_level, level),
                size = 30,
                align = TextAlign.Start,
                maxLines = 1,
            )
        }

        // Ví: vàng và pha lê suy ra từ tiến độ Tháp.
        val wallet = LobbyLayout.Wallet
        Box(modifier = Modifier.at(wallet.x, wallet.y, wallet.w, wallet.h).quietPanel(PanelTone.SUNKEN))
        ArtImage(R.drawable.item_gold_coin, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.at(690, 65, 56, 56))
        DesignText(gold.toString(), size = 36, weight = FontWeight.Bold, color = GothicColors.Gold,
            align = TextAlign.Start, alignment = Alignment.CenterStart, maxLines = 1,
            modifier = Modifier.at(750, 60, 130, 60))
        ArtImage(R.drawable.item_blue_crystal, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.at(690, 130, 56, 56))
        DesignText(crystals.toString(), size = 36, weight = FontWeight.Bold,
            align = TextAlign.Start, alignment = Alignment.CenterStart, maxLines = 1,
            modifier = Modifier.at(750, 125, 130, 60))

        // Cột icon bên phải, gom trên một thanh nền tối.
        val rail = LobbyLayout.IconRail
        Box(modifier = Modifier.at(rail.x, rail.y, rail.w, rail.h).quietPanel(PanelTone.SUNKEN))
        val icons = listOf(
            Triple(R.drawable.icon_journal, R.string.journal_open, onOpenJournal),
            Triple(R.drawable.icon_inventory, R.string.inventory_open, onOpenInventory),
            Triple(R.drawable.icon_settings, R.string.lobby_settings) { showSettings = true },
            Triple(R.drawable.icon_battle_history, R.string.history_open, onOpenHistory),
            Triple(R.drawable.icon_menu, R.string.modes_open, onOpenModes),
        )
        icons.forEachIndexed { i, (icon, label, action) ->
            val box = LobbyLayout.IconColumn[i]
            ArtImage(
                res = icon,
                maxEdge = ArtSizes.ICON,
                trim = true,
                contentDescription = stringResource(label),
                modifier = Modifier.at(box.x, box.y, box.w, box.h).clip(CircleShape)
                    .clickable(role = Role.Button, onClick = action),
            )
        }

        MenuButton(LobbyLayout.Tower, stringResource(R.string.menu_tower), ButtonArt.PRIMARY, onOpenTower)
        MenuButton(LobbyLayout.VsComputer, stringResource(R.string.menu_vs_ai), ButtonArt.SECONDARY, onPlayComputer)
        MenuButton(LobbyLayout.TwoPlayers, stringResource(R.string.menu_local), ButtonArt.SMALL, onPlayTwoPlayers)
        MenuButton(LobbyLayout.Lan, stringResource(R.string.menu_lan), ButtonArt.SMALL, onPlayLan)
    }

    if (showSettings) {
        SettingsPanel(
            name = name,
            onNameChange = onNameChange,
            onDismiss = { showSettings = false },
        )
    }
}

@Composable
private fun MenuButton(box: LobbyLayout.Box, label: String, art: ButtonArt, onClick: () -> Unit) {
    GothicButton(
        label = label,
        onClick = onClick,
        art = art,
        fontSize = LocalDesign.current.sp(if (art == ButtonArt.SMALL) 30 else 38),
        modifier = Modifier.at(box.x, box.y, box.w, box.h),
    )
}

/**
 * Chân dung vuông trong khung tròn `ui_character_frame`: khung rỗng giữa nên chân dung nằm
 * dưới, co vào ~82% ô (tương ứng 92/112 của bản thiết kế) và cắt tròn cho khớp lỗ khung.
 */
@Composable
fun PortraitInFrame(
    @DrawableRes portrait: Int?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    dimmed: Boolean = false,
    @DrawableRes frame: Int = R.drawable.ui_character_frame,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (portrait != null) {
            ArtImage(
                res = portrait,
                maxEdge = ArtSizes.PORTRAIT,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                alpha = if (dimmed) 0.35f else 1f,
                // Lề theo tỉ lệ của bản thiết kế: (112 - 92) / 2 / 112 ≈ 9% mỗi bên.
                modifier = Modifier.fillMaxSize().padding(maxWidth * 0.09f).clip(CircleShape),
            )
        }
        ArtImage(res = frame, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.fillMaxSize())
    }
}
