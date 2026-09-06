package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.lan.LanLobbyUiState
import kma.game.chess2d.net.DiscoveredRoom

/**
 * Sảnh chể LAN: mở phòng, hoặc chọn một phòng đã thấy trong mạng.
 *
 * Danh sách phòng tự cập nhật: phòng ngừng phát beacon sẽ tự biến mất sau vài giây,
 * nên không cần nút "làm mới".
 */
@Composable
fun LanLobbyScreen(
    state: LanLobbyUiState,
    onNameChange: (String) -> Unit,
    onHost: () -> Unit,
    onJoin: (DiscoveredRoom) -> Unit,
    onManualAddressChange: (String) -> Unit,
    onManualJoin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            Text(
                text = stringResource(R.string.lan_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        OutlinedTextField(
            value = state.localName,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.lan_your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(onClick = onHost, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.lan_host_room))
        }

        Text(
            text = stringResource(R.string.lan_rooms_title),
            style = MaterialTheme.typography.titleSmall,
        )

        // Danh sách chiếm phần cao còn lại, phần gõ địa chỉ luôn dính đáy.
        if (state.rooms.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.lan_no_rooms),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.rooms, key = { it.roomId }) { room ->
                    RoomRow(room = room, onJoin = { onJoin(room) })
                }
            }
        }

        OutlinedTextField(
            value = state.manualAddress,
            onValueChange = onManualAddressChange,
            label = { Text(stringResource(R.string.lan_manual_label)) },
            placeholder = { Text(stringResource(R.string.lan_manual_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedButton(onClick = onManualJoin, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.lan_manual_join))
        }
    }
}

/** Một phòng trong danh sách. Nút vào bị tắt khi phòng đầy hoặc khác phiên bản. */
@Composable
private fun RoomRow(room: DiscoveredRoom, onJoin: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = room.hostName, style = MaterialTheme.typography.bodyLarge)
                val detail = when {
                    !room.compatible -> stringResource(R.string.lan_room_incompatible)
                    room.busy -> stringResource(R.string.lan_room_busy)
                    else -> "${room.host}:${room.gamePort}"
                }
                Text(text = detail, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onJoin, enabled = room.joinable) {
                Text(stringResource(R.string.lan_join))
            }
        }
    }
}
