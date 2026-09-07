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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty

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
