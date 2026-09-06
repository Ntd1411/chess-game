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
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.GameViewModel
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog

/**
 * Màn hình chơi: hai người trên cùng máy hoặc đấu máy ở ba cấp độ.
 *
 * @param onOpenLan mở nhánh chơi qua LAN. LAN không phải một giá trị của [GameMode] mà
 *        là một nhánh riêng, vì nó có ViewModel và vòng đời riêng hẳn.
 */
@Composable
fun GameScreen(
    onOpenLan: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = statusLabel(state), style = MaterialTheme.typography.titleLarge)

        // Chọn chế độ. Đổi chế độ giữa ván được phép: ván đang chơi được giữ nguyên,
        // máy chỉ nhắc lượt từ thế cờ hiện tại.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ChoiceButton(
                label = stringResource(R.string.mode_two_players),
                selected = state.mode == GameMode.TWO_PLAYERS,
                onClick = { viewModel.setMode(GameMode.TWO_PLAYERS) },
            )
            ChoiceButton(
                label = stringResource(R.string.mode_vs_computer),
                selected = state.mode == GameMode.VS_COMPUTER,
                onClick = { viewModel.setMode(GameMode.VS_COMPUTER) },
            )
            // Không dùng ChoiceButton: đây không phải một lựa chọn của ván hiện tại mà là
            // cửa đi sang một màn hình khác.
            OutlinedButton(onClick = onOpenLan) {
                Text(stringResource(R.string.mode_lan))
            }
        }

        if (state.mode == GameMode.VS_COMPUTER) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceButton(
                    label = stringResource(R.string.difficulty_easy),
                    selected = state.difficulty == Difficulty.EASY,
                    onClick = { viewModel.setDifficulty(Difficulty.EASY) },
                )
                ChoiceButton(
                    label = stringResource(R.string.difficulty_medium),
                    selected = state.difficulty == Difficulty.MEDIUM,
                    onClick = { viewModel.setDifficulty(Difficulty.MEDIUM) },
                )
                ChoiceButton(
                    label = stringResource(R.string.difficulty_hard),
                    selected = state.difficulty == Difficulty.HARD,
                    onClick = { viewModel.setDifficulty(Difficulty.HARD) },
                )
            }
        }

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
            OutlinedButton(
                onClick = viewModel::undo,
                enabled = state.canUndo && !state.aiThinking,
            ) {
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

/** Nút chọn một trong nhiều: tô đậy là đang chọn. */
@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick) { Text(label) }
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
        GameStatus.DRAW_REPETITION -> stringResource(R.string.status_draw_repetition)
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> stringResource(R.string.status_draw_material)
    }
}
