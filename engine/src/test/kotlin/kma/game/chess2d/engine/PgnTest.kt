package kma.game.chess2d.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * PGN phải đi được cả hai chiều và phải từ chối mọi thứ không đúng luật.
 *
 * Tên test viết không dấu vì đây là định danh hàm, không phải câu văn tiếng Việt.
 */
class PgnTest {

    @Test
    fun `xuat ra du bay tag bat buoc`() {
        val text = Pgn.export(Pgn.Game(uciMoves = listOf("e2e4", "e7e5")))
        for (tag in Pgn.REQUIRED_TAGS) {
            assertTrue("thiếu tag $tag", text.contains("[$tag "))
        }
    }

    @Test
    fun `movetext dung so nuoc va ky hieu SAN`() {
        val text = Pgn.export(
            Pgn.Game(
                result = "1-0",
                uciMoves = listOf("e2e4", "e7e5", "g1f3"),
            ),
        )
        assertTrue(text, text.contains("1. e4 e5 2. Nf3 1-0"))
    }

    @Test
    fun `xuat roi nhap lai thi ra dung nuoc di cu`() {
        val moves = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6")
        val original = Pgn.Game(
            event = "Giải nội bộ",
            white = "An",
            black = "Bình",
            result = "1/2-1/2",
            uciMoves = moves,
        )

        val restored = Pgn.parse(Pgn.export(original))

        assertEquals(moves, restored.uciMoves)
        assertEquals("Giải nội bộ", restored.event)
        assertEquals("An", restored.white)
        assertEquals("1/2-1/2", restored.result)
    }

    @Test
    fun `van bat dau tu the giua van thi ghi kem FEN`() {
        val fen = "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 4 3"
        val text = Pgn.export(Pgn.Game(startFen = fen, uciMoves = listOf("f1b5")))

        assertTrue(text, text.contains("[SetUp \"1\"]"))
        assertTrue(text, text.contains("[FEN \"$fen\"]"))

        val restored = Pgn.parse(text)
        assertEquals(fen, restored.startFen)
        assertEquals(listOf("f1b5"), restored.uciMoves)
    }

    @Test
    fun `den di truoc thi so nuoc co ba dau cham`() {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
        val text = Pgn.export(Pgn.Game(startFen = fen, uciMoves = listOf("e7e5")))
        assertTrue(text, text.contains("1... e5"))
    }

    @Test
    fun `bo qua chu thich bien va NAG`() {
        val text = """
            [Event "Test"]
            [Site "Local"]
            [Date "2026.09.12"]
            [Round "1"]
            [White "An"]
            [Black "Bình"]
            [Result "*"]

            1. e4 {nước mở đầu phổ biến} e5 ${'$'}1 2. Nf3 (2. f4 exf4) Nc6 *
        """.trimIndent()

        val game = Pgn.parse(text)

        assertEquals(listOf("e2e4", "e7e5", "g1f3", "b8c6"), game.uciMoves)
    }

    @Test
    fun `doc duoc ca kieu viet nhap thanh bang so khong`() {
        val fen = "4k3/8/8/8/8/8/4P3/R3K2R w KQ - 0 1"
        val text = """
            [Event "Test"]
            [Site "Local"]
            [Date "2026.09.12"]
            [Round "1"]
            [White "An"]
            [Black "Bình"]
            [Result "*"]
            [SetUp "1"]
            [FEN "$fen"]

            1. 0-0 *
        """.trimIndent()

        val game = Pgn.parse(text)

        assertEquals(listOf("e1g1"), game.uciMoves)
    }

    @Test
    fun `thieu tag bat buoc thi bao loi`() {
        val text = "[Event \"Test\"]\n\n1. e4 *"
        val error = runCatching { Pgn.parse(text) }.exceptionOrNull()
        assertTrue("$error", error is Pgn.PgnException)
        assertTrue("$error", error!!.message!!.contains("Result"))
    }

    @Test
    fun `nuoc sai luat thi bao loi kem so nuoc`() {
        val text = """
            [Event "Test"]
            [Site "Local"]
            [Date "2026.09.12"]
            [Round "1"]
            [White "An"]
            [Black "Bình"]
            [Result "*"]

            1. e4 e5 2. Nf3 Nf6 3. Kd5 *
        """.trimIndent()

        val error = runCatching { Pgn.parse(text) }.exceptionOrNull() as? Pgn.PgnException

        assertNotNull(error)
        assertEquals(3, error!!.moveNumber)
        assertEquals("Kd5", error.token)
    }

    @Test
    fun `cu phap la thi bao loi kem so nuoc`() {
        val text = """
            [Event "Test"]
            [Site "Local"]
            [Date "2026.09.12"]
            [Round "1"]
            [White "An"]
            [Black "Bình"]
            [Result "*"]

            1. e4 zz9 *
        """.trimIndent()

        val error = runCatching { Pgn.parse(text) }.exceptionOrNull() as? Pgn.PgnException

        assertNotNull(error)
        assertEquals(1, error!!.moveNumber)
        assertEquals("zz9", error.token)
    }
}
