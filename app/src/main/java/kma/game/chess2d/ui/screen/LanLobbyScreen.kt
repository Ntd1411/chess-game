package kma.game.chess2d.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.lan.LanLobbyUiState
import kma.game.chess2d.net.RoomInfo
import kma.game.chess2d.net.TimeControl
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Responsive LAN setup. Network callbacks and automatic match start remain unchanged. */
@Composable
fun LanLobbyScreen(
    state: LanLobbyUiState,
    localAddresses: List<String>,
    onHost: () -> Unit,
    onJoin: (RoomInfo) -> Unit,
    onManualAddressChange: (String) -> Unit,
    onManualJoin: () -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
    hosting: Boolean = false,
    hostPort: Int = 0,
    onCancelHosting: () -> Unit = {},
    onTimeControlChange: (TimeControl) -> Unit = {},
) {
    var manualOpen by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    fun closeKeyboard() {
        focus.clearFocus()
        keyboard?.hide()
    }
    val address = when {
        hosting && hostPort > 0 && localAddresses.isNotEmpty() ->
            stringResource(R.string.lan_room_address, localAddresses.first(), hostPort)
        hosting -> null
        else -> localAddresses.firstOrNull()
    }
    val copiedText = stringResource(R.string.lan_address_copied)
    val manualJoin = {
        if (state.manualAddress.isNotBlank()) {
            closeKeyboard()
            onManualJoin()
        }
    }

    SimpleSetupPage(
        background = R.drawable.bg_lan_lobby,
        title = stringResource(R.string.lan_title),
        backLabel = stringResource(R.string.action_back),
        onBack = { closeKeyboard(); onOpenMenu() },
        modifier = modifier,
        keyboardAware = true,
        footer = {
            SimpleSetupButton(
                stringResource(if (hosting) R.string.lan_room_waiting else R.string.lan_host_room),
                onClick = { if (!hosting) { closeKeyboard(); onHost() } },
                primary = true, enabled = !hosting, maxLines = 2, modifier = Modifier.fillMaxWidth())
            SimpleSetupButton(
                stringResource(if (hosting) R.string.lan_cancel_room else R.string.result_to_lobby),
                onClick = { closeKeyboard(); if (hosting) onCancelHosting() else onOpenMenu() },
                modifier = Modifier.fillMaxWidth())
        },
    ) {
        SimpleSetupCard {
            SectionLabel(stringResource(if (hosting) R.string.lan_your_room else R.string.lan_address_title))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(address ?: stringResource(if (hosting) R.string.lan_room_address_unknown else R.string.lan_no_address),
                    modifier = Modifier.weight(1f), color = GothicColors.Gold,
                    fontFamily = if (address != null) FontFamily.Monospace else GameFontFamily,
                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                val copyLabel = stringResource(R.string.lan_copy_address)
                IconButton(onClick = { address?.let { copyAddress(context, it, copiedText) } },
                    enabled = address != null,
                    modifier = Modifier.size(48.dp).semantics { contentDescription = copyLabel }) {
                    Canvas(Modifier.size(24.dp)) {
                        val color = GothicColors.Gold.copy(alpha = if (address != null) 1f else 0.35f)
                        val stroke = Stroke(1.5.dp.toPx())
                        drawRect(color, Offset(size.width * 0.12f, size.height * 0.12f),
                            Size(size.width * 0.58f, size.height * 0.65f), style = stroke)
                        val front = Offset(size.width * 0.3f, size.height * 0.3f)
                        val rectangle = Size(size.width * 0.58f, size.height * 0.65f)
                        drawRect(GothicColors.Ink, front, rectangle)
                        drawRect(color, front, rectangle, style = stroke)
                    }
                }
            }
        }
        PlayerSlot(portrait = R.drawable.portrait_protagonist, name = state.localName,
            status = if (hosting) stringResource(R.string.lan_ready) + " · " + stringResource(timeControlLabel(state.timeControl))
                else stringResource(R.string.lan_ready), ready = true, host = hosting)
        if (hosting) {
            PlayerSlot(portrait = R.drawable.portrait_lan_opponent,
                name = stringResource(R.string.match_waiting_opponent),
                status = stringResource(R.string.lan_room_waiting), ready = false, host = false)
        } else {
            SimpleSetupCard {
                SectionLabel(stringResource(R.string.lan_time_control))
                Row(modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeControl.entries.forEach { control ->
                        SimpleSetupButton(stringResource(timeControlShortLabel(control)),
                            onClick = { onTimeControlChange(control) }, selected = control == state.timeControl,
                            modifier = Modifier.weight(1f))
                    }
                }
                BodyText(stringResource(timeControlLabel(state.timeControl)))
            }
        }
        SimpleSetupCard {
            SectionLabel(stringResource(R.string.lan_rooms_title) + " · " + state.rooms.size)
            if (state.rooms.isEmpty()) {
                BodyText(stringResource(R.string.lan_empty_hint_title))
                listOf(R.string.lan_empty_hint_1, R.string.lan_empty_hint_2, R.string.lan_empty_hint_3).forEach {
                    BodyText(stringResource(it))
                }
            } else {
                // Keep discovery identity stable even when a room changes address.
                state.rooms.forEach { room ->
                    key(room.key) {
                        RoomRow(room) { closeKeyboard(); onJoin(room) }
                    }
                }
            }
        }
        SimpleSetupCard {
            Text(stringResource(R.string.lan_manual_section) + if (manualOpen) " ▴" else " ▾",
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    .clickable(role = Role.Button) { manualOpen = !manualOpen }.padding(vertical = 12.dp),
                color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold)
            if (manualOpen) {
                OutlinedTextField(value = state.manualAddress, onValueChange = onManualAddressChange,
                    label = { Text(stringResource(R.string.lan_manual_label)) },
                    placeholder = { Text(stringResource(R.string.lan_manual_hint)) }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = { manualJoin() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GothicColors.Parchment, unfocusedTextColor = GothicColors.Parchment,
                        focusedBorderColor = GothicColors.Gold, unfocusedBorderColor = GothicColors.Gold.copy(alpha = 0.4f),
                        focusedLabelColor = GothicColors.Gold, unfocusedLabelColor = GothicColors.Muted,
                        cursorColor = GothicColors.Gold, unfocusedPlaceholderColor = GothicColors.Muted,
                        focusedPlaceholderColor = GothicColors.Muted),
                    modifier = Modifier.fillMaxWidth())
                SimpleSetupButton(stringResource(R.string.lan_manual_join), onClick = manualJoin,
                    enabled = state.manualAddress.isNotBlank(), maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

private fun copyAddress(context: Context, address: String, toast: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("LAN", address))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

@Composable
private fun PlayerSlot(portrait: Int, name: String, status: String, ready: Boolean, host: Boolean) {
    SimpleSetupCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ArtImage(portrait, maxEdge = ArtSizes.PORTRAIT, contentScale = ContentScale.Crop,
                alpha = if (ready) 1f else 0.5f, modifier = Modifier.size(52.dp).clip(CircleShape))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, color = GothicColors.Gold, fontFamily = GameFontFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (host) BodyText(stringResource(R.string.lan_host_badge))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape)
                        .background(if (ready) GothicColors.Gold else GothicColors.Muted))
                    BodyText(status)
                }
            }
        }
    }
}

