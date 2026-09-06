package kma.game.chess2d.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Giao thức LAN (mục 5.5).
 *
 * Cờ vua là game turn-based, mỗi lượt chỉ vài chục byte, nên không cần gì phức tạp
 * hơn JSON trên TCP. Đổi lại phải trả một cái giá: mọi thay đổi thông điệp đều là
 * thay đổi giao thức, nên [PROTOCOL_VERSION] được gửi ngay trong nước bắt tay đầu
 * tiên. Hai máy cài hai phiên bản khác nhau phải báo lỗi rõ ràng thay vì lệch bàn
 * cờ giữa ván.
 */
const val PROTOCOL_VERSION: Int = 1

/** Cổng UDP để phát và nghe beacon tìm phòng. Cố định vì client cần biết trước để nghe. */
const val DISCOVERY_PORT: Int = 45_454

/** Nhận diện beacon của chính app này, để bỏ qua mọi gói UDP lạ trên cùng cổng. */
const val BEACON_MAGIC: String = "chess2d-lan"

/**
 * Các mốc thời gian của tầng mạng.
 *
 * Beacon 1 giây một lần để đạt tiêu chí "thấy nhau trong vòng 3 giây" của Phase 4,
 * kể cả khi gói đầu tiên bị mất — UDP không bảo đảm gì cả.
 */
object LanTiming {
    const val BEACON_INTERVAL_MILLIS: Long = 1_000

    /** Quá mốc này mà không nhận thêm beacon thì coi phòng đã tắt và bỏ khỏi danh sách. */
    const val ROOM_STALE_MILLIS: Long = 3_500

    const val PING_INTERVAL_MILLIS: Long = 2_000

    /**
     * Không nhận được gì từ đối thủ trong khoảng này thì coi là mất kết nối.
     *
     * Đặt 8 giây có chủ ý: Android có thể ngắt socket khi app vào background, và tắt
     * Wi-Fi 10 giây rồi bật lại là một tiêu chí của Phase 4 — timeout ngắn hơn sẽ báo
     * mất kết nối trong khi mạng chỉ đang chớp.
     */
    const val CONNECTION_TIMEOUT_MILLIS: Long = 8_000

    const val CONNECT_TIMEOUT_MILLIS: Int = 4_000
}

/** Mã lỗi giao thức. Là hằng chuỗi để phiên bản cũ đọc log vẫn hiểu được. */
object LanErrorCode {
    const val VERSION_MISMATCH: String = "version_mismatch"
    const val ROOM_BUSY: String = "room_busy"
    const val BAD_HANDSHAKE: String = "bad_handshake"
    const val ILLEGAL_MOVE: String = "illegal_move"
}

/**
 * Gói UDP mà host phát định kỳ để client tìm thấy phòng mà không phải nhập IP.
 *
 * [gamePort] nằm trong beacon chứ không cố định: host xin cổng TCP rỗi từ hệ điều
 * hành, nên hai người cùng mở phòng trên một máy (hoặc test trên PC) không đụng nhau.
 */
@Serializable
data class RoomBeacon(
    val magic: String = BEACON_MAGIC,
    val protocolVersion: Int = PROTOCOL_VERSION,
    val roomId: String,
    val hostName: String,
    val gamePort: Int,
    /** Phòng đã có đối thủ. Vẫn phát beacon để client đang chờ biết mà bỏ khỏi danh sách. */
    val busy: Boolean = false,
)

/**
 * Toàn bộ ván đấu ở dạng gửi được qua dây: thế cờ gốc + danh sách nước đã đi.
 *
 * Đây là cách resync của mục 5.5, và lý do không gửi FEN hiện tại: danh sách nước đi
 * dựng lại được cả quyền nhập thành, ô bắt tốt qua đường và **lịch sử lặp thế** —
 * FEN một mình không mang theo lịch sử nên hai bên sẽ tính luật hòa ba lần lặp khác
 * nhau. Một ván dài vài trăm nước cũng chỉ vài KB.
 *
 * @param gameId tăng lên mỗi ván mới. Dùng để bỏ những thông điệp đến muộn của ván trước.
 */
@Serializable
data class StateSync(
    val startFen: String,
    val moves: List<Int>,
    val guestPlaysWhite: Boolean,
    val gameId: Int,
)

/** Kết cục do người chơi quyết định, không phải do luật cờ. */
@Serializable
enum class LanOutcome {
    RESIGNATION,
    DRAW_AGREED,
}

@Serializable
sealed interface NetMessage {

