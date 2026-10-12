package kma.game.chess2d.ui.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import kma.game.chess2d.ui.art.GothicConfirmDialog
import kma.game.chess2d.ui.art.Hairline
import kma.game.chess2d.ui.art.PanelTone
import kma.game.chess2d.ui.art.SectionTitle
import kma.game.chess2d.ui.art.quietPanel
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.DataReset
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.GothicRadio
import kma.game.chess2d.ui.art.GothicTextField
import kma.game.chess2d.ui.art.GothicToggle
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
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
 * Bố cục Cài đặt theo đơn vị thiết kế 1080x1920. Hàm thuần để kiểm thử.
 *
 * Năm tab nằm thành một hàng ngang ngay dưới tiêu đề thay vì chiếm cả một cột, nên các hàng cài đặt
 * có trọn chiều ngang 900; panel chứa hàng chỉ cao đúng bằng nội dung của tab.
 */
internal object SettingsLayout {
    const val TAB_Y = 190
    const val TAB_H = 96
    const val TAB_W = 172
    const val TAB_GAP = 10

    /** X của tab thứ [index] (90, 272, 454, ...). */
    fun tabX(index: Int): Int = CONTENT_X + index * (TAB_W + TAB_GAP)

    /** Vùng nội dung cuộn được, kết thúc trên nút Khôi phục (y=1680). */
    const val CONTENT_X = 90
    const val CONTENT_Y = 320
    const val CONTENT_W = 900
    const val CONTENT_H = 1330
    const val ROW_H = 120
    const val RESTORE_Y = 1680
}

/**
 * Bảng Cài đặt phủ kín màn hình, dùng chung cho Sảnh và Pause Menu.
 *
 * Là [Dialog] chứ không phải một màn riêng của [AppRoot]: mở từ trong ván đấu thì ván vẫn nằm
 * nguyên bên dưới, không bị rời khỏi composition.
 *
 * Bố cục theo bản thiết kế: nền `bg_settings`, panel `ui_panel_main` (48,200,984,1420), cột danh
 * mục bên trái (tab đang chọn nằm trên `button_secondary_normal`), bên phải là các hàng
 * nhãn + control (`toggle_*`, `radio_*`) cuộn được, dưới cùng là nút Khôi phục mặc định.
 *
 * Chỉ có những cài đặt **thật sự có tác dụng** trong app; chưa có thanh âm lượng nên chưa dùng
 * `slider_*`, tránh control không làm gì.
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
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        DesignCanvas(background = R.drawable.bg_settings, dim = 0.35f, scrim = true, modifier = modifier) {
            SubScreenHeader(title = stringResource(R.string.lobby_settings), onBack = onDismiss)

            SettingsTab.entries.forEachIndexed { i, entry ->
                key(entry) {
                    CategoryTab(
                        label = stringResource(entry.label),
                        selected = entry == tab,
                        onClick = { tab = entry },
                        modifier = Modifier.at(
                            SettingsLayout.tabX(i),
                            SettingsLayout.TAB_Y,
                            SettingsLayout.TAB_W,
                            SettingsLayout.TAB_H,
                        ),
                    )
                }
            }

            Column(
                modifier = Modifier
                    .at(
                        SettingsLayout.CONTENT_X,
                        SettingsLayout.CONTENT_Y,
                        SettingsLayout.CONTENT_W,
                        SettingsLayout.CONTENT_H,
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(LocalDesign.current.dp(24)),
            ) {
                when (tab) {
                    SettingsTab.GENERAL -> {
                        if (name != null && onNameChange != null) {
                            SettingsGroup(title = stringResource(R.string.lan_your_name)) {
                                GothicTextField(
                                    value = name,
                                    onValueChange = onNameChange,
                                    placeholder = stringResource(R.string.lan_your_name),
                                    modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(110)),
                                )
                            }
                        }
                        SettingsGroup {
                            ToggleRow(
                                label = stringResource(R.string.settings_haptic),
                                checked = settings.hapticEnabled,
                                onCheckedChange = { scope.launch { store.setHapticEnabled(it) } },
                            )
                        }
                    }

                    SettingsTab.SOUND -> SettingsGroup {
                        ToggleRow(
                            label = stringResource(R.string.settings_music),
                            checked = settings.musicEnabled,
                            onCheckedChange = { scope.launch { store.setMusicEnabled(it) } },
                        )
                        Hairline()
                        ToggleRow(
                            label = stringResource(R.string.settings_sound),
                            checked = settings.soundEnabled,
                            onCheckedChange = { scope.launch { store.setSoundEnabled(it) } },
                        )
                    }

                    SettingsTab.VISUAL -> {
                        SettingsGroup(title = stringResource(R.string.settings_board_palette)) {
                            BoardPalette.entries.forEachIndexed { i, palette ->
                                key(palette) {
                                    if (i > 0) Hairline()
                                    RadioRow(
                                        label = stringResource(paletteLabel(palette)),
                                        selected = palette == settings.boardPalette,
                                        onClick = { scope.launch { store.setBoardPalette(palette) } },
                                    )
                                }
                            }
                        }
                        SettingsGroup(title = stringResource(R.string.settings_piece_theme)) {
                            PieceTheme.entries.forEachIndexed { i, theme ->
                                key(theme) {
                                    if (i > 0) Hairline()
                                    RadioRow(
                                        label = stringResource(pieceThemeLabel(theme)),
                                        selected = theme == settings.pieceTheme,
                                        onClick = { scope.launch { store.setPieceTheme(theme) } },
                                    )
                                }
                            }
                        }
                    }

                    SettingsTab.CONTROLS -> SettingsGroup {
                        ToggleRow(
                            label = stringResource(R.string.settings_legal_moves),
                            checked = settings.showLegalMoves,
                            onCheckedChange = { scope.launch { store.setShowLegalMoves(it) } },
                        )
                    }

                    SettingsTab.OTHER -> {
                        SettingsGroup(title = stringResource(R.string.settings_data)) {
                            GothicButton(
                                label = stringResource(R.string.settings_reset_data),
                                onClick = { confirmReset = true },
                                art = ButtonArt.SECONDARY,
                                textColor = GothicColors.Alert,
                                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(100)),
                            )
                        }
                        SettingsGroup(title = stringResource(R.string.settings_credits_title)) {
                            DesignText(
                                text = stringResource(R.string.settings_credits_text),
                                size = 30,
                                color = GothicColors.Muted,
                                align = TextAlign.Start,
                                alignment = Alignment.TopStart,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            GothicButton(
                label = stringResource(R.string.settings_restore_defaults),
                onClick = { scope.launch { store.resetToDefaults() } },
                art = ButtonArt.SECONDARY,
                modifier = Modifier.at(170, SettingsLayout.RESTORE_Y, 740, 100),
            )
        }
    }

    if (confirmReset) {
        GothicConfirmDialog(
            title = stringResource(R.string.settings_reset_title),
            text = stringResource(R.string.settings_reset_text),
            confirmLabel = stringResource(R.string.settings_reset_confirm),
            dismissLabel = stringResource(R.string.action_cancel),
            onConfirm = {
                confirmReset = false
                scope.launch { DataReset.resetAll(context) }
            },
            onDismiss = { confirmReset = false },
        )
    }
}

/**
 * Một tab ở hàng ngang: tab đang chọn là panel [PanelTone.SELECTED] (ánh đỏ, viền vàng, chữ vàng),
 * tab khác chỉ là nền chìm viền mờ — không dùng khung PNG để tab không chiếm sự chú ý của nội dung.
 */
