package kma.game.chess2d.ui.screen

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GothicColors

@Composable
internal fun DrawnConfirmDialog(title: String, text: String, confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = GothicColors.Ink,
        titleContentColor = GothicColors.Gold, textContentColor = GothicColors.Parchment,
        title = { Text(title) }, text = { Text(text, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, color = GothicColors.Alert) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = GothicColors.Gold) } })
}
