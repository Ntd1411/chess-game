package kma.game.chess2d.opponent

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.TowerCatalog

/**
 * Một đối thủ AI trong màn "Chọn đối thủ AI" (xem `docs/design/story-bible.md`).
 *
 * In regular AI selection, strength comes from [difficulty]. Campaign bosses
 * share this identity with chapter-specific AiStrength search limits.
 * Phong cách, tính cách và câu thoại là phần mô tả/nhân vật, không đổi
 * cách AI nghĩ — nên số sao và tốc độ nghĩ hiển thị đều suy ra từ [difficulty] chứ không
 * khai báo riêng, để màn hình không bao giờ hứa một độ khó mà AI không có.
 *
 * @param portrait ảnh chân dung **vuông** (`portrait_*`), không phải ảnh toàn thân.
 * @param unlockFloor tầng của Tháp Cờ phải vượt qua để mở nhân vật; `null` là mở sẵn.
 */
data class AiCharacter(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val portrait: Int,
    val difficulty: Difficulty,
    @StringRes val style: Int,
    @StringRes val personality: Int,
    @StringRes val quote: Int,
    val unlockFloor: Int? = null,
) {

    /** Số sao độ khó, 1 đến 3, suy ra từ [difficulty]. */
    val stars: Int get() = difficulty.ordinal + 1

    /** Thời gian nghĩ tối đa mỗi nước, tính bằng giây, lấy thẳng từ ngân sách của AI. */
    val thinkSeconds: Float get() = difficulty.timeBudgetMillis / 1000f

    /** Nhân vật đã mở chưa, biết người chơi đã qua tới tầng [clearedUpTo] của Tháp Cờ. */
    fun isUnlocked(clearedUpTo: Int): Boolean = unlockFloor == null || clearedUpTo >= unlockFloor
}

/** Danh sách đối thủ AI theo thứ tự hiện trong carousel: từ dễ tới khó, nhân vật ẩn ở cuối. */
object AiCharacters {

    val all: List<AiCharacter> = listOf(
        AiCharacter(
            id = "chess_mage",
            name = R.string.ai_mage_name,
            portrait = R.drawable.portrait_ai_blue_queen,
            difficulty = Difficulty.EASY,
            style = R.string.ai_mage_style,
            personality = R.string.ai_mage_personality,
            quote = R.string.ai_mage_quote,
        ),
        AiCharacter(
            id = "dark_knight",
            name = R.string.ai_knight_name,
            portrait = R.drawable.portrait_crimson_knight,
            difficulty = Difficulty.MEDIUM,
            style = R.string.ai_knight_style,
            personality = R.string.ai_knight_personality,
            quote = R.string.ai_knight_quote,
        ),
        AiCharacter(
            id = "blood_queen",
            name = R.string.ai_queen_name,
            portrait = R.drawable.portrait_ai_blood_queen,
            difficulty = Difficulty.HARD,
            style = R.string.ai_queen_style,
            personality = R.string.ai_queen_personality,
            quote = R.string.ai_queen_quote,
        ),
        AiCharacter(
            id = "dark_king",
            name = R.string.ai_king_name,
            portrait = R.drawable.portrait_unknown_01,
            difficulty = Difficulty.HARD,
            style = R.string.ai_king_style,
            personality = R.string.ai_king_personality,
            quote = R.string.ai_king_quote,
            unlockFloor = 28,
        ),
        AiCharacter(
            id = "silent_bishop",
            name = R.string.ai_bishop_name,
            portrait = R.drawable.portrait_silent_bishop,
            difficulty = Difficulty.HARD,
            style = R.string.ai_bishop_style,
            personality = R.string.ai_bishop_personality,
            quote = R.string.ai_bishop_quote,
            unlockFloor = 35,
        ),
        AiCharacter(
            id = "golden_queen",
            name = R.string.ai_golden_queen_name,
            portrait = R.drawable.portrait_golden_queen,
            difficulty = Difficulty.HARD,
            style = R.string.ai_golden_queen_style,
            personality = R.string.ai_golden_queen_personality,
            quote = R.string.ai_golden_queen_quote,
            unlockFloor = 42,
        ),
        AiCharacter(
            id = "hidden",
            name = R.string.ai_hidden_name,
            portrait = R.drawable.portrait_unknown_02,
            difficulty = Difficulty.HARD,
            style = R.string.ai_hidden_style,
            personality = R.string.ai_hidden_personality,
            quote = R.string.ai_hidden_quote,
            // Defeating the final guardian unlocks the last regular AI opponent.
            unlockFloor = TowerCatalog.FLOOR_COUNT,
        ),
    )

    /** Resolve saved identity, with a difficulty-based fallback only for legacy saves. */
    fun resolve(id: String?, difficulty: Difficulty): AiCharacter =
        all.firstOrNull { it.id == id } ?: all.first { it.difficulty == difficulty }

    /** Nhân vật mặc định khi mở màn: đối thủ đầu tiên, luôn mở sẵn. */
    val default: AiCharacter get() = all.first()
}
