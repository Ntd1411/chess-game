package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import kma.game.chess2d.ui.art.ArtBar
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtPressable
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Khung bố trí sảnh chính theo bản thiết kế 1080x1920 (đơn vị thiết kế, không phải dp).
 * Tách ra để test kiểm được: thẻ không chồng nhau và nằm trong vùng an toàn.
 */
internal object LobbyLayout {
    data class Box(val x: Int, val y: Int, val w: Int, val h: Int) {
        val right: Int get() = x + w
        val bottom: Int get() = y + h
        fun overlaps(o: Box): Boolean = x < o.right && o.x < right && y < o.bottom && o.y < bottom
    }

    // Góc trái: avatar, tên, thanh kinh nghiệm (canvas 2078x757 ≈ 2,75:1, thanh thật giữa canvas).
    val Avatar = Box(48, 48, 150, 150)
    val Name = Box(212, 52, 463, 62)
    val XpBar = Box(206, 108, 360, 131)

    // Góc phải: hai ô tiền tệ, icon đè đầu trái ô, nút cộng cuối ô vàng.
    val GoldPill = Box(730, 62, 280, 56)
    val CrystalPill = Box(730, 140, 280, 56)

    /** Hai thẻ lớn (Tháp đỏ là lựa chọn chính, Đấu AI xanh), ba thẻ nhỏ bên dưới. */
    val Tower = Box(60, 1210, 470, 310)
    val VsComputer = Box(550, 1210, 470, 310)
    val TwoPlayers = Box(60, 1545, 300, 224)
    val Lan = Box(390, 1545, 300, 224)
    val Spectate = Box(720, 1545, 300, 224)
    val Buttons = listOf(Tower, VsComputer, TwoPlayers, Lan, Spectate)

    /**
     * Sáu nút tròn `frame_side_button` chia hai bên nhân vật: trái là Nhật ký, Túi đồ, Thành tích;
     * phải là Lịch sử, Chọn chế độ, Cài đặt.
     */
    val SideButtons = listOf(54, 918).flatMap { x -> listOf(300, 440, 580).map { y -> Box(x, y, 108, 108) } }

    const val SAFE_MARGIN = 48
}

/** Một thẻ chế độ trên sảnh. */
private class LobbyCard(
    val box: LobbyLayout.Box,
    @DrawableRes val panel: Int,
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val subtitle: Int?,
    val onClick: () -> Unit,
)

