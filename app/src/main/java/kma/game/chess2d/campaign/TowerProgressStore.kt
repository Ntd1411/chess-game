package kma.game.chess2d.campaign

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Nơi lưu [TowerProgress], sống qua cả lần tắt app.
 *
 * Dùng file DataStore riêng để xóa tiến độ chiến dịch không kéo theo mất cài đặt hay
 * ván đang chơi dở, và ngược lại.
 */
class TowerProgressStore(private val context: Context) {

    /** Tiến độ hiện tại. Đọc file lỗi hoặc giá trị lạ thì lùi về tầng 0 chứ không làm app chết. */
    val progress: Flow<TowerProgress> = context.towerDataStore.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs ->
            val cleared = (prefs[KEY_CLEARED] ?: 0).coerceIn(0, TowerCatalog.FLOOR_COUNT)
            TowerProgress(cleared)
        }

    /**
     * Ghi nhận vừa qua tầng [floor]. Luật mở khóa nằm ở [TowerProgress.withCleared];
     * đọc rồi ghi trong cùng một `edit` để hai lần gọi liền nhau không ghi đè nhau.
     */
    suspend fun markCleared(floor: Int) {
        context.towerDataStore.edit { prefs ->
            val current = TowerProgress((prefs[KEY_CLEARED] ?: 0).coerceIn(0, TowerCatalog.FLOOR_COUNT))
            prefs[KEY_CLEARED] = current.withCleared(floor).clearedUpTo
        }
    }

    /** Xóa toàn bộ tiến độ chiến dịch (dùng cho "Xóa dữ liệu" trong Cài đặt). */
    suspend fun clear() {
        context.towerDataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_CLEARED = intPreferencesKey("cleared_up_to")
    }
}

/** Mỗi tên file DataStore chỉ được có đúng một thực thể, nên khai báo ở cấp file. */
private val Context.towerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "tower_progress",
)
