package kma.game.chess2d.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn "Chọn đối thủ AI": nhân vật hiện lớn ở giữa, thông tin bên dưới, carousel để đổi.
 *
 * Nhân vật chưa mở vẫn xem được nhưng nút BẮT ĐẦU bị tắt và hiện điều kiện mở.
 *
 * @param clearedUpTo số tầng Tháp Cờ đã vượt qua, để biết nhân vật nào đã mở.
 * @param onStart báo nhân vật được chọn; chỉ gọi với nhân vật đã mở.
 */
@Composable
fun AiSelectionScreen(
    clearedUpTo: Int,
    onStart: (AiCharacter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val characters = AiCharacters.all
    var index by rememberSaveable { mutableIntStateOf(0) }
    val current = characters[index.coerceIn(characters.indices)]
    val unlocked = current.isUnlocked(clearedUpTo)

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_chess_battle),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Nền chỉ là phông mờ: phủ tối để nhân vật và chữ là trọng tâm.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = stringResource(R.string.ai_select_back),
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                )
                Text(
                    text = stringResource(R.string.ai_select_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = GothicColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Phần giữa cuộn được: máy nhỏ hoặc chữ lớn không được đẩy nút BẮT ĐẦU ra khỏi màn.
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Portrait(character = current, unlocked = unlocked)
                Text(
                    text = stringResource(current.name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = GothicColors.Parchment,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(current.quote),
                    style = MaterialTheme.typography.bodyMedium,
                    color = GothicColors.Parchment,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                )
                StatCard(character = current)
            }

            Carousel(
                characters = characters,
                selected = index,
                clearedUpTo = clearedUpTo,
                onSelect = { index = it },
            )

            if (unlocked) {
                Button(
                    onClick = { onStart(current) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GothicColors.Blood,
                        contentColor = GothicColors.Gold,
                    ),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ai_select_start),
                        fontWeight = FontWeight.Bold,
                    )
                }
            } else {
                // Không hiện nút xám: nói thẳng điều kiện để người chơi biết phải làm gì.
                Text(
                    text = stringResource(
                        R.string.ai_select_locked,
                        current.unlockFloor ?: 0,
                    ),
                    color = GothicColors.Parchment,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                )
            }
        }
    }
}

/** Ảnh lớn của nhân vật; nhân vật chưa mở bị làm tối kèm ổ khóa. */
@Composable
private fun Portrait(character: AiCharacter, unlocked: Boolean) {
    Box(
        modifier = Modifier
            .height(260.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(character.portrait),
            contentDescription = stringResource(character.name),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().alpha(if (unlocked) 1f else 0.35f),
        )
        if (!unlocked) {
            Image(
                painter = painterResource(R.drawable.icon_locked),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
        }
    }
}

/** Thông tin đối thủ: độ khó (sao), phong cách, tốc độ nghĩ, tính cách. */
@Composable
private fun StatCard(character: AiCharacter) {
    val stars = stringResource(R.string.ai_stars, character.stars)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatRow(label = stringResource(R.string.ai_stat_difficulty)) {
            Text(
                text = "\u2605".repeat(character.stars) + "\u2606".repeat(3 - character.stars),
                color = GothicColors.Gold,
                style = MaterialTheme.typography.titleMedium,
                // Đọc "2/3 sao" thay vì đọc từng ký tự sao.
                modifier = Modifier.semantics { contentDescription = stars },
            )
        }
        StatRow(label = stringResource(R.string.ai_stat_style)) {
            StatValue(stringResource(character.style))
        }
        StatRow(label = stringResource(R.string.ai_stat_speed)) {
            StatValue(stringResource(R.string.ai_speed_value, character.thinkSeconds))
        }
        StatRow(label = stringResource(R.string.ai_stat_personality)) {
            StatValue(stringResource(character.personality))
        }
    }
}

@Composable
private fun StatRow(label: String, value: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GothicColors.Gold,
        )
        value()
    }
}

@Composable
private fun StatValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = GothicColors.Parchment,
    )
}

/** Hàng chọn nhân vật: mũi tên trái/phải và ảnh nhỏ của cả năm nhân vật. */
@Composable
private fun Carousel(
    characters: List<AiCharacter>,
    selected: Int,
    clearedUpTo: Int,
    onSelect: (Int) -> Unit,
) {
    val previousLabel = stringResource(R.string.ai_select_prev)
    val nextLabel = stringResource(R.string.ai_select_next)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(
            onClick = { onSelect((selected - 1).coerceAtLeast(0)) },
            enabled = selected > 0,
            modifier = Modifier.size(36.dp).semantics { contentDescription = previousLabel },
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = "\u25C0", color = GothicColors.Gold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            characters.forEachIndexed { i, character ->
                val isSelected = i == selected
                val unlocked = character.isUnlocked(clearedUpTo)
                Image(
                    painter = painterResource(character.portrait),
                    contentDescription = stringResource(character.name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(if (isSelected) 44.dp else 36.dp)
                        .clip(CircleShape)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GothicColors.Gold else GothicColors.Locked,
                            shape = CircleShape,
                        )
                        .alpha(if (unlocked) 1f else 0.4f)
                        .clickable { onSelect(i) },
                )
            }
        }
        TextButton(
            onClick = { onSelect((selected + 1).coerceAtMost(characters.lastIndex)) },
            enabled = selected < characters.lastIndex,
            modifier = Modifier.size(36.dp).semantics { contentDescription = nextLabel },
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(text = "\u25B6", color = GothicColors.Gold)
        }
    }
}
