package kma.game.chess2d.shop

import kma.game.chess2d.ui.board.BoardPalette
import kma.game.chess2d.ui.board.PieceTheme

data class ShopItem(val id: String, val goldPrice: Int, val palette: BoardPalette? = null, val pieces: PieceTheme? = null)

object ShopCatalog {
    fun boardId(palette: BoardPalette) = "board_${palette.name}"
    fun pieceId(theme: PieceTheme) = "pieces_${theme.name}"
    val items: List<ShopItem> = BoardPalette.entries.map { palette ->
        ShopItem(boardId(palette), when (palette) {
            BoardPalette.GOTHIC -> 0
            BoardPalette.GREEN -> 200
            BoardPalette.WOOD -> 300
            BoardPalette.OCEAN -> 400
            BoardPalette.SLATE -> 500
        }, palette = palette)
    } + PieceTheme.entries.map { theme ->
        ShopItem(pieceId(theme), when (theme) {
            PieceTheme.IMAGE -> 0
            PieceTheme.SOLID -> 150
            PieceTheme.OUTLINE -> 250
            PieceTheme.LETTER -> 100
        }, pieces = theme)
    }
    val freeIds: Set<String> = items.filter { it.goldPrice == 0 }.map { it.id }.toSet()
    fun item(id: String) = items.firstOrNull { it.id == id }
}

data class ShopState(val owned: Set<String> = ShopCatalog.freeIds, val spentGold: Int = 0, val readable: Boolean = true) {
    fun availableGold(earnedGold: Int) = if (!readable) 0 else (earnedGold.toLong() - spentGold).coerceIn(0, Int.MAX_VALUE.toLong()).toInt()
}

enum class PurchaseResult { BOUGHT, ALREADY_OWNED, INSUFFICIENT_GOLD }
