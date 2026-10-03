package kma.game.chess2d.campaign

import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.puzzle.Puzzle
import kma.game.chess2d.puzzle.PuzzleCatalog

/**
 * Bộ 49 tầng của Tháp Cờ.
 *
 * Phần lớn tầng được sinh theo quy luật lặp lại trong mỗi chương (7 chương × 7 tầng),
 * chỉ 6 tầng mốc có nội dung riêng theo `docs/design/story-bible.md`. Giống
 * [PuzzleCatalog], [build] và [validate] đều là hàm thuần nên test JVM duyệt được cả
 * 49 tầng bằng engine thật, không cần máy Android.
 */
object TowerCatalog {

    const val FLOOR_COUNT = 49
    const val FLOORS_PER_CHAPTER = 7

    /** Các tầng mốc có thoại cốt truyện: mở đầu, Player #48, Aria, true reveal, lý do của Aria, kết. */
    val MILESTONES: Set<Int> = setOf(1, 5, 21, 42, 45, 49)

    /** Thế khởi đầu của tầng bí mật: Trắng mất Hậu, người chơi phải cầm cự. */
    const val FEN_WHITE_NO_QUEEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"

    fun chapterOf(floor: Int): Int = (floor - 1) / FLOORS_PER_CHAPTER + 1

    /**
     * Độ khó thường của tầng, theo 3 lớp bí ẩn: tầng 1–14, 15–35, 36–49.
     */
    fun difficultyFor(floor: Int): Difficulty = when {
        floor <= 14 -> Difficulty.EASY
        floor <= 35 -> Difficulty.MEDIUM
        else -> Difficulty.HARD
    }

    /** Trùm mạnh hơn tầng thường một bậc, trần là HARD. */
    private fun bossDifficultyFor(floor: Int): Difficulty = when (difficultyFor(floor)) {
        Difficulty.EASY -> Difficulty.MEDIUM
        Difficulty.MEDIUM, Difficulty.HARD -> Difficulty.HARD
    }

    /**
     * Dựng đủ [FLOOR_COUNT] tầng.
     *
     * @param puzzleCount số câu đố có sẵn; tầng PUZZLE của chương c lấy câu số
     *        `(c - 1) % puzzleCount`, nên bộ câu đố ít hơn số chương vẫn dùng được.
     */
    fun build(puzzleCount: Int): List<Floor> {
        require(puzzleCount > 0) { "cần ít nhất 1 câu đố cho các tầng PUZZLE" }
        return (1..FLOOR_COUNT).map { floor(it, puzzleCount) }
    }

    private fun floor(number: Int, puzzleCount: Int): Floor {
        val chapter = chapterOf(number)
        val position = (number - 1) % FLOORS_PER_CHAPTER + 1
        val difficulty = difficultyFor(number)
        val dialogue = number in MILESTONES

        // Tầng mốc có nội dung riêng; tầng 1, 5, 21, 42, 49 vẫn là ván cờ, chỉ 45 là thuần thoại.
        val milestoneTitle = when (number) {
            1 -> "Cổng tháp"
            5 -> "Player #48"
            21 -> "Aria, Nữ Hoàng Ký Ức"
            42 -> "Sự thật bị chôn vùi"
            45 -> "Lời Aria"
            49 -> "Nước đi thứ 49"
            else -> null
        }

        if (number == 45) {
            return Floor(
                number = number,
                kind = NodeKind.STORY,
                title = milestoneTitle!!,
                goal = FloorGoal.Claim,
                hasDialogue = true,
            )
        }

        return when (position) {
            2 -> Floor(
                number = number,
                kind = NodeKind.PUZZLE,
                title = milestoneTitle ?: "Thế cờ chương $chapter",
                goal = FloorGoal.SolvePuzzle((chapter - 1) % puzzleCount),
                hasDialogue = dialogue,
            )

            4 -> Floor(
                number = number,
                kind = NodeKind.TREASURE,
                title = milestoneTitle ?: "Kho báu chương $chapter",
                goal = FloorGoal.Claim,
                hasDialogue = dialogue,
            )

            6 -> Floor(
                number = number,
                kind = NodeKind.SECRET,
                title = milestoneTitle ?: "Căn phòng bí mật",
                goal = FloorGoal.SurviveMoves(moves = 10 + 2 * chapter, difficulty = difficulty),
                startFen = FEN_WHITE_NO_QUEEN,
                hasDialogue = dialogue,
            )

            7 -> Floor(
                number = number,
                kind = NodeKind.BOSS,
                title = milestoneTitle ?: "Trùm chương $chapter",
                goal = FloorGoal.DefeatAi(bossDifficultyFor(number)),
                hasDialogue = dialogue,
            )

            else -> Floor(
                number = number,
                kind = NodeKind.BATTLE,
                title = milestoneTitle ?: "Đấu cờ tầng $number",
                goal = FloorGoal.DefeatAi(difficulty),
                hasDialogue = dialogue,
            )
        }
    }

