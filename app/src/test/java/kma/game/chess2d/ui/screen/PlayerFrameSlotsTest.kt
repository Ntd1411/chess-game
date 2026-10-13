package kma.game.chess2d.ui.screen

import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerFrameSlotsTest {
    @Test
    fun drawnPlayerCardSlotsFitWithoutOverlap() {
        val slots = GamePlayLayout.player
        val rects = listOf(slots.avatar, slots.name, slots.subtitle, slots.clock, slots.captures)
        assertEquals(GamePlayLayout.FRAME_HEIGHT.toFloat(), slots.height, 0f)
        assertEquals(slots.avatar.w, slots.avatar.h, 0f)
        rects.forEach {
            assertTrue(it.x >= 0 && it.y >= 0)
            assertTrue(it.right <= GamePlayLayout.FRAME_WIDTH && it.bottom <= slots.height)
        }
        for (i in rects.indices) for (j in i + 1 until rects.size) {
            assertFalse("Slots $i/$j overlap", rects[i].overlaps(rects[j]))
        }
        assertTrue(slots.captures.y > slots.subtitle.bottom)
        assertTrue(slots.clock.x > slots.name.right)
        assertTrue(slots.clock.w >= 200 && slots.clock.h >= 100)
    }

    @Test
    fun playerAndTimerRenderingNoLongerDependsOnFrameArtwork() {
        val source = File("src/main/java/kma/game/chess2d/ui/screen/MatchScaffold.kt").readText()
        assertFalse(source.contains("R.drawable.game_play_frame_player_top"))
        assertFalse(source.contains("R.drawable.game_play_frame_player_bottom"))
    }

    @Test
    fun squareBoardFitsTheArtworkOpeningWithoutCoveringTheBorder() {
        val image = requireNotNull(ImageIO.read(File("src/main/res/drawable/chess_board_frame.webp")))
        assertEquals(1024, image.width)
        assertEquals(1024, image.height)
        val source = GamePlayLayout.boardSource
        assertEquals(source.w, source.h, 0f)
        for (x in 0..8) for (y in 0..8) {
            val px = (source.x + x * (source.w - 1) / 8).toInt()
            val py = (source.y + y * (source.h - 1) / 8).toInt()
            assertTrue("Board covers frame at $px,$py", (image.getRGB(px, py) ushr 24) <= 24)
        }
        val rendered = GamePlayLayout.board
        assertEquals(rendered.w, rendered.h, 0f)
        assertTrue(rendered.x >= 60 && rendered.right <= 1020)
        assertTrue(rendered.y >= GamePlayLayout.BOARD_TOP)
        assertTrue(rendered.bottom <= GamePlayLayout.BOARD_TOP + GamePlayLayout.BOARD_SIZE)
    }
}
