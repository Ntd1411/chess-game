package kma.game.chess2d.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.sound.MusicPlayer
import kma.game.chess2d.sound.MusicTrack

private val LocalMusicPlayer = staticCompositionLocalOf<MusicPlayer?> { null }

/**
 * Chủ của nhạc nền: một [MusicPlayer] duy nhất sống suốt thời gian app hiện trên màn hình.
 *
 * Phải là một chỗ duy nhất ở gốc: nếu mỗi màn tự giữ một bộ phát thì chuyển màn là tắt nhạc
 * rồi bật lại từ đầu, kể cả khi hai màn dùng cùng một bản.
 *
 * Việc ở đây gồm: đưa công tắc **Nhạc nền** trong Cài đặt vào bộ phát, tạm dừng khi app xuống nền
 * (ON_STOP) và phát tiếp khi quay lại (ON_START), và trả tài nguyên khi rời đi.
 */
@Composable
fun MusicHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val player = remember(context) { MusicPlayer(context) }
    DisposableEffect(player) { onDispose { player.release() } }

    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    LaunchedEffect(settings.musicEnabled) { player.setEnabled(settings.musicEnabled) }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { player.setSuspended(true) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { player.setSuspended(false) }

    CompositionLocalProvider(LocalMusicPlayer provides player, content = content)
}

/**
 * Khai báo bản nhạc mà màn hình này muốn phát; `null` là im lặng.
 *
 * Không tự tắt khi rời composition: màn kế tiếp sẽ khai báo bản của nó, và nếu là cùng bản thì
 * nhạc phải đi tiếp liền mạch. Vì vậy mỗi màn (hoặc nhánh) đều phải khai báo, kể cả khi muốn im.
 */
@Composable
internal fun PlayMusic(track: MusicTrack?) {
    val player = LocalMusicPlayer.current
    LaunchedEffect(player, track) { player?.setTrack(track) }
}
