package kma.game.chess2d

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import kma.game.chess2d.ui.screen.AppRoot
import kma.game.chess2d.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Giao diện luôn tối nên thanh hệ thống luôn dùng icon sáng, không theo chế độ của máy.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            ChessTheme {
                // Không còn Scaffold: nó cộng thêm padding inset trong khi nhiều màn tự xử lý inset,
                // gây cộng đôi và lộ dải nền hệ thống. AppRoot tự lo inset cho từng màn.
                AppRoot(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
