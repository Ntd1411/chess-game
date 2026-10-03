package kma.game.chess2d.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.Floor
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn Chiến thắng của một tầng.
 *
 * Chưa có hệ thống tiền tệ nên "phần thưởng" duy nhất là điều có thật: tầng kế tiếp được
 * mở khóa. Tầng cuối dùng nền kết thúc thật và câu chúc mừng chinh phục cả tháp.
 *
 * @param nextFloor tầng kế tiếp; `null` ở tầng cuối.
 * @param onNext lên thẳng [nextFloor]; chỉ hiện nút này khi có tầng kế.
 */
@Composable
fun VictoryScreen(
    floor: Floor,
    nextFloor: Floor?,
    onNext: () -> Unit,
    onMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFinal = floor.number == TowerCatalog.FLOOR_COUNT
    val subtitle = when {
        isFinal -> stringResource(R.string.victory_tower_cleared)
        floor.kind == NodeKind.TREASURE -> stringResource(R.string.victory_sub_treasure)
        floor.kind == NodeKind.STORY -> stringResource(R.string.victory_sub_story)
        else -> stringResource(R.string.victory_sub_floor, floor.number, floor.title)
    }

    ResultBackdrop(
        background = if (isFinal) R.drawable.screen_true_ending else R.drawable.screen_victory,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.victory_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = GothicColors.Gold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Parchment,
            textAlign = TextAlign.Center,
        )
        if (nextFloor != null) {
            Text(
                text = stringResource(R.string.victory_unlocked, nextFloor.number),
                style = MaterialTheme.typography.bodyLarge,
                color = GothicColors.Gold,
                textAlign = TextAlign.Center,
            )
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GothicColors.Blood,
                    contentColor = GothicColors.Parchment,
                ),
            ) { Text(stringResource(R.string.victory_next, nextFloor.number)) }
        }
        OutlinedButton(onClick = onMap, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.result_to_map), color = GothicColors.Parchment)
        }
    }
}

/**
 * Màn Thất bại: nói rõ vì sao thua, một câu động viên, rồi Thử lại hoặc Về bản đồ.
 */
@Composable
fun DefeatScreen(
    floor: Floor,
    reason: LossReason,
    onRetry: () -> Unit,
    onMap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ResultBackdrop(background = R.drawable.screen_defeat, modifier = modifier) {
        Text(
            text = stringResource(R.string.defeat_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = GothicColors.Blood,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.defeat_floor, floor.number),
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Parchment,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                when (reason) {
                    LossReason.CHECKMATE -> R.string.defeat_reason_checkmate
                    LossReason.DRAW -> R.string.defeat_reason_draw
                },
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = GothicColors.Parchment,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.defeat_encourage),
            style = MaterialTheme.typography.bodyMedium,
            color = GothicColors.Parchment.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = GothicColors.Blood,
                contentColor = GothicColors.Parchment,
            ),
        ) { Text(stringResource(R.string.defeat_retry)) }
        OutlinedButton(onClick = onMap, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.result_to_map), color = GothicColors.Parchment)
        }
    }
}

/** Nền ảnh phủ tối nhẹ và cột nội dung ở giữa, dùng chung cho Victory và Defeat. */
@Composable
private fun ResultBackdrop(
    background: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            content()
        }
    }
}
