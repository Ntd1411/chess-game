package kma.game.chess2d.net

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Một phòng thấy được trên mạng LAN.
 *
 * [protocolVersion] được giữ lại thay vì lọc bỏ ngay: tiêu chí Phase 4 đòi "hai bản
 * khác protocol version báo lỗi rõ ràng", nên UI cần thấy phòng đó để giải thích,
 * chứ không phải ẩn đi rồi để người dùng tự đoán vì sao không tìm thấy nhau.
 */
data class DiscoveredRoom(
    val roomId: String,
    val hostName: String,
    val host: String,
    val gamePort: Int,
    val busy: Boolean,
    val protocolVersion: Int,
    val lastSeenMillis: Long,
) {
    val compatible: Boolean get() = protocolVersion == PROTOCOL_VERSION
    val joinable: Boolean get() = compatible && !busy
}

/**
 * Phát beacon UDP định kỳ để máy khác tìm thấy phòng.
 *
 * Dùng UDP broadcast thay vì Network Service Discovery của Android là một lựa chọn có
 * chủ ý: NSD đòi quyền NEARBY_WIFI_DEVICES từ Android 13, và quan trọng hơn là không
 * chạy được trên JVM thuần — toàn bộ tầng mạng này phải test được bằng unit test
 * và bằng client dòng lệnh trên PC, không cần hai điện thoại thật.
 *
 * @param beacon là hàm chứ không phải giá trị, để cờ `busy` đổi được ngay khi có khách
 *        vào mà không phải dụng lại advertiser.
 * @param targets để null để tự dò địa chỉ broadcast; test truyền 127.0.0.1 vào đây.
 */
class RoomAdvertiser(
    private val beacon: () -> RoomBeacon,
    private val targets: List<InetAddress>? = null,
    private val port: Int = DISCOVERY_PORT,
) {

    suspend fun run() = withContext(Dispatchers.IO) {
        DatagramSocket().use { socket ->
            socket.broadcast = true
            while (currentCoroutineContext().isActive) {
                val payload = ProtocolJson.encodeBeacon(beacon()).toByteArray()
                // Tính lại đích mỗi vòng: địa chỉ broadcast đổi khi người dùng chuyển Wi-Fi.
                for (target in targets ?: lanBroadcastTargets()) {
                    // Một interface gửi không được thì bỏ qua, các interface còn lại vẫn phải được phát.
                    runCatching { socket.send(DatagramPacket(payload, payload.size, target, port)) }
                }
                delay(LanTiming.BEACON_INTERVAL_MILLIS)
            }
        }
    }
}

/**
 * Lắng nghe beacon và duy trì danh sách phòng đang sống.
 *
 * Phòng không có thông điệp "tắt phòng": host có thể hết pin hoặc ra khỏi vùng phủ
 * sóng. Vì vậy danh sách được dọn theo thời gian: quá [LanTiming.ROOM_STALE_MILLIS]
 * không nghe thấy gì thì phòng tự rạc khỏi danh sách.
 *
 * @param now tách ra để test kiểm tra được việc dọn phòng nguội mà không phải chờ thật.
 */
