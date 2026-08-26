package kma.game.chess2d.engine

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Khóa Zobrist được cập nhật tăng dần bằng XOR trong makeMove — đoạn dễ sai nhất của
 * Phase 3, và sai thì không crash mà chỉ làm TT trả về điểm của thế cờ khác: AI đi
 * những nước vô lý một cách ngẫu nhiên, không cách nào lần ra bằng mắt.
 *
 * Cách bắt duy nhất đáng tin: đối chiếu với Zobrist.compute tính lại từ đầu.
 */
class ZobristTest {

    @Test
    fun incrementalKeyMatchesRecomputeAlongRandomGames() {
        val random = Random(20260826)
        // Kiwipete có đủ nhập thành hai bên, bắt tốt qua đường và phong cấp — đúng bốn
        // chỗ mà cập nhật XOR hay thiếu sót.
        for (fen in listOf(Fen.START, Fen.KIWIPETE, "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1")) {
            repeat(5) {
                val board = Fen.parse(fen)
                var step = 0
                while (step < 60) {
                    val moves = MoveGenerator.legalMoves(board)
                    if (moves.isEmpty()) break
                    board.makeMove(moves[random.nextInt(moves.size)])
                    assertEquals(
                        "khóa tăng dần sai ở thế ${board.toFen()}",
                        Zobrist.compute(board),
                        board.hashKey,
                    )
                    step++
                }
            }
        }
    }

    @Test
    fun unmakeRestoresKeyExactly() {
        for (fen in listOf(Fen.START, Fen.KIWIPETE)) {
            val board = Fen.parse(fen)
            val before = board.hashKey
            for (move in MoveGenerator.legalMoves(board)) {
                board.makeMove(move)
                board.unmakeMove()
                assertEquals("hoàn nguyên ${move.toUci()} không trả lại khóa cũ", before, board.hashKey)
            }
        }
    }

    /** Đến cùng một thế bằng hai thứ tự nước khác nhau phải cho cùng khóa. */
    @Test
    fun samePositionFromDifferentMoveOrderSharesKey() {
        val first = Board.startPosition()
        play(first, "g1f3")
        play(first, "g8f6")
        play(first, "b1c3")
        play(first, "b8c6")

        val second = Board.startPosition()
        play(second, "b1c3")
        play(second, "b8c6")
        play(second, "g1f3")
        play(second, "g8f6")

        assertEquals(first.toFen(), second.toFen())
        assertEquals(first.hashKey, second.hashKey)
    }

    /** Về đúng thế ban đầu thì khóa cũng phải trở về đúng khóa ban đầu. */
    @Test
    fun returningToStartPositionRestoresStartKey() {
        val board = Board.startPosition()
        val startKey = board.hashKey
        play(board, "g1f3")
        play(board, "g8f6")
        play(board, "f3g1")
        play(board, "f6g8")
        assertEquals(startKey, board.hashKey)
    }

    @Test
    fun sideToMoveIsPartOfKey() {
        val whiteToMove = Fen.parse("4k3/8/8/8/8/8/8/4K3 w - - 0 1")
        val blackToMove = Fen.parse("4k3/8/8/8/8/8/8/4K3 b - - 0 1")
        assertNotEquals(whiteToMove.hashKey, blackToMove.hashKey)
    }

    @Test
    fun castlingRightsArePartOfKey() {
        val withRights = Fen.parse("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1")
        val withoutRights = Fen.parse("r3k2r/8/8/8/8/8/8/R3K2R w - - 0 1")
        assertNotEquals(withRights.hashKey, withoutRights.hashKey)
    }

    @Test
    fun enPassantSquareIsPartOfKey() {
        val withEp = Fen.parse("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq f6 0 3")
        val withoutEp = Fen.parse("rnbqkbnr/ppp1p1pp/8/3pPp2/8/8/PPPP1PPP/RNBQKBNR w KQkq - 0 3")
        assertNotEquals(withEp.hashKey, withoutEp.hashKey)
    }

    private fun play(board: Board, uci: String) {
        val move = MoveGenerator.legalMoves(board).first { it.toUci() == uci }
        board.makeMove(move)
    }
}
