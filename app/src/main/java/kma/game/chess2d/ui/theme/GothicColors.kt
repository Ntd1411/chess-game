package kma.game.chess2d.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Bảng màu gothic của chế độ Tháp Cờ: đỏ-đen-vàng (xem `docs/design/ui-screens.md`).
 *
 * Màu cố định, không theo dynamic color của hệ thống, để các màn của chiến dịch giữ
 * đúng một phong cách dù người chơi đổi hình nền máy.
 */
object GothicColors {
    val Ink = Color(0xFF0B0608)
    val Blood = Color(0xFF8B1A1A)
    val Gold = Color(0xFFD4AF37)
    val Parchment = Color(0xFFEADBB8)
    val Locked = Color(0xFF4A4348)
}