    /**
     * Kiểm tra một tầng bằng engine thật; ném lỗi nếu dữ liệu sai.
     *
     * Nhắm vào lỗi của người viết dữ liệu (FEN hỏng, chỉ số câu đố lệch, loại node
     * không khớp mục tiêu), nên sai là báo ngay chứ không âm thầm bỏ qua.
     */
    fun validate(floor: Floor, puzzles: List<Puzzle>) {
        val label = "tầng ${floor.number}"
        require(floor.number in 1..FLOOR_COUNT) { "$label ngoài khoảng 1..$FLOOR_COUNT" }
        require(floor.title.isNotBlank()) { "$label thiếu tên" }

        floor.startFen?.let { fen ->
            val board = Engine.fromFen(fen)
            check(!Rules.isGameOver(board)) { "$label: thế cờ bắt đầu đã kết thúc ($fen)" }
        }

        val goal = floor.goal
        val kindMatchesGoal = when (floor.kind) {
            NodeKind.BATTLE, NodeKind.BOSS -> goal is FloorGoal.DefeatAi
            NodeKind.PUZZLE -> goal is FloorGoal.SolvePuzzle
            NodeKind.SECRET -> goal is FloorGoal.SurviveMoves
            NodeKind.STORY, NodeKind.TREASURE -> goal is FloorGoal.Claim
        }
        check(kindMatchesGoal) { "$label: loại ${floor.kind} không khớp mục tiêu $goal" }

        when (goal) {
            is FloorGoal.SolvePuzzle -> {
                check(goal.puzzleIndex in puzzles.indices) {
                    "$label: câu đố số ${goal.puzzleIndex} không tồn tại (có ${puzzles.size} câu)"
                }
                PuzzleCatalog.validate(puzzles[goal.puzzleIndex])
            }

            is FloorGoal.SurviveMoves -> {
                check(goal.moves > 0) { "$label: số nước phải sống sót phải dương" }
                check(floor.startFen != null) { "$label: tầng sống sót cần thế cờ riêng" }
            }

            is FloorGoal.DefeatAi, FloorGoal.Claim -> Unit
        }
    }

    /**
     * Kiểm tra cả bộ: đủ [FLOOR_COUNT] tầng đánh số liên tục, thoại chỉ ở tầng mốc,
     * và từng tầng qua [validate].
     */
    fun validateAll(floors: List<Floor>, puzzles: List<Puzzle>) {
        check(floors.size == FLOOR_COUNT) { "cần $FLOOR_COUNT tầng, có ${floors.size}" }
        for ((index, floor) in floors.withIndex()) {
            check(floor.number == index + 1) { "tầng ở vị trí ${index + 1} đánh số ${floor.number}" }
            check(floor.hasDialogue == (floor.number in MILESTONES)) {
                "tầng ${floor.number}: cờ thoại không khớp danh sách tầng mốc"
            }
            validate(floor, puzzles)
        }
    }
}
