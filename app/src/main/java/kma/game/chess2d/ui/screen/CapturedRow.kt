package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.board.glyphOf

/**
 * Hàng quân đã ăn của một bên, kèm chênh lệch vật chất nếu bên đó đang hơn (mục 7.2).
 *
 * Dữ liệu được tính từ danh sách nước đi ở `:engine`, ở đây chỉ vẽ. Quân được sắp
 * theo giá trị giảm dần thay vì theo thứ tự bị bắt: nhìn hàng quân là biết ngay đã
 * ăn được quân lốn hay chỉ toàn tốt.
 *
 * @param captured những quân bên này đã ăn được của đối phương.
 * @param advantage điểm hơn của bên này; nhỏ hơn hoặc bằng 0 thì không hiện số.
 */
@Composable
fun CapturedRow(
    captured: List<Byte>,
    advantage: Int,
    modifier: Modifier = Modifier,
) {
    // Chưa ăn được gì thì không chiếm chỗ: một hàng rỗng chỉ làm bàn cờ bị đẩy xuống.
    if (captured.isEmpty()) return

    val glyphs = captured
        .sortedByDescending { Piece.typeOf(it) }
        .joinToString(separator = " ") { glyphOf(it) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = glyphs,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (advantage > 0) {
            Text(
                text = "+$advantage",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
