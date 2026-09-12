package kma.game.chess2d.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Các công tắc người chơi tự đặt, sống qua cả lần tắt app.
 *
 * Mặc định bật cả hai công tắc: âm thanh và rung là phản hồi giúp biết nước đi đã
 * được ghi nhận, nhưng phải tắt được vì chơi cờ thường là lúc cần yên tĩnh.
 *
 * Hai lựa chọn giao diện bàn cờ (mục 7.3) cũng nằm ở đây vì chúng thuộc về sở thích
 * của người chơi, không thuộc về một ván cụ thể nào.
 */
data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val boardPalette: BoardPalette = BoardPalette.GREEN,
    val pieceTheme: PieceTheme = PieceTheme.SOLID,
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
                boardPalette = paletteOf(prefs[KEY_PALETTE]),
                pieceTheme = pieceThemeOf(prefs[KEY_PIECES]),
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[KEY_HAPTIC] = enabled }
    }

    suspend fun setBoardPalette(palette: BoardPalette) {
        context.settingsDataStore.edit { it[KEY_PALETTE] = palette.name }
    }

    suspend fun setPieceTheme(theme: PieceTheme) {
        context.settingsDataStore.edit { it[KEY_PIECES] = theme.name }
    }

    private companion object {
        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_enabled")
        val KEY_PALETTE = stringPreferencesKey("board_palette")
        val KEY_PIECES = stringPreferencesKey("piece_theme")

        /**
         * Lưu theo tên enum chứ không theo số thứ tự: thêm hay sắp lại một bộ màu về sau
         * sẽ không biến lựa chọn cũ của người chơi thành bộ khác. Tên không đọc được
         * (bản cũ hơn, hoặc bộ đã bị bỏ) thì lùi về mặc định.
         */
        fun paletteOf(name: String?): BoardPalette =
            BoardPalette.entries.firstOrNull { it.name == name } ?: BoardPalette.GREEN

        fun pieceThemeOf(name: String?): PieceTheme =
            PieceTheme.entries.firstOrNull { it.name == name } ?: PieceTheme.SOLID
    }
}

/** Mỗi tên file DataStore chỉ được có đúng một thực thể, nên khai báo ở cấp file. */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)