@Composable
private fun CategoryTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(
        modifier = modifier
            .quietPanel(if (selected) PanelTone.SELECTED else PanelTone.SUNKEN)
            .clickable(role = Role.Tab, onClick = onClick),
    ) {
        DesignText(
            text = label,
            size = 28,
            color = if (selected) GothicColors.Gold else GothicColors.Muted,
            weight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 2,
            modifier = Modifier.fillMaxSize().padding(horizontal = LocalDesign.current.dp(8)),
        )
    }
}

/**
 * Một nhóm cài đặt: tiêu đề nhóm (nếu có) rồi panel tối giản cao đúng bằng các hàng bên trong,
 * nên tab ít mục không để lại một panel trống to.
 */
@Composable
private fun SettingsGroup(title: String? = null, content: @Composable () -> Unit) {
    val d = LocalDesign.current
    Column(modifier = Modifier.fillMaxWidth()) {
        if (title != null) SectionTitle(title)
        Column(
            modifier = Modifier.fillMaxWidth().quietPanel()
                .padding(horizontal = d.dp(36), vertical = d.dp(20)),
        ) { content() }
    }
}

/** Một hàng cài đặt: nhãn chiếm phần còn lại, control rộng 160 sát phải. */
@Composable
private fun SettingRow(label: String, modifier: Modifier = Modifier, control: @Composable () -> Unit) {
    val d = LocalDesign.current
    Row(
        modifier = modifier.fillMaxWidth().height(d.dp(SettingsLayout.ROW_H)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DesignText(
            text = label,
            size = 32,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 2,
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        Box(
            modifier = Modifier.width(d.dp(160)).fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd,
        ) { control() }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val d = LocalDesign.current
    SettingRow(label = label) {
        GothicToggle(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.size(d.dp(100), d.dp(50)),
        )
    }
}

/** Hàng chọn một trong nhiều: chạm cả hàng cũng chọn được, không phải nhắm đúng nút 48x48. */
@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    val d = LocalDesign.current
    SettingRow(label = label, modifier = Modifier.clickable(role = Role.RadioButton, onClick = onClick)) {
        GothicRadio(selected = selected, onClick = onClick, modifier = Modifier.size(d.dp(48)))
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
