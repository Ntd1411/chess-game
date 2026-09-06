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
    suspend fun run(
        room: DiscoveredRoom,
        reconnectWindowMillis: Long = RECONNECT_WINDOW_MILLIS,
    ) = run(room.host, room.gamePort, reconnectWindowMillis)

    /**
     * Nối tới host và phục vụ tới khi hết đường nối lại, bị từ chối, hoặc người dùng rời phòng.
     *
     * @param reconnectWindowMillis khoảng thời gian cố nối lại trước khi bỏ cuộc; 0 nghĩa là
     *        không nối lại. Đếm theo **thời gian** chứ không theo số lần thử, vì khi Wi-Fi
     *        đang tắt thì connect() thất bại tức thì (mạng không tới được, không phải hết
     *        hạn chờ): đếm số lần thì năm lần thử cháy hết trong vài giây, trước cả khi
     *        người dùng kịp bật lại Wi-Fi.
     */
    suspend fun run(
        host: String,
        port: Int,
        reconnectWindowMillis: Long = RECONNECT_WINDOW_MILLIS,
    ) {
        var giveUpAt = System.currentTimeMillis() + reconnectWindowMillis
        while (currentCoroutineContext().isActive && !closedByUser && !peerLeft) {
            val open = withContext(Dispatchers.IO) { dial(host, port) }
            if (open == null) {
                if (System.currentTimeMillis() >= giveUpAt) {
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

            pump(open)
            // Rời phòng có chủ đích (bên nào cũng vậy) thì không tự nối lại.
            if (closedByUser || peerLeft) return

            // Đã từng nối được, nên cửa sổ nối lại được tính lại từ đầu: một ván dài có thể
            // gặp nhiều lần sóng yếu, lần sau không được phạt vì lần trước.
            giveUpAt = System.currentTimeMillis() + reconnectWindowMillis
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
