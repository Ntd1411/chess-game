package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Đọc rồi ghi lại FEN phải ra đúng chuỗi ban đầu, kể cả các trường phụ. */
class FenTest {

    @Test
    fun roundTripKeepsFenIntact() {
        val fens = listOf(
            Fen.START,
            Fen.KIWIPETE,
            "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1",
            "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1",
            "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3",
            "7k/5Q2/6K1/8/8/8/8/8 b - - 0 1",
        )
        for (fen in fens) {
            assertEquals(fen, Fen.parse(fen).toFen())
        }
    }

    @Test
    fun parsesSideToMoveAndClocks() {
        val board = Fen.parse("7k/5Q2/6K1/8/8/8/8/8 b - - 7 42")
        assertFalse(board.whiteToMove)
        assertEquals(0, board.castlingRights)
        assertEquals(Squares.NONE, board.epSquare)
        assertEquals(7, board.halfmoveClock)
        assertEquals(42, board.fullmoveNumber)
    }

    @Test
    fun parsesPiecePlacementIntoRightSquares() {
        val board = Board.startPosition()
        assertEquals(Piece.of(Piece.ROOK, white = true), board.pieceAt(Squares.A1))
        assertEquals(Piece.of(Piece.KING, white = true), board.pieceAt(Squares.E1))
        assertEquals(Piece.of(Piece.ROOK, white = false), board.pieceAt(Squares.H8))
        assertEquals(Piece.NONE, board.pieceAt(Squares.of(0, 4)))
        assertEquals(Squares.E1, board.kingSquare(white = true))
        assertEquals(Squares.E8, board.kingSquare(white = false))
        assertEquals(Board.ALL_CASTLING, board.castlingRights)
    }

    @Test
    fun parsesEnPassantSquare() {
        val board = Fen.parse("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3")
        assertEquals(Squares.fromName("f6"), board.epSquare)
        assertTrue(board.whiteToMove)
    }

    @Test
    fun acceptsFenWithoutClockFields() {
        val board = Fen.parse("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -")
        assertEquals(0, board.halfmoveClock)
        assertEquals(1, board.fullmoveNumber)
    }
}
