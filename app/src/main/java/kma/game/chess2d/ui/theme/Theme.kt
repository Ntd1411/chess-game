package kma.game.chess2d.ui.theme

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * Bảng màu Material cố định theo phong cách gothic.
 *
 * Không còn theo dynamic color hay sáng/tối của hệ thống: trước đây dialog, thẻ và nút
 * đổi màu theo từng máy nên các màn không thống nhất với phần ảnh nền. Giờ mọi thành phần
 * Material (Button, Card, AlertDialog, TextField, Chip, Switch...) tự ăn theo bảng này.
 */
private val GothicColorScheme = darkColorScheme(
    primary = GothicColors.Blood,
    onPrimary = GothicColors.Gold,
    primaryContainer = GothicColors.Blood,
    onPrimaryContainer = GothicColors.Parchment,
    secondary = GothicColors.Gold,
    onSecondary = GothicColors.Ink,
    secondaryContainer = GothicColors.Blood,
    onSecondaryContainer = GothicColors.Gold,
    tertiary = GothicColors.Parchment,
    onTertiary = GothicColors.Ink,
    background = GothicColors.Ink,
    onBackground = GothicColors.Parchment,
    surface = GothicColors.Panel,
    onSurface = GothicColors.Parchment,
    surfaceVariant = GothicColors.PanelHigh,
    onSurfaceVariant = GothicColors.Parchment.copy(alpha = 0.78f),
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = GothicColors.Ink,
    surfaceContainerLow = GothicColors.Panel,
    surfaceContainer = GothicColors.Panel,
    surfaceContainerHigh = GothicColors.PanelHigh,
    surfaceContainerHighest = GothicColors.PanelHigh,
    outline = GothicColors.Gold.copy(alpha = 0.7f),
    outlineVariant = GothicColors.Gold.copy(alpha = 0.3f),
    error = GothicColors.Alert,
    onError = GothicColors.Ink,
)

/** Tiêu đề dùng font serif cho cảm giác sách cổ; chữ thân giữ sans để dễ đọc. */
private val GothicTypography: Typography = Typography().let { base ->
    fun androidx.compose.ui.text.TextStyle.serif() =
        copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
    base.copy(
        displayLarge = base.displayLarge.serif(),
        displayMedium = base.displayMedium.serif(),
        displaySmall = base.displaySmall.serif(),
        headlineLarge = base.headlineLarge.serif(),
        headlineMedium = base.headlineMedium.serif(),
        headlineSmall = base.headlineSmall.serif(),
        titleLarge = base.titleLarge.serif(),
        titleMedium = base.titleMedium.serif(),
    )
}

/**
 * Theme của toàn app: luôn tối và luôn gothic.
 *
 * App không còn Scaffold hay Surface gốc, nên `LocalContentColor` mặc định là đen: chữ đặt thẳng
 * trên nền tối (không nằm trong Card/Button) sẽ không đọc được. Đặt sẵn màu giấy cổ ở đây để
 * mọi chữ không tự chỉ định màu đều đọc được trên nền gothic.
 */
@Composable
fun ChessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GothicColorScheme,
        typography = GothicTypography,
    ) {
        CompositionLocalProvider(LocalContentColor provides GothicColors.Parchment, content = content)
    }
}
