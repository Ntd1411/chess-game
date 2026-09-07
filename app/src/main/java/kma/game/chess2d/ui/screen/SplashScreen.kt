package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kotlinx.coroutines.delay

/**
 * Màn hình mở đầu.
 *
 * Cần nói rõ nó **không** chờ tải gì: app không đọc file, không gọi mạng và không dựng
 * sẵn engine lúc khởi động, nên đây chỉ là một chuyển cảnh ngắn. Đó cũng là lý do
 * [SPLASH_MILLIS] cức ngắn: bắt người chơi ngồi nhìn logo lâu hơn thế chỉ là làm chậm
 * họ lại mỗi lần mở app.
 */
@Composable
fun SplashScreen(onDone: () -> Unit, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = tween(durationMillis = FADE_MILLIS),
        label = "splashAlpha",
    )

    LaunchedEffect(Unit) {
        shown = true
        delay(SPLASH_MILLIS)
        onDone()
    }

    Column(
        modifier = modifier.fillMaxSize().alpha(alpha),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Glyph Unicode thay cho file ảnh: không thêm asset nào vào project.
        Text(text = KING_GLYPH, fontSize = 96.sp)
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/** Vua đen trong bảng Unicode. */
private const val KING_GLYPH = "\u265A"
private const val SPLASH_MILLIS = 900L
private const val FADE_MILLIS = 400
