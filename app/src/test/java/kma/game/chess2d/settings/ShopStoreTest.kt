package kma.game.chess2d.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.File
import kma.game.chess2d.shop.*
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShopStoreTest {
    @get:Rule val tmp = TemporaryFolder()
    private fun withStore(block: suspend (SettingsStore) -> Unit) = runBlocking {
        val job = Job()
        val ds = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(tmp.root, "shop.preferences_pb") }
        try { block(SettingsStore(ds)) } finally { job.cancelAndJoin() }
    }
    @Test fun catalogueCoversEveryRealCosmeticWithStableUniqueIds() {
        assertEquals(9, ShopCatalog.items.size)
        assertEquals(9, ShopCatalog.items.map { it.id }.toSet().size)
        assertEquals(BoardPalette.entries.toSet(), ShopCatalog.items.mapNotNull { it.palette }.toSet())
        assertEquals(PieceTheme.entries.toSet(), ShopCatalog.items.mapNotNull { it.pieces }.toSet())
        assertEquals(setOf("board_GOTHIC", "pieces_IMAGE"), ShopCatalog.freeIds)
        assertTrue(ShopCatalog.items.all { it.goldPrice >= 0 && ((it.palette == null) != (it.pieces == null)) })
    }
    @Test fun insufficientGoldDoesNotChangeOwnershipOrBalance() = withStore { store ->
        val before = store.cosmetics.first()
        assertEquals(PurchaseResult.INSUFFICIENT_GOLD, store.purchaseCosmetic("board_GREEN", 199))
        assertEquals(before, store.cosmetics.first())
    }
    @Test fun exactBalancePurchasesOnceAndSurvivesReopening() {
        withStore { store ->
            assertEquals(PurchaseResult.BOUGHT, store.purchaseCosmetic("board_GREEN", 200))
            assertEquals(PurchaseResult.ALREADY_OWNED, store.purchaseCosmetic("board_GREEN", 200))
            assertEquals(0, store.cosmetics.first().availableGold(200))
            store.setBoardPalette(BoardPalette.GREEN)
        }
        withStore { store ->
            assertEquals(200, store.cosmetics.first().spentGold)
            assertTrue("board_GREEN" in store.cosmetics.first().owned)
            assertEquals(BoardPalette.GREEN, store.settings.first().boardPalette)
        }
    }
    @Test fun concurrentPurchasesCannotOverspendOrDoubleCharge() = withStore { store ->
        coroutineScope {
            val outcomes = List(8) { async { store.purchaseCosmetic("board_GREEN", 200) } }.awaitAll()
            assertEquals(1, outcomes.count { it == PurchaseResult.BOUGHT })
        }
        assertEquals(PurchaseResult.INSUFFICIENT_GOLD, store.purchaseCosmetic("pieces_LETTER", 200))
        assertEquals(200, store.cosmetics.first().spentGold)
    }
    @Test fun defaultsPreservePurchasesButExplicitResetClearsThem() = withStore { store ->
        store.purchaseCosmetic("board_WOOD", 300)
        store.setBoardPalette(BoardPalette.WOOD)
        store.resetToDefaults()
        assertEquals(AppSettings(), store.settings.first())
        assertTrue("board_WOOD" in store.cosmetics.first().owned)
        assertEquals(300, store.cosmetics.first().spentGold)
        store.clearAll()
        assertEquals(ShopState(), store.cosmetics.first())
    }
    @Test fun unownedCosmeticsCannotBeApplied() = withStore { store ->
        var rejected = false
        try { store.setBoardPalette(BoardPalette.OCEAN) } catch (error: IllegalArgumentException) { rejected = true }
        assertTrue(rejected)
        assertEquals(AppSettings(), store.settings.first())
        assertEquals(0, store.cosmetics.first().spentGold)
    }
    @Test fun legacySelectedCosmeticsAreGrandfatheredWithoutSpending() = runBlocking {
        val job = Job()
        val ds = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { File(tmp.root, "legacy.preferences_pb") }
        try {
            ds.edit { it[stringPreferencesKey("board_palette")] = "WOOD"; it[stringPreferencesKey("piece_theme")] = "OUTLINE" }
            val store = SettingsStore(ds)
            assertTrue("board_WOOD" in store.cosmetics.first().owned)
            assertTrue("pieces_OUTLINE" in store.cosmetics.first().owned)
            store.resetToDefaults()
            assertTrue("board_WOOD" in store.cosmetics.first().owned)
            assertEquals(0, store.cosmetics.first().spentGold)
        } finally { job.cancelAndJoin() }
    }
}
