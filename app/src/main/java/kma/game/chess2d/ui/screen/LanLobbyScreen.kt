package kma.game.chess2d.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.lan.LanLobbyUiState
import kma.game.chess2d.net.RoomInfo
import kma.game.chess2d.net.TimeControl
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.GothicTextField
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Bố cục sảnh LAN theo bản thiết kế 1080x1920. */
internal object LanLobbyLayout {
    val CodePanel = LobbyLayout.Box(90, 230, 900, 190)
    val CopyIcon = LobbyLayout.Box(820, 290, 70, 70)
    val YouSlot = LobbyLayout.Box(90, 480, 900, 170)
    val OpponentSlot = LobbyLayout.Box(90, 680, 900, 170)
    val Start = LobbyLayout.Box(150, 1450, 780, 110)
    val Leave = LobbyLayout.Box(150, 1580, 780, 100)

    /** Danh sách phòng bắt đầu ngay dưới ô chọn thể thức, hoặc dưới ô đối thủ khi đang mở phòng. */
    fun roomsTop(hosting: Boolean): Int = if (hosting) 880 else 1080
    const val ROOMS_BOTTOM = 1430
}

/**
 * Sảnh chờ LAN theo bản thiết kế: nền `bg_lan_lobby`, ô địa chỉ phòng `lan_room_code_panel` kèm
 * nút sao chép `lan_copy_icon`, hai ô người chơi `lan_player_slot` (miện vương `lan_host_crown`
 * cạnh tên chủ phòng, `lan_ready_indicator`/`lan_waiting_indicator` cạnh trạng thái), danh sách
 * phòng trong mạng cuộn được, nút chính Mở phòng và nút phụ Rời phòng / về Sảnh.
 *
 * Tên người chơi nhập ở Cài đặt của Sảnh, ở đây chỉ hiện lại. Ván tự bắt đầu khi có người vào
 * phòng, nên lúc đang chờ thì nút chính tắt và ghi "Đang chờ người vào…".
 */
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
    // Khối kết nối thủ công gập lại mặc định: nó là đường dự phòng khi Wi-Fi chặn broadcast.
    var manualOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Địa chỉ để đọc cho người đối diện: có cổng khi đang mở phòng, không thì chỉ IP của máy.
    val address = when {
        hosting && hostPort > 0 && localAddresses.isNotEmpty() ->
            stringResource(R.string.lan_room_address, localAddresses.first(), hostPort)
        hosting -> null
        else -> localAddresses.firstOrNull()
    }
    val copiedText = stringResource(R.string.lan_address_copied)

    DesignCanvas(background = R.drawable.bg_lan_lobby, dim = 0.35f, modifier = modifier) {
        // Đọc tỷ lệ bên trong DesignCanvas: bên ngoài chỉ có giá trị mặc định 1:1.
        val d = LocalDesign.current
        SubScreenHeader(title = stringResource(R.string.lan_title), onBack = onOpenMenu)

        // Ô địa chỉ phòng.
        val code = LanLobbyLayout.CodePanel
        SlicedArt(res = R.drawable.lan_room_code_panel, slice = 0.3f, modifier = Modifier.at(code.x, code.y, code.w, code.h))
        DesignText(
            text = stringResource(if (hosting) R.string.lan_your_room else R.string.lan_address_title),
            size = 30,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.at(140, 250, 660, 56),
        )
        DesignText(
            text = address ?: stringResource(
                if (hosting) R.string.lan_room_address_unknown else R.string.lan_no_address,
            ),
            size = 48,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            maxLines = 1,
            modifier = Modifier.at(140, 305, 660, 90),
        )
        if (address != null) {
            val copy = LanLobbyLayout.CopyIcon
            ArtImage(
                res = R.drawable.lan_copy_icon,
                maxEdge = ArtSizes.ICON,
                trim = true,
                contentDescription = stringResource(R.string.lan_copy_address),
                modifier = Modifier.at(copy.x, copy.y, copy.w, copy.h).clip(CircleShape)
                    .clickable(role = Role.Button) { copyAddress(context, address, copiedText) },
            )
        }

        // Hai ô người chơi: mình luôn sẵn sàng, ô thứ hai chờ người vào (khi đang mở phòng).
        PlayerSlot(
            box = LanLobbyLayout.YouSlot,
            portrait = R.drawable.portrait_protagonist,
            name = state.localName,
            status = if (hosting) {
                stringResource(R.string.lan_ready) + " · " + stringResource(timeControlLabel(state.timeControl))
            } else {
                stringResource(R.string.lan_ready)
            },
            ready = true,
            host = hosting,
        )
        if (hosting) {
            PlayerSlot(
                box = LanLobbyLayout.OpponentSlot,
                portrait = R.drawable.portrait_lan_opponent,
                name = stringResource(R.string.match_waiting_opponent),
                status = stringResource(R.string.lan_room_waiting),
                ready = false,
                host = false,
            )
        } else {
            // Chọn thể thức trước khi mở phòng: host giữ đồng hồ và gửi con số này khi bắt tay.
            TimeControlPicker(selected = state.timeControl, onSelect = onTimeControlChange)
        }

        // Danh sách phòng trong mạng + kết nối thủ công, cuộn được.
        val top = LanLobbyLayout.roomsTop(hosting)
        Column(
            modifier = Modifier.at(90, top, 900, LanLobbyLayout.ROOMS_BOTTOM - top)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(d.dp(14)),
        ) {
            Row(modifier = Modifier.fillMaxWidth().height(d.dp(64)), verticalAlignment = Alignment.CenterVertically) {
                DesignText(
                    text = stringResource(R.string.lan_rooms_title),
                    size = 36,
                    color = GothicColors.Gold,
                    weight = FontWeight.Bold,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                ArtImage(
                    res = R.drawable.lan_waiting_indicator,
                    maxEdge = ArtSizes.ICON,
                    trim = true,
                    modifier = Modifier.size(d.dp(44)),
                )
                DesignText(
                    text = state.rooms.size.toString(),
                    size = 32,
                    modifier = Modifier.width(d.dp(60)),
                )
            }
            if (state.rooms.isEmpty()) {
                EmptyRoomsHint()
            } else {
                // Khoá của mỗi thẻ là `room.key` (nguồn + mã phòng), không phải IP.
                for (room in state.rooms) {
                    key(room.key) { RoomRow(room = room, onJoin = { onJoin(room) }) }
                }
            }
            DesignText(
                text = stringResource(R.string.lan_manual_section) + if (manualOpen) " ▴" else " ▾",
                size = 32,
                color = GothicColors.Gold,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                modifier = Modifier.fillMaxWidth().height(d.dp(80))
                    .clickable(role = Role.Button) { manualOpen = !manualOpen },
            )
            if (manualOpen) {
                GothicTextField(
                    value = state.manualAddress,
                    onValueChange = onManualAddressChange,
                    placeholder = stringResource(R.string.lan_manual_hint),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    modifier = Modifier.fillMaxWidth().height(d.dp(110)),
                )
                GothicButton(
                    label = stringResource(R.string.lan_manual_join),
                    onClick = onManualJoin,
                    art = ButtonArt.SECONDARY,
                    modifier = Modifier.fillMaxWidth().height(d.dp(100)),
                )
            }
        }

        val start = LanLobbyLayout.Start
        GothicButton(
            label = stringResource(if (hosting) R.string.lan_room_waiting else R.string.lan_host_room),
            onClick = onHost,
            enabled = !hosting,
            modifier = Modifier.at(start.x, start.y, start.w, start.h),
        )
        val leave = LanLobbyLayout.Leave
        GothicButton(
            label = stringResource(if (hosting) R.string.lan_cancel_room else R.string.result_to_lobby),
            onClick = if (hosting) onCancelHosting else onOpenMenu,
            art = ButtonArt.SECONDARY,
            modifier = Modifier.at(leave.x, leave.y, leave.w, leave.h),
        )
    }
}

