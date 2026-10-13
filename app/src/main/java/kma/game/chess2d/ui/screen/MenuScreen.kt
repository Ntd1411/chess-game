package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtPressable
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GothicColors
import java.text.NumberFormat

/** Hình chữ nhật thiết kế dùng chung cho các màn phụ vẫn theo canvas 1080x1920. */
internal object LobbyLayout {
    data class Box(val x: Int, val y: Int, val w: Int, val h: Int) {
        val right: Int get() = x + w
        val bottom: Int get() = y + h
        fun overlaps(o: Box): Boolean = x < o.right && o.x < right && y < o.bottom && o.y < bottom
    }
    const val SAFE_MARGIN = 48
}

/** Nền tràn màn; header neo trên, mode neo đáy, toàn bộ nút phụ neo phải trong vùng an toàn. */
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
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        // Asset nền đã có nhân vật; màn ngang giữ khuôn mặt trong vùng crop nhìn thấy.
        ArtImage(R.drawable.main_menu_background, maxEdge = ArtSizes.SCREEN,
            contentScale = ContentScale.Crop,
            alignment = if (maxWidth >= maxHeight * 1.2f) BiasAlignment(0f, -0.4f) else Alignment.Center,
            modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            0f to Color.Black.copy(alpha = 0.42f),
            0.18f to Color.Transparent,
            0.60f to Color.Transparent,
            1f to Color.Black.copy(alpha = 0.62f),
        )))
        Box(Modifier.align(Alignment.CenterEnd).fillMaxHeight().fillMaxWidth(0.25f)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.38f)))))

        BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
            val layout = remember(maxWidth, maxHeight) { MainMenuLayout.calculate(maxWidth.value, maxHeight.value) }
            val profileLabel = stringResource(R.string.profile_open)
            if (layout.hero.h > 0f) {
                Box(Modifier.place(layout.hero).semantics { contentDescription = profileLabel }
                    .clickable(role = Role.Button, onClickLabel = profileLabel, onClick = onOpenProfile))
            }
            PlayerHeader(layout.profile, name, level, levelProgress, onOpenProfile)
            CurrencyPill(layout.gold, R.drawable.icon_currency_gold, gold, GothicColors.Gold)
            CurrencyPill(layout.crystals, R.drawable.icon_currency_crystal, crystals, GothicColors.Parchment)

            val sides = listOf(
                Triple(R.drawable.main_menu_journal, R.string.main_menu_journal, onOpenJournal),
                Triple(R.drawable.main_menu_inventory, R.string.main_menu_inventory, onOpenInventory),
                Triple(R.drawable.main_menu_achievements, R.string.main_menu_achievements, onOpenProfile),
                Triple(R.drawable.main_menu_history, R.string.main_menu_history, onOpenHistory),
                Triple(R.drawable.main_menu_settings, R.string.main_menu_settings) { showSettings = true },
            )
            val railScroll = rememberScrollState()
            val railOverflows = layout.sideItemHeight * sides.size > layout.rail.h + 0.1f
            Box(Modifier.place(layout.rail)) {
                Column(Modifier.fillMaxWidth().height((layout.rail.h - if (railOverflows && layout.rail.h >= 90f) 18f else 0f).dp)
                    .clipToBounds().verticalScroll(railScroll)) {
                    sides.forEach { (image, label, action) ->
                        SideButton(image, stringResource(label), layout.sideItemHeight, action, showLabel = layout.rail.h >= 90f)
                    }
                }
                if (railOverflows) {
                    Canvas(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(14.dp, 8.dp)) {
                        val tipY = if (railScroll.canScrollForward) size.height else 0f
                        val endY = size.height - tipY
                        drawLine(GothicColors.Gold, Offset(0f, endY), Offset(size.width / 2, tipY), 2.dp.toPx())
                        drawLine(GothicColors.Gold, Offset(size.width / 2, tipY), Offset(size.width, endY), 2.dp.toPx())
                    }
                }
            }
            val cards = listOf(
                Triple(R.drawable.main_menu_mode_tower, R.string.main_menu_tower, onOpenTower),
                Triple(R.drawable.main_menu_mode_ai, R.string.menu_vs_ai, onPlayComputer),
                Triple(R.drawable.main_menu_mode_local, R.string.main_menu_local, onPlayTwoPlayers),
                Triple(R.drawable.main_menu_mode_lan, R.string.main_menu_lan, onPlayLan),
                Triple(R.drawable.main_menu_mode_spectate, R.string.spectate_open, onOpenSpectate),
            )
            val compactLabels = listOf(R.string.main_menu_tower_short, R.string.main_menu_ai_short,
                R.string.main_menu_local_short, R.string.main_menu_lan_short, R.string.main_menu_spectate_short)
            cards.forEachIndexed { index, (image, label, action) ->
                val rect = layout.modes[index]
                val captionResource = when {
                    rect.h < 64f && index == 2 -> R.string.main_menu_local_tiny
                    rect.h < 64f && index == 4 -> R.string.main_menu_spectate_tiny
                    rect.w < 105f || rect.h < 90f -> compactLabels[index]
                    else -> label
                }
                ModeCard(rect, image, stringResource(label), index < 2, action,
                    caption = stringResource(captionResource))
            }
        }
    }
    if (showSettings) {
        SettingsPanel(name = name, onNameChange = onNameChange, onDismiss = { showSettings = false })
    }
}

private fun Modifier.place(rect: MainMenuLayout.Rect): Modifier = offset(rect.x.dp, rect.y.dp).size(rect.w.dp, rect.h.dp)

