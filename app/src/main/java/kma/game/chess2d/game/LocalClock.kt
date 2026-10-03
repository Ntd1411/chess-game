package kma.game.chess2d.game

import java.util.Locale
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.history.MatchResult
import kma.game.chess2d.net.ClockTimes

/**
 * Đồng hồ cờ cho ván hai người cùng máy. Thuần tính toán: mọi hàm nhận mốc thời gian từ ngoài vào,
 * nên test điều khiển được thời gian mà không phải chờ thật.
 *
 * Khác `MatchClock` của `:net` ở ba điểm cần cho ván offline: **tạm dừng/tiếp tục** (mở Pause Menu,
 * đưa app xuống nền), **dừng hẳn** khi hết ván, và **khôi phục** từ số giờ còn lại đã lưu.
 *
 * Quy ước:
 * - Đồng hồ chỉ bắt đầu chạy sau **nước đi đầu tiên**. Nước đầu của Trắng không tốn giờ, và giờ của
 *   Đen bắt đầu đếm ngay sau đó. Nhờ vậy lúc mới vào ván hay mở lại ván không bị trừ giờ oan.
 * - Không có phần cộng thêm sau mỗi nước. Ván có giờ không cho Đi lại nên không cần hoàn nguyên đồng hồ.
 *
 * Không an toàn đa luồng: dùng từ một luồng duy nhất (main thread của ViewModel).
 */
class LocalClock private constructor(
    val initialMillis: Long,
    private var whiteMillis: Long,
    private var blackMillis: Long,
    private var whiteToMove: Boolean,
    private var started: Boolean,
) {

    /** Đồng hồ mới cho một ván mới: hai bên đủ giờ, chưa chạy. */
    constructor(initialMillis: Long) : this(initialMillis, initialMillis, initialMillis, true, false)

    /** Mốc lần cuối trừ giờ; `null` là đang không chạy (chưa bắt đầu, tạm dừng hoặc đã dừng). */
    private var lastTick: Long? = null
    private var paused = false
    private var stopped = false

    /**
     * Ghi nhận một nước vừa đi xong: trừ giờ đã nghĩ của bên vừa đi, rồi chuyển sang bên kia.
     * Nước đi đầu tiên chỉ bật đồng hồ lên, không trừ gì.
     */
    fun onMovePlayed(nowMillis: Long) {
        if (stopped) return
        if (started) drain(nowMillis) else started = true
        whiteToMove = !whiteToMove
        lastTick = if (paused) null else nowMillis
    }

    /** Tạm dừng: phần giờ đã trôi tới lúc này vẫn bị trừ, sau đó không trừ nữa. */
    fun pause(nowMillis: Long) {
        if (stopped || paused) return
        drain(nowMillis)
        lastTick = null
        paused = true
    }

    /** Tiếp tục sau khi tạm dừng. Chưa có nước nào thì chưa chạy. */
    fun resume(nowMillis: Long) {
        if (stopped || !paused) return
        paused = false
        if (started) lastTick = nowMillis
    }

    /** Dừng hẳn (hết ván). Không chạy lại nữa. */
    fun stop(nowMillis: Long) {
        if (stopped) return
        drain(nowMillis)
        lastTick = null
        stopped = true
    }

    /** Giờ còn lại của hai bên tính đến [nowMillis], kể cả phần đang chạy của bên đến lượt. */
    fun snapshot(nowMillis: Long): ClockTimes {
        val elapsed = lastTick?.let { (nowMillis - it).coerceAtLeast(0) } ?: 0L
        return ClockTimes(
            whiteMillis = (whiteMillis - if (whiteToMove) elapsed else 0L).coerceAtLeast(0),
            blackMillis = (blackMillis - if (whiteToMove) 0L else elapsed).coerceAtLeast(0),
        )
    }

    /** `true` nếu Trắng hết giờ, `false` nếu Đen hết giờ, `null` khi chưa ai hết. */
    fun flaggedSide(nowMillis: Long): Boolean? {
        val times = snapshot(nowMillis)
        return when {
            times.whiteMillis <= 0L -> true
            times.blackMillis <= 0L -> false
            else -> null
        }
    }

    private fun drain(nowMillis: Long) {
        val last = lastTick ?: return
        val elapsed = (nowMillis - last).coerceAtLeast(0)
        if (whiteToMove) {
            whiteMillis = (whiteMillis - elapsed).coerceAtLeast(0)
        } else {
            blackMillis = (blackMillis - elapsed).coerceAtLeast(0)
        }
        lastTick = nowMillis
    }

    companion object {
        /**
         * Dựng lại đồng hồ từ số giờ còn lại đã lưu. Đồng hồ trả về đang **tạm dừng**: người gọi gọi
         * [resume] khi ván thực sự hiện ra, để thời gian ứng dụng bị tắt không bị tính vào ván.
         *
         * @param started ván đã có nước đi nào chưa.
         */
        fun restore(
            initialMillis: Long,
            whiteMillis: Long,
            blackMillis: Long,
            whiteToMove: Boolean,
            started: Boolean,
        ): LocalClock = LocalClock(
            initialMillis = initialMillis,
            whiteMillis = whiteMillis.coerceIn(0L, initialMillis),
            blackMillis = blackMillis.coerceIn(0L, initialMillis),
            whiteToMove = whiteToMove,
            started = started,
        ).also { it.paused = true }
    }
}

