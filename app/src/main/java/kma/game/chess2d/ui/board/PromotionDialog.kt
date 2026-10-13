package kma.game.chess2d.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GothicColors

internal fun orderedPromotionMoves(options: List<Move>): List<Move> =
    listOf(Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT).mapNotNull { type -> options.firstOrNull { it.isPromotion && it.promotionType == type } }

@Composable
fun PromotionDialog(pending: PendingPromotion, onChosen: (Move) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, containerColor = GothicColors.Ink,
        title = { Text(stringResource(R.string.promotion_title), color = GothicColors.Gold) },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                orderedPromotionMoves(pending.options).forEach { move ->
                    val piece = Piece.of(move.promotionType, pending.white)
                    val shape = RoundedCornerShape(12.dp)
                    Box(Modifier.weight(1f).aspectRatio(1f).clip(shape).background(GothicColors.Gold.copy(alpha = 0.08f))
                        .border(1.dp, GothicColors.Gold.copy(alpha = 0.5f), shape)
                        .clickable(role = Role.Button, onClick = { onChosen(move) })) {
                        ArtImage(pieceImageRes(piece), maxEdge = ArtSizes.PIECE, trim = true, contentDescription = glyphOf(piece),
                            modifier = Modifier.fillMaxSize().padding(8.dp))
                    }
                }
            }
        }, confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = GothicColors.Gold) } })
}