@Composable
private fun RoomRow(room: RoomInfo, onJoin: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(modifier = Modifier.fillMaxWidth().clip(shape).background(GothicColors.Ink.copy(alpha = 0.6f))
        .border(1.dp, GothicColors.Gold.copy(alpha = 0.25f), shape)
        .clickable(enabled = room.joinable, role = Role.Button, onClick = onJoin).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(room.hostName, color = GothicColors.Gold, fontFamily = GameFontFamily,
                fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            BodyText(stringResource(lanRoomBadge(room)))
            Text("${room.address}:${room.port}", color = GothicColors.Muted,
                fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        SimpleSetupButton(stringResource(R.string.lan_join), onJoin,
            enabled = room.joinable, modifier = Modifier.width(68.dp))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = GothicColors.Gold, fontFamily = GameFontFamily,
        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun BodyText(text: String) {
    Text(text, color = GothicColors.Muted, fontFamily = GameFontFamily, fontSize = 14.sp)
}

internal fun timeControlLabel(control: TimeControl): Int = when (control) {
    TimeControl.UNLIMITED -> R.string.time_control_unlimited
    TimeControl.BLITZ -> R.string.time_control_blitz
    TimeControl.RAPID -> R.string.time_control_rapid
}

private fun timeControlShortLabel(control: TimeControl): Int = when (control) {
    TimeControl.UNLIMITED -> R.string.time_control_unlimited_short
    TimeControl.BLITZ -> R.string.time_control_blitz_short
    TimeControl.RAPID -> R.string.time_control_rapid_short
}

/** Version mismatch takes priority over occupancy so the disabled reason is accurate. */
internal fun lanRoomBadge(room: RoomInfo): Int = when {
    !room.compatible -> R.string.lan_badge_version
    room.busy -> R.string.lan_badge_playing
    else -> R.string.lan_badge_open
}