/**
 * Sảnh chính: avatar trong `frame_player_avatar` + tên + thanh kinh nghiệm ở góc trái, hai ô tiền
 * `panel_currency_counter` ở góc phải, nhân vật toàn thân đứng trong quầng sáng
 * `decor_main_menu_glow` (chạm để mở Hồ sơ), sáu nút tròn hai bên và các thẻ chế độ bên dưới.
 *
 * Thứ bậc thị giác: Tháp Cờ (thẻ đỏ, có viền góc `decor_mode_card_corners`) là lựa chọn chính;
 * Đấu AI (thẻ xanh) ngang hàng nhưng không nhấn; Hai người / LAN / Máy vs Máy là thẻ nhỏ.
 *
 * @param levelProgress phần đã đi được của cấp hiện tại (0..1), vẽ trên thanh kinh nghiệm.
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
    levelProgress: Float = 0f,
    onOpenSpectate: () -> Unit = onOpenModes,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    val profileLabel = stringResource(R.string.profile_open)

    DesignCanvas(background = R.drawable.bg_main_menu, dim = 0.12f, modifier = modifier) {
        // Nhân vật trong quầng sáng, dưới mọi thành phần khác.
        ArtImage(R.drawable.decor_main_menu_glow, maxEdge = ArtSizes.PIECE, trim = true,
            alpha = 0.75f, modifier = Modifier.at(170, 300, 740, 760))
        ArtImage(
            res = R.drawable.character_protagonist_fullbody,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            contentDescription = profileLabel,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(180, 240, 720, 950).clickable(role = Role.Button, onClick = onOpenProfile),
        )

        PlayerHeader(name = name, level = level, levelProgress = levelProgress, onOpenProfile = onOpenProfile)
        CurrencyPill(LobbyLayout.GoldPill, R.drawable.icon_currency_gold, gold, GothicColors.Gold, onAdd = onOpenInventory)
        CurrencyPill(LobbyLayout.CrystalPill, R.drawable.icon_currency_crystal, crystals, GothicColors.Parchment, onAdd = null)

        val sides = listOf(
            Triple(R.drawable.icon_journal, R.string.journal_open, onOpenJournal),
            Triple(R.drawable.icon_inventory, R.string.inventory_open, onOpenInventory),
            Triple(R.drawable.icon_achievements, R.string.profile_open, onOpenProfile),
            Triple(R.drawable.icon_battle_history, R.string.history_open, onOpenHistory),
            Triple(R.drawable.icon_menu, R.string.modes_open, onOpenModes),
            Triple(R.drawable.icon_settings_gear, R.string.lobby_settings) { showSettings = true },
        )
        sides.forEachIndexed { i, (icon, label, action) ->
            SideButton(LobbyLayout.SideButtons[i], icon, stringResource(label), action)
        }

        val cards = listOf(
            LobbyCard(LobbyLayout.Tower, R.drawable.panel_mode_card_red, R.drawable.icon_story_mode,
                R.string.menu_tower, R.string.mode_story_desc, onOpenTower),
            LobbyCard(LobbyLayout.VsComputer, R.drawable.panel_mode_card_blue, R.drawable.icon_ai_battle,
                R.string.menu_vs_ai, R.string.mode_ai_desc, onPlayComputer),
            LobbyCard(LobbyLayout.TwoPlayers, R.drawable.panel_mode_card_small, R.drawable.icon_local_multiplayer,
                R.string.menu_local, null, onPlayTwoPlayers),
            LobbyCard(LobbyLayout.Lan, R.drawable.panel_mode_card_small, R.drawable.icon_lan_multiplayer,
                R.string.menu_lan, null, onPlayLan),
            LobbyCard(LobbyLayout.Spectate, R.drawable.panel_mode_card_small, R.drawable.icon_ai_vs_ai,
                R.string.spectate_open, null, onOpenSpectate),
        )
        cards.forEachIndexed { i, card -> ModeCardView(card, highlight = i == 0) }
    }

    if (showSettings) {
        SettingsPanel(
            name = name,
            onNameChange = onNameChange,
            onDismiss = { showSettings = false },
        )
    }
}

/** Avatar tròn trong `frame_player_avatar`, tên vàng, thanh kinh nghiệm có chữ cấp ở giữa. */
@Composable
private fun PlayerHeader(name: String, level: Int, levelProgress: Float, onOpenProfile: () -> Unit) {
    val profileLabel = stringResource(R.string.profile_open)
    val a = LobbyLayout.Avatar
    Box(
        modifier = Modifier.at(a.x, a.y, a.w, a.h).clip(CircleShape).clickable(role = Role.Button, onClick = onOpenProfile),
    ) {
        PortraitInFrame(
            R.drawable.portrait_protagonist,
            contentDescription = profileLabel,
            frame = R.drawable.frame_player_avatar,
        )
    }
    val n = LobbyLayout.Name
    DesignText(
        text = name.ifBlank { stringResource(R.string.match_player_one) },
        size = 40,
        weight = FontWeight.Bold,
        color = GothicColors.Gold,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(n.x, n.y, n.w, n.h).clickable(onClick = onOpenProfile),
    )
    val bar = LobbyLayout.XpBar
    ArtBar(
        frame = R.drawable.bar_player_xp_empty,
        fill = R.drawable.bar_player_xp_fill,
        fraction = levelProgress,
        fillOnTop = true,
        modifier = Modifier.at(bar.x, bar.y, bar.w, bar.h),
    )
    DesignText(
        text = stringResource(R.string.lobby_level, level),
        size = 24,
        weight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier.at(bar.x, bar.y + 40, bar.w, 56),
    )
}

