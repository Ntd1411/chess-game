package kma.game.chess2d.campaign

import kma.game.chess2d.ai.Difficulty

/**
 * Loại node trên bản đồ Tháp Cờ (xem `docs/design/ui-screens.md`, màn Tower Map).
 */
enum class NodeKind {
    /** Thắng một AI theo độ khó của tầng. */
    BATTLE,

    /** Tầng thuần cốt truyện: xem thoại là qua, không có ván cờ. */
    STORY,

    /** Tầng phần thưởng: nhận là qua, không có ván cờ. */
    TREASURE,

    /** Giải một thế cờ đóng gói trong `puzzles.txt`. */
    PUZZLE,

    /** Trùm cuối chương: AI mạnh hơn độ khó thường của tầng. */
    BOSS,

    /** Tầng bí mật: sống sót N nước từ một thế cờ bất lợi. */
    SECRET,
}

/**
 * Điều kiện để qua một tầng.
 *
 * Người chơi luôn cầm quân Trắng trong chế độ chiến dịch.
 */
sealed interface FloorGoal {

    /** Chiếu hết AI. */
    data class DefeatAi(val difficulty: Difficulty) : FloorGoal

    /** Giải đúng câu đố số [puzzleIndex] trong danh sách `PuzzleCatalog`. */
    data class SolvePuzzle(val puzzleIndex: Int) : FloorGoal

    /** Đi đủ [moves] nước mà không bị chiếu hết. */
    data class SurviveMoves(val moves: Int, val difficulty: Difficulty) : FloorGoal

    /** Không có ván cờ: tầng qua ngay khi người chơi nhận (tầng Story/Treasure). */
    data object Claim : FloorGoal
}

/**
 * Dữ liệu của một tầng trong Tháp Cờ.
 *
 * @param number số tầng, từ 1 đến [TowerCatalog.FLOOR_COUNT].
 * @param startFen thế cờ bắt đầu khi tầng cần thế riêng (tầng bí mật). `null` nghĩa là
 *        thế khởi đầu chuẩn, hoặc thế lấy từ câu đố nếu mục tiêu là [FloorGoal.SolvePuzzle].
 * @param hasDialogue có thoại cốt truyện trước tầng này không. Chỉ bật ở các tầng mốc
 *        theo `docs/design/story-bible.md`.
 */
data class Floor(
    val number: Int,
    val kind: NodeKind,
    val title: String,
    val goal: FloorGoal,
    val startFen: String? = null,
    val hasDialogue: Boolean = false,
)
