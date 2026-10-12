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
import kma.game.chess2d.ui.art.ArtBar
import kma.game.chess2d.ui.theme.GothicColors
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
 * Màn mở đầu theo bản thiết kế 1080x1920: nền `bg_splash`, logo `logo_chess` (đã gồm vương miện)
 * với hoa văn `splash_logo_divider` bên dưới, rồi hoa văn `splash_loading_divider`, thanh tải
 * `loading_bar_*` và nhãn trạng thái. Không dùng chân dung hay panel.
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
        val logo = SplashLayout.Logo
        ArtImage(
            res = R.drawable.logo_chess,
            maxEdge = ArtSizes.LOGO,
            trim = true,
            alpha = alpha,
            contentDescription = stringResource(R.string.app_name),
            modifier = Modifier.at(logo.x, logo.y, logo.w, logo.h),
        )
        val logoDivider = SplashLayout.LogoDivider
        ArtImage(
            res = R.drawable.splash_logo_divider,
            maxEdge = ArtSizes.BUTTON,
            trim = true,
            alpha = alpha,
            modifier = Modifier.at(logoDivider.x, logoDivider.y, logoDivider.w, logoDivider.h),
        )

        val loadingDivider = SplashLayout.LoadingDivider
        ArtImage(
            res = R.drawable.splash_loading_divider,
            maxEdge = ArtSizes.BUTTON,
            trim = true,
            alpha = alpha,
            modifier = Modifier.at(loadingDivider.x, loadingDivider.y, loadingDivider.w, loadingDivider.h),
        )
        // Khung và ruột cùng canvas 3:1; ruột thật nằm từ 2,2% đến 97,7% chiều ngang.
        val bar = SplashLayout.Bar
        ArtBar(
            frame = R.drawable.loading_bar_frame,
            fill = R.drawable.loading_bar_fill,
            fraction = shownProgress,
            fillStart = 0.022f,
            fillEnd = 0.977f,
            modifier = Modifier.at(bar.x, bar.y, bar.w, bar.h).alpha(alpha),
        )
        val label = SplashLayout.Label
        DesignText(
            text = stringResource(R.string.splash_loading, (shownProgress * 100).roundToInt()),
            size = 28,
            color = GothicColors.Muted,
            maxLines = 1,
            modifier = Modifier.at(label.x, label.y, label.w, label.h).alpha(alpha),
        )
    }
}

/** Bố cục splash (đơn vị thiết kế 1080x1920): logo, hoa văn dưới logo, hoa văn + thanh tải + nhãn. */
internal object SplashLayout {
    val Logo = LobbyLayout.Box(190, 520, 700, 560)
    val LogoDivider = LobbyLayout.Box(240, 1090, 600, 144)
    val LoadingDivider = LobbyLayout.Box(290, 1440, 500, 48)

    /** Canvas 2172x724 của `loading_bar_*` ≈ 3:1; thanh thật nằm giữa, cao khoảng 37%. */
    val Bar = LobbyLayout.Box(190, 1480, 700, 233)
    val Label = LobbyLayout.Box(190, 1690, 700, 52)
}

private const val MIN_SPLASH_MILLIS = 1400L
private const val FADE_MILLIS = 500
