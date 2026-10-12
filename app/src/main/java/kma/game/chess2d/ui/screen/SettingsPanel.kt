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
import kma.game.chess2d.ui.art.GothicConfirmDialog
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

/** Bố cục Cài đặt theo bản thiết kế 1080x1920. Hàm thuần để kiểm thử. */
internal object SettingsLayout {
    /** Y của tab thứ [index] ở cột danh mục bên trái (300, 440, 580, ...). */
    fun tabY(index: Int): Int = 300 + index * 140

    const val TAB_X = 90
    const val TAB_W = 270
    const val TAB_H = 110

    /** Vùng nội dung cuộn được bên phải: nhãn từ x=390 (rộng 350), control từ x=760 (rộng 220). */
    const val CONTENT_X = 390
    const val CONTENT_Y = 270
    const val CONTENT_W = 600
    const val CONTENT_H = 1320
    const val LABEL_W = 350
    const val CONTROL_X = 760
    const val CONTROL_W = 220
    const val ROW_H = 110
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
        DesignCanvas(background = R.drawable.bg_settings, dim = 0.3f, modifier = modifier) {
            SubScreenHeader(title = stringResource(R.string.lobby_settings), onBack = onDismiss)
            SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(48, 200, 984, 1420))

            SettingsTab.entries.forEachIndexed { i, entry ->
                key(entry) {
                    CategoryTab(
                        label = stringResource(entry.label),
                        selected = entry == tab,
                        onClick = { tab = entry },
                        modifier = Modifier.at(
                            SettingsLayout.TAB_X,
                            SettingsLayout.tabY(i),
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
            ) {
                when (tab) {
                    SettingsTab.GENERAL -> {
                        if (name != null && onNameChange != null) {
                            SectionLabel(stringResource(R.string.lan_your_name))
                            GothicTextField(
                                value = name,
                                onValueChange = onNameChange,
                                placeholder = stringResource(R.string.lan_your_name),
                                modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(110)),
                            )
                        }
                        ToggleRow(
                            label = stringResource(R.string.settings_haptic),
                            checked = settings.hapticEnabled,
                            onCheckedChange = { scope.launch { store.setHapticEnabled(it) } },
                        )
                    }

                    SettingsTab.SOUND -> {
                        ToggleRow(
                            label = stringResource(R.string.settings_music),
                            checked = settings.musicEnabled,
                            onCheckedChange = { scope.launch { store.setMusicEnabled(it) } },
                        )
                        ToggleRow(
                            label = stringResource(R.string.settings_sound),
                            checked = settings.soundEnabled,
                            onCheckedChange = { scope.launch { store.setSoundEnabled(it) } },
                        )
                    }

                    SettingsTab.VISUAL -> {
                        SectionLabel(stringResource(R.string.settings_board_palette))
                        for (palette in BoardPalette.entries) {
                            key(palette) {
                                RadioRow(
                                    label = stringResource(paletteLabel(palette)),
                                    selected = palette == settings.boardPalette,
                                    onClick = { scope.launch { store.setBoardPalette(palette) } },
                                )
                            }
                        }
                        SectionLabel(stringResource(R.string.settings_piece_theme))
                        for (theme in PieceTheme.entries) {
                            key(theme) {
                                RadioRow(
                                    label = stringResource(pieceThemeLabel(theme)),
                                    selected = theme == settings.pieceTheme,
                                    onClick = { scope.launch { store.setPieceTheme(theme) } },
                                )
                            }
                        }
                    }

                    SettingsTab.CONTROLS -> ToggleRow(
                        label = stringResource(R.string.settings_legal_moves),
                        checked = settings.showLegalMoves,
                        onCheckedChange = { scope.launch { store.setShowLegalMoves(it) } },
                    )

                    SettingsTab.OTHER -> {
                        SectionLabel(stringResource(R.string.settings_data))
                        GothicButton(
                            label = stringResource(R.string.settings_reset_data),
                            onClick = { confirmReset = true },
                            art = ButtonArt.SECONDARY,
                            textColor = GothicColors.Blood,
                            modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(100)),
                        )
                        SectionLabel(stringResource(R.string.settings_credits_title))
                        DesignText(
                            text = stringResource(R.string.settings_credits_text),
                            size = 30,
                            align = TextAlign.Start,
                            alignment = Alignment.TopStart,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            GothicButton(
                label = stringResource(R.string.settings_restore_defaults),
                onClick = { scope.launch { store.resetToDefaults() } },
                art = ButtonArt.SECONDARY,
                modifier = Modifier.at(170, 1650, 740, 100),
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

/** Một mục ở cột danh mục: nền `ui_tab_normal`, tab đang chọn dùng `ui_tab_selected` và chữ vàng. */
@Composable
private fun CategoryTab(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier = modifier.clickable(role = Role.Tab, onClick = onClick)) {
        SlicedArt(
            res = if (selected) R.drawable.ui_tab_selected else R.drawable.ui_tab_normal,
            slice = 0.45f,
            modifier = Modifier.fillMaxSize(),
        )
        DesignText(
            text = label,
            size = 34,
            color = if (selected) GothicColors.Gold else GothicColors.Parchment,
            weight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            modifier = Modifier.fillMaxSize().padding(horizontal = LocalDesign.current.dp(24)),
        )
    }
}

/** Tiêu đề nhóm trong vùng nội dung, có `ui_separator` mảnh bên dưới. */
@Composable
private fun SectionLabel(text: String) {
    val d = LocalDesign.current
    Column(modifier = Modifier.fillMaxWidth().padding(top = d.dp(16))) {
        DesignText(
            text = text,
            size = 36,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().height(d.dp(70)),
        )
        ArtImage(
            res = R.drawable.ui_separator,
            maxEdge = ArtSizes.BUTTON,
            trim = true,
            modifier = Modifier.fillMaxWidth().height(d.dp(20)),
        )
    }
}

/** Một hàng cài đặt: nhãn rộng 350 bên trái, control rộng 220 bắt đầu từ x=760. */
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
            modifier = Modifier.width(d.dp(SettingsLayout.LABEL_W)).fillMaxHeight(),
        )
        Spacer(
            Modifier.width(
                d.dp(SettingsLayout.CONTROL_X - SettingsLayout.CONTENT_X - SettingsLayout.LABEL_W),
            ),
        )
        Box(
            modifier = Modifier.width(d.dp(SettingsLayout.CONTROL_W)).fillMaxHeight(),
            contentAlignment = Alignment.Center,
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
