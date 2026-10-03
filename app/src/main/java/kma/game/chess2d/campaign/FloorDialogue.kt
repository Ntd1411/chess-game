package kma.game.chess2d.campaign

/** Người nói trong thoại chiến dịch (xem `docs/design/story-bible.md`, phần Nhân vật). */
enum class Speaker(val displayName: String) {
    /** Giọng dẫn chuyện của tháp, không có hình dạng cụ thể. */
    NARRATOR("Giọng nói trong tháp"),
    TRAVELER("Người Lữ Hành"),
    PLAYER_48("Player #48"),
    ARIA("Aria"),
}

/** Một câu thoại. */
data class DialogueLine(val speaker: Speaker, val text: String)

/**
 * Thoại của 6 tầng mốc theo `docs/design/story-bible.md` (mục "Quyết định triển khai").
 *
 * Chỉ tầng mốc mới có thoại: [TowerCatalog.validateAll] kiểm `Floor.hasDialogue` khớp với
 * [TowerCatalog.MILESTONES], và test kiểm [intro] khớp với hai thứ đó. Tầng 49 có thêm
 * [epilogue] vì cốt truyện cần một đoạn kết sau khi thắng.
 *
 * Để dạng dữ liệu thuần Kotlin (giống tên tầng trong [TowerCatalog]) để test JVM duyệt
 * được mà không cần máy Android.
 */
object FloorDialogues {

    /** Thoại trước khi vào tầng [floor]; rỗng nếu tầng đó không phải tầng mốc. */
    fun intro(floor: Int): List<DialogueLine> = when (floor) {
        1 -> listOf(
            line(Speaker.NARRATOR, "Cánh cổng đá khép lại sau lưng bạn. Trên cao, mặt trăng đỏ nhìn xuống một bàn cờ khổng lồ."),
            line(Speaker.NARRATOR, "Tháp Elysia có bốn mươi chín tầng. Mỗi tầng là một nước đi."),
            line(Speaker.TRAVELER, "Mình không nhớ đã vào đây bằng cách nào. Chỉ biết là phải đi lên."),
            line(Speaker.NARRATOR, "Hãy nhớ lấy điều này: đừng để Vua bước ra khỏi bàn cờ."),
        )

        5 -> listOf(
            line(Speaker.PLAYER_48, "Khoan đã... bạn cũng là người chơi sao? Tôi là Player #48."),
            line(Speaker.TRAVELER, "Người chơi thứ bốn mươi tám? Vậy bạn đã ở đây rất lâu rồi."),
            line(Speaker.PLAYER_48, "Hồ sơ của tôi bị [CORRUPTED]. Tôi không nhớ mình đã leo bao nhiêu lần."),
            line(Speaker.PLAYER_48, "Nghe này: đừng tin hoàn toàn vào những gì tháp cho bạn thấy."),
        )

        21 -> listOf(
            line(Speaker.ARIA, "Ta là Aria, Nữ Hoàng Ký Ức. Ta giữ lại mọi nước đi mà các ngươi từng bỏ quên."),
            line(Speaker.TRAVELER, "Các ngươi? Ở đây chỉ có mình tôi."),
            line(Speaker.ARIA, "Bốn mươi tám người đã thất bại trước ngươi, mỗi người một mình. Ngươi nghĩ sẽ khác sao?"),
            line(Speaker.TRAVELER, "Tôi sẽ thử."),
        )

        42 -> listOf(
            line(Speaker.PLAYER_48, "Nghe cho kỹ, vì hồ sơ này sẽ hỏng lần nữa ngay sau khi tôi nói xong."),
            line(Speaker.PLAYER_48, "Bạn không phải người thứ bốn mươi chín bước vào tháp. Bạn là Attempt #49."),
            line(Speaker.TRAVELER, "Vậy bốn mươi tám người trước..."),
            line(Speaker.PLAYER_48, "Tất cả đều là bạn. Mỗi lần thất bại, vòng lặp lại bắt đầu từ cổng tháp."),
        )

        45 -> listOf(
            line(Speaker.ARIA, "Ta không phải kẻ thù của ngươi. Ta được tạo từ ký ức của mọi lần thử."),
            line(Speaker.ARIA, "Ta cản ngươi vì mỗi lần ngươi lên tới đỉnh, vòng lặp lại bắt đầu và ngươi quên tất cả."),
            line(Speaker.TRAVELER, "Vậy tại sao lần này bà lại nói cho tôi biết?"),
            line(Speaker.ARIA, "Vì ta muốn ngươi nhớ. Hãy lên tầng cuối cùng với những ký ức này."),
        )

        49 -> listOf(
            line(Speaker.NARRATOR, "Bàn cờ cuối cùng. Từ đây, mọi lần thử trước đều dừng lại."),
            line(Speaker.TRAVELER, "Bốn mươi tám lần tôi đến đây, bốn mươi tám lần tôi quên."),
            line(Speaker.ARIA, "Lần này ngươi nhớ. Đó là nước đi thứ bốn mươi chín, nước đi chưa ai thử."),
            line(Speaker.NARRATOR, "Đừng để Vua bước ra khỏi bàn cờ."),
            line(Speaker.TRAVELER, "Còn tôi thì sẽ bước ra."),
        )

        else -> emptyList()
    }

    /** Thoại sau khi thắng tầng [floor]; chỉ tầng cuối có, các tầng khác rỗng. */
    fun epilogue(floor: Int): List<DialogueLine> = when (floor) {
        TowerCatalog.FLOOR_COUNT -> listOf(
            line(Speaker.ARIA, "Chiếu hết. Vòng lặp... khép lại rồi."),
            line(Speaker.TRAVELER, "Cảm ơn, Aria. Vì đã giữ lại những ký ức này."),
            line(Speaker.NARRATOR, "Cánh cổng đá mở ra. Người Lữ Hành bước ra khỏi bàn cờ lần đầu tiên."),
        )

        else -> emptyList()
    }

    private fun line(speaker: Speaker, text: String) = DialogueLine(speaker, text)
}
