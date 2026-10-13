package kma.game.chess2d.campaign

import java.io.File
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.puzzle.Puzzle
import kma.game.chess2d.puzzle.PuzzleCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Duyệt cả 49 tầng của Tháp Cờ bằng engine thật, cùng cách [PuzzleCatalogTest] duyệt
 * bộ câu đố: test JVM thuần, đọc `puzzles.txt` thẳng từ `src/main/assets`.
 *
 * Tên test viết không dấu vì đây là định danh hàm, không phải câu văn tiếng Việt.
 */
class TowerCatalogTest {

    private fun puzzles(): List<Puzzle> {
        // Thư mục làm việc của test là thư mục của module :app.
        val file = File("src/main/assets/${PuzzleCatalog.ASSET_NAME}")
        assertTrue("không thấy ${file.absolutePath}", file.exists())
        return PuzzleCatalog.parse(file.readText())
    }

    private fun floors(): List<Floor> = TowerCatalog.build(puzzles().size)

    @Test
    fun `du 49 tang danh so lien tuc tu 1`() {
        val floors = floors()
        assertEquals(TowerCatalog.FLOOR_COUNT, floors.size)
        assertEquals((1..TowerCatalog.FLOOR_COUNT).toList(), floors.map { it.number })
    }

    @Test
    fun `49 loi dan rieng khong lap va chi mot den hai cau`() {
        val introductions = floors().map { it.teaser }
        assertEquals(49, introductions.distinct().size)
        introductions.forEach { text ->
            val sentences = text.split(Regex("[.!?…]+")).count { it.isNotBlank() }
            assertTrue(text, sentences in 1..2)
            assertTrue(text, text.length <= 180)
            assertTrue(text, !text.contains("Tìm chuỗi", ignoreCase = true))
            assertTrue(text, !text.contains("dấu tay đã khô", ignoreCase = true))
        }
    }

    @Test
    fun `bay boss rieng deu dung loi chao cua chinh AI do`() {
        val allFloors = floors()
        val bosses = allFloors.filter { it.kind == NodeKind.BOSS }
        assertEquals(7, bosses.map { TowerBosses.greeting(it) }.filterNotNull().distinct().size)
        assertEquals(R.string.ai_knight_quote, TowerBosses.greeting(allFloors[13]))
        assertEquals(R.string.ai_bishop_quote, TowerBosses.greeting(allFloors[34]))
        assertEquals(R.string.ai_queen_quote, TowerBosses.greeting(allFloors[20]))
        assertEquals(R.string.ai_golden_queen_quote, TowerBosses.greeting(allFloors[41]))
        assertEquals(7, bosses.map { it.bossId }.distinct().size)
        bosses.forEach {
            assertEquals(TowerBosses.character(it)!!.quote, TowerBosses.greeting(it))
        }
    }

    @Test
    fun `ca 49 tang deu hop le theo engine`() {
        // validateAll ném lỗi nếu có tầng nào thiếu dữ liệu, FEN hỏng hay lệch loại/mục tiêu.
        TowerCatalog.validateAll(floors(), puzzles())
    }

    @Test
    fun `thoai chi nam o sau tang moc`() {
        val withDialogue = floors().filter { it.hasDialogue }.map { it.number }.toSet()
        assertEquals(setOf(1, 5, 21, 42, 45, 49), withDialogue)
    }

    @Test
    fun `tang moc dung loai theo story bible`() {
        val byNumber = floors().associateBy { it.number }
        assertEquals(NodeKind.BOSS, byNumber.getValue(21).kind) // Blood Queen guards Aria's memory chamber
        assertEquals(NodeKind.BOSS, byNumber.getValue(42).kind) // true reveal
        assertEquals(NodeKind.PUZZLE, byNumber.getValue(45).kind) // Aria tiết lộ lý do
        assertEquals(NodeKind.BOSS, byNumber.getValue(49).kind) // final reveal
    }

    @Test
    fun `cuoi moi chuong la boss`() {
        val floors = floors()
        for (chapter in 1..7) {
            val last = floors[chapter * TowerCatalog.FLOORS_PER_CHAPTER - 1]
            assertEquals("chương $chapter", NodeKind.BOSS, last.kind)
        }
    }

    @Test
    fun `do kho theo ba lop bi an`() {
        assertEquals(Difficulty.EASY, TowerCatalog.difficultyFor(1))
        assertEquals(Difficulty.EASY, TowerCatalog.difficultyFor(14))
        assertEquals(Difficulty.MEDIUM, TowerCatalog.difficultyFor(15))
        assertEquals(Difficulty.MEDIUM, TowerCatalog.difficultyFor(35))
        assertEquals(Difficulty.HARD, TowerCatalog.difficultyFor(36))
        assertEquals(Difficulty.HARD, TowerCatalog.difficultyFor(49))
    }

