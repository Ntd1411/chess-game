package kma.game.chess2d.ui.board

import androidx.compose.ui.graphics.Color

/**
 * Màu highlight và màu quân, gom một chỗ để code vẽ không chứa hằng số màu nào.
 *
 * Màu hai loại ô bàn cờ **không** ở đây mà nằm trong [BoardPalette], vì từ mục 7.3
 * người chơi đổi được bộ màu bàn; những màu còn lại dùng chung cho mọi bộ.
 *
 * Các màu highlight đều bán trong suốt và được vẽ phủ lên màu ô gốc, nên vẫn phân
 * biệt được ô sáng với ô tối ở bên dưới.
 */
object BoardColors {
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
