package kma.game.chess2d.ui.screen

import org.junit.Assert.assertEquals
import org.junit.Test

class SpectateButtonTest {
    @Test fun drawnButtonsRetainRunningPausedManualAndFinishedRules() {
        for (paused in listOf(false, true)) for (auto in listOf(false, true)) for (finished in listOf(false, true)) {
            val enabled = spectateControlAvailability(paused, auto, finished)
            assertEquals(auto && !finished, enabled.pause)
            assertEquals((!auto || paused) && !finished, enabled.step)
            assertEquals(!finished, enabled.auto)
        }
    }
}
