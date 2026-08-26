package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Perft — vàng ròng để kiểm chứng bộ sinh nước đi.
 *
 * Các con số dưới đây đã được cộng đồng chơi cờ máy xác nhận từ lâu, nên lệch dù một
 * node là chắc chắn engine sai. Mỗi thế cờ nhắm vào một nhóm lỗi khác nhau, nên
 * test nào đỏ sẽ khoanh vùng luôn được chức năng hỏng.
 */
class PerftTest {

    @Test
    fun startPositionPerft() {
        val board = Board.startPosition()
        assertEquals(20L, Perft.perft(board, 1))
        assertEquals(400L, Perft.perft(board, 2))
        assertEquals(8_902L, Perft.perft(board, 3))
        assertEquals(197_281L, Perft.perft(board, 4))
        assertEquals(4_865_609L, Perft.perft(board, 5))
    }

    /** Kiwipete: dày đặc nhập thành, bắt tốt qua đường và quân bị ghìm. */
    @Test
    fun kiwipetePerft() {
        val board = Fen.parse(Fen.KIWIPETE)
        assertEquals(48L, Perft.perft(board, 1))
        assertEquals(2_039L, Perft.perft(board, 2))
        assertEquals(97_862L, Perft.perft(board, 3))
        assertEquals(4_085_603L, Perft.perft(board, 4))
    }

    /** Thế cờ 3: tốt và xe, bắt lỗi bắt tốt qua đường làm hở chiếu ngang. */
    @Test
    fun rookAndPawnEndgamePerft() {
        val board = Fen.parse("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1")
        assertEquals(14L, Perft.perft(board, 1))
        assertEquals(191L, Perft.perft(board, 2))
        assertEquals(2_812L, Perft.perft(board, 3))
        assertEquals(43_238L, Perft.perft(board, 4))
    }

    /** Thế cờ 4: nhiều khả năng phong cấp, kể cả phong cấp kèm ăn quân. */
    @Test
    fun promotionHeavyPerft() {
        val board = Fen.parse("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1")
        assertEquals(6L, Perft.perft(board, 1))
        assertEquals(264L, Perft.perft(board, 2))
        assertEquals(9_467L, Perft.perft(board, 3))
    }

    /** Thế cờ 5: bắt lỗi cập nhật quyền nhập thành khi xe bị ăn tại ô gốc. */
    @Test
    fun castlingRightsPerft() {
        val board = Fen.parse("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8")
        assertEquals(44L, Perft.perft(board, 1))
        assertEquals(1_486L, Perft.perft(board, 2))
        assertEquals(62_379L, Perft.perft(board, 3))
    }

    /** divide phải cộng lại đúng bằng perft, nếu không thì công cụ gỡ lỗi cũng sai. */
    @Test
    fun divideSumsToPerft() {
        val board = Board.startPosition()
        val divided = Perft.divide(board, 3)
        assertEquals(20, divided.size)
        assertEquals(8_902L, divided.values.sum())
    }
}
