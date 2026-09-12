package kma.game.chess2d.net

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Một phòng chơi hiển thị được ở sảnh, không phụ thuộc vào cách tìm ra nó.
 *
 * Phòng được định danh bằng [roomId] chứ **không** bằng địa chỉ IP. Lý do rất thực tế:
 * điện thoại đổi IP mỗi lần xin lại DHCP hoặc chuyển giữa hai dải sóng của cùng một
 * router, nên lấy IP làm khoá thì cùng một phòng sẽ hiện ra hai lần trong sảnh. Ngược
 * lại, hai máy khác nhau không bao giờ trùng [roomId] vì nó sinh từ UUID.
 *
 * [protocolVersion] được giữ lại thay vì lọc bỏ ngay: phòng lệch phiên bản vẫn phải
 * hiện ra để sảnh giải thích được vì sao không vào được, thay vì ẩn đi để người dùng
 * tự đoán.
 *
 * @param sourceId nguồn đã tìm ra phòng này; hiện chỉ có LAN.
 * @param address địa chỉ để nối tới. Là dữ liệu để kết nối, không phải khoá định danh.
 */
data class RoomInfo(
    val sourceId: String,
    val roomId: String,
    val hostName: String,
    val address: String,
    val port: Int,
    val busy: Boolean,
    val protocolVersion: Int = PROTOCOL_VERSION,
    val lastSeenMillis: Long = 0,
) {

    /** Khoá duy nhất của phòng trong sảnh: nguồn + mã phòng, tuyệt đối không có IP. */
    val key: String get() = "$sourceId/$roomId"

    val compatible: Boolean get() = protocolVersion == PROTOCOL_VERSION

    val joinable: Boolean get() = compatible && !busy
}

/**
 * Một nơi có thể lấy ra danh sách phòng chơi.
 *
 * Tách ra thành giao diện để sảnh chờ không biết phòng đến từ đâu: hôm nay chỉ có
 * LAN ([LanRoomSource]), và test JVM dùng một nguồn giả để kiểm tra hành vi của sảnh
 * mà không cần mở socket nào. Đây **chỉ** là chỗ móc sẵn cho tương lai — trong phạm
 * vi này không có một dòng nào cho server online.
 *
 * [rooms] là Flow chứ không phải danh sách chụp một lần: phòng xuất hiện và biến mất
 * liên tục, sảnh phải cập nhật theo chứ không thể bắt người dùng bấm làm mới.
 */
interface RoomSource {

    /** Mã nguồn, cũng là phần đầu của [RoomInfo.key]. */
    val id: String

    val rooms: Flow<List<RoomInfo>>

    /**
     * Chạy vòng tìm phòng tới khi coroutine bị huỷ.
     *
     * Hàm này không trả về trong lúc bình thường, nên phía gọi phải đặt nó trong một
     * Job riêng để huỷ được khi rời sảnh.
     */
    suspend fun discover()

    /** Xoá danh sách đang có. Gọi khi quay lại sảnh để không hiện phòng đã nguội từ lần trước. */
    fun clear()
}

/** Nguồn phòng qua mạng LAN: bọc lại [RoomScanner] và đổi sang kiểu chung của sảnh. */
class LanRoomSource(
    private val scanner: RoomScanner = RoomScanner(),
) : RoomSource {

    override val id: String get() = SOURCE_ID

    override val rooms: Flow<List<RoomInfo>> =
        scanner.rooms.map { list -> list.map { it.toRoomInfo() } }

    override suspend fun discover() = scanner.run()

    override fun clear() = scanner.clear()

    companion object {
        const val SOURCE_ID: String = "lan"
    }
}

/** Đổi một beacon đã nhận được thành phòng hiển thị ở sảnh. */
fun DiscoveredRoom.toRoomInfo(): RoomInfo = RoomInfo(
    sourceId = LanRoomSource.SOURCE_ID,
    roomId = roomId,
    hostName = hostName,
    address = host,
    port = gamePort,
    busy = busy,
    protocolVersion = protocolVersion,
    lastSeenMillis = lastSeenMillis,
)
