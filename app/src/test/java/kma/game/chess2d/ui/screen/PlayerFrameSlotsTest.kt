package kma.game.chess2d.ui.screen

import java.io.File
import javax.imageio.ImageIO
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerFrameSlotsTest {
    @Test
    fun mappedSlotsStayInsideEachFrameAndDoNotOverlap() {
        for (upper in listOf(true, false)) {
            val slots = GamePlayLayout.player(upper)
            val rects = listOf(slots.avatar, slots.name, slots.subtitle, slots.clock, slots.captures)
            assertTrue(slots.height <= GamePlayLayout.FRAME_HEIGHT)
            assertEquals(slots.avatar.w, slots.avatar.h, 0.001f)
            rects.forEach {
                assertTrue(it.x >= 0 && it.y >= 0)
                assertTrue(it.right <= GamePlayLayout.FRAME_WIDTH && it.bottom <= slots.height)
            }
            for (i in rects.indices) for (j in i + 1 until rects.size) {
                assertFalse("Slots $i/$j overlap in upper=$upper", rects[i].overlaps(rects[j]))
            }
            assertTrue("Captured pieces belong below the name", slots.captures.y > slots.subtitle.bottom)
        }
    }

    @Test
    fun avatarsAreCenteredOnTheTransparentCircularOpenings() {
        for (upper in listOf(true, false)) {
            val name = if (upper) "top" else "bottom"
            val image = requireNotNull(ImageIO.read(File("src/main/res/drawable/game_play_frame_player_$name.webp")))
            val avatar = GamePlayLayout.player(upper).avatarSource
            val x = (avatar.x + avatar.w / 2).toInt()
            val y = (avatar.y + avatar.h / 2).toInt()
            assertEquals("Avatar opening must remain transparent", 0, image.getRGB(x, y) ushr 24)
        }
    }

    @Test
    fun squareBoardFitsTheNewArtworkOpeningWithoutCoveringTheBorder() {
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
