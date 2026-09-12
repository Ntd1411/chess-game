package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm tra thống kê quân bị bắt và chênh lệch vật chất (mục 7.2).
 *
 * Hai ca dễ tính sai được tách riêng: bắt tốt qua đường (quân bị bắt không ở ô đến)
 * và phong cấp có ăn quân (vừa ăn vừa đổi loại quân trong một nước).
 */
class CapturesTest {

    /** Đi lần lượt các nước theo ký hiệu UCI và trả về danh sách [Move] tương ứng. */
    private fun movesOf(startFen: String, vararg ucis: String): List<Move> {
        val board = Engine.fromFen(startFen)
        return ucis.map { uci ->
            val move = Engine.legalMoves(board).first { it.toUci() == uci }
            board.makeMove(move)
            move
        }
    }

    @Test
    fun `chua an quan thi khong ai hon ai`() {
        val moves = movesOf(Fen.START, "e2e4", "e7e5", "g1f3")
        val tally = CaptureTally.of(Fen.START, moves)
        assertTrue(tally.takenFromWhite.isEmpty())
        assertTrue(tally.takenFromBlack.isEmpty())
        assertEquals(0, tally.materialBalance)
    }

    @Test
    fun `an mot tot thi hon mot diem`() {
        val moves = movesOf(Fen.START, "e2e4", "d7d5", "e4d5")
        val tally = CaptureTally.of(Fen.START, moves)
        assertEquals(listOf(Piece.PAWN), tally.takenFromBlack.map { Piece.typeOf(it) })
        assertTrue(tally.takenFromWhite.isEmpty())
        assertEquals(1, tally.materialBalance)
    }

    @Test
    fun `bat tot qua duong van tinh dung quan bi bat`() {
        val fen = "4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1"
        val moves = movesOf(fen, "e5d6")
        val tally = CaptureTally.of(fen, moves)
        assertEquals(listOf(Piece.PAWN), tally.takenFromBlack.map { Piece.typeOf(it) })
        assertEquals(1, tally.materialBalance)
    }

    @Test
    fun `phong cap co an quan van tinh quan bi an`() {
        val fen = "1n2k3/P7/8/8/8/8/8/4K3 w - - 0 1"
        val moves = movesOf(fen, "a7b8q")
        val tally = CaptureTally.of(fen, moves)
        assertEquals(listOf(Piece.KNIGHT), tally.takenFromBlack.map { Piece.typeOf(it) })
        assertEquals(3, tally.materialBalance)
    }

    @Test
    fun `hai ben an qua an lai thi bu tru cho nhau`() {
        // 1. e4 d5 2. exd5 Qxd5: Trắng ăn một tốt, Đen ăn lại một tốt.
        val moves = movesOf(Fen.START, "e2e4", "d7d5", "e4d5", "d8d5")
        val tally = CaptureTally.of(Fen.START, moves)
        assertEquals(1, tally.takenFromWhite.size)
        assertEquals(1, tally.takenFromBlack.size)
        assertEquals(0, tally.materialBalance)
    }

    @Test
    fun `tinh duoc thong ke o mot the giua van`() {
        val moves = movesOf(Fen.START, "e2e4", "d7d5", "e4d5", "d8d5")
        // Chỉ tính ba nước đầu: lúc đó Trắng vẫn đang hơn một tốt.
        val tally = CaptureTally.of(Fen.START, moves, plies = 3)
        assertEquals(1, tally.materialBalance)
        assertTrue(tally.takenFromWhite.isEmpty())
    }
}
