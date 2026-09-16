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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.lan.LanNotice
import kma.game.chess2d.lan.LanNoticeKind
import kma.game.chess2d.lan.LanUiState
import kma.game.chess2d.net.ClockTimes
import kma.game.chess2d.net.LanOutcome
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog
import kotlinx.coroutines.delay

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

    // Bàn cờ LAN dùng chung lựa chọn giao diện với bàn cờ offline: đổi bộ màu ở menu
    // thì cả hai chế độ đều đổi theo.
    val context = LocalContext.current
    val appearanceStore = remember(context) { SettingsStore(context.applicationContext) }
    val appearance by appearanceStore.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )

    // Đồng hồ hiển thị được nội suy ở đây, vì host chỉ gửi con số mới mỗi nhịp ping
    // (2 giây) — không nội suy thì người chơi thấy đồng hồ đứng im rồi nhảy từng cục.
    val clock = rememberTickingClock(state)

    // Bàn cờ LAN dùng chung đường phát tiếng với bàn cờ offline; cue được suy ra từ số
    // nước đã đi vì ván LAN không đi qua `GameViewModel`.
    MatchSounds(rememberMatchCue(state.ply, state.status, state.finished))

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
            subtitle = opponentSubtitle(state, clock),
            active = state.connected && !state.finished && !state.yourTurn,
        ),
        you = MatchPlayer(
            name = state.lobby.localName,
            subtitle = youSubtitle(state, clock),
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
            palette = appearance.boardPalette,
            pieceTheme = appearance.pieceTheme,
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
        state.outcome == LanOutcome.TIMEOUT -> {
            val loser = stringResource(
                if (state.flaggedWhite == true) R.string.side_white else R.string.side_black,
            )
            stringResource(R.string.lan_outcome_timeout, loser)
        }

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

/**
 * Phụ đề của đối thủ: thời gian còn lại nếu phòng có bấm giờ, không thì độ trễ.
 *
 * Đồng hồ được ưu tiên hơn độ trễ: khi đang bấm giờ thì thứ người chơi cần biết là
 * đối thủ còn bao nhiêu phút, không phải mạng nhanh hay chậm.
 */
@Composable
private fun opponentSubtitle(state: LanUiState, clock: ClockTimes?): String = when {
    !state.connected -> ""
    state.timeControl.limited -> clockLabel(state, clock, forYou = false)
    state.latencyMillis >= 0 -> stringResource(R.string.lan_latency, state.latencyMillis)
    else -> ""
}

/** Phụ đề của mình: đang cầm quân bên nào, kèm thời gian còn lại khi có bấm giờ. */
@Composable
private fun youSubtitle(state: LanUiState, clock: ClockTimes?): String {
    val side = stringResource(if (state.youPlayWhite) R.string.side_white else R.string.side_black)
    val mine = stringResource(R.string.match_side, side)
    if (!state.timeControl.limited) return mine
    return "$mine · " + clockLabel(state, clock, forYou = true)
}

/**
 * Đồng hồ để hiển thị: con số mới nhất của host, trừ dần theo thời gian thực cho
 * bên đang đến lượt.
 *
 * Đây chỉ là phép nội suy để mắt người thấy giây trôi; mỗi lần host gửi số mới thì
 * mốc đếm được đặt lại, nên hai máy không thể trôi xa nhau. Việc phân định hết giờ
 * vẫn hoàn toàn thuộc về đồng hồ duy nhất của host (mục 7.2).
 */
@Composable
private fun rememberTickingClock(state: LanUiState): ClockTimes? {
    val times = state.clock ?: return null
    if (!state.timeControl.limited) return times
    val running = state.connected && !state.finished
    // Mốc đếm: đặt lại mỗi khi host gửi con số mới hoặc khi lượt đi đổi bên.
    val anchor = remember(times, state.whiteToMove, running) { System.currentTimeMillis() }
    var now by remember(times, state.whiteToMove, running) { mutableStateOf(anchor) }
    LaunchedEffect(times, state.whiteToMove, running) {
        if (!running) return@LaunchedEffect
        while (true) {
            delay(TICK_MILLIS)
            now = System.currentTimeMillis()
        }
    }
    if (!running) return times
    val elapsed = (now - anchor).coerceAtLeast(0L)
    return if (state.whiteToMove) {
        times.copy(whiteMillis = (times.whiteMillis - elapsed).coerceAtLeast(0L))
    } else {
        times.copy(blackMillis = (times.blackMillis - elapsed).coerceAtLeast(0L))
    }
}

/** Nhịp vẽ lại đồng hồ. 200 ms đủ mượt mà vẫn xa ngưỡng làm nóng máy. */
private const val TICK_MILLIS: Long = 200

/**
 * Con số thời gian của một bên, lấy từ đồng hồ đã nội suy ở [rememberTickingClock].
 */
@Composable
private fun clockLabel(state: LanUiState, clock: ClockTimes?, forYou: Boolean): String {
    val times = clock ?: state.clock
    val white = if (forYou) state.youPlayWhite else !state.youPlayWhite
    val millis = when {
        times != null -> if (white) times.whiteMillis else times.blackMillis
        else -> state.timeControl.initialMillis
    }
    return stringResource(R.string.lan_clock, formatClock(millis))
}

/** Định dạng `m:ss`. Làm tròn lên để không hiện 0:00 khi vẫn còn nửa giây. */
private fun formatClock(millis: Long): String {
    val safe = millis.coerceAtLeast(0L)
    val totalSeconds = (safe + 999L) / 1_000L
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
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
