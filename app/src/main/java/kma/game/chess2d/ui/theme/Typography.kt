package kma.game.chess2d.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kma.game.chess2d.R

/** Hai font đóng gói trong APK, dùng chung cho Material và các thành phần gothic tự vẽ. */
internal val GameFontFamily = FontFamily(
    Font(R.font.font_normal, weight = FontWeight.Normal),
    Font(R.font.font_bold, weight = FontWeight.Bold),
)

/** Giữ nguyên cỡ chữ Material; tiêu đề dùng bản đậm, nội dung dùng bản thường. */
internal val GothicTypography: Typography = Typography().let { base ->
    fun TextStyle.game(weight: FontWeight = fontWeight ?: FontWeight.Normal) =
        copy(fontFamily = GameFontFamily, fontWeight = weight)
    base.copy(
        displayLarge = base.displayLarge.game(FontWeight.Bold),
        displayMedium = base.displayMedium.game(FontWeight.Bold),
        displaySmall = base.displaySmall.game(FontWeight.Bold),
        headlineLarge = base.headlineLarge.game(FontWeight.Bold),
        headlineMedium = base.headlineMedium.game(FontWeight.Bold),
        headlineSmall = base.headlineSmall.game(FontWeight.Bold),
        titleLarge = base.titleLarge.game(FontWeight.Bold),
        titleMedium = base.titleMedium.game(FontWeight.Bold),
        titleSmall = base.titleSmall.game(FontWeight.Bold),
        bodyLarge = base.bodyLarge.game(),
        bodyMedium = base.bodyMedium.game(),
        bodySmall = base.bodySmall.game(),
        labelLarge = base.labelLarge.game(FontWeight.Bold),
        labelMedium = base.labelMedium.game(FontWeight.Bold),
        labelSmall = base.labelSmall.game(FontWeight.Bold),
    )
}
