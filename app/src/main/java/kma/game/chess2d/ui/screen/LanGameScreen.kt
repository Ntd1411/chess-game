package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.lan.LanNotice
import kma.game.chess2d.lan.LanNoticeKind
import kma.game.chess2d.lan.LanUiState
import kma.game.chess2d.net.LanOutcome
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog

/**
 * Bàn cờ của một phiên LAN.
 *
 * Khác <code>GameScreen</code> ở ba điểm: không có "đi lại" (một ván hai người qua
 * mạng thì đi lại là vô nghĩa nếu không có đồng thuận), có thêm đầu hàng / xin hòa /
 * đấu lại, và bàn cờ được lật khi mình cầm Đen.
 */
@Composable
fun LanGameScreen(
    state: LanUiState,
    onSquareTap: (Int) -> Unit,
    onPromotionChosen: (Move) -> Unit,
    onPromotionDismissed: () -> Unit,
    onResign: () -> Unit,
    onOfferDraw: () -> Unit,
    onRespondDraw: (Boolean) -> Unit,
    onOfferRematch: () -> Unit,
    onRespondRematch: (Boolean) -> Unit,
    onDismissNotice: () -> Unit,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = headline(state), style = MaterialTheme.typography.titleMedium)
        Text(text = subline(state), style = MaterialTheme.typography.bodyMedium)

        state.notice?.let { notice ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = noticeText(notice),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f),
                    )
                    // Nút thử lại đứng ngay cạnh lời báo lỗi chứ không xếp cùng hàng với đầu
                    // hàng / xin hòa: nó chỉ sống lúc mất kết nối, không phải một việc của ván đấu.
                    if (state.offerRetry) {
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.lan_action_retry))
                        }
                    }
                    TextButton(onClick = onDismissNotice) {
                        Text(stringResource(R.string.action_dismiss))
                    }
                }
            }
        }

        ChessBoard(
            pieces = state.board.pieces,
            selectedSquare = state.board.selectedSquare,
            legalTargets = state.board.legalTargets,
            lastMoveFrom = state.board.lastMoveFrom,
            lastMoveTo = state.board.lastMoveTo,
            checkedKingSquare = state.board.checkedKingSquare,
            onSquareTap = onSquareTap,
            flipped = state.board.flipped,
            modifier = Modifier.fillMaxWidth(),
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onResign,
                enabled = state.connected && !state.finished,
            ) { Text(stringResource(R.string.lan_action_resign)) }
            OutlinedButton(
                onClick = onOfferDraw,
                enabled = state.connected && !state.finished && !state.waitingDrawReply,
            ) { Text(stringResource(R.string.lan_action_draw)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onOfferRematch,
                enabled = state.connected && state.finished && !state.waitingRematchReply,
            ) { Text(stringResource(R.string.lan_action_rematch)) }
            // Một nút duy nhất cho cả hai vai: rời phòng là rời khỏi phòng, không phải đóng
            // phòng. Bên còn lại giữ phòng tiếp, nên phòng chỉ biến mất khi cả hai đều rời.
            OutlinedButton(onClick = onLeave) {
                Text(stringResource(R.string.lan_action_leave))
            }
        }

        if (state.waitingDrawReply) Text(stringResource(R.string.lan_waiting_draw))
        if (state.waitingRematchReply) Text(stringResource(R.string.lan_waiting_rematch))
    }

    // Đề nghị của đối thủ phải là hộp thoại chứ không phải dòng chữ: bỏ sót một lời
    // xin hòa làm đối thủ ngồi chờ vô nghĩa.
    if (state.opponentOffersDraw) {
        ConfirmDialog(
            text = stringResource(R.string.lan_draw_offer_text, state.opponentName),
            onAccept = { onRespondDraw(true) },
            onDecline = { onRespondDraw(false) },
        )
    } else if (state.opponentOffersRematch) {
        ConfirmDialog(
            text = stringResource(R.string.lan_rematch_offer_text, state.opponentName),
            onAccept = { onRespondRematch(true) },
            onDecline = { onRespondRematch(false) },
        )
    }

    state.board.pendingPromotion?.let { pending ->
        PromotionDialog(
            pending = pending,
            onChosen = onPromotionChosen,
            onDismiss = onPromotionDismissed,
        )
    }
}

