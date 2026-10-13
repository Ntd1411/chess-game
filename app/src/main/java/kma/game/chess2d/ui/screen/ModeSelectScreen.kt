package kma.game.chess2d.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ui.theme.GothicColors

internal enum class ModeChoice(val title: Int, val description: Int) {
    TOWER(R.string.menu_tower, R.string.mode_story_desc), AI(R.string.menu_vs_ai, R.string.mode_ai_desc),
    LOCAL(R.string.menu_local, R.string.mode_local_desc), LAN(R.string.menu_lan, R.string.mode_lan_desc),
    SPECTATE(R.string.spectate_open, R.string.mode_spectate_desc),
}

@Composable
fun ModeSelectScreen(onOpenTower: () -> Unit, onPlayComputer: () -> Unit, onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit, onOpenSpectate: () -> Unit, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val actions = listOf(onOpenTower, onPlayComputer, onPlayTwoPlayers, onPlayLan, onOpenSpectate)
    SimpleSetupPage(R.drawable.bg_mode_select, stringResource(R.string.modes_title), stringResource(R.string.action_back), onBack, modifier,
        footer = { SimpleSetupButton(stringResource(R.string.action_back), onBack, Modifier.fillMaxWidth()) }) {
        ModeChoice.entries.forEach { choice ->
            SimpleSetupCard(Modifier.clickable(role = Role.Button, onClick = actions[choice.ordinal])) {
                Text(stringResource(choice.title), color = GothicColors.Gold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(choice.description), color = GothicColors.Muted, fontSize = 14.sp)
            }
        }
    }
}
