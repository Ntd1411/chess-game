package kma.game.chess2d.profile

/**
 * Tổng thời gian chơi tách thành giờ/phút/giây để màn Hồ sơ hiển thị.
 *
 * Thuần (không biết Android) để test được trên JVM. Chọn đơn vị hiển thị ở [unit]: chỉ hiện
 * đơn vị lớn nhất có nghĩa, vì "0 giờ 0 phút" hay "5 giây" đều đọc không tự nhiên.
 */
data class PlayTime(val hours: Long, val minutes: Int, val seconds: Int) {

    /** Đơn vị nên dùng để hiển thị. */
    enum class Unit {
        /** Từ một giờ trở lên: "H giờ M phút". */
        HOURS_MINUTES,

        /** Từ một phút đến dưới một giờ: "M phút". */
        MINUTES,

        /** Dưới một phút: "S giây". */
        SECONDS,
    }

    val unit: Unit
        get() = when {
            hours > 0 -> Unit.HOURS_MINUTES
            minutes > 0 -> Unit.MINUTES
            else -> Unit.SECONDS
        }

    companion object {
        /** Tách [totalSeconds] (số âm coi như 0). */
        fun of(totalSeconds: Long): PlayTime {
            val safe = totalSeconds.coerceAtLeast(0L)
            return PlayTime(
                hours = safe / 3600,
                minutes = ((safe % 3600) / 60).toInt(),
                seconds = (safe % 60).toInt(),
            )
        }
    }
}
