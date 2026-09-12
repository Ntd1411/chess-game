package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Kiểm tra bộ sinh SAN (mục 7.2).
 *
 * Trọng tâm là những chỗ SAN dễ sai: phân biệt hai nước trùng nghĩa, dấu chiếu và
 * chiếu hết, nhập thành, phong cấp. Thêm một bài canh việc bàn cờ phải còn nguyên
 * sau khi sinh, vì bên trong có đi thử để biết có `+` hay `#`.
 */
class SanTest {

    /** Tìm nước hợp lệ theo ký hiệu UCI cho gọn phần dựng dự liệu của từng bài. */
    private fun sanOf(fen: String, uci: String): String {
        val board = Engine.fromFen(fen)
        val move = Engine.legalMoves(board).first { it.toUci() == uci }
        return San.of(board, move)
    }

    @Test
    fun `quan tot khong co chu dau`() {
        assertEquals("e4", sanOf(Fen.START, "e2e4"))
    }

    @Test
    fun `quan ma co chu dau`() {
        assertEquals("Nf3", sanOf(Fen.START, "g1f3"))
    }

    @Test
    fun `tot an quan thi viet cot xuat phat`() {
        val fen = "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w kq d6 0 2"
        assertEquals("exd5", sanOf(fen, "e4d5"))
    }

    @Test
    fun `hai xe cung hang thi phan biet bang cot`() {
        // Vua trắng để ở e2 chứ không ở e1: đứng ở e1 thì nó chặn đường xe h1 sang d1,
        // thành ra chỉ còn một xe tới được và không có gì phải phân biệt nữa.
        val fen = "4k3/8/8/8/8/8/4K3/R6R w - - 0 1"
        assertEquals("Rad1", sanOf(fen, "a1d1"))
        assertEquals("Rhd1", sanOf(fen, "h1d1"))
    }

    @Test
    fun `hai xe cung cot thi phan biet bang hang`() {
        // Vua đen đứng ở e5, không ở hàng 8: đứng cùng hàng với xe a8 thì thế cờ đã
        // là đang chiếu sẵn, mà đó là thế không hợp lệ khi đến lượt Trắng đi.
        val fen = "R7/8/8/4k3/8/8/8/R6K w - - 0 1"
        assertEquals("R8a4", sanOf(fen, "a8a4"))
        assertEquals("R1a4", sanOf(fen, "a1a4"))
    }

    @Test
    fun `nhap thanh co ky hieu rieng`() {
        val fen = "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1"
        assertEquals("O-O", sanOf(fen, "e1g1"))
        assertEquals("O-O-O", sanOf(fen, "e1c1"))
    }

    @Test
    fun `phong cap viet kem loai quan`() {
        val fen = "8/P7/8/4k3/8/8/8/K7 w - - 0 1"
        assertEquals("a8=Q", sanOf(fen, "a7a8q"))
        assertEquals("a8=N", sanOf(fen, "a7a8n"))
    }

    @Test
    fun `the chieu duoc danh dau bang dau cong`() {
        val fen = "4k3/8/8/8/8/8/8/4K2R w K - 0 1"
        assertEquals("Rh8+", sanOf(fen, "h1h8"))
    }

    @Test
    fun `chieu het duoc danh dau bang dau thang`() {
        val fen = "6k1/5ppp/8/8/8/8/8/R6K w - - 0 1"
        assertEquals("Ra8#", sanOf(fen, "a1a8"))
    }

    @Test
    fun `sinh san khong lam thay doi ban co`() {
        val board = Engine.fromFen(Fen.START)
        val before = board.toFen()
        for (move in Engine.legalMoves(board)) San.of(board, move)
        assertEquals(before, board.toFen())
    }
}
