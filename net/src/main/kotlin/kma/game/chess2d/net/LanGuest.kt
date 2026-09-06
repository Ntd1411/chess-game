package kma.game.chess2d.net

import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * Bên vào phòng.
 *
 * Khác host, khách không phải trọng tài: khi phát hiện bàn cờ lệch, nó xin đồng bộ
 * chứ không áp trạng thái của mình lên đối phương (xem [LanEndpoint]).
 *
 * Tự nối lại được đặt ở phía khách vì chỉ khách biết địa chỉ cần gọi; host chỉ việc
 * tiếp tục lắng nghe. Đổi lại, khách phải phân biệt hai loại thất bại: **không tới
 * được** thì thử lại, còn **bị từ chối** (lệch phiên bản, phòng đầy) thì thử lại bao
 * nhiêu lần cũng vậy, phải dừng và báo lỗi.
 */
class LanGuest(localName: String) : LanEndpoint(LanRole.GUEST, localName) {

    /** Tiện dùng từ màn hình sảnh chờ: vào thẳng một phòng vừa tìm thấy. */
    suspend fun run(room: DiscoveredRoom, reconnectAttempts: Int = DEFAULT_RECONNECT_ATTEMPTS) =
        run(room.host, room.gamePort, reconnectAttempts)

    /**
     * Nối tới host và phục vụ tới khi hết đường nối lại, bị từ chối, hoặc người dùng rời phòng.
     */
    suspend fun run(
        host: String,
        port: Int,
        reconnectAttempts: Int = DEFAULT_RECONNECT_ATTEMPTS,
    ) {
        var failures = 0
        while (currentCoroutineContext().isActive && !closedByUser && !peerLeft) {
            val open = withContext(Dispatchers.IO) { dial(host, port) }
            if (open == null) {
                failures += 1
                if (failures > reconnectAttempts) {
                    emit(LanEvent.Disconnected("cannot reach $host:$port", canRetry = false))
                    return
                }
                delay(RECONNECT_DELAY_MILLIS)
                continue
            }

            val accepted = withContext(Dispatchers.IO) { handshake(open) }
            if (!accepted) {
                open.close()
                // Bị từ chối là quyết định của host, không phải sự cố mạng: dừng luôn.
                emit(LanEvent.Disconnected("handshake refused", canRetry = false))
                return
            }

            failures = 0
            pump(open)
            // Rời phòng có chủ đích (bên nào cũng vậy) thì không tự nối lại.
            if (closedByUser || peerLeft) return

            failures += 1
            if (failures > reconnectAttempts) {
                emit(LanEvent.Disconnected("lost connection to $host:$port", canRetry = false))
                return
            }
            delay(RECONNECT_DELAY_MILLIS)
        }
    }

    private fun dial(host: String, port: Int): MessageChannel? = runCatching {
        // connect() có hạn chờ riêng: Socket(host, port) có thể treo rất lâu khi địa chỉ
        // không ai trả lời — trường hợp rất thường gặp khi host vừa rời mạng Wi-Fi.
        val socket = Socket()
        socket.connect(InetSocketAddress(host, port), LanTiming.CONNECT_TIMEOUT_MILLIS)
        MessageChannel(socket)
    }.getOrNull()

    /**
     * Gửi [NetMessage.Hello] và chờ [NetMessage.Welcome].
     *
     * [LanEndpoint.resumeToken] khác null nghĩa là lần này đang nối lại, nên gửi kèm để
     * host biết đây vẫn là người cũ và trả về đúng ván đang chơi.
     */
    private fun handshake(open: MessageChannel): Boolean {
        val hello = NetMessage.Hello(name = localName, resumeToken = resumeToken)
        if (runCatching { open.send(hello) }.isFailure) return false

        return when (val reply = runCatching { open.receive() }.getOrNull()) {
            is NetMessage.Welcome -> {
                if (reply.protocolVersion != PROTOCOL_VERSION) {
                    emit(
                        LanEvent.Failed(
                            LanErrorCode.VERSION_MISMATCH,
                            "guest speaks v$PROTOCOL_VERSION, host speaks v${reply.protocolVersion}",
                        ),
                    )
                    false
                } else {
                    resumeToken = reply.resumeToken
                    channel = open
                    applySync(reply.sync)
                    onConnected(reply.hostName)
                    true
                }
            }

            is NetMessage.Error -> {
                emit(LanEvent.Failed(reply.code, reply.detail))
                false
            }

            else -> {
                emit(LanEvent.Failed(LanErrorCode.BAD_HANDSHAKE, "unexpected reply: $reply"))
                false
            }
        }
    }
}
