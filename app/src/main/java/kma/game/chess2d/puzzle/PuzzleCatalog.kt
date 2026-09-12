package kma.game.chess2d.puzzle

import android.content.Context
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move

/**
 * Một câu đố cờ: thế bắt đầu và chuỗi nước giải đúng.
 *
 * Nước giải lưu kiểu UCI chứ không kiểu SAN: UCI không phụ thuộc cách viết và không
 * nhầm lẫn giữa hai nước trùng nghĩa, nên so với nước người chơi vừa đi rất gọn.
 */
data class Puzzle(
    val title: String,
    val startFen: String,
    val solutionUci: List<String>,
)

/**
 * Bộ câu đố đóng gói trong assets (mục 7.3).
 *
 * Phần đọc file và phần hiểu nội dung được tách hẳn: [parse] và [validate] là hàm
 * thuần, nên test JVM duyệt được toàn bộ bộ câu đố bằng engine thật mà không cần máy
 * Android và không cần [Context].
 */
object PuzzleCatalog {

    /** Tên file trong `assets`. */
    const val ASSET_NAME = "puzzles.txt"

    /** Đọc bộ câu đố đóng gói trong app. */
    fun load(context: Context): List<Puzzle> =
        context.assets.open(ASSET_NAME).bufferedReader().use { parse(it.readText()) }

    /**
     * Đọc nội dung file câu đố.
     *
     * Dòng trống và dòng chú thích (`#`) bị bỏ qua. Dòng sai định dạng thì ném lỗi chứ
     * không lặng lẽ bỏ qua: file nằm trong app, sai là lỗi của người viết chứ không
     * phải dự liệu lạ từ bên ngoài, nên phải thấy ngay lúc chạy test.
     */
    fun parse(text: String): List<Puzzle> = text.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .map { line ->
            val parts = line.split('|')
            require(parts.size == 3) { "câu đố sai định dạng: $line" }
            val moves = parts[2].trim().split(' ').filter { it.isNotEmpty() }
            require(moves.isNotEmpty()) { "câu đố không có nước giải: $line" }
            Puzzle(
                title = parts[0].trim(),
                startFen = parts[1].trim(),
                solutionUci = moves,
            )
        }
        .toList()

    /**
     * Duyệt một câu đố bằng engine: dựng thế cờ rồi đi lần lượt từng nước giải.
     *
     * Trả về danh sách nước đã giải mã để nơi gọi dùng lại, và ném lỗi khi có một nước
     * không hợp lệ. Chính hàm này được test JVM gọi cho **tất cả** câu đố trong assets.
     */
    fun validate(puzzle: Puzzle): List<Move> {
        val board: Board = Engine.fromFen(puzzle.startFen)
        val decoded = mutableListOf<Move>()
        for ((index, uci) in puzzle.solutionUci.withIndex()) {
            val move = Engine.legalMoves(board).firstOrNull { it.toUci() == uci }
                ?: error("\"${puzzle.title}\": nước thứ ${index + 1} ($uci) không hợp lệ")
            decoded.add(move)
            board.makeMove(move)
        }
        return decoded
    }
}
