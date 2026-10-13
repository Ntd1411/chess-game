package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Real dp sizes: the dialog is independent of the gameplay design canvas. */
internal object PauseMenuLayout {
    const val MAX_WIDTH_DP = 400
    const val CONTENT_PADDING_DP = 20
    const val ROW_HEIGHT_DP = 56
    const val ROW_GAP_DP = 8
    const val ROW_PADDING_DP = 14
    const val ICON_SIZE_DP = 24
}

/** Drawn pause window. Close, Back and outside taps all resume without changing the match. */
@Composable
fun PauseMenuOverlay(
    items: List<PauseItem>,
    onItem: (PauseItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resume = { onItem(PauseItem.RESUME) }
    Dialog(onDismissRequest = resume, properties = DialogProperties(
        dismissOnBackPress = true, dismissOnClickOutside = true, usePlatformDefaultWidth = false,
    )) {
        BoxWithConstraints(
            modifier = Modifier.widthIn(max = PauseMenuLayout.MAX_WIDTH_DP.dp).fillMaxWidth(0.9f)
                .safeDrawingPadding().padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            val shape = RoundedCornerShape(20.dp)
            Column(
                modifier = modifier.fillMaxWidth().heightIn(max = maxHeight).clip(shape)
                    .background(GothicColors.Ink)
                    .border(1.dp, GothicColors.Gold.copy(alpha = 0.5f), shape)
                    .verticalScroll(rememberScrollState()).padding(PauseMenuLayout.CONTENT_PADDING_DP.dp),
                verticalArrangement = Arrangement.spacedBy(PauseMenuLayout.ROW_GAP_DP.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.pause_title),
                        modifier = Modifier.weight(1f).semantics { heading() },
                        color = GothicColors.Gold, fontFamily = GameFontFamily,
                        fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = resume, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.pause_resume),
                            tint = GothicColors.Muted)
                    }
                }
                items.forEach { item ->
                    key(item) {
                        if (item == PauseItem.LEAVE) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp),
                                color = GothicColors.Gold.copy(alpha = 0.15f))
                        }
                        PauseActionRow(item = item, onClick = { onItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PauseActionRow(item: PauseItem, onClick: () -> Unit) {
    val primary = item == PauseItem.RESUME
    val leaving = item == PauseItem.LEAVE
    val shape = RoundedCornerShape(12.dp)
    val fill = when {
        primary -> GothicColors.Gold
        leaving -> GothicColors.Crimson.copy(alpha = 0.18f)
        else -> GothicColors.Gold.copy(alpha = 0.06f)
    }
    val textColor = if (primary) GothicColors.Ink else GothicColors.Parchment
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = PauseMenuLayout.ROW_HEIGHT_DP.dp)
            .clip(shape).background(fill)
            .border(1.dp, GothicColors.Gold.copy(alpha = if (primary) 1f else 0.25f), shape)
            .clickable(role = Role.Button, onClick = onClick).padding(PauseMenuLayout.ROW_PADDING_DP.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(iconFor(item), contentDescription = null, tint = if (primary) textColor else GothicColors.Gold,
            modifier = Modifier.size(PauseMenuLayout.ICON_SIZE_DP.dp))
        Text(stringResource(labelFor(item)), modifier = Modifier.weight(1f),
            color = textColor, fontFamily = GameFontFamily, fontSize = 16.sp,
            fontWeight = if (primary) FontWeight.Bold else FontWeight.Medium)
    }
}

internal fun labelFor(item: PauseItem): Int = when (item) {
    PauseItem.RESUME -> R.string.pause_resume
    PauseItem.RESTART -> R.string.pause_restart
    PauseItem.SETTINGS -> R.string.pause_settings
    PauseItem.LEAVE -> R.string.pause_leave
}

/** Vector icons avoid depending on decorative bitmap frames or button artwork. */
internal fun iconFor(item: PauseItem): ImageVector = when (item) {
    PauseItem.RESUME -> Icons.Default.PlayArrow
    PauseItem.RESTART -> Icons.Default.Refresh
    PauseItem.SETTINGS -> Icons.Default.Settings
    PauseItem.LEAVE -> Icons.AutoMirrored.Filled.ArrowBack
}
