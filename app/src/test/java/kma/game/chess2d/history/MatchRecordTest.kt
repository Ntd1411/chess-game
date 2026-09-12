package kma.game.chess2d.history

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Fen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm tra phần không cần Room của lịch sử ván đấu: tách dãy nước đã lưu và dựng lại
 * ván bằng engine.
 *
 * Bản thân DAO cần SQLite của Android nên không kiểm ở đây được; đây cũng là lý do chỉ
 * lưu nước đi: phần dễ sai nhất (dựng lại thế cờ) vẫn kiểm được bằng JVM.
 */
class MatchRecordTest {

    @Test
    fun `tach day nuoc da luu`() {
        val record = MatchRecord(
            playedAtMillis = 0L,
            mode = "TWO_PLAYERS",
            startFen = Fen.START,
            moves = "e2e4 e7e5 g1f3",
            result = MatchResult.DRAW,
        )

        assertEquals(listOf("e2e4", "e7e5", "g1f3"), record.uciMoves)
    }

    @Test
    fun `van khong co nuoc di thi day nuoc rong`() {
        val record = MatchRecord(
            playedAtMillis = 0L,
            mode = "LAN",
            startFen = Fen.START,
            moves = "",
            result = MatchResult.LOSS,
        )

        assertTrue(record.uciMoves.isEmpty())
    }

    @Test
    fun `dung lai duoc van tu fen dau va day nuoc`() {
        val record = MatchRecord(
            playedAtMillis = 0L,
            mode = "VS_COMPUTER",
            difficulty = "HARD",
            startFen = Fen.START,
            moves = "e2e4 e7e5 g1f3 b8c6",
            result = MatchResult.WIN,
        )

        val board = Engine.fromFen(record.startFen)
        for (uci in record.uciMoves) {
            val move = Engine.legalMoves(board).firstOrNull { it.toUci() == uci }
            assertTrue("nước $uci không hợp lệ", move != null)
            board.makeMove(move!!)
        }

        assertEquals(4, board.movesPlayed)
    }
}