class RoomScanner(
    private val port: Int = DISCOVERY_PORT,
    private val now: () -> Long = System::currentTimeMillis,
) {

    private val _rooms = MutableStateFlow<List<DiscoveredRoom>>(emptyList())
    val rooms: StateFlow<List<DiscoveredRoom>> = _rooms.asStateFlow()

    /**
     * Cổng thật đang nghe, -1 khi chưa bind xong.
     *
     * Trên thiết bị thật nó luôn bằng [DISCOVERY_PORT]. Nhưng test phải tránh cổng
     * thật để không bắt được beacon của ván đấu đang diễn ra trong cùng mạng, nên
     * nó truyền `port = 0` rồi đọc số thật ở đây — chắc chắn hơn việc tự đoán một
     * cổng rỗi rồi hy vọng nó vẫn rỗi.
     */
    @Volatile
    var boundPort: Int = -1
        private set

    suspend fun run() = withContext(Dispatchers.IO) {
        // Phải copy ra biến cục bộ TRƯỚC khi vào apply: bên trong apply, receiver là
        // DatagramSocket, nên `port` sẽ trỏ tới DatagramSocket.getPort() — cổng của đầu
        // bên kia, bằng -1 khi socket chưa connect — chứ không phải tham số của lớp này.
        // Đó là lý do cũ bind luôn báo "port out of range:-1".
        val bindPort = port
        // DatagramSocket(null) + reuseAddress: bắt buộc phải đặt TRƯỚC khi bind, và cần
        // thiết để hai tiến trình trên cùng một máy (khi test) cùng nghe một cổng.
        val socket = DatagramSocket(null).apply {
            reuseAddress = true
            soTimeout = RECEIVE_POLL_MILLIS
            bind(InetSocketAddress(bindPort))
        }
        // Đọc tại localSocketAddress chứ không dùng getLocalPort(): đây là địa chỉ đã bind
        // thật sự, đúng cả khi bind vào cổng 0.
        boundPort = (socket.localSocketAddress as? InetSocketAddress)?.port ?: bindPort
        val buffer = ByteArray(MAX_BEACON_BYTES)
        socket.use {
            while (currentCoroutineContext().isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                try {
                    socket.receive(packet)
                    remember(packet)
                } catch (_: SocketTimeoutException) {
                    // Hết hạn chờ là bình thường: nó chính là nhịp để dọn phòng nguội và
                    // để vòng lặp kịp thấy coroutine đã bị hủy.
                }
                prune()
            }
        }
    }

    private fun remember(packet: DatagramPacket) {
        val text = String(packet.data, packet.offset, packet.length, Charsets.UTF_8)
        val beacon = ProtocolJson.decodeBeacon(text) ?: return
        val room = DiscoveredRoom(
            roomId = beacon.roomId,
            hostName = beacon.hostName,
            host = packet.address?.hostAddress ?: return,
            gamePort = beacon.gamePort,
            busy = beacon.busy,
            protocolVersion = beacon.protocolVersion,
            lastSeenMillis = now(),
        )
        _rooms.update { current ->
            current.filterNot { it.roomId == room.roomId } + room
        }
    }

    private fun prune() {
        val deadline = now() - LanTiming.ROOM_STALE_MILLIS
        _rooms.update { current -> current.filter { it.lastSeenMillis >= deadline } }
    }

    /** Dùng cho test và cho lúc mở lại màn hình sảnh chờ. */
    fun clear() {
        _rooms.value = emptyList()
    }

    private companion object {
        const val RECEIVE_POLL_MILLIS = 400
        const val MAX_BEACON_BYTES = 2_048
    }
}

/** Cập nhật nguyên tử, tránh mất beacon khi có hai nguồn ghi. */
private inline fun <T> MutableStateFlow<T>.update(transform: (T) -> T) {
    while (true) {
        val current = value
        if (compareAndSet(current, transform(current))) return
    }
}

/**
 * Địa chỉ broadcast của mọi interface đang bật.
 *
 * Phải gửi tới đúng broadcast của từng subnet (ví dụ 192.168.1.255) vì nhiều router
 * và bản Android mới chặn 255.255.255.255. Ngược lại, vẫn giữ 255.255.255.255 ở cuối
 * làm phương án dự phòng cho trường hợp không đọc được danh sách interface.
 */
fun lanBroadcastTargets(): List<InetAddress> {
    val targets = LinkedHashSet<InetAddress>()
    runCatching {
        for (nif in NetworkInterface.getNetworkInterfaces()) {
            if (!nif.isUp || nif.isLoopback) continue
            for (address in nif.interfaceAddresses) {
                address.broadcast?.let { targets.add(it) }
            }
        }
    }
    runCatching { targets.add(InetAddress.getByName("255.255.255.255")) }
    return targets.toList()
}
