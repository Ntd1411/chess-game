package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hai luật hòa được hoãn từ Phase 1 sang Phase 3, vì cả hai đều phải chờ Zobrist:
 * lặp ba lần thế cần so thế cờ theo khóa, còn thiếu quân thì cần đếm chất.
 */
class DrawRulesTest {

    @Test
    fun repetitionCountStartsAtOne() {
        assertEquals(1, Board.startPosition().repetitionCount())
    }

    @Test
    fun threefoldRepetitionIsDraw() {
        val board = Board.startPosition()
        // Đưa Mã ra rồi trả về hai lần: thế ban đầu xuất hiện lần thứ ba.
        repeat(2) {
            play(board, "g1f3")
            play(board, "g8f6")
            play(board, "f3g1")
            play(board, "f6g8")
        }
        assertEquals(3, board.repetitionCount())
        assertEquals(GameStatus.DRAW_REPETITION, Rules.status(board))
        assertTrue(Rules.isGameOver(board))
    }

    /** Lặp hai lần thì chưa hòa — luật đòi đủ ba lần. */
    @Test
    fun twofoldRepetitionIsNotDrawYet() {
        val board = Board.startPosition()
        play(board, "g1f3")
        play(board, "g8f6")
        play(board, "f3g1")
        play(board, "f6g8")
        assertEquals(2, board.repetitionCount())
        assertEquals(GameStatus.ONGOING, Rules.status(board))
    }

    /** Ăn quân xóa sạch lịch sử lặp: thế trước đó vĩnh viễn không thể trở lại. */
    @Test
    fun captureResetsRepetitionWindow() {
        val board = Fen.parse("4k3/8/8/3q4/8/8/8/3QK3 w - - 0 1")
        play(board, "d1d5")
        assertEquals(1, board.repetitionCount())
    }

    @Test
    fun kingVersusKingIsDraw() {
        assertStatus("4k3/8/8/8/8/8/8/4K3 w - - 0 1", GameStatus.DRAW_INSUFFICIENT_MATERIAL)
    }

    @Test
    fun singleKnightIsDraw() {
        assertStatus("4k3/8/8/8/8/8/8/3NK3 w - - 0 1", GameStatus.DRAW_INSUFFICIENT_MATERIAL)
    }

    @Test
    fun singleBishopIsDraw() {
        assertStatus("4k3/8/8/8/8/8/8/2B1K3 w - - 0 1", GameStatus.DRAW_INSUFFICIENT_MATERIAL)
    }

    /** Hai Tượng cùng màu ô (c1 và f8 đều màu sáng) thì không bên nào thắng được. */
    @Test
    fun bishopsOnSameSquareColorIsDraw() {
        assertStatus("4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1", GameStatus.DRAW_INSUFFICIENT_MATERIAL)
    }

    /** Khác màu ô thì về nguyên tắc vẫn còn thế hết nước, không được tự xử hòa. */
    @Test
    fun bishopsOnOppositeSquareColorIsNotDraw() {
        assertStatus("2b1k3/8/8/8/8/8/8/2B1K3 w - - 0 1", GameStatus.ONGOING)
    }

    @Test
    fun rookIsSufficientMaterial() {
        assertStatus("4k3/8/8/8/8/8/8/R3K3 w - - 0 1", GameStatus.ONGOING)
    }

    /** Hai Mã vẫn hết nước được nếu đối phương phụ họa, nên KHÔNG tính là hòa. */
    @Test
    fun twoKnightsIsNotAutomaticDraw() {
        assertStatus("4k3/8/8/8/8/8/8/2NNK3 w - - 0 1", GameStatus.ONGOING)
    }

    @Test
    fun pawnIsAlwaysSufficientMaterial() {
        assertStatus("4k3/8/8/8/8/8/4P3/4K3 w - - 0 1", GameStatus.ONGOING)
    }

    /** Hết nước phải thắng mọi luật hòa: đứng ở nước 100 mà bị chiếu bí là THUA. */
    @Test
    fun checkmateBeatsFiftyMoveRule() {
        val board = Fen.parse("rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 120 3")
        assertEquals(GameStatus.CHECKMATE, Rules.status(board))
    }

    private fun assertStatus(fen: String, expected: GameStatus) {
        assertEquals(fen, expected, Rules.status(Fen.parse(fen)))
    }

    private fun play(board: Board, uci: String) {
        val move = MoveGenerator.legalMoves(board).first { it.toUci() == uci }
        board.makeMove(move)
    }
}
