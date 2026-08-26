package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Các luật đặc biệt. Perft đã bắt được số lượng, nhưng khi nó đỏ thì khó biết là sai
 * ở đâu. Những test nhỏ dưới đây chỉ thẳng vào từng luật một.
 */
class SpecialMovesTest {

    private fun movesUci(fen: String): Set<String> =
        MoveGenerator.legalMoves(Fen.parse(fen)).map { it.toUci() }.toSet()

    @Test
    fun bothSidesCanCastleBothWays() {
        val white = movesUci("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertTrue(white.contains("e1g1"))
        assertTrue(white.contains("e1c1"))

        val black = movesUci("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1")
        assertTrue(black.contains("e8g8"))
        assertTrue(black.contains("e8c8"))
    }

    /** Vua không được đi qua ô bị tấn công: tượng g2 kiểm soát f1 nên mất nhập thành cánh vua. */
    @Test
    fun cannotCastleThroughAttackedSquare() {
        val moves = movesUci("r3k2r/8/8/8/8/8/6b1/R3K2R w KQkq - 0 1")
        assertFalse(moves.contains("e1g1"))
        assertTrue(moves.contains("e1c1"))
    }

    /** Ô b1 được phép bị tấn công vì vua không đi qua đó, chỉ có xe đi qua. */
    @Test
    fun canCastleQueenSideWhenOnlyRookPathIsAttacked() {
        val moves = movesUci("1r2k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        assertTrue(moves.contains("e1c1"))
    }

    @Test
    fun cannotCastleWhileInCheck() {
        val moves = movesUci("r3k2r/8/8/8/8/8/4r3/R3K2R w KQkq - 0 1")
        assertFalse(moves.contains("e1g1"))
        assertFalse(moves.contains("e1c1"))
    }

    @Test
    fun castlingMovesRookToTheOtherSideOfTheKing() {
        val board = Fen.parse("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val castle = MoveGenerator.legalMoves(board).first { it.toUci() == "e1g1" }
        board.makeMove(castle)
        assertEquals(Piece.of(Piece.KING, white = true), board.pieceAt(Squares.G1))
        assertEquals(Piece.of(Piece.ROOK, white = true), board.pieceAt(5))
        assertEquals(Piece.NONE, board.pieceAt(Squares.H1))
        // Nhập thành rồi là mất sạch quyền nhập thành của bên mình.
        assertEquals(0, board.castlingRights and (Board.WHITE_KING_SIDE or Board.WHITE_QUEEN_SIDE))
    }

    @Test
    fun enPassantCaptureIsAvailableAndRemovesTheRightPawn() {
        val fen = "rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3"
        assertTrue(movesUci(fen).contains("e5f6"))

        val board = Fen.parse(fen)
        val enPassant = MoveGenerator.legalMoves(board).first { it.toUci() == "e5f6" }
        board.makeMove(enPassant)
        assertEquals(Piece.of(Piece.PAWN, white = true), board.pieceAt(Squares.fromName("f6")))
        // Quân bị ăn nằm ở f5, không phải ô đích f6.
        assertEquals(Piece.NONE, board.pieceAt(Squares.fromName("f5")))
        assertEquals(Squares.NONE, board.epSquare)
    }

    @Test
    fun doublePawnPushSetsEnPassantSquare() {
        val board = Board.startPosition()
        val push = MoveGenerator.legalMoves(board).first { it.toUci() == "e2e4" }
        board.makeMove(push)
        assertEquals(Squares.fromName("e3"), board.epSquare)
    }

    @Test
    fun promotionOffersAllFourPieces() {
        val moves = movesUci("8/P6k/8/8/8/8/7K/8 w - - 0 1")
        assertTrue(moves.containsAll(listOf("a7a8q", "a7a8r", "a7a8b", "a7a8n")))
    }

    @Test
    fun promotionPlacesTheChosenPiece() {
        val board = Fen.parse("8/P6k/8/8/8/8/7K/8 w - - 0 1")
        val promoteToKnight = MoveGenerator.legalMoves(board).first { it.toUci() == "a7a8n" }
        board.makeMove(promoteToKnight)
        assertEquals(Piece.of(Piece.KNIGHT, white = true), board.pieceAt(Squares.A8))
    }

    /** Đòn bí gà: 1.f3 e5 2.g4 Qh4#. */
    @Test
    fun detectsCheckmate() {
        val board = Fen.parse("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3")
        assertTrue(board.isInCheck())
        assertTrue(MoveGenerator.legalMoves(board).isEmpty())
        assertEquals(GameStatus.CHECKMATE, Rules.status(board))
    }

    @Test
    fun detectsStalemate() {
        val board = Fen.parse("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1")
        assertFalse(board.isInCheck())
        assertTrue(MoveGenerator.legalMoves(board).isEmpty())
        assertEquals(GameStatus.STALEMATE, Rules.status(board))
    }

    @Test
    fun detectsCheckWithoutMate() {
        val board = Fen.parse("r3k2r/8/8/8/8/8/4r3/R3K2R w KQkq - 0 1")
        assertEquals(GameStatus.CHECK, Rules.status(board))
    }

    @Test
    fun detectsFiftyMoveDraw() {
        val board = Fen.parse(Fen.KIWIPETE)
        board.halfmoveClock = Rules.FIFTY_MOVE_LIMIT
        assertEquals(GameStatus.DRAW_FIFTY_MOVES, Rules.status(board))
    }

    /** Đồng hồ 50 nước phải reset khi đi tốt hoặc ăn quân, và tăng trong mọi trường hợp khác. */
    @Test
    fun halfmoveClockFollowsFiftyMoveRule() {
        val board = Fen.parse("4k3/8/8/8/8/8/4P3/4K1N1 w - - 10 20")
        val knightMove = MoveGenerator.legalMoves(board).first { it.toUci() == "g1f3" }
        board.makeMove(knightMove)
        assertEquals(11, board.halfmoveClock)
        board.unmakeMove()

        val pawnMove = MoveGenerator.legalMoves(board).first { it.toUci() == "e2e4" }
        board.makeMove(pawnMove)
        assertEquals(0, board.halfmoveClock)
    }

    /** Quân bị ghìm trước vua thì không được rời tia ghìm. */
    @Test
    fun pinnedPieceCannotLeaveTheLine() {
        val moves = movesUci("4k3/8/8/8/8/8/4R3/4K3 b - - 0 1")
        assertEquals(setOf("e8d8", "e8f8", "e8d7", "e8f7"), moves)
    }
}
