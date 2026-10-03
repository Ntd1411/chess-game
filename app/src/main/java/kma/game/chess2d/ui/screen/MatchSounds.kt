package kma.game.chess2d.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.game.MoveSound
import kma.game.chess2d.game.SoundCue
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.sound.SoundEffects

/**
 * Phát tiếng và rung theo nước đi gần nhất.
 *
 * Đặt thành tệp riêng để cả bàn cờ offline và bàn cờ LAN dùng chung một đường phát
 * tiếng: trước đây hàm này nằm riêng trong màn hình offline, nên đánh qua mạng LAN
 * thì không nghe tiếng nào.
 *
 * [SoundEffects] sống đúng bằng thời gian bàn cờ ở trên màn hình: rời bàn cờ là trả
 * lại bộ nhớ âm thanh, không để nó treo lại.
 *
 * Không phát tiếng cho cue đã có sẵn ở lần vẽ đầu tiên: xoay máy hay quay lại ván
 * đang dở không phải là vừa đi một nước, nghe tiếng lúc đó sẽ rất vô duyên.
 */
@Composable
internal fun MatchSounds(cue: SoundCue?) {
    val context = LocalContext.current
    val effects = remember(context) { SoundEffects(context) }
    DisposableEffect(effects) { onDispose { effects.release() } }

    val store = remember(context) { SettingsStore(context.applicationContext) }
    // Đọc cài đặt từ đĩa cần một nhịp; trong nhịp đó cứ coi như mặc định.
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    var lastSerial by remember { mutableStateOf(cue?.serial ?: 0) }
    LaunchedEffect(cue?.serial) {
        val current = cue ?: return@LaunchedEffect
        if (current.serial == lastSerial) return@LaunchedEffect
        lastSerial = current.serial
        effects.play(
            sound = current.sound,
            soundEnabled = settings.soundEnabled,
            hapticEnabled = settings.hapticEnabled,
        )
    }
}

/**
 * Dựng cue âm thanh cho bàn cờ LAN từ số nước đã đi.
 *
 * Ván LAN không đi qua <code>GameViewModel</code> nên không có [SoundCue] sẵn; ở đây
 * suy ra từ trạng thái: số nước tăng là vừa có nước đi (của bên nào cũng phát, vì
 * tiếng đối thủ đi mới là thứ người chơi cần biết nhất khi đang nhìn chọ khác), ván
 * kết thúc là một tiếng riêng.
 *
 * Không phân biệt ăn quân: trạng thái mạng chỉ mang dãy nước thô, mà việc dựng lại
 * bàn cờ chỉ để chọn một tiếng thì không đáng.
 */
@Composable
internal fun rememberMatchCue(ply: Int, status: GameStatus, finished: Boolean): SoundCue? {
    var cue by remember { mutableStateOf<SoundCue?>(null) }
    var lastPly by remember { mutableStateOf(ply) }
    var serial by remember { mutableStateOf(0) }
    LaunchedEffect(ply, finished) {
        val sound = when {
            finished -> MoveSound.GAME_END
            ply == lastPly -> return@LaunchedEffect
            status == GameStatus.CHECK -> MoveSound.CHECK
            else -> MoveSound.MOVE
        }
        lastPly = ply
        serial += 1
        cue = SoundCue(serial, sound)
    }
    return cue
}
