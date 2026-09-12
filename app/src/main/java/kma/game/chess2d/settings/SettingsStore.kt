package kma.game.chess2d.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Các công tắc người chơi tự đặt, sống qua cả lần tắt app.
 *
 * Mặc định bật cả hai: âm thanh và rung là phản hồi giúp biết nước đi đã được ghi
 * nhận, nhưng phải tắt được vì chơi cờ thường là lúc cần yên tĩnh.
 */
data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
)

/**
 * Nơi lưu [AppSettings].
 *
 * Tách khỏi nơi lưu ván đang chơi (dùng file DataStore riêng): bỏ một ván dở thì
 * không được làm mất luôn cài đặt âm thanh của người chơi.
 */
class SettingsStore(private val context: Context) {

    /** Cài đặt hiện tại. Đọc file lỗi thì lùi về mặc định chứ không làm app chết. */
    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs ->
            AppSettings(
                soundEnabled = prefs[KEY_SOUND] ?: true,
                hapticEnabled = prefs[KEY_HAPTIC] ?: true,
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_HAPTIC] = enabled }
    }

    private companion object {
        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_enabled")
    }
}

/** Mỗi tên file DataStore chỉ được có đúng một thực thể, nên khai báo ở cấp file. */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)
