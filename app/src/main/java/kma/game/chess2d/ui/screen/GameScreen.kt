package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.GameViewModel
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog

/** Màn hình chơi 2 người trên cùng máy — mốc 1 của MVP. */
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = statusLabel(state), style = MaterialTheme.typography.titleLarge)

        ChessBoard(
            pieces = state.pieces,
            selectedSquare = state.selectedSquare,
            legalTargets = state.legalTargets,
            lastMoveFrom = state.lastMoveFrom,
            lastMoveTo = state.lastMoveTo,
            checkedKingSquare = state.checkedKingSquare,
            onSquareTap = viewModel::onSquareTap,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = viewModel::undo, enabled = state.canUndo) {
                Text(stringResource(R.string.action_undo))
            }
            Button(onClick = viewModel::newGame) {
                Text(stringResource(R.string.action_new_game))
            }
        }
    }

    state.pendingPromotion?.let { pending ->
        PromotionDialog(
            pending = pending,
            onChosen = viewModel::onPromotionChosen,
            onDismiss = viewModel::onPromotionDismissed,
        )
    }
}

/**
 * Nhãn trạng thái.
 *
 * Lưu ý khi hết nước: bên đến lượt chính là bên <b>thua</b>, nên phải đảo màu lại
 * trước khi hiển thị ai thắng.
 */
@Composable
private fun statusLabel(state: GameUiState): String {
    val sideToMove = stringResource(if (state.whiteToMove) R.string.side_white else R.string.side_black)
    val otherSide = stringResource(if (state.whiteToMove) R.string.side_black else R.string.side_white)
    return when (state.status) {
        GameStatus.ONGOING -> stringResource(R.string.status_turn, sideToMove)
        GameStatus.CHECK -> stringResource(R.string.status_check, sideToMove)
        GameStatus.CHECKMATE -> stringResource(R.string.status_checkmate, otherSide)
        GameStatus.STALEMATE -> stringResource(R.string.status_stalemate)
        GameStatus.DRAW_FIFTY_MOVES -> stringResource(R.string.status_draw_fifty)
    }
}
