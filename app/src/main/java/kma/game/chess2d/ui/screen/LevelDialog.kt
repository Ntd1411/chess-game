package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

internal object LevelDialogLayout {
    const val MAX_WIDTH_DP = 400
    const val OPTION_MIN_HEIGHT_DP = 64
    val levels: List<Difficulty> = Difficulty.entries.toList()
}

/** Picking a row applies immediately. Closing the dialog never changes the selected level. */
@Composable
internal fun LevelDialog(current: Difficulty, onChosen: (Difficulty) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(modifier = Modifier.widthIn(max = LevelDialogLayout.MAX_WIDTH_DP.dp)
            .fillMaxWidth(0.9f).safeDrawingPadding().padding(vertical = 16.dp)) {
            val shape = RoundedCornerShape(20.dp)
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = maxHeight).clip(shape)
                .background(GothicColors.Ink).border(1.dp, GothicColors.Gold.copy(alpha = 0.5f), shape)
                .verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.menu_difficulty), modifier = Modifier.weight(1f).semantics { heading() },
                        color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 24.sp,
                        fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, stringResource(R.string.action_cancel), tint = GothicColors.Muted)
                    }
                }
                Column(modifier = Modifier.fillMaxWidth().selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LevelDialogLayout.levels.forEach { level ->
                        DifficultyRow(level, selected = current == level, onClick = { onChosen(level) })
                    }
                }
                SimpleSetupButton(stringResource(R.string.action_cancel), onDismiss,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun DifficultyRow(level: Difficulty, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(modifier = Modifier.fillMaxWidth().heightIn(min = LevelDialogLayout.OPTION_MIN_HEIGHT_DP.dp)
        .clip(shape).background(GothicColors.Gold.copy(alpha = if (selected) 0.18f else 0.05f))
        .border(1.dp, GothicColors.Gold.copy(alpha = if (selected) 1f else 0.25f), shape)
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(labelOf(level)), color = if (selected) GothicColors.Gold else GothicColors.Parchment,
                fontFamily = GameFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Text(stringResource(R.string.ai_speed_value, level.timeBudgetMillis / 1000f),
                color = GothicColors.Muted, fontFamily = GameFontFamily, fontSize = 13.sp)
        }
        Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = GothicColors.Gold)
        }
    }
}
