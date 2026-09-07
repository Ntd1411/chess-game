package kma.game.chess2d.net

import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Bên mở phòng: vừa phát beacon, vừa nghe kết nối, và là trọng tài của ván đấu.
 *
 * Vòng lặp accept **không** dừng khi kết nối đứt: ván đấu nằm ở [LanEndpoint], còn
 * socket chỉ là thứ tạm thời. Nhờ vậy khách tắt Wi-Fi rồi bật lại sẽ nối vào đúng
 * ván cũ nhờ vé `resumeToken`, đúng tiêu chí "nối lại được ván đang chơi".
 *
 * Mỗi kết nối được bắt tay trong một coroutine riêng, không bắt tay tuần tự ngay
 * trong vòng accept. Khác biệt này quan trọng: nếu đợi xong ván mới accept tiếp thì
 * người thứ ba vào phòng sẽ bị treo tới khi hết hạn chờ, thay vì nhận ngay lỗi
 * [LanErrorCode.ROOM_BUSY].
 *
 * @param requestedPort để 0 để hệ điều hành chọn cổng rỗi; cổng thật được công bố qua
 *        beacon nên không cần cố định, và hai phòng trên cùng một máy không đụng nhau.
 * @param advertiseTargets để null để tự dò broadcast; test truyền 127.0.0.1.
 */
class LanHost(
    localName: String,
    val roomId: String = randomLanId(),
    private val requestedPort: Int = 0,
    private val discoveryPort: Int = DISCOVERY_PORT,
    private val advertiseTargets: List<InetAddress>? = null,
) : LanEndpoint(LanRole.HOST, localName) {

    /** Cổng TCP thật đang lắng nghe, -1 khi chưa chạy. */
    @Volatile
    var gamePort: Int = -1
        private set

    /** Chỉ một kết nối được trở thành đối thủ; ổ khóa này giữ việc đó là nguyên tử. */
    private val joinLock = Any()

    /**
     * Mở phòng và phục vụ tới khi coroutine bị hủy hoặc người dùng đóng phòng.
     *
     * @param onReady được gọi sau khi đã biết cổng thật — hữu ích cho test và cho UI
     *        muốn hiện "đang chờ đối thủ".
     */
    suspend fun run(onReady: (Int) -> Unit = {}) = coroutineScope {
        val server = withContext(Dispatchers.IO) {
            // soTimeout biến accept() thành chờ có hạn, nhờ đó vòng lặp thấy được coroutine
            // đã bị hủy. Không có nó thì luồng IO nằm lại mãi trong accept().
            ServerSocket(requestedPort).apply { soTimeout = ACCEPT_POLL_MILLIS }
        }
        gamePort = server.localPort
        onReady(gamePort)

        val advertiser = launch {
            RoomAdvertiser(
                beacon = {
                    RoomBeacon(
                        roomId = roomId,
                        hostName = localName,
                        gamePort = gamePort,
                        busy = channel != null,
                    )
                },
                targets = advertiseTargets,
                port = discoveryPort,
            ).run()
        }

        try {
            while (currentCoroutineContext().isActive && !closedByUser) {
                val socket = withContext(Dispatchers.IO) {
                    runCatching { server.accept() }.getOrElse { error ->
                        if (error is SocketTimeoutException) null else throw error
                    }
                } ?: continue

                launch {
                    val open = MessageChannel(socket)
                    val accepted = withContext(Dispatchers.IO) { handshake(open) }
                    if (accepted) pump(open) else open.close()
                }
            }
        } finally {
            advertiser.cancel()
            runCatching { server.close() }
        }
    }

    /**
     * Bắt tay: kiểm phiên bản, kiểm chỗ trống, rồi gửi [NetMessage.Welcome].
     *
     * Ba trường hợp phải phân biệt rõ, vì cách xử lý hoàn toàn khác nhau:
     * 1. Lệch [PROTOCOL_VERSION] — từ chối kèm mã lỗi để khách hiện thông báo rõ ràng.
     * 2. Đúng vé nối lại — tiếp tục ván cũ, không xóa gì.
     * 3. Khách mới trong khi đã có ván đang chạy — từ chối, phòng chỉ hai người.
     *
     * Phần đọc [NetMessage.Hello] nằm ngoài [joinLock]: nó có thể chờ tới hạn timeout,
     * không được giữ khóa lâu như vậy.
     */
    private fun handshake(open: MessageChannel): Boolean {
        val hello = runCatching { open.receive() }.getOrNull() as? NetMessage.Hello
        if (hello == null) {
            runCatching {
                open.send(NetMessage.Error(LanErrorCode.BAD_HANDSHAKE, "expected hello"))
            }
            return false
        }
        if (hello.protocolVersion != PROTOCOL_VERSION) {
            val detail = "host speaks v$PROTOCOL_VERSION, guest speaks v${hello.protocolVersion}"
            runCatching { open.send(NetMessage.Error(LanErrorCode.VERSION_MISMATCH, detail)) }
            emit(LanEvent.Failed(LanErrorCode.VERSION_MISMATCH, detail))
            return false
        }

        synchronized(joinLock) {
            val resuming = resumeToken != null && hello.resumeToken == resumeToken
            // Đọc bàn cờ trong ổ khóa của nó: lúc này ván cũ có thể đang được luồng đọc
            // của kết nối trước sửa dở.
            val occupied = channel != null || withGame { game.ply } > 0
            if (occupied && !resuming) {
                runCatching {
                    open.send(NetMessage.Error(LanErrorCode.ROOM_BUSY, "room already has a game"))
                }
                return false
            }

            if (!resuming) {
                // Khách mới: ván sạch, host giữ Trắng, vé nối lại mới.
                gameId += 1
                guestPlaysWhite = false
                withGame { game.reset() }
                resumeToken = randomLanId()
            }

            channel = open
            val welcome = NetMessage.Welcome(
                hostName = localName,
                resumeToken = requireNotNull(resumeToken),
                sync = withGame { currentSync() },
            )
            if (runCatching { open.send(welcome) }.isFailure) {
                channel = null
                return false
            }
            // Phòng có người mới: xóa dấu vết của phiên trước, nếu không thì mọi lần đứt
            // kết nối sau đó đều bị im lặng bỏ qua.
            peerLeft = false
        }

        onConnected(hello.name)
        return true
    }

    private companion object {
        const val ACCEPT_POLL_MILLIS = 400
    }
}
