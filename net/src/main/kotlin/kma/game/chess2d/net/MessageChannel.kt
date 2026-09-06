package kma.game.chess2d.net

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Closeable
import java.io.IOException
import java.net.Socket

/**
 * Một kết nối TCP đã mở, đọc/ghi theo từng thông điệp.
 *
 * Khung tin là **JSON một dòng, phân cách bằng '\n'**. Đơn giản nhưng hợp lệ ở đây vì
 * [ProtocolJson] không bật pretty print, nên một thông điệp mã hóa ra không bao giờ
 * chứa ký tự xuống dòng thô — chuỗi trong JSON luôn được escape thành \\n. Đễ đọc
 * bằng mắt khi debug bằng `nc`, và client dòng lệnh gõ tay được.
 *
 * Hạn đọc được đặt thắng trên socket thay vì dùng một luồng canh riêng: khi Wi-Fi bị
 * tắt, TCP không hề biết — [receive] sẽ treo vô hạn chứ không báo lỗi. `soTimeout`
 * biến sự im lặng đó thành một exception đứt khoát sau [LanTiming.CONNECTION_TIMEOUT_MILLIS].
 *
 * KHÔNG thread-safe cho việc đọc: chỉ một luồng được gọi [receive]. Ghi thì an toàn,
 * vì heartbeat và nước đi của người chơi được gửi từ hai luồng khác nhau.
 */
class MessageChannel(private val socket: Socket) : Closeable {

    private val reader: BufferedReader
    private val writer: BufferedWriter
    private val writeLock = Any()

    val remoteAddress: String get() = socket.inetAddress?.hostAddress ?: "?"

    init {
        // Không gom gói: cờ vua gửi từng thông điệp rất nhỏ, đợi cho đầy gói chỉ thêm độ trễ.
        socket.tcpNoDelay = true
        socket.soTimeout = LanTiming.CONNECTION_TIMEOUT_MILLIS.toInt()
        reader = socket.getInputStream().bufferedReader()
        writer = socket.getOutputStream().bufferedWriter()
    }

    /** Ghi một thông điệp và flush ngay: không được để một nước đi nằm chờ trong buffer. */
    @Throws(IOException::class)
    fun send(message: NetMessage) {
        val line = ProtocolJson.encode(message)
        synchronized(writeLock) {
            writer.write(line)
            writer.write("\n")
            writer.flush()
        }
    }

    /**
     * Đọc thông điệp kế tiếp.
     *
     * @return null khi đầu bên kia đóng kết nối một cách bình thường.
     * @throws java.net.SocketTimeoutException khi im lặng quá lâu (mất mạng).
     * @throws BadMessageException khi nhận phải dữ liệu không giải mã được.
     */
    @Throws(IOException::class)
    fun receive(): NetMessage? {
        val line = reader.readLine() ?: return null
        if (line.isBlank()) return receive()
        return try {
            ProtocolJson.decode(line)
        } catch (error: Exception) {
            // Bọc lại thành lỗi của tầng mạng: phía trên chỉ cần biết "gói này không hiểuđược",
            // không cần biết chi tiết của kotlinx.serialization.
            throw BadMessageException(line, error)
        }
    }

    override fun close() {
        runCatching { socket.close() }
    }
}

/** Nhận được dữ liệu không phải thông điệp hợp lệ. Thường là dấu hiệu lệch phiên bản. */
class BadMessageException(val line: String, cause: Throwable) :
    IOException("cannot decode message: ${line.take(120)}", cause)
