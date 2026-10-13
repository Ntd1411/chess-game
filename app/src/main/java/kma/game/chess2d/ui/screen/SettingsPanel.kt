package kma.game.chess2d.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.DataReset
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.launch

enum class SettingsTab(@param:StringRes val label: Int) {
    GENERAL(R.string.settings_tab_general), SOUND(R.string.settings_tab_sound),
    VISUAL(R.string.settings_tab_visual), CONTROLS(R.string.settings_tab_controls), OTHER(R.string.settings_tab_other),
}

internal object SettingsLayout {
    const val ROW_MIN_DP = 56
    const val TAB_MIN_DP = 48
    const val MAX_WIDTH_DP = 560
}

/** A full-screen dialog keeps the active match composed underneath. */
@Composable
fun SettingsPanel(onDismiss: () -> Unit, modifier: Modifier = Modifier, name: String? = null, onNameChange: ((String) -> Unit)? = null) {
    var tab by rememberSaveable { mutableStateOf(SettingsTab.GENERAL) }
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val cosmetics by store.cosmetics.collectAsStateWithLifecycle(initialValue = kma.game.chess2d.shop.ShopState())
    val scope = rememberCoroutineScope()
    var confirmReset by rememberSaveable { mutableStateOf(false) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val focus = LocalFocusManager.current
        val keyboard = LocalSoftwareKeyboardController.current
        val close = { focus.clearFocus(); keyboard?.hide(); onDismiss() }
        Box(modifier.fillMaxSize().background(GothicColors.Ink)) {
            ArtBackdrop(R.drawable.bg_settings, dim = 0.6f)
            Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                SimpleSetupHeader(stringResource(R.string.lobby_settings), stringResource(R.string.action_back), close, titleSize = 18.sp)
                Row(Modifier.widthIn(max = SettingsLayout.MAX_WIDTH_DP.dp).fillMaxWidth().horizontalScroll(rememberScrollState()).selectableGroup()
                    .padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SettingsTab.entries.forEach { entry ->
                        val selected = entry == tab
                        val shape = RoundedCornerShape(12.dp)
                        Box(Modifier.heightIn(min = SettingsLayout.TAB_MIN_DP.dp).clip(shape)
                            .background(if (selected) GothicColors.Gold.copy(alpha = 0.18f) else GothicColors.Ink.copy(alpha = 0.8f))
                            .border(1.dp, GothicColors.Gold.copy(alpha = if (selected) 0.9f else 0.3f), shape)
                            .selectable(selected, role = Role.Tab, onClick = { focus.clearFocus(); keyboard?.hide(); tab = entry })
                            .padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                            Text(stringResource(entry.label), color = if (selected) GothicColors.Gold else GothicColors.Muted,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp)
                        }
                    }
                }
                key(tab) {
                    SettingsPanelContent(currentTab = tab, settings = settings, owned = cosmetics.owned, name = name, onNameChange = onNameChange,
                        onHapticChanged = { scope.launch { store.setHapticEnabled(it) } },
                        onMusicChanged = { scope.launch { store.setMusicEnabled(it) } },
                        onSoundChanged = { scope.launch { store.setSoundEnabled(it) } },
                        onBoardPaletteChanged = { scope.launch { store.setBoardPalette(it) } },
                        onPieceThemeChanged = { scope.launch { store.setPieceTheme(it) } },
                        onShowLegalMovesChanged = { scope.launch { store.setShowLegalMoves(it) } },
                        onResetDataClick = { confirmReset = true },
                        unlockContent = { UnlockCodeCard(store) },
                        modifier = Modifier.weight(1f).widthIn(max = SettingsLayout.MAX_WIDTH_DP.dp).fillMaxWidth())
                }
                Column(Modifier.widthIn(max = SettingsLayout.MAX_WIDTH_DP.dp).fillMaxWidth().padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SimpleSetupButton(stringResource(R.string.settings_restore_defaults), { scope.launch { store.resetToDefaults() } }, Modifier.fillMaxWidth())
                    SimpleSetupButton(stringResource(R.string.action_back), close, Modifier.fillMaxWidth())
                }
            }
        }
        if (confirmReset) {
            DrawnConfirmDialog(title = stringResource(R.string.settings_reset_title), text = stringResource(R.string.settings_reset_text),
                confirmLabel = stringResource(R.string.settings_reset_confirm),
                onConfirm = { confirmReset = false; scope.launch { DataReset.resetAll(context) } },
                onDismiss = { confirmReset = false })
        }
    }
}

