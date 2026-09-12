package kma.game.chess2d.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Bộ màu của toàn app: theo nền sáng/tối của hệ thống, và lấy màu nhấn từ hình nền
 * máy khi Android còn hỗ trợ (mục 7.2).
 *
 * **Không áp bộ màu này cho ô bàn cờ.** Màu ô nằm riêng trong
 * <code>BoardColors</code> với giá trị cố định, vì hai lý do: ô sáng và ô tối phải
 * giữ đủ độ tương phản để nhìn ra bàn cờ (màu lấy từ hình nền có thể ra hai sắc gần
 * nhau), và người chơi cờ vốn quen một bảng màu bàn cờ ổn định chứ không muốn bàn cờ
 * đổi màu theo ảnh nền. Bộ màu bàn cờ được đổi bằng một lựa chọn riêng, không phải
 * bằng dynamic color.
 *
 * @param darkTheme mặc định theo cài đặt hệ thống, nên đổi nền tối ở thanh cài đặt
 *        nhanh là app đổi theo ngay, không cần công tắc riêng trong app.
 * @param dynamicColor tắt được để xem giao diện với bộ màu gốc; từ Android 11 trở
 *        xuống thì tham số này không có tác dụng vì hệ thống chưa có màu động.
 */
@Composable
fun ChessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
