package kma.game.chess2d.net

import kotlinx.serialization.Serializable

/**
 * Thể thức thọi gian (mục 7.2).
 *
 * @param initialMillis thọi gian ban đầu mỗi bên; 0 là không giới hạn.
 * @param incrementMillis cộng thêm sau mỗi nước đi.
 */
@Serializable
enum class TimeControl(val initialMillis: Long, val incrementMillis: Long) {
    /** Không bấm giờ — vẫn là mặc định để người chơi mới không bị đồng hồ dục. */
    UNLIMITED(0, 0),

    /** Cờ nhanh 5 phút mỗi bên. */
    BLITZ(5 * 60_000L, 0),

    /** 10 phút + 5 giây mỗi nước. */
    RAPID(10 * 60_000L, 5_000L),
    ;

    val limited: Boolean get() = initialMillis > 0
}

/**
 * Thọi gian còn lại của hai bên tại một thọi điểm — dạng gửi được qua dây.
 *
 * Gửi **thọi gian còn lại** chứ không gửi mốc bắt đầu: hai máy không chung đồng hồ
 * hệ thống, mọi phép trừ dựa trên mốc của máy kia đều sai.
 */
@Serializable
data class ClockTimes(
    val whiteMillis: Long,
    val blackMillis: Long,
)

/**
 * Đồng hồ thi đấu. **Chỉ host giữ một cái duy nhất** (mục 7.2): khách chỉ vẽ lại
 * con số host gửi kèm `MoveAck`, không tự trừ. Hai đồng hồ chạy độc lập thì chỉ sau
 * vài phút là lệch nhau, và không có cách nào phân định bên nào hết giờ trước.
 *
 * Lớp này thuần tính toán: mọi hàm nhận mốc thọi gian từ ngoài vào thay vì tự gọi
 * `System.currentTimeMillis()`, nhờ vậy test JVM điều khiển được thọi gian mà không
 * phải chờ thật.
 *
 * Không an toàn đa luồng: được dùng từ đúng một vòng đọc thông điệp của host.
 */
class MatchClock(val control: TimeControl) {

    private var whiteMillis: Long = control.initialMillis
    private var blackMillis: Long = control.initialMillis

    /** Mốc lần cuối trừ thọi gian; `null` là đồng hồ chưa chạy. */
    private var lastTickMillis: Long? = null

    /** Bên đang bị trừ thọi gian. */
    private var whiteToMove: Boolean = true

    /**
     * Bắt đầu hoặc đặt lại đồng hồ cho một ván mới.
     *
     * Đồng hồ chỉ chạy từ khi gọi hàm này: lúc chờ khách vào phòng không được trừ
     * giờ của ai.
     */
    fun start(nowMillis: Long, whiteToMove: Boolean = true) {
        whiteMillis = control.initialMillis
        blackMillis = control.initialMillis
        this.whiteToMove = whiteToMove
        lastTickMillis = if (control.limited) nowMillis else null
    }

    /** Dừng trừ giờ (ván kết thúc hoặc đứt kết nối). */
    fun stop(nowMillis: Long) {
        if (lastTickMillis == null) return
        drain(nowMillis)
        lastTickMillis = null
    }

    /**
     * Ghi nhận một nước vừa đi xong: trừ thọi gian đã nghĩ của bên vừa đi, cộng phần
     * tăng thêm, rồi chuyển lượt trừ sang bên kia.
     */
    fun onMovePlayed(nowMillis: Long) {
        if (!control.limited) return
        if (lastTickMillis == null) lastTickMillis = nowMillis
        drain(nowMillis)
        // Phần tăng thêm cộng sau khi đã trừ: đi xong nước nào mới được cộng nước đó.
        if (whiteToMove) {
            whiteMillis += control.incrementMillis
        } else {
            blackMillis += control.incrementMillis
        }
        whiteToMove = !whiteToMove
        lastTickMillis = nowMillis
    }

    /** Thọi gian còn lại tính đến [nowMillis], kể cả phần đang chạy của bên đến lượt. */
    fun snapshot(nowMillis: Long): ClockTimes {
        if (!control.limited) return ClockTimes(0, 0)
        val elapsed = lastTickMillis?.let { (nowMillis - it).coerceAtLeast(0) } ?: 0
        return ClockTimes(
            whiteMillis = (whiteMillis - if (whiteToMove) elapsed else 0).coerceAtLeast(0),
            blackMillis = (blackMillis - if (whiteToMove) 0 else elapsed).coerceAtLeast(0),
        )
    }

    /** Bên đã hết giờ, `null` khi chưa ai hết. */
    fun flaggedSide(nowMillis: Long): Boolean? {
        if (!control.limited) return null
        val times = snapshot(nowMillis)
        return when {
            times.whiteMillis <= 0 -> true
            times.blackMillis <= 0 -> false
            else -> null
        }
    }

    /** Trừ phần thọi gian đã trôi của bên đến lượt vào số còn lại. */
    private fun drain(nowMillis: Long) {
        val last = lastTickMillis ?: return
        val elapsed = (nowMillis - last).coerceAtLeast(0)
        if (whiteToMove) {
            whiteMillis = (whiteMillis - elapsed).coerceAtLeast(0)
        } else {
            blackMillis = (blackMillis - elapsed).coerceAtLeast(0)
        }
        lastTickMillis = nowMillis
    }
}
