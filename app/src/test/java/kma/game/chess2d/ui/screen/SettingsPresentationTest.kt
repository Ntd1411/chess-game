package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsPresentationTest {
    @Test fun everyBoardPaletteRetainsItsLocalizedLabel() {
        val labels = mapOf(BoardPalette.GOTHIC to R.string.palette_gothic, BoardPalette.GREEN to R.string.palette_green,
            BoardPalette.WOOD to R.string.palette_wood, BoardPalette.OCEAN to R.string.palette_ocean, BoardPalette.SLATE to R.string.palette_slate)
        assertEquals(BoardPalette.entries.toSet(), labels.keys)
        labels.forEach { (palette, label) -> assertEquals(label, paletteLabel(palette)) }
    }
    @Test fun everyPieceThemeRetainsItsLocalizedLabel() {
        val labels = mapOf(PieceTheme.SOLID to R.string.piece_theme_solid, PieceTheme.OUTLINE to R.string.piece_theme_outline,
            PieceTheme.LETTER to R.string.piece_theme_letter, PieceTheme.IMAGE to R.string.piece_theme_image)
        assertEquals(PieceTheme.entries.toSet(), labels.keys)
        labels.forEach { (theme, label) -> assertEquals(label, pieceThemeLabel(theme)) }
    }
}
