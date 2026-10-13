package kma.game.chess2d.ui.screen

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class SplashAssetAlignmentTest {
    @Test fun progressReflectsRealPreloadCountsAndStaysBounded() {
        assertEquals(0f, splashFraction(0, 20), 0f)
        assertEquals(0.5f, splashFraction(10, 20), 0f)
        assertEquals(1f, splashFraction(20, 20), 0f)
        assertEquals(1f, splashFraction(21, 20), 0f)
        assertEquals(1f, splashFraction(0, 0), 0f)
        assertEquals(0f, splashFraction(-1, 20), 0f)
    }
    @Test fun splashRetainsLogoAndRealPreloadCompletionInsteadOfArtworkBar() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/SplashScreen.kt").readText()
        assertTrue(source.contains("bg_splash"))
        assertTrue(source.contains("ArtCache.preload"))
        assertTrue(source.contains("snapshotFlow { shownProgress }.first { it >= 1f }"))
        assertTrue(source.contains("FULL_BAR_HOLD_MILLIS"))
        assertFalse(source.contains("ArtBar"))
    }
}
