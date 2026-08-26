package kma.game.chess2d.ui.board

import androidx.compose.ui.graphics.Color

/**
 * Bộ màu bàn cờ, gom một chỗ để việc "đổi bộ quân và màu bàn cờ" ở mục 7 chỉ là
 * thêm một bộ giá trị chứ không phải đi sửa rải rác trong code vẽ.
 *
 * Các màu highlight đều bán trong suốt và được vẽ phủ lên màu ô gốc, nên vẫn phân
 * biệt được ô sáng với ô tối ở bên dưới.
 */
object BoardColors {
    val lightSquare = Color(0xFFEEEED2)
    val darkSquare = Color(0xFF769656)

    val lastMove = Color(0x80F7F169)
    val selected = Color(0x99F5C542)
    val check = Color(0x99E3564A)
    val legalTarget = Color(0x4D1B1B1B)

    val whitePiece = Color(0xFFFCFCFA)
    val blackPiece = Color(0xFF23211F)

    /** Viền của quân, ngược màu với thân quân để cả hai bên đều rõ trên mọi ô. */
    val whitePieceOutline = Color(0xFF23211F)
    val blackPieceOutline = Color(0xFFF2F1EE)
}
