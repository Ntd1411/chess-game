package kma.game.chess2d.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kma.game.chess2d.shop.ShopCatalog
import kma.game.chess2d.ui.board.BoardPalette
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UnlockCodeStoreTest {
    @get:Rule val tmp = TemporaryFolder()
    private fun withStore(block: suspend (SettingsStore) -> Unit) = runBlocking {
        val job = Job()
        val ds = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(tmp.root, "unlock.preferences_pb") }
        try { block(SettingsStore(ds)) } finally { job.cancelAndJoin() }
    }
    @Test fun codeIgnoresOuterWhitespaceAndCaseButRejectsOtherValues() {
        assertTrue(UnlockCode.matches("TOWER49"))
        assertTrue(UnlockCode.matches("  tower49\n"))
        for (code in listOf("", "TOWER 49", "TOWER48", "TOWER49x")) assertFalse(UnlockCode.matches(code))
    }
    @Test fun invalidCodeDoesNotChangeAnySavedState() = withStore { store ->
        val before = store.cosmetics.first()
        assertEquals(UnlockResult.INVALID_CODE, store.redeemUnlockCode("wrong"))
        assertFalse(store.fullUnlock.first())
        assertEquals(before, store.cosmetics.first())
        assertEquals(AppSettings(), store.settings.first())
    }
    @Test fun unlockGrantsEverythingWithoutChangingSpendingOrSelectedSettings() = withStore { store ->
        store.purchaseCosmetic("board_WOOD", 1000)
        store.setBoardPalette(BoardPalette.WOOD)
        store.setSoundEnabled(false)
        val settings = store.settings.first()
        assertEquals(UnlockResult.UNLOCKED, store.redeemUnlockCode("tower49"))
        assertTrue(store.fullUnlock.first())
        assertEquals(ShopCatalog.items.map { it.id }.toSet(), store.cosmetics.first().owned)
        assertEquals(300, store.cosmetics.first().spentGold)
        assertEquals(700, store.cosmetics.first().availableGold(1000))
        assertEquals(settings, store.settings.first())
        assertEquals(UnlockResult.ALREADY_UNLOCKED, store.redeemUnlockCode("TOWER49"))
    }
    @Test fun concurrentRedemptionsApplyExactlyOnce() = withStore { store ->
        val results = coroutineScope { List(8) { async { store.redeemUnlockCode("TOWER49") } }.awaitAll() }
        assertEquals(1, results.count { it == UnlockResult.UNLOCKED })
        assertEquals(7, results.count { it == UnlockResult.ALREADY_UNLOCKED })
        assertEquals(0, store.cosmetics.first().spentGold)
    }
    @Test fun reopeningAndRestoringDefaultsKeepUnlockButFullResetRemovesIt() {
        withStore { it.redeemUnlockCode("TOWER49") }
        withStore { store ->
            assertTrue(store.fullUnlock.first())
            store.resetToDefaults()
            assertTrue(store.fullUnlock.first())
            assertEquals(ShopCatalog.items.map { it.id }.toSet(), store.cosmetics.first().owned)
            store.clearAll()
            assertFalse(store.fullUnlock.first())
            assertEquals(ShopCatalog.freeIds, store.cosmetics.first().owned)
        }
    }
}
