package kma.game.chess2d.profile

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kma.game.chess2d.history.MatchResult

/**
 * Thống kê của người chơi cho màn Hồ sơ (Profile).
 *
 * Chỉ có **một dòng** trong bảng (khóa cố định [SINGLE_ID]): app chưa có tài khoản nên chỉ
 * có một người chơi. Mọi hàm `with...` đều thuần (trả bản sao mới, không đụng I/O) để test
 * được trên JVM mà không cần Room.
 *
 * Nguồn dữ liệu: ván đấu xong (thắng/thua/hòa, chiếu hết) và tầng Tháp Cờ vừa vượt qua.
 * Không suy ra từ bảng lịch sử `matches`, vì xóa lịch sử không được làm mất thành tích.
 */
@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = SINGLE_ID,
    @ColumnInfo(name = "wins") val wins: Int = 0,
    @ColumnInfo(name = "losses") val losses: Int = 0,
    @ColumnInfo(name = "draws") val draws: Int = 0,
    /** Số ván người chơi thắng bằng cách chiếu hết đối thủ. */
    @ColumnInfo(name = "checkmates") val checkmates: Int = 0,
    @ColumnInfo(name = "play_seconds") val playSeconds: Long = 0L,
    /** Tầng cao nhất đã vượt qua trong Tháp Cờ, 0 là chưa vượt tầng nào. */
    @ColumnInfo(name = "highest_floor") val highestFloor: Int = 0,
) {

    /** Tổng số ván đã chơi xong. */
    val totalGames: Int get() = wins + losses + draws

    /**
     * Cấp của người chơi: mỗi [GAMES_PER_LEVEL] điểm tiến triển (thắng + tầng đã vượt) lên
     * một cấp, bắt đầu từ cấp 1. Thua và hòa không trừ điểm.
     */
    val level: Int get() = 1 + (wins + highestFloor) / GAMES_PER_LEVEL

    /** Cộng một ván đã xong. [result] là một trong các hằng của [MatchResult]. */
    fun withMatch(result: String, byCheckmate: Boolean, seconds: Long): PlayerStats {
        val safeSeconds = seconds.coerceAtLeast(0L)
        return when (result) {
            MatchResult.WIN -> copy(
                wins = wins + 1,
                checkmates = checkmates + if (byCheckmate) 1 else 0,
                playSeconds = playSeconds + safeSeconds,
            )
            MatchResult.LOSS -> copy(losses = losses + 1, playSeconds = playSeconds + safeSeconds)
            MatchResult.DRAW -> copy(draws = draws + 1, playSeconds = playSeconds + safeSeconds)
            // Kết quả lạ thì bỏ qua thay vì ghi sai số liệu.
            else -> this
        }
    }

    /** Ghi nhận đã vượt [floor]; không bao giờ làm tầng cao nhất tụt xuống. */
    fun withFloorCleared(floor: Int): PlayerStats =
        if (floor > highestFloor) copy(highestFloor = floor) else this

    companion object {
        const val SINGLE_ID = 1
        const val GAMES_PER_LEVEL = 5
    }
}

/**
 * Một nhân vật đã gặp trong Nhật ký (Journal). Chỉ nhân vật **đã gặp** mới có dòng ở đây;
 * trạng thái "chưa rõ" và "khóa" được suy ra từ tiến độ Tháp, xem [JournalState].
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey @ColumnInfo(name = "npc_id") val npcId: String,
    @ColumnInfo(name = "first_met_at") val firstMetAtMillis: Long,
    @ColumnInfo(name = "times_met") val timesMet: Int = 1,
) {
    /** Gặp thêm một lần nữa: giữ nguyên lần đầu, chỉ tăng số lần. */
    fun metAgain(): JournalEntry = copy(timesMet = timesMet + 1)
}
