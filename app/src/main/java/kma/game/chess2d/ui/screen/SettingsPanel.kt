package kma.game.chess2d.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.DataReset
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.launch

/** Năm tab của Cài đặt theo `ui-screens.md`: Chung / Âm thanh / Hình ảnh / Điều khiển / Khác. */
enum class SettingsTab(@param:StringRes val label: Int) {
    GENERAL(R.string.settings_tab_general),
    SOUND(R.string.settings_tab_sound),
    VISUAL(R.string.settings_tab_visual),
    CONTROLS(R.string.settings_tab_controls),
    OTHER(R.string.settings_tab_other),
}

/**
 * Bảng Cài đặt dạng tab, phủ gần kín màn hình. Dùng chung cho Sảnh và Pause Menu.
 *
 * Là [Dialog] chứ không phải một màn riêng của [AppRoot]: mở từ trong ván đấu thì ván vẫn nằm
 * nguyên bên dưới, không bị rời khỏi composition.
 *
 * Chỉ có những cài đặt **thật sự có tác dụng** trong app. Ngôn ngữ, thông báo, nhạc nền, chất
 * lượng hiệu ứng, FPS và xác nhận nước đi trong spec chưa có tính năng tương ứng nên chưa đưa
 * vào, tránh công tắc không làm gì.
 *
 * @param name tên người chơi; `null` thì ẩn ô nhập (vd. mở từ trong ván).
 * @param onNameChange gọi khi sửa tên, dùng cùng [name].
 */
@Composable
fun SettingsPanel(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    name: String? = null,
    onNameChange: ((String) -> Unit)? = null,
) {
    var tab by rememberSaveable { mutableStateOf(SettingsTab.GENERAL) }
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier.fillMaxSize().statusBarsPadding().padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            color = GothicColors.Ink,
            border = BorderStroke(2.dp, GothicColors.Gold),
        ) {
            Column {
                Text(
                    text = stringResource(R.string.lobby_settings),
                    style = MaterialTheme.typography.headlineSmall,
                    color = GothicColors.Gold,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp),
                )
                ScrollableTabRow(
                    selectedTabIndex = tab.ordinal,
                    containerColor = Color.Transparent,
                    contentColor = GothicColors.Gold,
                    edgePadding = 8.dp,
                ) {
                    for (entry in SettingsTab.entries) {
                        key(entry) {
                            Tab(
                                selected = entry == tab,
                                onClick = { tab = entry },
                                text = { Text(stringResource(entry.label)) },
                                selectedContentColor = GothicColors.Gold,
                                unselectedContentColor = GothicColors.Parchment,
                            )
                        }
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    when (tab) {
                        SettingsTab.GENERAL -> {
                            if (name != null && onNameChange != null) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = onNameChange,
                                    label = { Text(stringResource(R.string.lan_your_name)) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                            SettingSwitch(
                                label = stringResource(R.string.settings_haptic),
                                checked = settings.hapticEnabled,
                                onCheckedChange = { scope.launch { store.setHapticEnabled(it) } },
                            )
                        }

                        SettingsTab.SOUND -> SettingSwitch(
                            label = stringResource(R.string.settings_sound),
                            checked = settings.soundEnabled,
                            onCheckedChange = { scope.launch { store.setSoundEnabled(it) } },
                        )

                        SettingsTab.VISUAL -> {
                            SectionLabel(stringResource(R.string.settings_board_palette))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (palette in BoardPalette.entries) {
                                    key(palette) {
                                        FilterChip(
                                            selected = palette == settings.boardPalette,
                                            onClick = { scope.launch { store.setBoardPalette(palette) } },
                                            label = { Text(stringResource(paletteLabel(palette))) },
                                        )
                                    }
                                }
                            }
                            SectionLabel(stringResource(R.string.settings_piece_theme))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (theme in PieceTheme.entries) {
                                    key(theme) {
                                        FilterChip(
                                            selected = theme == settings.pieceTheme,
                                            onClick = { scope.launch { store.setPieceTheme(theme) } },
                                            label = { Text(stringResource(pieceThemeLabel(theme))) },
                                        )
                                    }
                                }
                            }
                        }

                        SettingsTab.CONTROLS -> SettingSwitch(
                            label = stringResource(R.string.settings_legal_moves),
                            checked = settings.showLegalMoves,
                            onCheckedChange = { scope.launch { store.setShowLegalMoves(it) } },
                        )

                        SettingsTab.OTHER -> {
                            SectionLabel(stringResource(R.string.settings_data))
                            OutlinedButton(
                                onClick = { confirmReset = true },
                                border = BorderStroke(1.dp, GothicColors.Blood),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = GothicColors.Parchment,
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text(stringResource(R.string.settings_reset_data)) }
                            SectionLabel(stringResource(R.string.settings_credits_title))
                            Text(
                                text = stringResource(R.string.settings_credits_text),
                                style = MaterialTheme.typography.bodyMedium,
                                color = GothicColors.Parchment,
                            )
                        }
                    }
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End).padding(end = 12.dp, bottom = 8.dp),
                ) { Text(stringResource(R.string.lobby_close), color = GothicColors.Gold) }
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        scope.launch { DataReset.resetAll(context) }
                    },
                ) { Text(stringResource(R.string.settings_reset_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, color = GothicColors.Gold)
}

/** Một dòng công tắc: nhãn bên trái, [Switch] ở sát lề phải. */
@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = GothicColors.Parchment)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/** Nhãn hiện cho một bộ màu bàn. */
private fun paletteLabel(palette: BoardPalette): Int = when (palette) {
    BoardPalette.GOTHIC -> R.string.palette_gothic
    BoardPalette.GREEN -> R.string.palette_green
    BoardPalette.WOOD -> R.string.palette_wood
    BoardPalette.OCEAN -> R.string.palette_ocean
    BoardPalette.SLATE -> R.string.palette_slate
}

/** Nhãn hiện cho một bộ quân. */
private fun pieceThemeLabel(theme: PieceTheme): Int = when (theme) {
    PieceTheme.SOLID -> R.string.piece_theme_solid
    PieceTheme.OUTLINE -> R.string.piece_theme_outline
    PieceTheme.LETTER -> R.string.piece_theme_letter
    PieceTheme.IMAGE -> R.string.piece_theme_image
}
