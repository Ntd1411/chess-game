package kma.game.chess2d.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.campaign.DialogueLine
import kma.game.chess2d.campaign.Speaker
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.delay

internal fun nextDialogueIndex(current: Int, total: Int): Int? {
    if (total <= 0 || current >= total - 1) return null
    return (current + 1).coerceAtLeast(0)
}

@Composable
fun DialogueScreen(lines: List<DialogueLine>, onFinished: () -> Unit, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    if (lines.isEmpty()) { LaunchedEffect(Unit) { onFinished() }; return }
    var index by rememberSaveable(lines) { mutableIntStateOf(0) }
    var auto by rememberSaveable { mutableStateOf(false) }
    val current = index.coerceIn(0, lines.lastIndex)
    val line = lines[current]
    val isLast = current == lines.lastIndex
    val advance = { nextDialogueIndex(current, lines.size)?.let { index = it } ?: onFinished() }
    val latestAdvance by rememberUpdatedState(advance)
    LaunchedEffect(auto, current, lines) {
        if (auto && !isLast) { delay(3000); latestAdvance() }
    }
    SimpleSetupPage(R.drawable.bg_conversation, line.speaker.displayName, stringResource(R.string.action_back), onBack ?: onFinished, modifier,
        footer = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SimpleSetupButton(stringResource(R.string.dialogue_auto), { auto = !auto }, Modifier.weight(1f), selected = auto)
                SimpleSetupButton(stringResource(R.string.dialogue_skip), onFinished, Modifier.weight(1f))
            }
            SimpleSetupButton(stringResource(if (isLast) R.string.dialogue_continue else R.string.dialogue_next), advance, Modifier.fillMaxWidth(), primary = true)
        }) {
        ArtImage(portraitRes(line.speaker), contentDescription = line.speaker.displayName,
            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(20.dp)).align(Alignment.CenterHorizontally))
        SimpleSetupCard(Modifier.clickable(role = Role.Button, onClick = advance)) {
            Text(line.speaker.displayName, color = GothicColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.dialogue_progress, current + 1, lines.size), color = GothicColors.Muted, fontSize = 12.sp)
            Text(line.text, color = GothicColors.Parchment, fontSize = 18.sp, lineHeight = 27.sp)
        }
    }
}

internal fun portraitRes(speaker: Speaker): Int = when (speaker) {
    Speaker.NARRATOR -> R.drawable.portrait_tower_keeper
    Speaker.TRAVELER -> R.drawable.portrait_protagonist
    Speaker.PLAYER_48 -> R.drawable.portrait_unknown_03
    Speaker.ARIA -> R.drawable.portrait_golden_queen
}