    @Test
    fun `boss dung roster AI va tang suc manh tung chuong`() {
        val bosses = floors().filter { it.kind == NodeKind.BOSS }
        assertEquals(listOf(7, 14, 21, 28, 35, 42, 49), bosses.map { it.number })
        assertEquals(listOf("chess_mage", "dark_knight", "blood_queen", "dark_king",
            "silent_bishop", "golden_queen", "hidden"), bosses.map { it.bossId })
        val strengths = bosses.map { (it.goal as FloorGoal.DefeatAi).strength!! }
        strengths.zipWithNext().forEach { (a, b) ->
            assertTrue(b.depth > a.depth)
            assertTrue(b.timeBudgetMillis > a.timeBudgetMillis)
            assertTrue(b.topMoveChoices <= a.topMoveChoices)
            assertTrue(b.blunderChance <= a.blunderChance)
        }
        bosses.forEach { assertNotNull(TowerBosses.character(it)) }
    }

    @Test
    fun `sau puzzle moi chuong co chi so rieng va elo tang tu 800`() {
        val puzzles = floors().filter { it.kind == NodeKind.PUZZLE }
        assertEquals(42, puzzles.size)
        assertEquals((0 until 42).toList(), puzzles.map { (it.goal as FloorGoal.SolvePuzzle).puzzleIndex })
        assertEquals((0 until 42).map { 800 + 25 * it }, puzzles.map { it.estimatedElo })
        assertTrue(puzzles.all { it.number % 7 != 0 && it.teaser.isNotBlank() })
        assertTrue(floors().take(6).all { it.kind == NodeKind.PUZZLE })
    }

    @Test
    fun `khong lap puzzle khi thieu du lieu`() {
        assertTrue(runCatching { TowerCatalog.build(41) }.isFailure)
    }

    @Test
    fun `khong co cau do thi bao loi`() {
        val error = runCatching { TowerCatalog.build(puzzleCount = 0) }.exceptionOrNull()
        assertTrue("$error", error is IllegalArgumentException)
    }

    @Test
    fun `chi so cau do ngoai khoang thi bao loi`() {
        val puzzles = puzzles()
        val floor = Floor(
            number = 2,
            kind = NodeKind.PUZZLE,
            title = "Sai chỉ số",
            goal = FloorGoal.SolvePuzzle(puzzles.size),
        )
        val error = runCatching { TowerCatalog.validate(floor, puzzles) }.exceptionOrNull()
        assertTrue("$error", error is IllegalStateException)
    }

    @Test
    fun `loai node lech muc tieu thi bao loi`() {
        val floor = Floor(
            number = 1,
            kind = NodeKind.TREASURE,
            title = "Lệch mục tiêu",
            goal = FloorGoal.DefeatAi(Difficulty.EASY),
        )
        val error = runCatching { TowerCatalog.validate(floor, puzzles()) }.exceptionOrNull()
        assertTrue("$error", error is IllegalStateException)
    }

    @Test
    fun `the co bat dau da ket thuc thi bao loi`() {
        // Fool's mate: Trắng đã bị chiếu hết, không thể làm thế khởi đầu của một tầng.
        val floor = Floor(
            number = 6,
            kind = NodeKind.SECRET,
            title = "Thế đã chết",
            goal = FloorGoal.SurviveMoves(moves = 10, difficulty = Difficulty.EASY),
            startFen = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3",
        )
        val error = runCatching { TowerCatalog.validate(floor, puzzles()) }.exceptionOrNull()
        assertTrue("$error", error is IllegalStateException)
    }

    @Test
    fun `fen hong thi bao loi`() {
        val floor = Floor(
            number = 6,
            kind = NodeKind.SECRET,
            title = "FEN hỏng",
            goal = FloorGoal.SurviveMoves(moves = 10, difficulty = Difficulty.EASY),
            startFen = "không phải fen",
        )
        val error = runCatching { TowerCatalog.validate(floor, puzzles()) }.exceptionOrNull()
        assertNotNull("FEN hỏng phải bị từ chối", error)
    }

    @Test
    fun `thieu tang thi validateAll bao loi`() {
        val short = floors().dropLast(1)
        val error = runCatching { TowerCatalog.validateAll(short, puzzles()) }.exceptionOrNull()
        assertTrue("$error", error is IllegalStateException)
    }
}