/** Định dạng giờ còn lại để hiện trên thanh người chơi. */
object ClockFormat {

    /** Làm tròn **lên** tới giây: còn 0,4 giây vẫn hiện 00:01, chỉ hiện 00:00 khi thật sự hết. */
    fun roundUpToSecond(millis: Long): Long = (millis.coerceAtLeast(0) + 999) / 1000 * 1000

    /** `mm:ss`, phút không giới hạn hai chữ số. Dùng `Locale.ROOT` để không ra chữ số theo ngôn ngữ máy. */
    fun mmss(millis: Long): String {
        val totalSeconds = roundUpToSecond(millis) / 1000
        return String.format(Locale.ROOT, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
    }
}

/**
 * Luật khi một bên hết giờ.
 *
 * Hết giờ thì thua, **trừ khi bên còn lại không thể chiếu hết** (bên đó chỉ còn Vua, hoặc Vua với
 * một quân nhẹ trong khi đối phương chỉ còn mỗi Vua): lúc đó ván hòa. Đây là xấp xỉ gọn của luật
 * FIDE, đúng với các thế thường gặp; thế hiếm cần dựng chuỗi nước đi để chứng minh thì coi là có
 * thể chiếu hết.
 */
object TimeoutRule {

    /**
     * Kết quả theo góc nhìn Bên Trắng (khớp cách lịch sử hai người lưu): `WIN` là Trắng thắng.
     *
     * @param whiteFlagged `true` nếu Trắng là bên hết giờ.
     */
    fun resultForWhite(board: Board, whiteFlagged: Boolean): String {
        val winnerWhite = !whiteFlagged
        if (!canMate(board, winnerWhite)) return MatchResult.DRAW
        return if (whiteFlagged) MatchResult.LOSS else MatchResult.WIN
    }

    /** Bên [white] còn đủ quân để có thể chiếu hết Vua đối phương hay không (xấp xỉ, xem KDoc lớp). */
    fun canMate(board: Board, white: Boolean): Boolean {
        var minors = 0
        var opponentOthers = 0
        for (square in 0 until Squares.COUNT) {
            val piece = board.pieceAt(square)
            if (piece == Piece.NONE) continue
            val type = Piece.typeOf(piece)
            if (Piece.isColor(piece, white)) {
                when (type) {
                    Piece.PAWN, Piece.ROOK, Piece.QUEEN -> return true
                    Piece.KNIGHT, Piece.BISHOP -> minors++
                }
            } else if (type != Piece.KING) {
                opponentOthers++
            }
        }
        return when {
            minors >= 2 -> true
            // Một quân nhẹ lẻ loi chỉ chiếu hết được khi Vua đối phương bị chính quân mình chặn.
            minors == 1 -> opponentOthers > 0
            else -> false
        }
    }
}