    /** Client gửi đầu tiên. [resumeToken] khác null nghĩa là đang kết nối lại vào ván cũ. */
    @Serializable
    @SerialName("hello")
    data class Hello(
        val protocolVersion: Int = PROTOCOL_VERSION,
        val name: String,
        val resumeToken: String? = null,
    ) : NetMessage

    /**
     * Host trả lời: phân màu và gửi luôn trạng thái ván.
     *
     * Gộp [sync] vào đây thay vì gửi rời một [Sync] ngay sau: kết nối lại giữa ván
     * thì client cần cả màu và cả ván cũ, gộp lại thì không có khoảng thời gian nào
     * client biết màu mà chưa biết bàn cờ.
     */
    @Serializable
    @SerialName("welcome")
    data class Welcome(
        val protocolVersion: Int = PROTOCOL_VERSION,
        val hostName: String,
        val resumeToken: String,
        val sync: StateSync,
    ) : NetMessage

    /**
     * Một nước đi.
     *
     * @param ply số nước đã đi TRƯỚC nước này. Đây là chốt chống lệch: bên nhận chỉ
     *        áp dụng khi số này khớp với ván của mình, lệch là biết ngay và xin resync
     *        thay vì đi tiếp trên hai bàn cờ khác nhau.
     * @param raw dạng Int đóng gói của Move.
     * @param uci để đọc log và để client dòng lệnh gõ tay được.
     */
    @Serializable
    @SerialName("move")
    data class MoveMade(val gameId: Int, val ply: Int, val raw: Int, val uci: String) : NetMessage

    @Serializable
    @SerialName("moveAck")
    data class MoveAck(val gameId: Int, val ply: Int) : NetMessage

    /**
     * Host từ chối nước đi của client và kèm luôn trạng thái đúng.
     *
     * Kèm [sync] để client tự trả bàn cờ về đúng mà không cần thêm một vòng hỏi đáp:
     * client đã áp dụng nước đó cục bộ nên đang lệch, và mọi thao tác của người chơi
     * trong lúc chờ đều sẽ sai.
     */
    @Serializable
    @SerialName("moveReject")
    data class MoveRejected(val gameId: Int, val reason: String, val sync: StateSync) : NetMessage

    @Serializable
    @SerialName("resign")
    data class Resign(val gameId: Int) : NetMessage

    @Serializable
    @SerialName("drawOffer")
    data class DrawOffer(val gameId: Int) : NetMessage

    @Serializable
    @SerialName("drawResponse")
    data class DrawResponse(val gameId: Int, val accepted: Boolean) : NetMessage

    @Serializable
    @SerialName("rematchOffer")
    data class RematchOffer(val gameId: Int) : NetMessage

    /** Đồng ý đấu lại thì host đổi màu hai bên và gửi ván mới trong [sync]. */
    @Serializable
    @SerialName("rematchResponse")
    data class RematchResponse(val accepted: Boolean, val sync: StateSync? = null) : NetMessage

    @Serializable
    @SerialName("syncRequest")
    data class SyncRequest(val reason: String = "") : NetMessage

    @Serializable
    @SerialName("sync")
    data class Sync(val state: StateSync) : NetMessage

    /** Heartbeat. [nanos] để đo độ trễ và để pong khớp đúng ping. */
    @Serializable
    @SerialName("ping")
    data class Ping(val nanos: Long) : NetMessage

    @Serializable
    @SerialName("pong")
    data class Pong(val nanos: Long) : NetMessage

    /** Rời phòng có báo trước, để bên kia phân biệt với mất kết nối. */
    @Serializable
    @SerialName("bye")
    data class Bye(val reason: String = "") : NetMessage

    @Serializable
    @SerialName("error")
    data class Error(val code: String, val detail: String = "") : NetMessage
}

/**
 * Bộ mã hóa dùng chung.
 *
 * [Json.ignoreUnknownKeys] bật để một phiên bản sau thêm trường mới không làm phiên
 * bản này chết ngay ở tầng parse — lỗi lệch phiên bản phải do [PROTOCOL_VERSION] báo
 * ra một cách rõ ràng, không phải do một exception giải mã.
 */
object ProtocolJson {

    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(message: NetMessage): String = json.encodeToString(NetMessage.serializer(), message)

    fun decode(line: String): NetMessage = json.decodeFromString(NetMessage.serializer(), line)

    fun encodeBeacon(beacon: RoomBeacon): String =
        json.encodeToString(RoomBeacon.serializer(), beacon)

    /** Trả về null với mọi gói không phải beacon của app này, kể cả rác trên cùng cổng. */
    fun decodeBeacon(text: String): RoomBeacon? =
        runCatching { json.decodeFromString(RoomBeacon.serializer(), text) }
            .getOrNull()
            ?.takeIf { it.magic == BEACON_MAGIC }
}
