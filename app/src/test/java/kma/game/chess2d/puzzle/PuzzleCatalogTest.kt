package kma.game.chess2d.puzzle

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Duyệt toàn bộ bộ câu đố đóng gói trong assets bằng engine thật (mục 7.3).
 *
 * Đọc file trực tiếp từ `src/main/assets` thay vì qua `AssetManager`: như vậy test chạy
 * được bằng JVM thuần, không cần máy Android, nên sai một FEN hay một nước giải là biết
 * ngay ở bước build.
 *
 * Tên test viết không dấu vì đây là định danh hàm, không phải câu văn tiếng Việt.
 */
class PuzzleCatalogTest {

    private fun assetText(): String {
        // Thư mục làm việc của test là thư mục của module :app.
        val file = File("src/main/assets/${PuzzleCatalog.ASSET_NAME}")
        assertTrue("không thấy ${file.absolutePath}", file.exists())
        return file.readText()
    }

    @Test
    fun `bo cau do trong assets khong rong`() {
        val puzzles = PuzzleCatalog.parse(assetText())
        assertTrue("bộ câu đố rỗng", puzzles.isNotEmpty())
        for (puzzle in puzzles) {
            assertTrue("câu đố thiếu tên", puzzle.title.isNotBlank())
        }
    }

    @Test
    fun `moi nuoc giai deu hop le theo engine`() {
        val puzzles = PuzzleCatalog.parse(assetText())
        for (puzzle in puzzles) {
            val decoded = PuzzleCatalog.validate(puzzle)
            assertEquals(puzzle.title, puzzle.solutionUci.size, decoded.size)
        }
    }

    @Test
    fun `dong chu thich va dong trong bi bo qua`() {
        val text = """
            # chú thích

            Một nước|6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1|a1a8
        """.trimIndent()

        val puzzles = PuzzleCatalog.parse(text)

        assertEquals(1, puzzles.size)
        assertEquals(listOf("a1a8"), puzzles.first().solutionUci)
    }

    @Test
    fun `dong sai dinh dang thi bao loi`() {
        val error = runCatching { PuzzleCatalog.parse("thiếu cột") }.exceptionOrNull()
        assertTrue("$error", error is IllegalArgumentException)
    }

    @Test
    fun `nuoc giai sai luat thi bao loi`() {
        val puzzle = Puzzle(
            title = "Sai luật",
            startFen = "6k1/5ppp/8/8/8/8/8/R5K1 w - - 0 1",
            solutionUci = listOf("a1a4b"),
        )
        val error = runCatching { PuzzleCatalog.validate(puzzle) }.exceptionOrNull()
        assertTrue("$error", error is IllegalStateException)
    }
}
