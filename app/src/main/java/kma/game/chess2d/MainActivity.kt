package kma.game.chess2d

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kma.game.chess2d.ai.Ai
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.net.PROTOCOL_VERSION
import kma.game.chess2d.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Phase0Screen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/** Man hinh tam cua Phase 0: xac nhan :app noi duoc voi ca 3 module JVM. */
@Composable
fun Phase0Screen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Chess 2D - Phase 0")
        Text("engine: ${Engine.BOARD_SIZE} o")
        Text("ai: ${Ai.describe(Difficulty.HARD)}")
        Text("net: protocol v$PROTOCOL_VERSION")
    }
}

@Preview(showBackground = true)
@Composable
private fun Phase0ScreenPreview() {
    ChessTheme { Phase0Screen() }
}
