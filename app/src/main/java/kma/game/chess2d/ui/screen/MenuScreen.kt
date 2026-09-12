package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kotlinx.coroutines.launch

/**
 * Màn hình menu: chọn chế độ rồi vào ván.
 *
 * Tên nhập ở đây dùng cho cả avatar trong ván đấu và tên phòng khi chơi LAN — một chỗ
 * nhập duy nhất, thay vì bắt nhập lại ở sảnh LAN như trước.
 *
 * Cấp độ máy được chọn ngay ở đây chứ không phải trong ván: hàng nút trong ván
 * để cho các hành động của ván, không để cấu hình.
 */
@Composable
fun MenuScreen(
    name: String,
    onNameChange: (String) -> Unit,
    difficulty: Difficulty,
    onDifficultyChange: (Difficulty) -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayLan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = KING_GLYPH, fontSize = 64.sp)
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        // Avatar hiện ngay đây để người chơi thấy trước tên mình sẽ ra avatar thế nào,
        // vì màu được sinh từ chính cái tên đang nhập.
        PlayerAvatar(name = name, size = 72.dp)

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.lan_your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = onPlayTwoPlayers, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.mode_two_players))
        }

        Button(onClick = onPlayComputer, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.mode_vs_computer))
        }

        Text(
            text = stringResource(R.string.menu_difficulty),
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LevelButton(
                label = stringResource(R.string.difficulty_easy),
                selected = difficulty == Difficulty.EASY,
                onClick = { onDifficultyChange(Difficulty.EASY) },
            )
            LevelButton(
                label = stringResource(R.string.difficulty_medium),
                selected = difficulty == Difficulty.MEDIUM,
                onClick = { onDifficultyChange(Difficulty.MEDIUM) },
            )
            LevelButton(
                label = stringResource(R.string.difficulty_hard),
                selected = difficulty == Difficulty.HARD,
                onClick = { onDifficultyChange(Difficulty.HARD) },
            )
        }

        Button(onClick = onPlayLan, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.mode_lan))
        }

        SoundSettings()
        AppearanceSettings()
    }
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

/** Nút chọn cấp độ: tô đậm là đang chọn. */
@Composable
private fun LevelButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
    }
}

private const val KING_GLYPH = "\u265A"
