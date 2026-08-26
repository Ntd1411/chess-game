package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * make rồi unmake phải trả bàn cờ về y nguyên trạng thái cũ.
 *
 * Vì cả search và nút Undo đều dụa trên một bàn cờ duy nhất được sửa tại chỗ, một
 * lỗi hoàn nguyên sẽ làm sai lệch âm thầm ở tận đâu đó vài chục nước sau. So sánh
 * bằng FEN vì FEN gói đủ cả quyền nhập thành, ô bắt tốt qua đường và hai đồng hồ.
 */
class MakeUnmakeTest {

    private val positions = listOf(
        Fen.START,
        Fen.KIWIPETE,
        "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
        "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
        "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8",
        "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3",
    )

    @Test
    fun unmakeRestoresPositionExactly() {
        for (fen in positions) {
            val board = Fen.parse(fen)
            verifyRecursively(board, depth = 2)
            assertEquals(fen, board.toFen())
        }
    }

    private fun verifyRecursively(board: Board, depth: Int) {
        if (depth == 0) return
        val before = board.toFen()
        for (move in MoveGenerator.legalMoves(board)) {
            board.makeMove(move)
            verifyRecursively(board, depth - 1)
            board.unmakeMove()
            assertEquals("Hoàn nguyên sai sau nước ${move.toUci()}", before, board.toFen())
        }
    }

    @Test
    fun undoStackTracksMoveCount() {
        val board = Board.startPosition()
        assertFalse(board.canUndo())
        assertEquals(0, board.movesPlayed)

        val first = MoveGenerator.legalMoves(board).first { it.toUci() == "e2e4" }
        board.makeMove(first)
        assertTrue(board.canUndo())
        assertEquals(1, board.movesPlayed)

        board.unmakeMove()
        assertFalse(board.canUndo())
        assertEquals(Fen.START, board.toFen())
    }

    /** Hoàn nguyên nhập thành phải trả cả vua và xe về ô gốc. */
    @Test
    fun unmakeRestoresCastling() {
        val fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        for (uci in listOf("e1g1", "e1c1")) {
            val board = Fen.parse(fen)
            val move = MoveGenerator.legalMoves(board).first { it.toUci() == uci }
            board.makeMove(move)
            board.unmakeMove()
            assertEquals(fen, board.toFen())
            assertEquals(Squares.E1, board.kingSquare(white = true))
        }
    }

    /**
     * Hoàn nguyên phong cấp kèm ăn quân: tốt về chỗ cũ, quân bị ăn sống lại.
     *
     * Tốt a7 vừa tiến được a8 vừa ăn được mã b8, nên một thế cờ tối giản này cho đủ
     * bốn nước phong cấp thường và bốn nước phong cấp kèm ăn quân.
     */
    @Test
    fun unmakeRestoresPromotionCapture() {
        val fen = "1n6/P6k/8/8/8/8/8/K7 w - - 0 1"
        val board = Fen.parse(fen)
        val promotionCaptures = MoveGenerator.legalMoves(board).filter { it.isPromotion && it.isCapture }
        assertEquals(4, promotionCaptures.size)
        for (move in promotionCaptures) {
            board.makeMove(move)
            // Mã đen phải biến mất và bị thay bằng quân trắng vừa được phong.
            assertTrue(Piece.isWhite(board.pieceAt(Squares.of(1, 7))))
            board.unmakeMove()
            assertEquals(fen, board.toFen())
        }
    }
}
