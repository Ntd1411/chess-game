package kma.game.chess2d.ui.theme

import androidx.compose.ui.text.font.Font as ComposeFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import java.awt.Font
import java.io.File
import kma.game.chess2d.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GameTypographyTest {
    @Test
    fun everyMaterialTextRoleUsesBundledFonts() {
        val t = GothicTypography
        val styles = listOf(
            t.displayLarge, t.displayMedium, t.displaySmall,
            t.headlineLarge, t.headlineMedium, t.headlineSmall,
            t.titleLarge, t.titleMedium, t.titleSmall,
            t.bodyLarge, t.bodyMedium, t.bodySmall,
            t.labelLarge, t.labelMedium, t.labelSmall,
        )
        styles.forEach { assertEquals(GameFontFamily, it.fontFamily) }
        listOf(t.bodyLarge, t.bodyMedium, t.bodySmall).forEach {
            assertEquals(FontWeight.Normal, it.fontWeight)
        }
        styles.take(7).forEach { assertEquals(FontWeight.Bold, it.fontWeight) }
        listOf(t.titleMedium, t.titleSmall, t.labelLarge).forEach {
            assertEquals(FontWeight.SemiBold, it.fontWeight)
        }
        listOf(t.labelMedium, t.labelSmall).forEach {
            assertEquals(FontWeight.Medium, it.fontWeight)
        }
    }

    @Test
    fun familyMapsAllNineWeightsAndRealItalicFaces() {
        val faces = listOf(
            Triple(R.font.fz_poppins_thin, R.font.fz_poppins_thin_italic, FontWeight.Thin),
            Triple(R.font.fz_poppins_extra_light, R.font.fz_poppins_extra_light_italic, FontWeight.ExtraLight),
            Triple(R.font.fz_poppins_light, R.font.fz_poppins_light_italic, FontWeight.Light),
            Triple(R.font.fz_poppins_regular, R.font.fz_poppins_regular_italic, FontWeight.Normal),
            Triple(R.font.fz_poppins_medium, R.font.fz_poppins_medium_italic, FontWeight.Medium),
            Triple(R.font.fz_poppins_semi_bold, R.font.fz_poppins_semi_bold_italic, FontWeight.SemiBold),
            Triple(R.font.fz_poppins_bold, R.font.fz_poppins_bold_italic, FontWeight.Bold),
            Triple(R.font.fz_poppins_extra_bold, R.font.fz_poppins_extra_bold_italic, FontWeight.ExtraBold),
            Triple(R.font.fz_poppins_black, R.font.fz_poppins_black_italic, FontWeight.Black),
        )
        val expected = faces.flatMap { (normal, italic, weight) ->
            listOf(ComposeFont(normal, weight), ComposeFont(italic, weight, FontStyle.Italic))
        }
        assertEquals(FontFamily(expected), GameFontFamily)
    }

    @Test
    fun allFontFilesHaveAndroidNamesAndVietnameseGlyphs() {
        val sample = "Khám Phá Tháp Cờ Người vs Máy Nhật ký Đối thủ Cài đặt ăâđêôơưĂÂĐÊÔƠƯ 0123456789"
        val vietnamese = "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯư" +
            (0x1EA0..0x1EF9).map { it.toChar() }.joinToString("")
        val dir = File("src/main/res/font")
        val weights = listOf("thin", "extra_light", "light", "regular", "medium", "semi_bold", "bold", "extra_bold", "black")
        val names = weights.flatMap { listOf("fz_poppins_$it.ttf", "fz_poppins_${it}_italic.ttf") }
        assertEquals(names.toSet(), dir.listFiles()!!.map { it.name }.toSet())
        val report = StringBuilder()
        for (name in names) {
            assertTrue("Invalid Android resource name: $name", name.matches(Regex("[a-z][a-z0-9_]*\\.ttf")))
            val file = File(dir, name)
            val font = Font.createFont(Font.TRUETYPE_FONT, file)
            assertTrue("Font must support labels and numbers", font.canDisplayUpTo("Tower of Chess 0123456789") < 0)
            val missing = (sample + vietnamese).filterNot { font.canDisplay(it) }.toSet()
            assertTrue("Vietnamese glyphs missing in $name: $missing", missing.isEmpty())
            report.append("$name family=${font.family} name=${font.fontName} bytes=${file.length()} missing=$missing\n")
        }
        report.append("timestamp=" + java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        File("build/game_font_report.txt").writeText(report.toString())
    }
}
