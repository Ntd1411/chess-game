package kma.game.chess2d.ui.screen

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.board.orderedPromotionMoves
import org.junit.Assert.*
import org.junit.Test

class PromotionPresentationTest {
    @Test fun choicesPreserveLegalMovesInQueenRookBishopKnightOrder() {
        val board = Engine.fromFen("k7/4P3/8/8/8/8/8/7K w - - 0 1")
        val options = Engine.legalMoves(board).filter { it.isPromotion }
        val ordered = orderedPromotionMoves(options.reversed())
        assertEquals(listOf(Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT), ordered.map { it.promotionType })
        assertEquals(options.toSet(), ordered.toSet())
        assertEquals(emptyList<Any>(), orderedPromotionMoves(emptyList()))
        assertEquals(1, orderedPromotionMoves(options.take(1)).size)
        assertEquals(emptyList<Any>(), orderedPromotionMoves(Engine.legalMoves(board).filter { !it.isPromotion }))
    }
}
