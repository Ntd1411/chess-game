package kma.game.chess2d.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.board.glyphOf

/**
 * Avatar tự vẽ, không dùng một file ảnh nào.
 *
 * Màu lấy từ hash của tên, nên cùng một tên luôn cho cùng một avatar còn hai người
 * tên khác nhau gần như chắc chắn khác màu. Đổi lại việc không dùng asset: không
 * vướng license (rủi ro đã ghi ở mục 6), không phải tải mạng, và thêm người chơi
 * mới không tốn thêm gì.
 *
 * @param piece glyph quân cờ vẽ ở giữa. Truyền [Piece.NONE] để lấy chữ đầu của tên.
 * @param active bên đang tới lượt. Báo bằng viền sáng quanh avatar thay vì thêm
 *        một dòng chữ "lượt của ai" vào khung.
 */
@Composable
fun PlayerAvatar(
    name: String,
    piece: Byte = Piece.NONE,
    active: Boolean = false,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier,
) {
    // hashCode có thể âm, đưa về UInt trước khi lấy dư để không ra góc màu âm.
    val hue = (name.hashCode().toUInt() % 360u).toFloat()
    val top = Color.hsv(hue, 0.40f, 0.90f)
    val bottom = Color.hsv((hue + 40f) % 360f, 0.65f, 0.55f)
    val ring = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(brush = Brush.linearGradient(listOf(top, bottom)))
            if (active) drawCircle(color = ring, style = Stroke(width = size.toPx() * 0.09f))
        }
        Text(
            text = if (piece == Piece.NONE) initialOf(name) else glyphOf(piece),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

/** Chữ đầu của tên. Tên rỗng vẫn phải ra một ký tự, không để avatar trống trơn. */
private fun initialOf(name: String): String =
    name.trim().take(1).ifEmpty { "?" }.uppercase()
