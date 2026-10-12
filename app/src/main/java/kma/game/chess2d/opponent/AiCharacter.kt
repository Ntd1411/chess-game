package kma.game.chess2d.opponent

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.TowerCatalog

/**
 * Một đối thủ AI trong màn "Chọn đối thủ AI" (xem `docs/design/story-bible.md`).
 *
 * Engine chỉ có ba cấp độ ([Difficulty]); sức mạnh thật của nhân vật **chỉ** đến từ
 * [difficulty]. Phong cách, tính cách và câu thoại là phần mô tả/nhân vật, không đổi
 * cách AI nghĩ — nên số sao và tốc độ nghĩ hiển thị đều suy ra từ [difficulty] chứ không
 * khai báo riêng, để màn hình không bao giờ hứa một độ khó mà AI không có.
 *
 * @param portrait ảnh chân dung **vuông** (`portrait_*`), không phải ảnh toàn thân.
 * @param fullBody ảnh toàn thân nếu có (`character_*_fullbody`); `null` thì màn chọn đối thủ dùng
 *        chân dung vuông chứ không phóng chân dung thành hình toàn thân.
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
    @DrawableRes val fullBody: Int? = null,
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
            fullBody = R.drawable.character_ai_blood_queen,
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
        ),
        AiCharacter(
            id = "hidden",
            name = R.string.ai_hidden_name,
            portrait = R.drawable.portrait_unknown_02,
            difficulty = Difficulty.HARD,
            style = R.string.ai_hidden_style,
            personality = R.string.ai_hidden_personality,
            quote = R.string.ai_hidden_quote,
            // Điều kiện mở của nhân vật ẩn chưa được chốt trong story bible: tạm đặt là
            // đã chinh phục hết Tháp Cờ, để nó thật sự là phần thưởng cuối game.
            unlockFloor = TowerCatalog.FLOOR_COUNT,
        ),
    )

    /** Nhân vật mặc định khi mở màn: đối thủ đầu tiên, luôn mở sẵn. */
    val default: AiCharacter get() = all.first()
}
