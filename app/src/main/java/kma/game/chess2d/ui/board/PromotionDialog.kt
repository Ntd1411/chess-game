package kma.game.chess2d.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.game.PendingPromotion

/**
 * Hỏi người chơi muốn phong cấp thành quân gì.
 *
 * Xếp Hậu đứng đầu vì đó là lựa chọn đúng trong hầu hết trường hợp, nhưng vẫn
 * bắt phải chọn chứ không tự động chọn hộ — có thế phong Mã mới thắng.
 */
@Composable
fun PromotionDialog(
    pending: PendingPromotion,
    onChosen: (Move) -> Unit,
    onDismiss: () -> Unit,
) {
    // Sắp lại theo thứ tự quen thuộc với người chơi, không theo thứ tự engine sinh ra.
    val ordered = PROMOTION_ORDER.mapNotNull { type ->
        pending.options.firstOrNull { it.promotionType == type }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.promotion_title)) },
        text = {
            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                for (move in ordered) {
                    TextButton(onClick = { onChosen(move) }) {
                        Text(
                            text = glyphOf(Piece.of(move.promotionType, pending.white)),
                            style = TextStyle(fontSize = 34.sp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

// Dùng List chứ không IntArray: mảng nguyên thủy không có mapNotNull.
private val PROMOTION_ORDER = listOf(Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT)
