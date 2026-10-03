package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtCache
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.delay

/**
 * Màn hình mở đầu: nền tháp cổ dưới trăng đỏ, logo Tower of Chess và thanh tiến độ.
 *
 * Thanh tiến độ phản ánh việc thật: trong lúc hiện splash, app giải mã trước nền menu, nút,
 * bàn cờ và quân cờ vào [ArtCache] để menu và ván đấu mở ra là có ảnh ngay, không nhấp nháy.
 * Splash ở lại tối thiểu [MIN_SPLASH_MILLIS] để người chơi kịp thấy logo, nhưng không quá lâu:
 * nạp xong là vào menu.
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

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(res = R.drawable.screen_splash_background, dim = 0.15f, maxEdge = ArtSizes.SCREEN)
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding().alpha(alpha).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Vùng giữa-trên của ảnh nền được vẽ chừa trống cho logo.
            Spacer(Modifier.fillMaxHeight(0.16f))
            ArtImage(
                res = R.drawable.ui_logo_tower_of_chess,
                maxEdge = ArtSizes.LOGO,
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.fillMaxWidth(0.92f),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = GothicColors.Parchment,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.weight(1f))
            LoadingBar(progress = shownProgress, modifier = Modifier.fillMaxWidth(0.62f))
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** Thanh tiến độ vàng trên rãnh đỏ sẫm, bo tròn. */
@Composable
private fun LoadingBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(GothicColors.Blood.copy(alpha = 0.45f)),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(50))
                .background(GothicColors.Gold),
        )
    }
}

private const val MIN_SPLASH_MILLIS = 1400L
private const val FADE_MILLIS = 500