@Composable
private fun SettingsPanelContent(currentTab: SettingsTab, settings: AppSettings, owned: Set<String>, name: String?, onNameChange: ((String) -> Unit)?,
    onHapticChanged: (Boolean) -> Unit, onMusicChanged: (Boolean) -> Unit, onSoundChanged: (Boolean) -> Unit,
    onBoardPaletteChanged: (BoardPalette) -> Unit, onPieceThemeChanged: (PieceTheme) -> Unit,
    onShowLegalMovesChanged: (Boolean) -> Unit, onResetDataClick: () -> Unit, unlockContent: @Composable () -> Unit, modifier: Modifier = Modifier) {
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    Column(modifier.verticalScroll(rememberScrollState()).padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (currentTab) {
            SettingsTab.GENERAL -> {
                if (name != null && onNameChange != null) {
                    SettingsGroup(stringResource(R.string.lan_your_name)) {
                        OutlinedTextField(value = name, onValueChange = onNameChange, singleLine = true, modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.lan_your_name)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); keyboard?.hide() }),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GothicColors.Parchment, unfocusedTextColor = GothicColors.Parchment,
                                focusedBorderColor = GothicColors.Gold, unfocusedBorderColor = GothicColors.Gold.copy(alpha = 0.4f),
                                focusedLabelColor = GothicColors.Gold, unfocusedLabelColor = GothicColors.Muted, cursorColor = GothicColors.Gold))
                    }
                }
                SettingsGroup { ToggleRow(stringResource(R.string.settings_haptic), settings.hapticEnabled, onHapticChanged) }
            }
            SettingsTab.SOUND -> SettingsGroup {
                ToggleRow(stringResource(R.string.settings_music), settings.musicEnabled, onMusicChanged)
                SettingDivider()
                ToggleRow(stringResource(R.string.settings_sound), settings.soundEnabled, onSoundChanged)
            }
            SettingsTab.VISUAL -> {
                Text(stringResource(R.string.shop_settings_hint), color = GothicColors.Muted, fontSize = 13.sp)
                SettingsGroup(stringResource(R.string.settings_board_palette)) {
                    Column(Modifier.selectableGroup()) {
                        BoardPalette.entries.forEachIndexed { index, palette ->
                            if (index > 0) SettingDivider()
                            RadioRow(stringResource(paletteLabel(palette)), palette == settings.boardPalette,
                                enabled = kma.game.chess2d.shop.ShopCatalog.boardId(palette) in owned) { onBoardPaletteChanged(palette) }
                        }
                    }
                }
                SettingsGroup(stringResource(R.string.settings_piece_theme)) {
                    Column(Modifier.selectableGroup()) {
                        PieceTheme.entries.forEachIndexed { index, theme ->
                            if (index > 0) SettingDivider()
                            RadioRow(stringResource(pieceThemeLabel(theme)), theme == settings.pieceTheme,
                                enabled = kma.game.chess2d.shop.ShopCatalog.pieceId(theme) in owned) { onPieceThemeChanged(theme) }
                        }
                    }
                }
            }
            SettingsTab.CONTROLS -> SettingsGroup {
                ToggleRow(stringResource(R.string.settings_legal_moves), settings.showLegalMoves, onShowLegalMovesChanged)
            }
            SettingsTab.OTHER -> {
                unlockContent()
                SettingsGroup(stringResource(R.string.settings_data)) {
                    SimpleSetupButton(stringResource(R.string.settings_reset_data), onResetDataClick, Modifier.fillMaxWidth())
                }
                SettingsGroup(stringResource(R.string.settings_credits_title)) {
                    Text(stringResource(R.string.settings_credits_text), color = GothicColors.Muted, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun SettingsGroup(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    SimpleSetupCard {
        if (title != null) Text(title, color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        content()
    }
}

@Composable
private fun SettingDivider() { HorizontalDivider(color = GothicColors.Gold.copy(alpha = 0.15f)) }

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = SettingsLayout.ROW_MIN_DP.dp)
        .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, color = GothicColors.Parchment, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null, colors = SwitchDefaults.colors(checkedThumbColor = GothicColors.Ink,
            checkedTrackColor = GothicColors.Gold, uncheckedThumbColor = GothicColors.Muted,
            uncheckedTrackColor = GothicColors.Ink, uncheckedBorderColor = GothicColors.Gold.copy(alpha = 0.4f)))
    }
}

@Composable
private fun RadioRow(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = SettingsLayout.ROW_MIN_DP.dp)
        .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick).padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (enabled) label else label + " · " + stringResource(R.string.shop_locked), color = if (!enabled) GothicColors.Muted else if (selected) GothicColors.Gold else GothicColors.Parchment, fontSize = 16.sp, modifier = Modifier.weight(1f))
        RadioButton(selected = selected, enabled = enabled, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = GothicColors.Gold, unselectedColor = GothicColors.Muted))
    }
}

internal fun paletteLabel(palette: BoardPalette): Int = when (palette) {
    BoardPalette.GOTHIC -> R.string.palette_gothic
    BoardPalette.GREEN -> R.string.palette_green
    BoardPalette.WOOD -> R.string.palette_wood
    BoardPalette.OCEAN -> R.string.palette_ocean
    BoardPalette.SLATE -> R.string.palette_slate
}

internal fun pieceThemeLabel(theme: PieceTheme): Int = when (theme) {
    PieceTheme.SOLID -> R.string.piece_theme_solid
    PieceTheme.OUTLINE -> R.string.piece_theme_outline
    PieceTheme.LETTER -> R.string.piece_theme_letter
    PieceTheme.IMAGE -> R.string.piece_theme_image
}
