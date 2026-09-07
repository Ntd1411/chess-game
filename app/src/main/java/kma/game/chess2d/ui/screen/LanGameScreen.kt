package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * Bàn cờ của một phiên LAN, dựng trên [MatchScaffold] giống hệt hai chế độ offline.
 *
 * Khác chế độ offline ở ba điểm: không có Đi lại (đi lại qua mạng là vô nghĩa nếu
 * không có đồng thuận), có thêm đầu hàng / xin hoà / đấu lại / đồng bộ lại, và bàn cờ
 * được lật khi mình cầm Đen.
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
    onRequestSync: () -> Unit,
    onDismissNotice: () -> Unit,
    onRetry: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Hai hành động không lấy lại được phải hỏi lại, vì chúng đứng ngay cạnh các nút
    // bình thường trong cùng một hàng.
    var confirmResign by remember { mutableStateOf(false) }
    var confirmLeave by remember { mutableStateOf(false) }

    val actions = buildList {
        add(
            MatchAction(
                icon = Icons.Filled.Close,
                label = stringResource(R.string.lan_action_resign),
                enabled = state.connected && !state.finished,
                onClick = { confirmResign = true },
            ),
        )
        add(
            MatchAction(
                icon = Icons.Filled.ThumbUp,
                label = stringResource(R.string.lan_action_draw),
                enabled = state.connected && !state.finished && !state.waitingDrawReply,
                onClick = onOfferDraw,
            ),
        )
        add(
            MatchAction(
                icon = Icons.Filled.PlayArrow,
                label = stringResource(R.string.lan_action_rematch),
                enabled = state.connected && state.finished && !state.waitingRematchReply,
                onClick = onOfferRematch,
            ),
        )
        add(
            MatchAction(
                icon = Icons.Filled.Refresh,
                label = stringResource(R.string.lan_action_sync),
                enabled = state.connected,
                onClick = onRequestSync,
            ),
        )
        add(
            // Một nút duy nhất cho cả hai vai: rời phòng là rời khỏi phòng, không phải đóng
            // phòng. Bên còn lại giữ phòng tiếp, nên phòng chỉ mất khi cả hai đều rời.
            MatchAction(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                label = stringResource(R.string.lan_action_leave),
                onClick = { if (state.finished || !state.connected) onLeave() else confirmLeave = true },
            ),
        )
    }

    MatchScaffold(
        opponent = MatchPlayer(
            name = state.opponentName.ifEmpty { stringResource(R.string.match_waiting_opponent) },
            subtitle = opponentSubtitle(state),
            active = state.connected && !state.finished && !state.yourTurn,
        ),
        you = MatchPlayer(
            name = state.lobby.localName,
            subtitle = youSubtitle(state),
            active = state.connected && !state.finished && state.yourTurn,
        ),
        actions = actions,
        headline = headline(state),
        notice = state.notice?.let { notice ->
            {
                NoticeCard(
                    text = noticeText(notice),
                    offerRetry = state.offerRetry,
                    onRetry = onRetry,
                    onDismiss = onDismissNotice,
                )
            }
        },
        modifier = modifier,
    ) {
        ChessBoard(
            pieces = state.board.pieces,
            selectedSquare = state.board.selectedSquare,
            legalTargets = state.board.legalTargets,
            lastMoveFrom = state.board.lastMoveFrom,
            lastMoveTo = state.board.lastMoveTo,
            checkedKingSquare = state.board.checkedKingSquare,
            onSquareTap = onSquareTap,
            flipped = state.board.flipped,
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (confirmResign) {
        ConfirmDialog(
            text = stringResource(R.string.confirm_resign),
            onAccept = {
                confirmResign = false
                onResign()
            },
            onDecline = { confirmResign = false },
        )
    }

    if (confirmLeave) {
        ConfirmDialog(
            text = stringResource(R.string.confirm_leave),
            onAccept = {
                confirmLeave = false
                onLeave()
            },
            onDecline = { confirmLeave = false },
        )
    }

    // Đề nghị của đối thủ phải là hộp thoại chứ không phải dòng chữ: bỏ sót một lời
    // xin hoà làm đối thủ ngồi chờ vô nghĩa.
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

/** Ô thông báo. Nút thử lại nằm ngay cạnh lời báo lỗi, không xếp vào hàng nút của ván. */
@Composable
private fun NoticeCard(
    text: String,
    offerRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
            )
            if (offerRetry) {
                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.lan_action_retry))
                }
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_dismiss))
            }
        }
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

/**
 * Dòng trạng thái trên cùng: kết quả nếu ván đã xong, chưa xong thì là tình trạng
 * phòng hoặc thế chiếu.
 *
 * Lượt của ai **không** viết ở đây nữa — viền sáng quanh avatar đã nói điều đó.
 */
@Composable
private fun headline(state: LanUiState): String {
    val winnerSide = stringResource(
        if (state.whiteToMove) R.string.side_black else R.string.side_white,
    )
    val sideToMove = stringResource(
        if (state.whiteToMove) R.string.side_white else R.string.side_black,
    )

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

    return when {
        state.waitingForOpponent && state.hostPort > 0 ->
            stringResource(R.string.lan_waiting_guest, state.hostPort)

        !state.connected -> stringResource(R.string.lan_connecting)
        state.status == GameStatus.CHECK -> stringResource(R.string.status_check, sideToMove)
        state.waitingDrawReply -> stringResource(R.string.lan_waiting_draw)
        state.waitingRematchReply -> stringResource(R.string.lan_waiting_rematch)
        else -> stringResource(R.string.lan_opponent, state.opponentName)
    }
}

/** Phụ đề của đối thủ: độ trễ đường truyền, vì đó là thứ duy nhất thuộc về bên kia. */
@Composable
private fun opponentSubtitle(state: LanUiState): String = when {
    !state.connected -> ""
    state.latencyMillis >= 0 -> stringResource(R.string.lan_latency, state.latencyMillis)
    else -> ""
}

/** Phụ đề của mình: đang cầm quân bên nào. */
@Composable
private fun youSubtitle(state: LanUiState): String {
    val side = stringResource(if (state.youPlayWhite) R.string.side_white else R.string.side_black)
    return stringResource(R.string.match_side, side)
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
