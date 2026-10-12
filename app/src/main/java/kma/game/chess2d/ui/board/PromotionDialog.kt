package kma.game.chess2d.ui.board

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.GothicDialog

/**
 * Hỏi người chơi muốn phong cấp thành quân gì: bốn ô `ui_promotion_slot_*` chứa ảnh quân cờ
 * trên hộp `ui_dialog_panel`. Ô đang bị nhấn đổi sang ảnh `selected`.
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

    GothicDialog(onDismiss = onDismiss, title = stringResource(R.string.promotion_title)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (move in ordered) {
                PromotionSlot(
                    piece = Piece.of(move.promotionType, pending.white),
                    onClick = { onChosen(move) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        GothicButton(
            label = stringResource(R.string.action_cancel),
            onClick = onDismiss,
            art = ButtonArt.SECONDARY,
            modifier = Modifier.fillMaxWidth(0.6f).height(52.dp),
        )
    }
}

@Composable
private fun PromotionSlot(piece: Byte, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick),
    ) {
        ArtImage(
            res = if (pressed) R.drawable.ui_promotion_slot_selected else R.drawable.ui_promotion_slot_normal,
            maxEdge = ArtSizes.ICON,
            trim = true,
            modifier = Modifier.fillMaxSize(),
        )
        ArtImage(
            res = pieceImageRes(piece),
            maxEdge = ArtSizes.PIECE,
            trim = true,
            contentDescription = glyphOf(piece),
            modifier = Modifier.fillMaxSize().padding(12.dp),
        )
    }
}

// Dùng List chứ không IntArray: mảng nguyên thủy không có mapNotNull.
private val PROMOTION_ORDER = listOf(Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT)
