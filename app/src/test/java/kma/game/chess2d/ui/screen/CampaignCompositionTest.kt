package kma.game.chess2d.ui.screen

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class CampaignCompositionTest {
    @Test fun campaignInitializationIsCommittedBeforeAiAttachmentAndResultRendering() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/CampaignBattleScreen.kt").readText()
        assertFalse(source.contains("remember(floor.number, attempt) { viewModel.start"))
        val effect = source.indexOf("DisposableEffect(viewModel, floor.number, attempt)")
        val start = source.indexOf("viewModel.start(floor, attempt, puzzles)", effect)
        val attach = source.indexOf("viewModel.attach()", effect)
        val guard = source.indexOf("if (!initialized)", effect)
        val result = source.indexOf("LaunchedEffect(state.result", effect)
        assertTrue(effect >= 0 && start > effect && attach > start && guard > attach && result > guard)
    }
}