private fun copyAddress(context: Context, address: String, toast: String) {
    val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("LAN", address))
    Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
}

/** Một ô `lan_player_slot`: chân dung, tên (kèm miện vương nếu là chủ phòng) và dòng trạng thái. */
@Composable
private fun PlayerSlot(
    box: LobbyLayout.Box,
    portrait: Int,
    name: String,
    status: String,
    ready: Boolean,
    host: Boolean,
) {
    SlicedArt(res = R.drawable.lan_player_slot, slice = 0.3f, modifier = Modifier.at(box.x, box.y, box.w, box.h))
    Box(modifier = Modifier.at(box.x + 25, box.y + 20, 130, 130)) {
        PortraitInFrame(portrait = portrait, dimmed = !ready)
    }
    DesignText(
        text = name,
        size = 40,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(box.x + 180, box.y + 22, 560, 66),
    )
    if (host) {
        ArtImage(
            res = R.drawable.lan_host_crown,
            maxEdge = ArtSizes.ICON,
            trim = true,
            contentDescription = stringResource(R.string.lan_host_badge),
            modifier = Modifier.at(box.x + 770, box.y + 25, 80, 60),
        )
    }
    ArtImage(
        res = if (ready) R.drawable.lan_ready_indicator else R.drawable.lan_waiting_indicator,
        maxEdge = ArtSizes.ICON,
        trim = true,
        modifier = Modifier.at(box.x + 180, box.y + 100, 44, 44),
    )
    DesignText(
        text = status,
        size = 30,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(box.x + 240, box.y + 92, 620, 60),
    )
}

