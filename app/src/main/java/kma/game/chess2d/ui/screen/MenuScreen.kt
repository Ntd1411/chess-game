package kma.game.chess2d.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.launch

/** Kích thước các nút chế độ ở sảnh: hai chế độ chính phải to hơn ba chế độ phụ. */
internal object LobbyLayout {
    val PrimaryButtonHeight = 72.dp
    val SecondaryButtonHeight = 48.dp
}

/**
 * Sảnh chính: tên người chơi và nút cài đặt ở trên, nhân vật ở giữa, năm chế độ ở dưới.
 *
 * **Khám Phá Tháp Cờ** và **Người vs Máy** là hai chế độ chính nên to hơn; hai người cùng
 * máy, LAN và Máy vs Máy là ba chế độ phụ. Cấp độ máy không còn chọn ở đây mà chọn ở màn
 * Chọn đối thủ AI. Tên người chơi, âm thanh và giao diện bàn cờ nằm trong hộp thoại Cài đặt.
 *
 * Chưa có ba lối tắt Nhật ký/Túi đồ/Thành tựu: các màn đó thuộc Giai đoạn 5, và một nút
 * bấm không dẫn đi đâu là ngõ cụt.
 */
@Composable
fun MenuScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onOpenTower: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit,
    onOpenSpectate: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_main_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Phủ tối nhẹ để tên và nút luôn đọc được trên mọi vùng của ảnh nền.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        ) {
            LobbyHeader(name = name, onOpenSettings = { showSettings = true })

            // Nhân vật chiếm phần còn lại giữa màn hình; Fit để không bao giờ bị cắt đầu/chân.
            Image(
                painter = painterResource(R.drawable.char_protagonist_full_body),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(8.dp),
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PrimaryModeButton(
                    label = stringResource(R.string.tower_open),
                    onClick = onOpenTower,
                )
                PrimaryModeButton(
                    label = stringResource(R.string.mode_vs_computer),
                    onClick = onPlayComputer,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryModeButton(
                        label = stringResource(R.string.mode_two_players),
                        onClick = onPlayTwoPlayers,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryModeButton(
                        label = stringResource(R.string.mode_lan),
                        onClick = onPlayLan,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryModeButton(
                        label = stringResource(R.string.spectate_open),
                        onClick = onOpenSpectate,
                        modifier = Modifier.weight(1f),
                    )
                }
                TextButton(onClick = onOpenHistory, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.history_open),
                        color = GothicColors.Parchment,
                    )
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            name = name,
            onNameChange = onNameChange,
            onDismiss = { showSettings = false },
        )
    }
}

/** Hàng trên cùng: avatar và tên người chơi bên trái, nút cài đặt bên phải. */
@Composable
private fun LobbyHeader(name: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerAvatar(name = name, size = 48.dp)
        Text(
            text = name.ifBlank { stringResource(R.string.match_player_one) },
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Parchment,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(R.drawable.icon_settings),
            contentDescription = stringResource(R.string.lobby_settings),
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onOpenSettings),
        )
    }
}

/** Nút chế độ chính: cao, nền đỏ máu, chữ vàng. */
@Composable
private fun PrimaryModeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = GothicColors.Blood,
            contentColor = GothicColors.Gold,
        ),
        modifier = Modifier.fillMaxWidth().height(LobbyLayout.PrimaryButtonHeight),
    ) {
        Text(text = label, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

/** Nút chế độ phụ: thấp hơn, chỉ có viền vàng. Cho phép xuống hai dòng vì ba nút chia chung một hàng. */
@Composable
private fun SecondaryModeButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        border = BorderStroke(1.dp, GothicColors.Gold),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = GothicColors.Parchment),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
        modifier = modifier.height(LobbyLayout.SecondaryButtonHeight),
    ) {
        Text(text = label, fontSize = 12.sp, maxLines = 2, textAlign = TextAlign.Center)
    }
}

/** Hộp thoại cài đặt: tên người chơi, âm thanh, giao diện bàn cờ. */
@Composable
private fun SettingsDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lobby_settings)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Tên nhập một chỗ duy nhất: dùng cho avatar trong ván và tên phòng LAN.
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.lan_your_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SoundSettings()
                AppearanceSettings()
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.lobby_close)) }
        },
    )
}

/**
 * Chọn bộ màu bàn và bộ quân (mục 7.3).
 *
 * Đọc ghi trực tiếp qua [SettingsStore] giống hai công tắc âm thanh, và các màn hình
 * chơi cũng đọc từ đó, nên không cần đẩy lựa chọn này xuyên qua [AppRoot].
 */
@Composable
private fun AppearanceSettings() {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(R.string.settings_appearance),
        style = MaterialTheme.typography.titleSmall,
    )

    Text(
        text = stringResource(R.string.settings_board_palette),
        style = MaterialTheme.typography.bodyMedium,
    )
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

    Text(
        text = stringResource(R.string.settings_piece_theme),
        style = MaterialTheme.typography.bodyMedium,
    )
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

/** Nhãn hiện cho một bộ màu bàn. */
private fun paletteLabel(palette: BoardPalette): Int = when (palette) {
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

/**
 * Hai công tắc âm thanh và rung.
 *
 * Đọc và ghi trực tiếp qua [SettingsStore] thay vì đẩy lên [AppRoot]: đây là cài đặt
 * của riêng thiết bị, không phải trạng thái của ván đấu, nên không có lý do để nó
 * đi xuyên qua các màn hình khác.
 */
@Composable
private fun SoundSettings() {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    // Chưa đọc xong file thì hiện mặc định; đúng với giá trị mà store sẽ trả về.
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    // Ghi đĩa là việc treo, mà `onCheckedChange` thì không: cần một scope sống theo màn hình.
    val scope = rememberCoroutineScope()

    Text(
        text = stringResource(R.string.settings_title),
        style = MaterialTheme.typography.titleSmall,
    )
    SettingSwitch(
        label = stringResource(R.string.settings_sound),
        checked = settings.soundEnabled,
        onCheckedChange = { enabled -> scope.launch { store.setSoundEnabled(enabled) } },
    )
    SettingSwitch(
        label = stringResource(R.string.settings_haptic),
        checked = settings.hapticEnabled,
        onCheckedChange = { enabled -> scope.launch { store.setHapticEnabled(enabled) } },
    )
}

/** Một dòng công tắc: nhãn bên trái, [Switch] ở sát lề phải. */
@Composable
private fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