@Composable
private fun ConfirmDialog(text: String, onAccept: () -> Unit, onDecline: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDecline,
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onAccept) { Text(stringResource(R.string.action_yes)) }
        },
        dismissButton = {
            TextButton(onClick = onDecline) { Text(stringResource(R.string.action_no)) }
        },
    )
}

/** Dòng trên: ai đang chơi với ai, hoặc đang chờ ai. */
@Composable
private fun headline(state: LanUiState): String = when {
    state.waitingForOpponent && state.hostPort > 0 ->
        stringResource(R.string.lan_waiting_guest, state.hostPort)

    !state.connected -> stringResource(R.string.lan_connecting)
    else -> stringResource(R.string.lan_opponent, state.opponentName)
}

/** Dòng dưới: kết quả nếu ván đã xong, nếu chưa thì lượt của ai. */
@Composable
private fun subline(state: LanUiState): String {
    val side = stringResource(if (state.youPlayWhite) R.string.side_white else R.string.side_black)
    // Bên đến lượt là bên đang bị chiếu, và khi hết nước thì chính là bên thua.
    val sideToMove = stringResource(
        if (state.whiteToMove) R.string.side_white else R.string.side_black,
    )
    val winnerSide = stringResource(
        if (state.whiteToMove) R.string.side_black else R.string.side_white,
    )
    if (!state.connected) return stringResource(R.string.lan_you_play, side)

    val outcome = when {
        state.outcome == LanOutcome.DRAW_AGREED -> stringResource(R.string.lan_outcome_draw)
        state.outcome == LanOutcome.RESIGNATION -> {
            val loser = stringResource(
                if (state.resignedByWhite == true) R.string.side_white else R.string.side_black,
            )
            stringResource(R.string.lan_outcome_resign, loser)
        }

        state.status == GameStatus.CHECKMATE -> stringResource(R.string.status_checkmate, winnerSide)
        state.status == GameStatus.STALEMATE -> stringResource(R.string.status_stalemate)
        state.status == GameStatus.DRAW_FIFTY_MOVES -> stringResource(R.string.status_draw_fifty)
        state.status == GameStatus.DRAW_REPETITION -> stringResource(R.string.status_draw_repetition)
        state.status == GameStatus.DRAW_INSUFFICIENT_MATERIAL ->
            stringResource(R.string.status_draw_material)

        else -> null
    }
    if (outcome != null) return outcome

    val turn = stringResource(
        if (state.yourTurn) R.string.lan_your_turn else R.string.lan_their_turn,
    )
    val check = if (state.status == GameStatus.CHECK) {
        "  ·  " + stringResource(R.string.status_check, sideToMove)
    } else {
        ""
    }
    val latency = if (state.latencyMillis >= 0) {
        "  ·  " + stringResource(R.string.lan_latency, state.latencyMillis)
    } else {
        ""
    }
    return stringResource(R.string.lan_you_play, side) + "  ·  " + turn + check + latency
}

@Composable
private fun noticeText(notice: LanNotice): String = when (notice.kind) {
    LanNoticeKind.VERSION_MISMATCH -> stringResource(R.string.lan_notice_version)
    LanNoticeKind.ROOM_BUSY -> stringResource(R.string.lan_notice_busy)
    LanNoticeKind.BAD_HANDSHAKE -> stringResource(R.string.lan_notice_handshake)
    LanNoticeKind.MOVE_REJECTED -> stringResource(R.string.lan_notice_move_rejected, notice.detail)
    LanNoticeKind.RESYNCED -> stringResource(R.string.lan_notice_resynced)
    LanNoticeKind.OPPONENT_LEFT -> stringResource(R.string.lan_notice_opponent_left)
    LanNoticeKind.BECAME_HOST -> stringResource(R.string.lan_notice_became_host)
    LanNoticeKind.DISCONNECTED_RETRYING -> stringResource(R.string.lan_notice_retrying)
    LanNoticeKind.DISCONNECTED_FINAL -> stringResource(R.string.lan_notice_disconnected, notice.detail)
    LanNoticeKind.CONNECT_FAILED -> stringResource(R.string.lan_notice_connect_failed)
    LanNoticeKind.BAD_ADDRESS -> stringResource(R.string.lan_notice_bad_address)
}
