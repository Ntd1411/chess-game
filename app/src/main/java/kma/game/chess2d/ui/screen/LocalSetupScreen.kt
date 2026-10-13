package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.game.LocalSetup
import kma.game.chess2d.game.LocalTimeControl
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.board.pieceImageRes
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Drawn local setup with safe edge anchors, scrollable cards and keyboard-aware inputs. */
@Composable
fun LocalSetupScreen(
    initialWhiteName: String,
    onStart: (LocalSetup) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var whiteInput by rememberSaveable { mutableStateOf(initialWhiteName) }
    var blackInput by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf(LocalTimeControl.UNLIMITED) }
    val defaultWhite = stringResource(R.string.match_player_one)
    val defaultBlack = stringResource(R.string.match_player_two)
    val blackFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val closeKeyboard = {
        focusManager.clearFocus()
        keyboard?.hide()
        Unit
    }
    val leave = {
        closeKeyboard()
        onBack()
    }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.bg_local_setup, dim = 0.6f)
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().imePadding()
                .padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SimpleSetupHeader(
                title = stringResource(R.string.local_setup_title),
                backLabel = stringResource(R.string.local_setup_back),
                onBack = leave,
                titleSize = 18.sp,
            )
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp),
                contentAlignment = Alignment.Center) {
                Column(
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    PlayerPanel(
                        title = defaultWhite,
                        portrait = R.drawable.portrait_protagonist,
                        white = true,
                        value = whiteInput,
                        onValueChange = { whiteInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
                        placeholder = defaultWhite,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { blackFocus.requestFocus() }),
                    )
                    PlayerPanel(
                        title = defaultBlack,
                        portrait = R.drawable.portrait_player_two,
                        white = false,
                        value = blackInput,
                        onValueChange = { blackInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
                        placeholder = defaultBlack,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { closeKeyboard() }),
                        fieldModifier = Modifier.focusRequester(blackFocus),
                    )
                    TimePanel(current = time, onChosen = { time = it })
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SimpleSetupButton(
                    label = stringResource(R.string.local_setup_start),
                    primary = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        closeKeyboard()
                        onStart(LocalSetup.of(whiteInput, blackInput, time, defaultWhite, defaultBlack))
                    },
                )
                SimpleSetupButton(stringResource(R.string.local_setup_back), onClick = leave,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LocalSetupCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(GothicColors.Ink.copy(alpha = 0.86f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.4f), shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

/** Names remain optional and retain the same normalization and length limit as before. */
@Composable
private fun PlayerPanel(
    title: String,
    @DrawableRes portrait: Int,
    white: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    fieldModifier: Modifier = Modifier,
) {
    val side = stringResource(if (white) R.string.side_white else R.string.side_black)
    LocalSetupCard {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ArtImage(portrait, maxEdge = ArtSizes.PORTRAIT, contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(52.dp).clip(CircleShape)
                    .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), CircleShape))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = GothicColors.Gold, fontFamily = GameFontFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(side, color = GothicColors.Muted, fontFamily = GameFontFamily, fontSize = 13.sp)
            }
            ArtImage(pieceImageRes(Piece.of(Piece.KING, white)), maxEdge = ArtSizes.PIECE,
                trim = true, contentDescription = side, modifier = Modifier.size(32.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.local_setup_name_label)) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = TextStyle(fontFamily = GameFontFamily, fontSize = 16.sp),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GothicColors.Parchment,
                unfocusedTextColor = GothicColors.Parchment,
                cursorColor = GothicColors.Gold,
                focusedBorderColor = GothicColors.Gold,
                unfocusedBorderColor = GothicColors.Gold.copy(alpha = 0.35f),
                focusedLabelColor = GothicColors.Gold,
                unfocusedLabelColor = GothicColors.Muted,
                focusedPlaceholderColor = GothicColors.Muted,
                unfocusedPlaceholderColor = GothicColors.Muted,
                focusedContainerColor = GothicColors.Ink.copy(alpha = 0.5f),
                unfocusedContainerColor = GothicColors.Ink.copy(alpha = 0.5f),
            ),
            modifier = fieldModifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.local_setup_name_hint), color = GothicColors.Muted,
            fontFamily = GameFontFamily, fontSize = 12.sp)
    }
}

@Composable
private fun TimePanel(current: LocalTimeControl, onChosen: (LocalTimeControl) -> Unit) {
    LocalSetupCard {
        Text(stringResource(R.string.local_setup_time), color = GothicColors.Gold,
            fontFamily = GameFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Column(modifier = Modifier.fillMaxWidth().selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LocalTimeControl.entries.chunked(2).forEach { options ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    options.forEach { option ->
                        SimpleSetupButton(
                            label = timeLabel(option),
                            onClick = { onChosen(option) },
                            selected = option == current,
                            maxLines = 2,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        Text(stringResource(R.string.local_setup_time_note), color = GothicColors.Muted,
            fontFamily = GameFontFamily, fontSize = 13.sp)
    }
}

@Composable
internal fun timeLabel(time: LocalTimeControl): String =
    if (time.limited) stringResource(R.string.local_time_minutes, time.minutes)
    else stringResource(R.string.local_time_unlimited)
