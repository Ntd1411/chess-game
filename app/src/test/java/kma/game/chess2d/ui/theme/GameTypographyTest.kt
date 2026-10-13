package kma.game.chess2d.ui.theme

import androidx.compose.ui.text.font.FontWeight
import java.awt.Font
import java.io.File
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
        (styles.take(9) + styles.takeLast(3)).forEach {
            assertEquals(FontWeight.Bold, it.fontWeight)
        }
    }

    @Test
    fun bundledFontFilesAreValidAndContainBasicText() {
        val sample = "Khám Phá Tháp Cờ Người vs Máy Nhật ký Đối thủ Cài đặt ăâđêôơưĂÂĐÊÔƠƯ 0123456789"
        val vietnamese = "ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚÝàáâãèéêìíòóôõùúýĂăĐđĨĩŨũƠơƯư" +
            (0x1EA0..0x1EF9).map { it.toChar() }.joinToString("")
        val report = StringBuilder()
        for (name in listOf("font_normal", "font_bold")) {
            val file = File("src/main/res/font/$name.ttf")
            assertTrue("Missing font $name", file.isFile)
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
