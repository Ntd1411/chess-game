package kma.game.chess2d.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kma.game.chess2d.shop.*
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
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val boardPalette: BoardPalette = BoardPalette.GOTHIC,
    val pieceTheme: PieceTheme = PieceTheme.IMAGE,
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
                musicEnabled = prefs[KEY_MUSIC] ?: true,
                hapticEnabled = prefs[KEY_HAPTIC] ?: true,
                boardPalette = paletteOf(prefs[KEY_PALETTE]),
                pieceTheme = pieceThemeOf(prefs[KEY_PIECES]),
                showLegalMoves = prefs[KEY_LEGAL_MOVES] ?: true,
            )
        }

    suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_SOUND] = enabled }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_MUSIC] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_HAPTIC] = enabled }
    }

    val cosmetics: Flow<ShopState> = dataStore.data.map { shopState(it) }
        .catch { cause -> if (cause is IOException) emit(ShopState(readable = false)) else throw cause }

    val fullUnlock: Flow<Boolean> = dataStore.data.map { it[KEY_FULL_UNLOCK] ?: false }
        .catch { cause -> if (cause is IOException) emit(false) else throw cause }

    suspend fun redeemUnlockCode(input: String): UnlockResult {
        if (!UnlockCode.matches(input)) return UnlockResult.INVALID_CODE
        var result = UnlockResult.ALREADY_UNLOCKED
        dataStore.edit { prefs ->
            if (prefs[KEY_FULL_UNLOCK] != true) {
                prefs[KEY_FULL_UNLOCK] = true
                prefs[KEY_OWNED] = shopState(prefs).owned + ShopCatalog.items.map { it.id }
                result = UnlockResult.UNLOCKED
            }
        }
        return result
    }

    // Ownership and spending share one serialized edit, preventing double charges.
    suspend fun purchaseCosmetic(id: String, earnedGold: Int): PurchaseResult {
        val item = requireNotNull(ShopCatalog.item(id)) { "Unknown cosmetic: $id" }
        require(earnedGold >= 0)
        var result = PurchaseResult.INSUFFICIENT_GOLD
        dataStore.edit { prefs ->
            val state = shopState(prefs)
            result = when {
                id in state.owned -> PurchaseResult.ALREADY_OWNED
                state.availableGold(earnedGold) < item.goldPrice -> PurchaseResult.INSUFFICIENT_GOLD
                else -> {
                    prefs[KEY_OWNED] = state.owned + id
                    prefs[KEY_SPENT] = Math.addExact(state.spentGold, item.goldPrice)
                    PurchaseResult.BOUGHT
                }
            }
        }
        return result
    }

    suspend fun setBoardPalette(palette: BoardPalette) {
        dataStore.edit {
            val state = shopState(it)
            require(ShopCatalog.boardId(palette) in state.owned) { "Board palette is not owned" }
            it[KEY_OWNED] = state.owned
            it[KEY_PALETTE] = palette.name
        }
    }

    suspend fun setPieceTheme(theme: PieceTheme) {
        dataStore.edit {
            val state = shopState(it)
            require(ShopCatalog.pieceId(theme) in state.owned) { "Piece theme is not owned" }
            it[KEY_OWNED] = state.owned
            it[KEY_PIECES] = theme.name
        }
    }

    suspend fun setShowLegalMoves(show: Boolean) {
        dataStore.edit { it[KEY_LEGAL_MOVES] = show }
    }

    /** Đưa mọi cài đặt về mặc định. */
    suspend fun resetToDefaults() {
        dataStore.edit {
            val state = shopState(it)
            val unlocked = it[KEY_FULL_UNLOCK] ?: false
            it.clear()
            it[KEY_OWNED] = state.owned
            it[KEY_SPENT] = state.spentGold
            it[KEY_FULL_UNLOCK] = unlocked
        }
    }

    /** Explicit data reset removes purchases as well as settings. */
    suspend fun clearAll() { dataStore.edit { it.clear() } }

    private companion object {
        val KEY_FULL_UNLOCK = booleanPreferencesKey("full_content_unlocked")
        val KEY_OWNED = stringSetPreferencesKey("cosmetics_owned")
        val KEY_SPENT = intPreferencesKey("cosmetics_spent_gold")

        fun shopState(prefs: Preferences): ShopState {
            // Grandfather the selected cosmetics from installations predating the shop.
            val legacy = setOf(ShopCatalog.boardId(paletteOf(prefs[KEY_PALETTE])), ShopCatalog.pieceId(pieceThemeOf(prefs[KEY_PIECES])))
            val fullAccess = if (prefs[KEY_FULL_UNLOCK] == true) ShopCatalog.items.map { it.id }.toSet() else emptySet()
            val owned = (prefs[KEY_OWNED] ?: legacy).filter { ShopCatalog.item(it) != null }.toSet() + ShopCatalog.freeIds + fullAccess
            return ShopState(owned, (prefs[KEY_SPENT] ?: 0).coerceAtLeast(0))
        }

        val KEY_SOUND = booleanPreferencesKey("sound_enabled")
        val KEY_MUSIC = booleanPreferencesKey("music_enabled")
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
            BoardPalette.entries.firstOrNull { it.name == name } ?: BoardPalette.GOTHIC

        fun pieceThemeOf(name: String?): PieceTheme =
            PieceTheme.entries.firstOrNull { it.name == name } ?: PieceTheme.IMAGE
    }
}

/** Mỗi tên file DataStore chỉ được có đúng một thực thể, nên khai báo ở cấp file. */
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings",
)
