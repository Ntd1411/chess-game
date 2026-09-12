package kma.game.chess2d.game

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Một ván offline đang chơi dở, ở dạng gọn nhất có thể lưu xuống đĩa.
 *
 * Lưu **thế cờ đầu + danh sách nước đi** chứ không lưu ảnh chụp bàn cờ: đi lại cả ván
 * trên một bàn cờ mới thì lấy lại được y nguyên quyền nhập thành, ô bắt tốt qua đường,
 * bộ đếm 50 nước và cả lịch sử lặp thế — những thứ một ảnh chụp bàn cờ không chở nổi.
 * Đổi lại chỉ tốn vài chục byte cho một ván bình thường.
 *
 * @param startFen thế cờ mở ván. Hiện luôn là thế chuẩn, nhưng giữ sẵn để sau này mở
 *        ván từ câu đố hoặc từ FEN nhập vào mà không phải đổi định dạng lưu.
 * @param uciMoves các nước đã đi, theo ký hiệu UCI ("e2e4", "e7e8q").
 */
data class SavedGame(
    val startFen: String,
    val uciMoves: List<String>,
    val mode: GameMode,
    val difficulty: Difficulty,
)

/**
 * Nơi lưu ván offline đang chơi dở, đúng một ván.
 *
 * Chỉ dành cho ván offline (hai người một máy hoặc đấu máy). Ván LAN không lưu: socket
 * đã chết cùng tiến trình, dựng lại bàn cờ mà không có đối thủ thì vô nghĩa.
 *
 * Dùng DataStore chứ không SharedPreferences vì mọi lời gọi đọc/ghi đều là suspend và
 * chạy trên luồng nền, không có `commit()` chặn luồng chính như thói quen cũ.
 */
class SavedGameStore(private val context: Context) {

    /**
     * Ván đã lưu, hoặc null nếu chưa có gì.
     *
     * Đọc file lỗi (đĩa đầy, file bị cắt ngang vì máy tắt giữa lúc ghi) thì trả về
     * "không có ván nào" chứ không để ngoại lệ nổ lên giao diện: mất một ván đang dở
     * đã đủ tệ, không nên kèm thêm một lần crash.
     */
    val saved: Flow<SavedGame?> = context.savedGameDataStore.data
        .catch { cause -> if (cause is IOException) emit(emptyPreferences()) else throw cause }
        .map { prefs -> prefs.toSavedGame() }

    /** Ghi đè ván đang lưu. */
    suspend fun save(game: SavedGame) {
        context.savedGameDataStore.edit { prefs ->
            prefs[KEY_START_FEN] = game.startFen
            prefs[KEY_MOVES] = game.uciMoves.joinToString(MOVE_SEPARATOR)
            prefs[KEY_MODE] = game.mode.name
            prefs[KEY_DIFFICULTY] = game.difficulty.name
        }
    }

    /** Xoá ván đã lưu: người chơi chọn bỏ, hoặc ván vừa kết thúc. */
    suspend fun clear() {
        context.savedGameDataStore.edit { it.clear() }
    }

    /**
     * Đổi dữ liệu thô thành [SavedGame].
     *
     * Ván không có nước nào coi như không có ván: hiện câu hỏi "tiếp tục hay bỏ" cho một
     * bàn cờ còn nguyên vị trí đầu thì chỉ làm người chơi bối rối.
     *
     * Chế độ hoặc cấp độ đọc không ra tên hợp lệ (bản cũ ghi tên khác, hoặc file bị sửa
     * tay) thì lùi về giá trị mặc định thay vì bỏ cả ván.
     */
    private fun Preferences.toSavedGame(): SavedGame? {
        val moves = this[KEY_MOVES]?.split(MOVE_SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()
        if (moves.isEmpty()) return null
        return SavedGame(
            startFen = this[KEY_START_FEN] ?: Engine.START_FEN,
            uciMoves = moves,
            mode = this[KEY_MODE]?.let { name ->
                runCatching { GameMode.valueOf(name) }.getOrNull()
            } ?: GameMode.TWO_PLAYERS,
            difficulty = this[KEY_DIFFICULTY]?.let { name ->
                runCatching { Difficulty.valueOf(name) }.getOrNull()
            } ?: Difficulty.MEDIUM,
        )
    }

    private companion object {
        val KEY_START_FEN = stringPreferencesKey("start_fen")
        val KEY_MOVES = stringPreferencesKey("uci_moves")
        val KEY_MODE = stringPreferencesKey("mode")
        val KEY_DIFFICULTY = stringPreferencesKey("difficulty")

        /** Nước đi UCI không bao giờ chứa dấu cách, nên nối bằng dấu cách là đủ an toàn. */
        const val MOVE_SEPARATOR = " "
    }
}

/**
 * DataStore của ván đang lưu.
 *
 * Khai báo bằng delegate ở cấp file vì mỗi tên file chỉ được có **đúng một** thực thể
 * DataStore trong cả tiến trình; tạo hai lần cùng tên sẽ nổ ngay lúc chạy.
 */
private val Context.savedGameDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "saved_game",
)