/**
 * Ba thể thức thời gian, chọn một: ba nút ngắn chia đều một hàng (nút đang chọn là nút chính),
 * câu đầy đủ của thể thức đang chọn ghi ngay bên dưới.
 */
@Composable
private fun TimeControlPicker(selected: TimeControl, onSelect: (TimeControl) -> Unit) {
    DesignText(
        text = stringResource(R.string.lan_time_control),
        size = 34,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(90, 690, 900, 60),
    )
    TimeControl.entries.forEachIndexed { i, control ->
        GothicButton(
            label = stringResource(timeControlShortLabel(control)),
            onClick = { onSelect(control) },
            art = if (control == selected) ButtonArt.PRIMARY else ButtonArt.SECONDARY,
            modifier = Modifier.at(90 + i * 305, 765, 290, 100),
        )
    }
    DesignText(
        text = stringResource(timeControlLabel(selected)),
        size = 30,
        maxLines = 1,
        modifier = Modifier.at(90, 880, 900, 60),
    )
    ArtImage(
        res = R.drawable.ui_separator,
        maxEdge = ArtSizes.BUTTON,
        trim = true,
        modifier = Modifier.at(140, 960, 800, 40),
    )
}

/** Nhãn tiếng Việt đầy đủ của từng thể thức. */
internal fun timeControlLabel(control: TimeControl): Int = when (control) {
    TimeControl.UNLIMITED -> R.string.time_control_unlimited
    TimeControl.BLITZ -> R.string.time_control_blitz
    TimeControl.RAPID -> R.string.time_control_rapid
}

/** Nhãn ngắn, dùng cho hàng nút chọn thể thức. */
private fun timeControlShortLabel(control: TimeControl): Int = when (control) {
    TimeControl.UNLIMITED -> R.string.time_control_unlimited_short
    TimeControl.BLITZ -> R.string.time_control_blitz_short
    TimeControl.RAPID -> R.string.time_control_rapid_short
}

/**
 * Một phòng thấy được trong mạng, trên nền `ui_list_row_normal` (đang nhấn thì `ui_list_row_pressed`).
 * Chạm cả dòng cũng là Vào phòng nếu phòng vào được.
 *
 * Nhãn nói rõ vì sao nút Vào bị khoá: phòng đầy, hay hai máy lệch phiên bản.
 */
@Composable
private fun RoomRow(room: RoomInfo, onJoin: () -> Unit) {
    val d = LocalDesign.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = Modifier.fillMaxWidth().height(d.dp(150)).clickable(
            interactionSource = interaction,
            indication = null,
            enabled = room.joinable,
            role = Role.Button,
            onClick = onJoin,
        ),
    ) {
        SlicedArt(
            res = if (pressed) R.drawable.ui_list_row_pressed else R.drawable.ui_list_row_normal,
            slice = 0.3f,
            modifier = Modifier.fillMaxSize(),
        )
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = d.dp(36)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(d.dp(20)),
        ) {
            Box(modifier = Modifier.size(d.dp(100))) {
                PortraitInFrame(portrait = R.drawable.portrait_lan_opponent, dimmed = !room.joinable)
            }
            Column(modifier = Modifier.weight(1f)) {
                DesignText(
                    text = room.hostName,
                    size = 36,
                    color = GothicColors.Gold,
                    weight = FontWeight.Bold,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
                DesignText(
                    text = stringResource(badgeOf(room)),
                    size = 28,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            GothicButton(
                label = stringResource(R.string.lan_join),
                onClick = onJoin,
                art = ButtonArt.SMALL,
                enabled = room.joinable,
                modifier = Modifier.width(d.dp(200)).height(d.dp(84)),
            )
        }
    }
}

/** Nhãn trạng thái của phòng. */
private fun badgeOf(room: RoomInfo): Int = when {
    !room.compatible -> R.string.lan_badge_version
    room.busy -> R.string.lan_badge_playing
    else -> R.string.lan_badge_open
}

/**
 * Ba bước gợi ý khi không thấy phòng nào. Bước hotspot bắt buộc phải có: nhiều Wi-Fi công cộng
 * bật AP isolation, hai máy cùng mạng nhưng không bao giờ thấy nhau.
 */
@Composable
private fun EmptyRoomsHint() {
    val d = LocalDesign.current
    Box(modifier = Modifier.fillMaxWidth()) {
        SlicedArt(res = R.drawable.ui_panel_sub, slice = 0.2f, modifier = Modifier.matchParentSize())
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = d.dp(40), vertical = d.dp(30)),
            verticalArrangement = Arrangement.spacedBy(d.dp(8)),
        ) {
            for (line in listOf(
                R.string.lan_empty_hint_title,
                R.string.lan_empty_hint_1,
                R.string.lan_empty_hint_2,
                R.string.lan_empty_hint_3,
            )) {
                DesignText(
                    text = stringResource(line),
                    size = 28,
                    align = TextAlign.Start,
                    alignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
