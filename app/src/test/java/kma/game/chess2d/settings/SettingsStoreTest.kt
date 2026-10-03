package kma.game.chess2d.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Cài đặt: đổi giá trị → lưu → đọc lại đúng, kể cả sau khi mở lại file (như tắt/mở app). */
class SettingsStoreTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun file() = File(tmp.root, "settings.preferences_pb")

    /** Chạy [block] với một DataStore trên file tạm, rồi đóng hẳn để file mở lại được. */
    private fun withStore(block: suspend (SettingsStore, DataStore<Preferences>) -> Unit) = runBlocking {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.IO + job)
        try {
            val ds = PreferenceDataStoreFactory.create(scope = scope) { file() }
            block(SettingsStore(ds), ds)
        } finally {
            job.cancelAndJoin()
        }
    }

    @Test
    fun `chua luu gi thi tra ve mac dinh`() = withStore { store, _ ->
        assertEquals(AppSettings(), store.settings.first())
    }

    @Test
    fun `mac dinh bat am thanh rung va nuoc di hop le`() {
        val d = AppSettings()
        assertTrue(d.soundEnabled)
        assertTrue(d.musicEnabled)
        assertTrue(d.hapticEnabled)
        assertTrue(d.showLegalMoves)
    }

    @Test
    fun `doi tung gia tri roi doc lai dung`() = withStore { store, _ ->
        store.setSoundEnabled(false)
        store.setMusicEnabled(false)
        store.setHapticEnabled(false)
        store.setShowLegalMoves(false)
        store.setBoardPalette(BoardPalette.WOOD)
        store.setPieceTheme(PieceTheme.LETTER)

        val s = store.settings.first()
        assertFalse(s.soundEnabled)
        assertFalse(s.musicEnabled)
        assertFalse(s.hapticEnabled)
        assertFalse(s.showLegalMoves)
        assertEquals(BoardPalette.WOOD, s.boardPalette)
        assertEquals(PieceTheme.LETTER, s.pieceTheme)
    }

    @Test
    fun `doi mot gia tri khong lam hong gia tri khac`() = withStore { store, _ ->
        store.setBoardPalette(BoardPalette.OCEAN)
        store.setShowLegalMoves(false)
        val s = store.settings.first()
        assertEquals(BoardPalette.OCEAN, s.boardPalette)
        assertFalse(s.showLegalMoves)
        // Các giá trị chưa đụng tới vẫn là mặc định.
        assertTrue(s.soundEnabled)
        assertEquals(PieceTheme.IMAGE, s.pieceTheme)
    }

    @Test
    fun `gia tri van con sau khi mo lai file`() {
        withStore { store, _ ->
            store.setSoundEnabled(false)
            store.setPieceTheme(PieceTheme.OUTLINE)
        }
        // DataStore thứ hai trên cùng file = mở lại app.
        withStore { store, _ ->
            val s = store.settings.first()
            assertFalse(s.soundEnabled)
            assertEquals(PieceTheme.OUTLINE, s.pieceTheme)
        }
    }

    @Test
    fun `ten bo mau la thi lui ve mac dinh`() = withStore { store, ds ->
        ds.edit { it[stringPreferencesKey("board_palette")] = "BO_MAU_DA_BI_BO" }
        ds.edit { it[stringPreferencesKey("piece_theme")] = "???" }
        val s = store.settings.first()
        assertEquals(BoardPalette.GOTHIC, s.boardPalette)
        assertEquals(PieceTheme.IMAGE, s.pieceTheme)
    }

    @Test
    fun `dat lai mac dinh xoa moi thay doi`() = withStore { store, _ ->
        store.setSoundEnabled(false)
        store.setShowLegalMoves(false)
        store.setBoardPalette(BoardPalette.SLATE)
        store.resetToDefaults()
        assertEquals(AppSettings(), store.settings.first())
    }

    @Test
    fun `tat nhac nen khong dong den tieng dat quan`() = withStore { store, _ ->
        store.setMusicEnabled(false)
        val s = store.settings.first()
        assertFalse(s.musicEnabled)
        assertTrue(s.soundEnabled)
    }

    @Test
    fun `nam tab dung thu tu spec`() {
        assertEquals(
            listOf("GENERAL", "SOUND", "VISUAL", "CONTROLS", "OTHER"),
            kma.game.chess2d.ui.screen.SettingsTab.entries.map { it.name },
        )
    }
}
