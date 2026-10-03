package kma.game.chess2d.game

/** Các mục của Pause Menu, theo đúng thứ tự spec: Tiếp tục / Khởi động lại / Cài đặt / Rời trận. */
enum class PauseItem { RESUME, RESTART, SETTINGS, LEAVE }

/** Luật chọn mục cho Pause Menu. Thuần, không phụ thuộc Android. */
object PauseMenu {

    /**
     * Danh sách mục hiện ra theo chế độ chơi.
     *
     * [PauseItem.SETTINGS] hiện chỉ mở hộp chọn cấp máy, nên chỉ có nghĩa khi đấu máy; ở chế độ
     * hai người thì ẩn hẳn thay vì để một nút không làm gì (cùng nguyên tắc với nút Đổi cấp).
     */
    fun itemsFor(mode: GameMode): List<PauseItem> = buildList {
        add(PauseItem.RESUME)
        add(PauseItem.RESTART)
        if (mode == GameMode.VS_COMPUTER) add(PauseItem.SETTINGS)
        add(PauseItem.LEAVE)
    }
}
