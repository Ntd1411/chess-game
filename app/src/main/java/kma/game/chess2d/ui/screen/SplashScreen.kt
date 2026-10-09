package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import kma.game.chess2d.ui.art.ArtCache
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.at
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Màn mở đầu theo bản thiết kế 1080x1920: nền `bg_splash`, vương miện phía trên logo, logo
 * Tower of Chess ở giữa, thanh tải và nhãn trạng thái phía dưới. Không dùng chân dung hay panel.
 *
 * Thanh tiến độ phản ánh việc thật: trong lúc hiện splash, app giải mã trước nền sảnh, nút,
 * bàn cờ và quân cờ vào [ArtCache] để màn sau mở ra là có ảnh ngay. Splash ở lại tối thiểu
 * [MIN_SPLASH_MILLIS] để người chơi kịp thấy logo, nạp xong là vào menu.
 */
@Composable
fun SplashScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
        onDone()
    }

    DesignCanvas(background = R.drawable.bg_splash, dim = 0.1f, modifier = modifier) {
        // Hạt sáng: trên nền, dưới logo.
        ArtImage(
            res = R.drawable.decor_main_menu_particles,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            alpha = 0.55f * alpha,
            modifier = Modifier.at(140, 620, 800, 560),
        )
        ArtImage(
            res = R.drawable.decor_main_menu_crown,
            maxEdge = ArtSizes.PIECE,
            trim = true,
            alpha = alpha,
            modifier = Modifier.at(450, 700, 180, 100),
        )
        ArtImage(
            res = R.drawable.logo_tower_of_chess,
            maxEdge = ArtSizes.LOGO,
            trim = true,
            alpha = alpha,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.at(120, 810, 840, 300),
        )
        LoadingBar(progress = shownProgress, modifier = Modifier.at(210, 1450, 660, 48).alpha(alpha))
        DesignText(
            text = stringResource(R.string.splash_loading, (shownProgress * 100).roundToInt()),
            size = 30,
            maxLines = 1,
            modifier = Modifier.at(240, 1510, 600, 48).alpha(alpha),
        )
    }
}

/** Thanh tải: khung `loading_bar_frame` và ruột `loading_bar_fill` cắt theo tiến độ. */
@Composable
private fun LoadingBar(progress: Float, modifier: Modifier = Modifier) {
    val d = LocalDesign.current
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = d.dp(22), vertical = d.dp(13)),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (progress > 0.02f) {
                SlicedArt(
                    res = R.drawable.loading_bar_fill,
                    slice = 0.5f,
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(progress.coerceIn(0f, 1f)),
                )
            }
        }
        SlicedArt(res = R.drawable.loading_bar_frame, slice = 0.5f, modifier = Modifier.fillMaxSize())
    }
}

private const val MIN_SPLASH_MILLIS = 1400L
private const val FADE_MILLIS = 500
