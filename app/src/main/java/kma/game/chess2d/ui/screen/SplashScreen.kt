package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import kma.game.chess2d.ui.art.ArtBar
import kma.game.chess2d.ui.art.ArtCache
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.at
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Màn mở đầu: chỉ gồm nền `bg_splash` (đã vẽ sẵn logo "Tower of Chess") và thanh tải
 * `splash_loading_frame` + `splash_loading_fill`. Không có chữ nào khác.
 *
 * Thanh tiến độ phản ánh việc thật: trong lúc hiện splash, app giải mã trước nền sảnh, nút,
 * bàn cờ và quân cờ vào [ArtCache] để màn sau mở ra là có ảnh ngay. Splash ở lại tối thiểu
 * [MIN_SPLASH_MILLIS] để người chơi kịp thấy logo; chỉ vào menu sau khi thanh tải
 * chạy đầy và giữ ở 100% trong [FULL_BAR_HOLD_MILLIS].
 */
@Composable
fun SplashScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentOnDone by rememberUpdatedState(onDone)
    var shown by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = FADE_MILLIS),
        label = "splashAlpha",
    )
    val shownProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 250),
        label = "splashProgress",
    )

    LaunchedEffect(Unit) {
        shown = true
        val startedAt = System.currentTimeMillis()
        ArtCache.preload(context, ArtAssets.preload) { done, total ->
            progress = done.toFloat() / total
        }
        progress = 1f
        val remaining = MIN_SPLASH_MILLIS - (System.currentTimeMillis() - startedAt)
        if (remaining > 0) delay(remaining)
        // Nạp xong không có nghĩa animation đã xong: đợi giá trị đang vẽ đạt 100%.
        snapshotFlow { shownProgress }.first { it >= 1f }
        // Cho thanh đầy được hiển thị trước khi splash bị gỡ khỏi cây giao diện.
        delay(FULL_BAR_HOLD_MILLIS)
        currentOnDone()
    }

    DesignCanvas(background = R.drawable.bg_splash, dim = 0f, modifier = modifier) {
        // Khung và ruột cùng canvas 1536x1024; ruột thật nằm từ 4,0% đến 95,8% chiều ngang.
        val bar = SplashLayout.Bar
        ArtBar(
            frame = R.drawable.splash_loading_frame,
            fill = R.drawable.splash_loading_fill,
            fraction = shownProgress,
            fillStart = SplashLayout.FILL_START,
            fillEnd = SplashLayout.FILL_END,
            modifier = Modifier.at(bar.x, bar.y, bar.w, bar.h).alpha(alpha),
        )
    }
}

/** Bố cục splash (đơn vị thiết kế 1080x1920): chỉ có thanh tải ở phần dưới màn hình. */
internal object SplashLayout {
    /**
     * Canvas 1536x1024 (3:2) của `splash_loading_*`; khung nhìn thấy chiếm 32–63% chiều cao
     * canvas, nên phần khung thật nằm khoảng y 1553–1707.
     */
    val Bar = LobbyLayout.Box(160, 1390, 760, 507)
    const val FILL_START = 0.040f
    const val FILL_END = 0.958f
}

private const val MIN_SPLASH_MILLIS = 1400L
private const val FADE_MILLIS = 500
private const val FULL_BAR_HOLD_MILLIS = 150L
