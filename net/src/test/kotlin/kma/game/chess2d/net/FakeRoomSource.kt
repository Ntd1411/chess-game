package kma.game.chess2d.net

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Nguồn phòng giả, **chỉ dùng trong test JVM**.
 *
 * Mục đích là kiểm tra hành vi của sảnh chờ mà không mở socket nào: bắt UDP thật
 * trong test vừa chậm vừa phụ thuộc vào máy đang chạy có bị tường lửa chặn hay không.
 *
 * [discover] cố tình treo cho tới khi bị huỷ để giống hệt hợp đồng của [RoomSource]:
 * nếu nó trả về ngay thì test sẽ bỏ sót lỗi "quên huỷ job quét".
 */
class FakeRoomSource(
    override val id: String = "fake",
    initialRooms: List<RoomInfo> = emptyList(),
) : RoomSource {

    private val state = MutableStateFlow(initialRooms)

    override val rooms: Flow<List<RoomInfo>> = state.asStateFlow()

    /** Số lần [discover] được gọi, để test biết sảnh có quét lại hay không. */
    var discoverCount: Int = 0
        private set

    override suspend fun discover() {
        discoverCount++
        awaitCancellation()
    }

    override fun clear() {
        state.value = emptyList()
    }

    /** Đẩy một beacon giả vào nguồn, định danh bằng mã phòng giống hệt bản thật. */
    fun publish(room: RoomInfo) {
        state.value = state.value.filterNot { it.roomId == room.roomId } + room
    }

    fun publishAll(rooms: List<RoomInfo>) {
        state.value = rooms
    }
}

/** Một phòng giả vừa đủ trường để test đọc cho gọn. */
fun fakeRoom(
    roomId: String,
    hostName: String = "Phòng $roomId",
    address: String = "192.168.1.10",
    port: Int = 45_455,
    busy: Boolean = false,
    protocolVersion: Int = PROTOCOL_VERSION,
    sourceId: String = "fake",
): RoomInfo = RoomInfo(
    sourceId = sourceId,
    roomId = roomId,
    hostName = hostName,
    address = address,
    port = port,
    busy = busy,
    protocolVersion = protocolVersion,
)
