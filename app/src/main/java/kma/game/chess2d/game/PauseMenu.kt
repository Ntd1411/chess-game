package kma.game.chess2d.game

/** Các mục của Pause Menu, theo đúng thứ tự spec: Tiếp tục / Khởi động lại / Cài đặt / Rời trận. */
enum class PauseItem { RESUME, RESTART, SETTINGS, LEAVE }

/** Luật chọn mục cho Pause Menu. Thuần, không phụ thuộc Android. */
object PauseMenu {

    /**
     * Danh sách mục hiện ra. Hiện giống nhau ở mọi chế độ: Cài đặt mở bảng Cài đặt chung (âm
     * thanh, giao diện bàn cờ, nước đi hợp lệ), có nghĩa với cả đấu máy lẫn hai người.
     * Giữ tham số [mode] để sau này mục nào chỉ hợp một chế độ thì ẩn được ở đúng một chỗ.
     */
    @Suppress("UNUSED_PARAMETER")
    fun itemsFor(mode: GameMode): List<PauseItem> =
        listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.SETTINGS, PauseItem.LEAVE)

    /**
     * Mục cho ván chiến dịch (Tháp Cờ). Không phụ thuộc [GameMode] nên tách riêng: Khởi động lại là chơi lại
     * tầng từ đầu, Rời trận là về bản đồ (không thắng không thua).
     */
    fun itemsForCampaign(): List<PauseItem> =
        listOf(PauseItem.RESUME, PauseItem.RESTART, PauseItem.SETTINGS, PauseItem.LEAVE)
}
