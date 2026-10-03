package kma.game.chess2d.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.DialogueLine
import kma.game.chess2d.campaign.Speaker
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn thoại: nền cảnh, nhân vật đứng trái/phải, hộp thoại lớn ở dưới và nút tiếp tục.
 *
 * Người Lữ Hành đứng bên trái, các nhân vật khác bên phải; giọng dẫn chuyện của tháp
 * không có hình nên chỉ hiện hộp thoại. Chạm vào hộp thoại cũng là tiếp tục. [onFinished]
 * chạy khi hết thoại hoặc khi bấm Bỏ qua.
 */
@Composable
fun DialogueScreen(
    lines: List<DialogueLine>,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Không có thoại thì không có gì để hiện: đi tiếp luôn thay vì kẹt ở màn trống.
    if (lines.isEmpty()) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var index by rememberSaveable { mutableIntStateOf(0) }
    val current = index.coerceIn(0, lines.lastIndex)
    val line = lines[current]
    val isLast = current == lines.lastIndex
    val advance = { if (isLast) onFinished() else index = current + 1 }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_dialogue_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f)))

        portraitRes(line.speaker)?.let { portrait ->
            val onLeft = line.speaker == Speaker.TRAVELER
            Image(
                painter = painterResource(portrait),
                contentDescription = line.speaker.displayName,
                contentScale = ContentScale.Fit,
                alignment = if (onLeft) Alignment.BottomStart else Alignment.BottomEnd,
                modifier = Modifier
                    .align(if (onLeft) Alignment.BottomStart else Alignment.BottomEnd)
                    .fillMaxHeight(0.62f)
                    .fillMaxWidth(0.6f)
                    .padding(bottom = 190.dp),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .background(GothicColors.Ink.copy(alpha = 0.88f), RoundedCornerShape(12.dp))
                .border(2.dp, GothicColors.Gold, RoundedCornerShape(12.dp))
                .clickable(onClick = advance)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = line.speaker.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GothicColors.Gold,
            )
            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyLarge,
                color = GothicColors.Parchment,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onFinished) {
                    Text(stringResource(R.string.dialogue_skip), color = GothicColors.Parchment)
                }
                Text(
                    text = "${current + 1}/${lines.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GothicColors.Parchment.copy(alpha = 0.7f),
                )
                Button(
                    onClick = advance,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GothicColors.Blood,
                        contentColor = GothicColors.Parchment,
                    ),
                ) {
                    Text(
                        stringResource(
                            if (isLast) R.string.dialogue_continue else R.string.dialogue_next,
                        ),
                    )
                }
            }
        }
    }
}

/** Ảnh đứng của người nói; `null` cho giọng dẫn chuyện vì tháp không có hình. */
private fun portraitRes(speaker: Speaker): Int? = when (speaker) {
    Speaker.NARRATOR -> null
    Speaker.TRAVELER -> R.drawable.char_protagonist_portrait
    Speaker.PLAYER_48 -> R.drawable.char_shadow_opponent
    Speaker.ARIA -> R.drawable.char_chess_queen
}