/** Các phần của header đặt theo đúng canvas 1967x799 của khung mới. */
@Composable
private fun PlayerHeader(rect: MainMenuLayout.Rect, name: String, level: Int, progress: Float, onOpenProfile: () -> Unit) {
    Box(Modifier.place(rect).clickable(role = Role.Button, onClickLabel = stringResource(R.string.profile_open), onClick = onOpenProfile)) {
        ArtImage(
            R.drawable.main_menu_avatar, maxEdge = ArtSizes.PORTRAIT, trim = true,
            contentScale = ContentScale.Crop,
            modifier = Modifier.offset((rect.w * 0.053f).dp, (rect.h * 0.164f).dp)
                .size((rect.w * 0.210f).dp).clip(CircleShape),
        )
        ArtImage(R.drawable.main_menu_header_frame, maxEdge = ArtSizes.BUTTON, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        // Khung có nền đặc ở lòng bar: ruột EXP vẽ phía trên, chỉ trong vùng rỗng của viền.
        ArtImage(
            R.drawable.main_menu_exp_fill, maxEdge = ArtSizes.BUTTON, trim = true,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset((rect.w * 0.397f).dp, (rect.h * 0.486f).dp)
                .size((rect.w * 0.537f).dp, (rect.h * 0.092f).dp)
                .drawWithContent { clipRect(right = size.width * progress.coerceIn(0f, 1f)) { this@drawWithContent.drawContent() } },
        )
        Text(
            text = name.ifBlank { stringResource(R.string.match_player_one) },
            color = GothicColors.Parchment,
            fontSize = (rect.w * 0.067f).coerceIn(12f, 22f).sp,
            lineHeight = ((rect.w * 0.067f).coerceIn(12f, 22f) * 1.15f).sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium.copy(shadow = Shadow(Color.Black, blurRadius = 6f)),
            modifier = Modifier.offset((rect.w * 0.33f).dp, (rect.h * 0.08f).dp).width((rect.w * 0.67f).dp),
        )
        Text(
            text = stringResource(R.string.lobby_level, level),
            color = GothicColors.Gold, fontSize = 12.sp, maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.offset((rect.w * 0.397f).dp, (rect.h * 0.64f).dp),
        )
    }
}

/** Số dư thật của hồ sơ; không dựng nút mua tiền khi chưa có chức năng mua. */
@Composable
private fun CurrencyPill(rect: MainMenuLayout.Rect, @DrawableRes icon: Int, amount: Int, color: Color) {
    val formatted = remember(amount) { NumberFormat.getIntegerInstance().format(amount) }
    Box(Modifier.place(rect), contentAlignment = Alignment.CenterEnd) {
        ArtImage(R.drawable.panel_currency_counter, maxEdge = ArtSizes.BUTTON, trim = true,
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        ArtImage(icon, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.align(Alignment.CenterStart).size(rect.h.dp))
        Text(formatted, color = color, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = rect.h.dp, end = 10.dp))
    }
}

@Composable
private fun SideButton(@DrawableRes image: Int, label: String, itemHeight: Float, onClick: () -> Unit, showLabel: Boolean = true) {
    ArtPressable(onClick, modifier = Modifier.fillMaxWidth().height(itemHeight.dp).semantics { contentDescription = label }) {
        val iconSize = (itemHeight - 34f).coerceIn(38f, 60f)
        ArtImage(image, maxEdge = ArtSizes.ICON, trim = true,
            modifier = Modifier.align(Alignment.TopCenter).size(iconSize.dp))
        if (showLabel) Text(label, color = GothicColors.Parchment, textAlign = TextAlign.Center,
            fontSize = 12.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium.copy(shadow = Shadow(Color.Black, blurRadius = 5f)),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 3.dp))
    }
}

@Composable
private fun ModeCard(rect: MainMenuLayout.Rect, @DrawableRes image: Int, label: String, primary: Boolean, onClick: () -> Unit, caption: String = label) {
    ArtPressable(onClick, modifier = Modifier.place(rect).semantics { contentDescription = label }) {
        ArtImage(image, maxEdge = ArtSizes.BUTTON, trim = true, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
        val aspect = when (image) {
            R.drawable.main_menu_mode_tower -> 1447f / 970f
            R.drawable.main_menu_mode_ai -> 1441f / 928f
            else -> 1.03f
        }
        val artWidth = minOf(rect.w, rect.h * aspect)
        val artHeight = artWidth / aspect
        val captionBottom = (rect.h - artHeight) / 2f + artHeight * 0.10f + 6f
        Box(Modifier.align(Alignment.Center).size(artWidth.dp, artHeight.dp)
            .padding(horizontal = (artWidth * 0.06f).coerceAtLeast(6f).dp,
                vertical = (artHeight * 0.08f).coerceAtLeast(6f).dp).clip(RoundedCornerShape(8.dp))
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.42f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.82f))))
        val size = (rect.w * if (primary) 0.095f else 0.115f).coerceIn(12f, 19f)
        Text(caption, color = GothicColors.Parchment, fontSize = size.sp, lineHeight = (size * 1.15f).sp,
            fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(shadow = Shadow(Color.Black, blurRadius = 5f)),
            modifier = Modifier.align(Alignment.BottomCenter).width(artWidth.dp)
                .padding(start = 10.dp, end = 10.dp, bottom = captionBottom.dp))
    }
}

/** Chân dung dùng chung ở các màn phụ, độc lập với header mới của menu. */
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
                modifier = Modifier.fillMaxSize().padding(maxWidth * 0.09f).clip(CircleShape),
            )
        }
        ArtImage(res = frame, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.fillMaxSize())
    }
}
