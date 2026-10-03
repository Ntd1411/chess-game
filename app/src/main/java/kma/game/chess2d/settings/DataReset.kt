package kma.game.chess2d.settings

import android.content.Context
import kma.game.chess2d.campaign.TowerProgressStore
import kma.game.chess2d.game.SavedGameStore
import kma.game.chess2d.history.MatchHistoryDatabase
import kma.game.chess2d.profile.ProfileDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * "Xóa dữ liệu" trong Cài đặt: đưa app về như mới cài.
 *
 * Xóa cài đặt, tiến độ Tháp Cờ, ván đang dở, lịch sử ván đấu, Hồ sơ và Nhật ký. Không xóa tên
 * người chơi vì tên chỉ nằm trong bộ nhớ của phiên hiện tại.
 *
 * Phải gọi từ coroutine: `clearAllTables` là I/O đồng bộ nên được chuyển sang [Dispatchers.IO].
 */
object DataReset {

    suspend fun resetAll(context: Context) {
        val app = context.applicationContext
        SettingsStore(app).resetToDefaults()
        TowerProgressStore(app).clear()
        SavedGameStore(app).clear()
        withContext(Dispatchers.IO) {
            MatchHistoryDatabase.get(app).clearAllTables()
            ProfileDatabase.get(app).clearAllTables()
        }
    }
}
