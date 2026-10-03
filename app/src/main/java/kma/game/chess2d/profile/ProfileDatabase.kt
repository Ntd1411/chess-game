package kma.game.chess2d.profile

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import androidx.room.Query
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Truy vấn Hồ sơ và Nhật ký.
 *
 * Các hàm đọc trả về [Flow] để màn hình tự cập nhật sau khi ghi.
 */
@Dao
interface ProfileDao {

    @Query("SELECT * FROM player_stats WHERE id = ${PlayerStats.SINGLE_ID}")
    fun observeStats(): Flow<PlayerStats?>

    @Query("SELECT * FROM player_stats WHERE id = ${PlayerStats.SINGLE_ID}")
    suspend fun stats(): PlayerStats?

    @Upsert
    suspend fun upsertStats(stats: PlayerStats)

    @Query("SELECT * FROM journal_entries")
    fun observeJournal(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE npc_id = :npcId")
    suspend fun entry(npcId: String): JournalEntry?

    @Upsert
    suspend fun upsertEntry(entry: JournalEntry)
}

/**
 * Cơ sở dữ liệu Hồ sơ + Nhật ký, **tách riêng** khỏi `MatchHistoryDatabase`.
 *
 * Tách file để thêm tính năng mới không phải đổi lược đồ của lịch sử ván đấu (và không có
 * rủi ro migration làm hỏng lịch sử cũ của người chơi). Khi đổi lược đồ ở đây phải tăng
 * `version` và viết migration thật, không dùng `fallbackToDestructiveMigration`.
 */
@Database(entities = [PlayerStats::class, JournalEntry::class], version = 1, exportSchema = false)
abstract class ProfileDatabase : RoomDatabase() {

    abstract fun profile(): ProfileDao

    companion object {
        private const val FILE_NAME = "profile.db"

        @Volatile
        private var instance: ProfileDatabase? = null

        /** Lấy bản dùng chung, mở file một lần duy nhất cho cả ứng dụng. */
        fun get(context: Context): ProfileDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ProfileDatabase::class.java,
                    FILE_NAME,
                ).build().also { instance = it }
            }
    }
}

/**
 * Nơi duy nhất ghi Hồ sơ và Nhật ký. Mỗi lần ghi là đọc-sửa-ghi trong **một giao dịch** để hai
 * lần ghi gần nhau (vd. thắng ván rồi vượt tầng) không đè mất nhau.
 */
class ProfileRepository(private val db: ProfileDatabase) {

    private val dao = db.profile()

    /** Thống kê hiện tại; chưa chơi gì thì là bản mặc định toàn số 0. */
    val stats: Flow<PlayerStats> = dao.observeStats().map { it ?: PlayerStats() }

    /** Tập id nhân vật đã gặp. */
    val metIds: Flow<Set<String>> = dao.observeJournal().map { list -> list.map { it.npcId }.toSet() }

    /** Ghi một ván đã xong. */
    suspend fun recordMatch(result: String, byCheckmate: Boolean, seconds: Long) =
        updateStats { it.withMatch(result, byCheckmate, seconds) }

    /** Ghi tầng Tháp Cờ vừa vượt qua. */
    suspend fun recordFloorCleared(floor: Int) = updateStats { it.withFloorCleared(floor) }

    /** Ghi đã gặp nhân vật [npcId]: lần đầu tạo dòng mới, các lần sau tăng số lần gặp. */
    suspend fun recordMet(npcId: String, nowMillis: Long = System.currentTimeMillis()) {
        db.withTransaction {
            val old = dao.entry(npcId)
            dao.upsertEntry(old?.metAgain() ?: JournalEntry(npcId, firstMetAtMillis = nowMillis))
        }
    }

    private suspend fun updateStats(change: (PlayerStats) -> PlayerStats) {
        db.withTransaction {
            val current = dao.stats() ?: PlayerStats()
            dao.upsertStats(change(current))
        }
    }
}
