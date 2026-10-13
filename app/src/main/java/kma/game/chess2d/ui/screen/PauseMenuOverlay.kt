package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.GothicDialog
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Pause menu uses real dp because it is displayed outside the design canvas. */
internal object PauseMenuLayout {
    const val ROW_HEIGHT_DP = 56
    const val ROW_GAP_DP = 8
    const val ICON_SIZE_DP = 36
    const val ICON_INSET_DP = 18
    const val LABEL_INSET_DP = 60
    val rowArt = R.drawable.setting_line
}

/**
 * Gameplay pause menu with compact `setting_line` action rows.
 * Back or an outside tap resumes the match. Settings remains accessible here,
 * and the dialog blocks taps on the board underneath.
 */
@Composable
fun PauseMenuOverlay(
    items: List<PauseItem>,
    onItem: (PauseItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    GothicDialog(
        onDismiss = { onItem(PauseItem.RESUME) },
        title = stringResource(R.string.pause_title),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PauseMenuLayout.ROW_GAP_DP.dp)) {
            for (item in items) {
                PauseActionRow(item = item, onClick = { onItem(item) })
            }
        }
    }
}

@Composable
private fun PauseActionRow(item: PauseItem, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed) 0.98f else 1f, label = "pauseActionPress")
    Box(
        modifier = Modifier.fillMaxWidth().height(PauseMenuLayout.ROW_HEIGHT_DP.dp)
            .scale(pressScale)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Keep the ornamental ends intact while adapting the strip to dialog widths.
        SlicedArt(res = PauseMenuLayout.rowArt, slice = 0.5f, modifier = Modifier.matchParentSize())
        ArtImage(iconFor(item), maxEdge = ArtSizes.ICON, trim = true,
            modifier = Modifier.align(Alignment.CenterStart)
                .padding(start = PauseMenuLayout.ICON_INSET_DP.dp).size(PauseMenuLayout.ICON_SIZE_DP.dp))
        Text(
            text = stringResource(labelFor(item)),
            fontFamily = GameFontFamily,
            fontSize = 16.sp,
            fontWeight = if (item == PauseItem.RESUME) FontWeight.Bold else FontWeight.Medium,
            color = if (item == PauseItem.RESUME) GothicColors.Gold else GothicColors.Parchment,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.fillMaxWidth().padding(horizontal = PauseMenuLayout.LABEL_INSET_DP.dp),
        )
    }
}

private fun labelFor(item: PauseItem): Int = when (item) {
    PauseItem.RESUME -> R.string.pause_resume
    PauseItem.RESTART -> R.string.pause_restart
    PauseItem.SETTINGS -> R.string.pause_settings
    PauseItem.LEAVE -> R.string.pause_leave
}

/** Each action keeps its matching gameplay icon. */
internal fun iconFor(item: PauseItem): Int = when (item) {
    PauseItem.RESUME -> R.drawable.game_play_button_continue
    PauseItem.RESTART -> R.drawable.game_play_button_replay
    PauseItem.SETTINGS -> R.drawable.game_play_button_settings
    PauseItem.LEAVE -> R.drawable.game_play_button_exit
}
