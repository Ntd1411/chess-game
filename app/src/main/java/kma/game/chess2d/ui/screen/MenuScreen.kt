package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GothicColors

internal object LobbyLayout {
    data class Box(val x: Int, val y: Int, val w: Int, val h: Int) {
        val right: Int get() = x + w
        val bottom: Int get() = y + h
        fun overlaps(o: Box): Boolean = x < o.right && o.x < right && y < o.bottom && o.y < bottom
    }
    const val SAFE_MARGIN = 48
}

@Composable
fun MenuScreen(name: String, level: Int, gold: Int, crystals: Int, onNameChange: (String) -> Unit,
    onOpenTower: () -> Unit, onPlayComputer: () -> Unit, onPlayTwoPlayers: () -> Unit, onPlayLan: () -> Unit,
    onOpenModes: () -> Unit, onOpenHistory: () -> Unit, onOpenProfile: () -> Unit,
    onOpenJournal: () -> Unit, onOpenInventory: () -> Unit, modifier: Modifier = Modifier,
    levelProgress: Float = 0f, onOpenSpectate: () -> Unit = onOpenModes, onOpenShop: () -> Unit = onOpenProfile) {
    var showSettings by rememberSaveable { mutableStateOf(false) }
    Box(modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.main_menu_background, dim = 0.42f)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SimpleSetupCard(Modifier.clickable(role = Role.Button, onClick = onOpenProfile)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        ArtImage(R.drawable.main_menu_avatar, contentScale = ContentScale.Crop, modifier = Modifier.size(56.dp).clip(CircleShape))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(name.ifBlank { stringResource(R.string.match_player_one) }, color = GothicColors.Parchment, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(stringResource(R.string.lobby_level, level), color = GothicColors.Gold, fontSize = 13.sp)
                            LinearProgressIndicator(progress = { levelProgress.coerceIn(0f, 1f) }, color = GothicColors.Gold,
                                trackColor = GothicColors.Gold.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(stringResource(R.string.shop_balance, gold), color = GothicColors.Gold, fontSize = 14.sp)
                        Text(stringResource(R.string.menu_crystals, crystals), color = GothicColors.Parchment, fontSize = 14.sp)
                    }
                }
                Text(stringResource(R.string.menu_heading), color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 28.sp, modifier = Modifier.padding(top = 20.dp, bottom = 12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MenuTile(stringResource(R.string.main_menu_tower), onOpenTower, Modifier.weight(1f), primary = true)
                    MenuTile(stringResource(R.string.menu_vs_ai), onPlayComputer, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MenuTile(stringResource(R.string.main_menu_local), onPlayTwoPlayers, Modifier.weight(1f), compact = true)
                    MenuTile(stringResource(R.string.main_menu_lan), onPlayLan, Modifier.weight(1f), compact = true)
                    MenuTile(stringResource(R.string.spectate_open), onOpenSpectate, Modifier.weight(1f), compact = true)
                }
                BoxWithConstraints {
                    val columns = MainMenuLayout.columnsFor(maxWidth.value)
                    val actions = listOf(onOpenProfile, onOpenJournal, onOpenInventory, onOpenShop, onOpenHistory, { showSettings = true })
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MainMenuShortcut.entries.chunked(columns).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { entry -> SimpleSetupButton(stringResource(entry.label), actions[entry.ordinal], Modifier.weight(1f), maxLines = 2) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showSettings) SettingsPanel(name = name, onNameChange = onNameChange, onDismiss = { showSettings = false })
}

@Composable
private fun MenuTile(label: String, onClick: () -> Unit, modifier: Modifier, primary: Boolean = false, compact: Boolean = false) {
    val shape = RoundedCornerShape(16.dp)
    Box(modifier.heightIn(min = if (compact) 88.dp else 112.dp).clip(shape)
        .background(if (primary) GothicColors.Crimson.copy(alpha = 0.85f) else GothicColors.Ink.copy(alpha = 0.88f))
        .border(if (primary) 2.dp else 1.dp, GothicColors.Gold.copy(alpha = if (primary) 0.9f else 0.4f), shape)
        .clickable(role = Role.Button, onClick = onClick).padding(12.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (primary) GothicColors.Gold else GothicColors.Parchment,
            fontWeight = FontWeight.Bold, fontSize = if (compact) 14.sp else 19.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
