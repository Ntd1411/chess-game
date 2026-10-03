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
 * Mặc định bật các công tắc phản hồi: âm thanh và rung giúp biết nước đi đã được ghi nhận,
 * nhưng phải tắt được vì chơi cờ thường là lúc cần yên tĩnh.
 *
 * Hai lựa chọn giao diện bàn cờ (mục 7.3) cũng nằm ở đây vì chúng thuộc về sở thích
 * của người chơi, không thuộc về một ván cụ thể nào.
 *
 * @param showLegalMoves có chấm các ô đi được khi chọn một quân hay không. Người chơi giỏi
 *        thường tắt để tự tính; mặc định bật cho người mới.
 */
data class AppSettings(
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val boardPalette: BoardPalette = BoardPalette.GREEN,
    val pieceTheme: PieceTheme = PieceTheme.SOLID,
    val showLegalMoves: Boolean = true,
)

/**
 * Nơi lưu [AppSettings].
 *
 * Tách khỏi nơi lưu ván đang chơi (dùng file DataStore riêng): bỏ một ván dở thì
 * không được làm mất luôn cài đặt âm thanh của người chơi.
 *
 * Hàm tạo chính nhận thẳng [DataStore] để test được trên JVM với file tạm; ứng dụng dùng
 * hàm tạo nhận [Context].
 */
class SettingsStore internal constructor(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsDataStore)

    /** Cài đặt hiện tại. Đọc file lỗi thì lùi về mặc định chứ không làm app chết. */
    val settings: Flow<AppSettings> = dataStore.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs ->
            AppSettings(
                soundEnabled = prefs[KEY_SOUND] ?: true,
                hapticEnabled = prefs[KEY_HAPTIC] ?: true,
                boardPalette = paletteOf(prefs[KEY_PALETTE]),
                pieceTheme = pieceThemeOf(prefs[KEY_PIECES]),
                showLegalMoves = prefs[KEY_LEGAL_MOVES] ?: true,
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTIC] = enabled }
    }

    suspend fun setBoardPalette(palette: BoardPalette) {
        dataStore.edit { it[KEY_PALETTE] = palette.name }
    }

    suspend fun setPieceTheme(theme: PieceTheme) {
        dataStore.edit { it[KEY_PIECES] = theme.name }
    }

    suspend fun setShowLegalMoves(show: Boolean) {
        dataStore.edit { it[KEY_LEGAL_MOVES] = show }
    }

    /** Đưa mọi cài đặt về mặc định. */
    suspend fun resetToDefaults() {
        dataStore.edit { it.clear() }
    }

    private companion object {
        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_HAPTIC = booleanPreferencesKey("haptic_enabled")
        val KEY_PALETTE = stringPreferencesKey("board_palette")
        val KEY_PIECES = stringPreferencesKey("piece_theme")
        val KEY_LEGAL_MOVES = booleanPreferencesKey("show_legal_moves")

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
