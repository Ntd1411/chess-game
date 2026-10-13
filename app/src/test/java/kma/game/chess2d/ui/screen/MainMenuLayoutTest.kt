package kma.game.chess2d.ui.screen

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtAssets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainMenuLayoutTest {
    private val sizes = listOf(
        240f to 240f, 320f to 480f, 360f to 640f, 390f to 844f, 412f to 915f,
        600f to 960f, 800f to 1280f, 640f to 360f, 844f to 390f, 1024f to 768f,
    )

    @Test
    fun responsiveControlsStayInsideSafeViewportWithoutOverlap() {
        val previews = mutableListOf<String>()
        for ((width, height) in sizes) {
            val layout = MainMenuLayout.calculate(width, height)
            val controls = listOf(layout.profile, layout.gold, layout.crystals, layout.rail) + layout.modes +
                listOf(layout.hero).filter { it.h > 0f }
            assertEquals(5, layout.modes.size)
            controls.forEach { rect ->
                assertTrue("$width x $height: positive box $rect", rect.w > 0 && rect.h > 0)
                assertTrue("$width x $height: left/top $rect", rect.x >= 0 && rect.y >= 0)
                assertTrue("$width x $height: right/bottom $rect", rect.right <= width + 0.01f && rect.bottom <= height + 0.01f)
            }
            for (i in controls.indices) for (j in i + 1 until controls.size) {
                assertFalse("$width x $height: controls $i/$j overlap", controls[i].overlaps(controls[j]))
            }
            layout.modes.forEach { assertTrue("Mode touch target", it.w >= 48f && it.h >= 48f) }
            assertTrue("Side touch target and scroll access", layout.rail.w >= 48f && layout.sideItemHeight >= 48f)
            assertEquals(layout.margin, layout.profile.y, 0.01f)
            assertEquals(width - layout.margin, layout.rail.right, 0.01f)
            assertEquals(height - layout.margin, layout.modes.maxOf { it.bottom }, 0.01f)
            fun MainMenuLayout.Rect.json() = "[$x,$y,$w,$h]"
            previews.add("{\"width\":$width,\"height\":$height,\"profile\":${layout.profile.json()},\"gold\":${layout.gold.json()},\"crystals\":${layout.crystals.json()},\"rail\":${layout.rail.json()},\"sideItemHeight\":${layout.sideItemHeight},\"modes\":[${layout.modes.joinToString(",") { it.json() }}]}")
        }
        File("build/menu_layout_preview.json").writeText("[${previews.joinToString(",")}]")
        File("build/menu_ui_check_timestamp.txt").writeText(
            java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
        )
    }

    @Test
    fun portraitKeepsTwoLargeCardsAboveThreeSmallerCards() {
        val layout = MainMenuLayout.calculate(390f, 844f)
        assertFalse(layout.landscape)
        assertEquals(layout.modes[0].y, layout.modes[1].y, 0f)
        assertTrue(layout.modes[0].w > layout.modes[2].w)
        assertTrue(layout.modes[0].bottom < layout.modes[2].y)
        assertEquals(layout.modes[2].y, layout.modes[4].y, 0f)
    }

    @Test
    fun landscapeKeepsModesInOneRowClearOfRightRail() {
        val layout = MainMenuLayout.calculate(844f, 390f)
        assertTrue(layout.landscape)
        layout.modes.forEach {
            assertEquals(layout.modes.first().y, it.y, 0f)
            assertTrue(it.right < layout.rail.x)
        }
    }

    @Test
    fun tallScreensAnchorControlsToEdgesInsteadOfCenteredDesignCanvas() {
        val short = MainMenuLayout.calculate(390f, 700f)
        val tall = MainMenuLayout.calculate(390f, 1000f)
        assertEquals(short.profile.y, tall.profile.y, 0f)
        assertEquals(short.rail.right, tall.rail.right, 0f)
        assertEquals(300f, tall.modes.last().bottom - short.modes.last().bottom, 0.01f)
    }

    @Test
    fun allNewAssetsAreLoadedBySplashAndAvailableAtResourceRoot() {
        val ids = listOf(
            R.drawable.main_menu_background, R.drawable.main_menu_header_frame,
            R.drawable.main_menu_avatar, R.drawable.main_menu_exp_fill,
            R.drawable.main_menu_mode_tower, R.drawable.main_menu_mode_ai,
            R.drawable.main_menu_mode_local, R.drawable.main_menu_mode_lan, R.drawable.main_menu_mode_spectate,
            R.drawable.main_menu_journal, R.drawable.main_menu_inventory,
            R.drawable.main_menu_achievements, R.drawable.main_menu_history, R.drawable.main_menu_settings,
        )
        assertTrue(ArtAssets.preload.map { it.res }.containsAll(ids))
        assertEquals(ArtAssets.mainMenu, ArtAssets.preload.takeLast(ArtAssets.mainMenu.size))
        val names = listOf("background", "header_frame", "avatar", "exp_fill", "mode_tower", "mode_ai", "mode_local", "mode_lan", "mode_spectate", "journal", "inventory", "achievements", "history", "settings")
        names.forEach { assertTrue(File("src/main/res/drawable/main_menu_$it.webp").isFile) }
    }
}