/** Một ô tiền tệ: nền `panel_currency_counter`, icon đè đầu trái, số ở giữa, nút cộng tùy chọn. */
@Composable
private fun CurrencyPill(box: LobbyLayout.Box, @DrawableRes icon: Int, amount: Int, color: androidx.compose.ui.graphics.Color, onAdd: (() -> Unit)?) {
    ArtImage(R.drawable.panel_currency_counter, maxEdge = ArtSizes.BUTTON, trim = true,
        contentScale = ContentScale.FillBounds, modifier = Modifier.at(box.x, box.y, box.w, box.h))
    ArtImage(icon, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.at(box.x - 32, box.y - 12, 80, 80))
    DesignText(
        text = amount.toString(),
        size = 32,
        weight = FontWeight.Bold,
        color = color,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(box.x + 62, box.y, box.w - 130, box.h),
    )
    if (onAdd != null) {
        ArtImage(
            R.drawable.button_currency_add,
            maxEdge = ArtSizes.ICON,
            trim = true,
            contentDescription = stringResource(R.string.inventory_open),
            modifier = Modifier.at(box.right - 58, box.y - 2, 60, 60).clip(CircleShape)
                .clickable(role = Role.Button, onClick = onAdd),
        )
    }
}

/** Nút tròn hai bên: khung `frame_side_button`, icon nằm trong lòng khung. */
@Composable
private fun SideButton(box: LobbyLayout.Box, @DrawableRes icon: Int, label: String, onClick: () -> Unit) {
    ArtPressable(onClick = onClick, modifier = Modifier.at(box.x, box.y, box.w, box.h).clip(CircleShape)) {
        ArtImage(R.drawable.frame_side_button, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.fillMaxSize())
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ArtImage(
                res = icon,
                maxEdge = ArtSizes.ICON,
                trim = true,
                contentDescription = label,
                modifier = Modifier.size(maxWidth * 0.56f),
            )
        }
    }
}

/** Thẻ chế độ: khung ảnh, icon trên, tên (và mô tả ngắn ở thẻ lớn) bên dưới. */
@Composable
private fun ModeCardView(card: LobbyCard, highlight: Boolean) {
    val d = LocalDesign.current
    val big = card.subtitle != null
    val box = card.box
    ArtPressable(onClick = card.onClick, modifier = Modifier.at(box.x, box.y, box.w, box.h)) {
        ArtImage(card.panel, maxEdge = ArtSizes.BUTTON, trim = true,
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        if (highlight) {
            ArtImage(R.drawable.decor_mode_card_corners, maxEdge = ArtSizes.BUTTON, trim = true,
                contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(
                horizontal = d.dp(if (big) 44 else 26),
                vertical = d.dp(if (big) 30 else 22),
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(d.dp(4), Alignment.CenterVertically),
        ) {
            ArtImage(card.icon, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.size(d.dp(if (big) 112 else 92)))
            DesignText(
                text = stringResource(card.title),
                size = if (big) 34 else 25,
                color = GothicColors.Gold,
                weight = FontWeight.Bold,
                maxLines = if (big) 1 else 2,
                modifier = Modifier.fillMaxWidth(),
            )
            card.subtitle?.let {
                DesignText(
                    text = stringResource(it),
                    size = 21,
                    color = GothicColors.Parchment,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Chân dung vuông trong khung tròn (mặc định `ui_character_frame`): khung rỗng giữa nên chân dung
 * nằm dưới, co vào ~82% ô (tương ứng 92/112 của bản thiết kế) và cắt tròn cho khớp lỗ khung.
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
