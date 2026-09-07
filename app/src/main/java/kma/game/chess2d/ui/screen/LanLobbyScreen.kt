package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.lan.LanLobbyUiState
import kma.game.chess2d.net.DiscoveredRoom

/**
 * Sảnh chờ LAN: mở phòng, hoặc chọn một phòng đã thấy trong mạng.
 *
 * Tên người chơi không còn ô nhập ở đây nữa: menu đã nhập một lần rồi, nhập lại ở
 * đây chỉ làm hai chỗ cùng sửa một giá trị. Ở đây chỉ hiện lại tên để người chơi
 * biết đối thủ sẽ thấy mình dưới tên nào.
 */
@Composable
fun LanLobbyScreen(
    state: LanLobbyUiState,
    localAddresses: List<String>,
    onHost: () -> Unit,
    onJoin: (DiscoveredRoom) -> Unit,
    onManualAddressChange: (String) -> Unit,
    onManualJoin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Khối kết nối thủ công gập lại mặc định: nó là đường dự phòng khi Wi-Fi chặn
    // broadcast, không phải việc đầu tiên người chơi nên làm.
    var manualOpen by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.lan_title),
            style = MaterialTheme.typography.headlineSmall,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerAvatar(name = state.localName)
            Column {
                Text(text = state.localName, style = MaterialTheme.typography.bodyLarge)
                if (localAddresses.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.lan_my_address,
                            localAddresses.joinToString(", "),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Button(onClick = onHost, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Text(
                text = stringResource(R.string.lan_host_room),
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Text(
            text = stringResource(R.string.lan_rooms_title),
            style = MaterialTheme.typography.titleMedium,
        )

        // Quét chạy liên tục suốt lúc ở sảnh, nên thanh tiến trình luôn hiện: nó trả lời
        // câu "app treo hay đang tìm?" mà danh sách trống không trả lời được.
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.lan_scanning, state.rooms.size),
            style = MaterialTheme.typography.bodySmall,
        )

        if (state.rooms.isEmpty()) {
            EmptyRoomsHint()
        } else {
            for (room in state.rooms) {
                RoomCard(room = room, onJoin = { onJoin(room) })
            }
        }

        TextButton(onClick = { manualOpen = !manualOpen }) {
            Text(stringResource(R.string.lan_manual_section))
        }
        if (manualOpen) {
            OutlinedTextField(
                value = state.manualAddress,
                onValueChange = onManualAddressChange,
                label = { Text(stringResource(R.string.lan_manual_label)) },
                placeholder = { Text(stringResource(R.string.lan_manual_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onManualJoin, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.lan_manual_join))
            }
        }

        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_back))
        }
    }
}

/**
 * Một phòng thấy được trong mạng.
 *
 * Badge nói rõ vì sao nút Vào phòng bị khoá: phòng đầy, hay hai máy lệch phiên bản.
 * Trước đây nút chỉ xám đi mà không nói lý do.
 */
@Composable
private fun RoomCard(room: DiscoveredRoom, onJoin: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PlayerAvatar(name = room.hostName)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = room.hostName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(badgeOf(room)),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(onClick = onJoin, enabled = room.joinable) {
                Text(stringResource(R.string.lan_join))
            }
        }
    }
}

/** Nhãn trạng thái của phòng. */
private fun badgeOf(room: DiscoveredRoom): Int = when {
    !room.compatible -> R.string.lan_badge_version
    room.busy -> R.string.lan_badge_playing
    else -> R.string.lan_badge_open
}

/**
 * Ba bước gợi ý khi không thấy phòng nào.
 *
 * Bước hotspot là bắt buộc phải có: nhiều Wi-Fi trường và quán cà phê bật AP
 * isolation, hai máy cùng mạng nhưng không bao giờ thấy nhau, và không câu chữ nào
 * trong app sửa được điều đó.
 */
@Composable
private fun EmptyRoomsHint() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = stringResource(R.string.lan_empty_hint_title),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(R.string.lan_empty_hint_1),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.lan_empty_hint_2),
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = stringResource(R.string.lan_empty_hint_3),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
