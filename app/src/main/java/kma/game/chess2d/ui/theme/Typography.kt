package kma.game.chess2d.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kma.game.chess2d.R

/** FZ Poppins: đủ 9 độ đậm và bản nghiêng thật, dùng chung cho toàn bộ chữ giao diện. */
internal val GameFontFamily = FontFamily(
    Font(R.font.fz_poppins_thin, FontWeight.Thin),
    Font(R.font.fz_poppins_thin_italic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.fz_poppins_extra_light, FontWeight.ExtraLight),
    Font(R.font.fz_poppins_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.fz_poppins_light, FontWeight.Light),
    Font(R.font.fz_poppins_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.fz_poppins_regular, FontWeight.Normal),
    Font(R.font.fz_poppins_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.fz_poppins_medium, FontWeight.Medium),
    Font(R.font.fz_poppins_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.fz_poppins_semi_bold, FontWeight.SemiBold),
    Font(R.font.fz_poppins_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.fz_poppins_bold, FontWeight.Bold),
    Font(R.font.fz_poppins_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.fz_poppins_extra_bold, FontWeight.ExtraBold),
    Font(R.font.fz_poppins_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.fz_poppins_black, FontWeight.Black),
    Font(R.font.fz_poppins_black_italic, FontWeight.Black, FontStyle.Italic),
)

/** Giữ nguyên cỡ chữ: Regular cho nội dung, Medium/SemiBold cho nhãn, Bold cho tiêu đề lớn. */
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
        titleMedium = base.titleMedium.game(FontWeight.SemiBold),
        titleSmall = base.titleSmall.game(FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.game(FontWeight.Normal),
        bodyMedium = base.bodyMedium.game(FontWeight.Normal),
        bodySmall = base.bodySmall.game(FontWeight.Normal),
        labelLarge = base.labelLarge.game(FontWeight.SemiBold),
        labelMedium = base.labelMedium.game(FontWeight.Medium),
        labelSmall = base.labelSmall.game(FontWeight.Medium),
    )
}
