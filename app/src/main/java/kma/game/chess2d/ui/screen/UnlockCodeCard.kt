package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import java.io.IOException
import kma.game.chess2d.R
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.settings.UnlockResult
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.launch

@Composable
internal fun UnlockCodeCard(store: SettingsStore) {
    var code by rememberSaveable { mutableStateOf("") }
    var notice by rememberSaveable { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val submit: () -> Unit = {
        if (!busy && code.isNotBlank()) {
            val input = code
            busy = true
            focus.clearFocus()
            keyboard?.hide()
            scope.launch {
                try {
                    val result = store.redeemUnlockCode(input)
                    notice = when (result) {
                        UnlockResult.UNLOCKED -> R.string.unlock_success
                        UnlockResult.ALREADY_UNLOCKED -> R.string.unlock_already
                        UnlockResult.INVALID_CODE -> R.string.unlock_invalid
                    }
                    if (result != UnlockResult.INVALID_CODE) code = ""
                } catch (error: IOException) { notice = R.string.unlock_save_failed }
                finally { busy = false }
            }
        }
    }
    SimpleSetupCard {
        Text(stringResource(R.string.unlock_title), color = GothicColors.Gold, fontSize = 18.sp)
        Text(stringResource(R.string.unlock_hint), color = GothicColors.Muted, fontSize = 13.sp)
        OutlinedTextField(value = code, onValueChange = { code = it.take(64); notice = null }, singleLine = true, enabled = !busy,
            modifier = Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.unlock_field)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = GothicColors.Parchment, unfocusedTextColor = GothicColors.Parchment,
                focusedBorderColor = GothicColors.Gold, unfocusedBorderColor = GothicColors.Gold.copy(alpha = 0.4f),
                focusedLabelColor = GothicColors.Gold, unfocusedLabelColor = GothicColors.Muted, cursorColor = GothicColors.Gold))
        SimpleSetupButton(stringResource(if (busy) R.string.unlock_busy else R.string.unlock_redeem), submit, Modifier.fillMaxWidth(), primary = true,
            enabled = !busy && code.isNotBlank())
        notice?.let { Text(stringResource(it), color = if (it == R.string.unlock_invalid || it == R.string.unlock_save_failed) GothicColors.Alert else GothicColors.Gold, fontSize = 14.sp) }
    }
}
