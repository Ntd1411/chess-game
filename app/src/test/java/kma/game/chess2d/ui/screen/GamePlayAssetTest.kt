package kma.game.chess2d.ui.screen

import java.io.File
import javax.imageio.ImageIO
import kma.game.chess2d.R
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.art.ArtAssets
import kma.game.chess2d.ui.art.BattleButton
import kma.game.chess2d.ui.board.pieceImageRes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GamePlayAssetTest {
    private val sizes = mapOf(
        "game_play_background" to (941 to 1672),
        "game_play_frame_player_top" to (1536 to 1024),
        "game_play_frame_player_bottom" to (1536 to 1024),
        "game_play_button_exit" to (1275 to 1233),
        "game_play_button_replay" to (1277 to 1231),
        "game_play_button_resign" to (1274 to 1234),
        "game_play_button_settings" to (1277 to 1231),
        "game_play_button_draw" to (1275 to 1234),
        "game_play_button_continue" to (1254 to 1254),
        "game_play_button_undo" to (1254 to 1254),
        "game_play_button_level" to (1254 to 1254),
        "game_play_button_menu" to (1254 to 1254),
        "setting_line" to (1536 to 1024),
        "chess_piece_black_bishop" to (683 to 1024),
        "chess_piece_black_king" to (683 to 1024),
        "chess_piece_black_knight" to (1024 to 1024),
        "chess_piece_black_pawn" to (1024 to 1024),
        "chess_piece_black_queen" to (1024 to 1024),
        "chess_piece_black_rook" to (1024 to 1024),
        "chess_piece_white_bishop" to (778 to 1024),
        "chess_piece_white_king" to (655 to 1024),
        "chess_piece_white_knight" to (986 to 1024),
        "chess_piece_white_pawn" to (1024 to 1024),
        "chess_piece_white_queen" to (821 to 1024),
        "chess_piece_white_rook" to (1024 to 1024),
    )

    @Test
    fun allWebpAssetsDecodeWithOriginalCanvasAndAlpha() {
        val dir = File("src/main/res/drawable")
        assertEquals(25, sizes.size)
        assertFalse("Android drawable resources must not have nested asset folders", File(dir, "game_play").exists())
        for ((name, size) in sizes) {
            val file = File(dir, "$name.webp")
            assertTrue("Missing gameplay WebP: $name", file.isFile)
            assertFalse("Duplicate PNG resource: $name", File(dir, "$name.png").exists())
            val image = requireNotNull(ImageIO.read(file)) { "Unable to decode $name" }
            assertEquals("Width of $name", size.first, image.width)
            assertEquals("Height of $name", size.second, image.height)
            if (name != "game_play_background") {
                assertTrue("Alpha channel of $name", image.colorModel.hasAlpha())
            }
        }
    }

    @Test
    fun allPieceImagesAreDistinctAndPreloaded() {
        val types = listOf(Piece.KING, Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT, Piece.PAWN)
        val ids = listOf(true, false).flatMap { white -> types.map { pieceImageRes(Piece.of(it, white)) } }
        assertEquals(12, ids.toSet().size)
        assertTrue(ArtAssets.preload.map { it.res }.containsAll(ids))
        assertTrue(ArtAssets.preload.map { it.res }.containsAll(ArtAssets.gamePlay.map { it.res }))
        assertEquals(11, ArtAssets.gamePlay.size)
        assertFalse(ArtAssets.gamePlay.any { it.res == R.drawable.game_play_frame_player_top ||
            it.res == R.drawable.game_play_frame_player_bottom })
        assertEquals(ArtAssets.mainMenu, ArtAssets.preload.takeLast(ArtAssets.mainMenu.size))
    }

    @Test
    fun gameplayButtonsMatchTheirExistingActions() {
        assertEquals(R.drawable.game_play_button_continue, BattleButton.CONTINUE.normal)
        assertEquals(R.drawable.game_play_button_continue, iconFor(kma.game.chess2d.game.PauseItem.RESUME))
        assertEquals(R.drawable.game_play_button_undo, BattleButton.UNDO.normal)
        assertEquals(R.drawable.game_play_button_level, BattleButton.LEVEL.normal)
        assertEquals(R.drawable.game_play_button_menu, BattleButton.MENU.normal)
        assertEquals(R.drawable.game_play_button_replay, BattleButton.RESTART.normal)
        assertEquals(R.drawable.game_play_button_replay, BattleButton.REMATCH.normal)
        assertEquals(R.drawable.game_play_button_resign, BattleButton.RESIGN.normal)
        assertEquals(R.drawable.game_play_button_draw, BattleButton.DRAW.normal)
        assertEquals(R.drawable.game_play_button_settings, BattleButton.SETTINGS.normal)
        assertEquals(R.drawable.game_play_button_exit, BattleButton.LEAVE.normal)
    }

    @Test
    fun framesBoardActionsAndDetailsHaveSeparateVerticalBands() {
        assertTrue(GamePlayLayout.MOVES_TOP >= 0)
        assertTrue(GamePlayLayout.MOVES_TOP + GamePlayLayout.MOVES_HEIGHT < GamePlayLayout.OPPONENT_TOP)
        assertTrue(GamePlayLayout.OPPONENT_TOP + GamePlayLayout.FRAME_HEIGHT < GamePlayLayout.HEADLINE_TOP)
        assertTrue(GamePlayLayout.HEADLINE_TOP + GamePlayLayout.HEADLINE_HEIGHT < GamePlayLayout.BOARD_TOP)
        assertTrue(GamePlayLayout.BOARD_TOP + 960 < GamePlayLayout.PLAYER_TOP)
        assertTrue(GamePlayLayout.PLAYER_TOP + GamePlayLayout.FRAME_HEIGHT < GamePlayLayout.ACTIONS_TOP)
        assertTrue(GamePlayLayout.ACTIONS_TOP + 110 + 4 + 56 < GamePlayLayout.DETAILS_TOP)
        assertTrue(GamePlayLayout.DETAILS_TOP + GamePlayLayout.DETAILS_HEIGHT <= 1920)
    }
}
