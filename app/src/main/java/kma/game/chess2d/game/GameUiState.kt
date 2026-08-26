package kma.game.chess2d.game

import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Squares

/**
 * Một quân đang đứng trên bàn.
 *
 * [id] là định danh ổn định theo quân, không phải theo ô. Nhờ vậy Compose hiểu
 * được rằng "con mã vừa đi từ g1 sang f3" chứ không phải "một quân ở g1 biến mất
 * và một quân khác xuất hiện ở f3" — điều kiện bắt buộc để animate được vị trí.
 */
data class PieceOnBoard(val id: Int, val piece: Byte, val square: Int)

/**
 * Những lựa chọn phong cấp đang chờ người chơi quyết định.
 *
 * Phải hỏi vì cùng một ô đi và ô đến có tới bốn nước đi khác nhau; giao diện không
 * được tự ý chọn Hậu vì có thế phong Mã mới thắng còn phong Hậu lại hoà.
 */
data class PendingPromotion(
    val from: Int,
    val to: Int,
    val white: Boolean,
    val options: List<Move>,
)

/**
 * Toàn bộ những gì giao diện cần để vẽ một khung hình.
 *
 * Đây là ranh giới module được nói ở mục 2.3: bên trong engine là bàn cờ mutable
 * với make/unmake, còn đi ra UI thì là snapshot immutable. Giao diện không bao giờ
 * giữ tham chiếu tới <code>Board</code>, nên không thể đọc phải trạng thái nửa vời trong
 * lúc search đang đi thử hàng triệu nước ở Phase 3.
 */
data class GameUiState(
    val pieces: List<PieceOnBoard> = emptyList(),
    val whiteToMove: Boolean = true,
    val status: GameStatus = GameStatus.ONGOING,
    val selectedSquare: Int = Squares.NONE,
    val legalTargets: Set<Int> = emptySet(),
    val lastMoveFrom: Int = Squares.NONE,
    val lastMoveTo: Int = Squares.NONE,
    val checkedKingSquare: Int = Squares.NONE,
    val canUndo: Boolean = false,
    val pendingPromotion: PendingPromotion? = null,
)
