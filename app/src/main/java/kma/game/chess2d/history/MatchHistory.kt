package kma.game.chess2d.history

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Một ván đã đánh xong, lưu trong Room (mục 7.3).
 *
 * **Chỉ lưu danh sách nước đi, không lưu thế cờ.** Từ [startFen] cộng [moves] là
 * dựng lại được mọi thế cờ bằng engine, nên lưu thêm thế cờ chỉ tốn chỗ và thêm
 * chỗ để dữ liệu lệch nhau.
 *
 * Nước đi lưu kiểu UCI cách nhau bằng dấu cách: một cột văn bản là đủ, không cần
 * bảng con cho từng nước vì không bao giờ truy vấn theo từng nước riêng lẻ.
 */
@Entity(tableName = "matches")
data class MatchRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    /** Thời điểm kết thúc ván, dùng để sắp xếp lịch sử. */
    @ColumnInfo(name = "played_at") val playedAtMillis: Long,
    /** `TWO_PLAYERS`, `VS_COMPUTER` hay `LAN`. */
    @ColumnInfo(name = "mode") val mode: String,
    /** Cấp máy khi đánh với máy, các chế độ khác để trống. */
    @ColumnInfo(name = "difficulty") val difficulty: String? = null,
    /** Tên đối thủ để hiện trong danh sách. */
    @ColumnInfo(name = "opponent") val opponent: String = "",
    @ColumnInfo(name = "start_fen") val startFen: String,
    /** Dãy nước kiểu UCI, cách nhau bằng dấu cách. */
    @ColumnInfo(name = "moves") val moves: String,
    /** `WIN`, `LOSS` hay `DRAW`, nhìn từ phía người chơi. */
    @ColumnInfo(name = "result") val result: String,
) {
    /** Dãy nước tách sẵn để dựng lại ván. */
    val uciMoves: List<String>
        get() = moves.split(' ').filter { it.isNotEmpty() }
}

/** Kết quả một ván, nhìn từ phía người chơi. */
object MatchResult {
    const val WIN = "WIN"
    const val LOSS = "LOSS"
    const val DRAW = "DRAW"
}

/** Thống kê gọn cho màn lịch sử. */
data class MatchStats(
    val total: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val draws: Int = 0,
)

/**
 * Truy vấn lịch sử ván đấu.
 *
 * Các hàm đọc trả về [Flow] để màn lịch sử tự cập nhật sau khi xóa một ván.
 */
@Dao
interface MatchDao {

    @Insert
    suspend fun insert(record: MatchRecord): Long

    @Query("SELECT * FROM matches ORDER BY played_at DESC")
    fun observeAll(): Flow<List<MatchRecord>>

    @Query("SELECT * FROM matches WHERE id = :id")
    suspend fun byId(id: Long): MatchRecord?

    @Query("DELETE FROM matches WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM matches")
    suspend fun deleteAll()

    @Query(
        // COALESCE để bảng rỗng vẫn trả về số 0 chứ không trả về null.
        "SELECT COUNT(*) AS total, " +
            "COALESCE(SUM(CASE WHEN result = 'WIN' THEN 1 ELSE 0 END), 0) AS wins, " +
            "COALESCE(SUM(CASE WHEN result = 'LOSS' THEN 1 ELSE 0 END), 0) AS losses, " +
            "COALESCE(SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END), 0) AS draws " +
            "FROM matches",
    )
    fun observeStats(): Flow<MatchStats>
}

/**
 * Cơ sở dữ liệu lịch sử ván đấu.
 *
 * Chưa có bản cũ nào ngoài thực tế nên chưa cần migration; khi đổi lược đồ phải tăng
 * `version` và viết migration thật, không dùng `fallbackToDestructiveMigration` để không
 * âm thầm xóa lịch sử của người chơi.
 */
@Database(entities = [MatchRecord::class], version = 1, exportSchema = false)
abstract class MatchHistoryDatabase : RoomDatabase() {

    abstract fun matches(): MatchDao

    companion object {
        private const val FILE_NAME = "match-history.db"

        @Volatile
        private var instance: MatchHistoryDatabase? = null

        /** Lấy bản dùng chung, mở file một lần duy nhất cho cả ứng dụng. */
        fun get(context: Context): MatchHistoryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    MatchHistoryDatabase::class.java,
                    FILE_NAME,
                ).build().also { instance = it }
            }
    }
}
