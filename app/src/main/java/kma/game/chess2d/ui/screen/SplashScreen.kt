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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GothicColors
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import kma.game.chess2d.ui.art.ArtCache
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
            progress = splashFraction(done, total)
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

    Box(modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtImage(R.drawable.bg_splash, maxEdge = ArtSizes.SCREEN, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().alpha(alpha))
        Column(Modifier.align(Alignment.BottomCenter).safeDrawingPadding().widthIn(max = SplashLayout.MAX_BAR_WIDTH_DP.dp)
            .fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
            LinearProgressIndicator(progress = { shownProgress.coerceIn(0f, 1f) }, color = GothicColors.Gold,
                trackColor = GothicColors.Gold.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth().height(SplashLayout.BAR_HEIGHT_DP.dp).clip(CircleShape).alpha(alpha))
        }
    }
}

internal object SplashLayout {
    const val MAX_BAR_WIDTH_DP = 480
    const val BAR_HEIGHT_DP = 6
}
internal fun splashFraction(done: Int, total: Int): Float = if (total <= 0) 1f else (done.toFloat() / total).coerceIn(0f, 1f)

private const val MIN_SPLASH_MILLIS = 1400L
private const val FADE_MILLIS = 500
private const val FULL_BAR_HOLD_MILLIS = 150L
