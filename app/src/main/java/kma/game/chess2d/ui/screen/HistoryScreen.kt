package kma.game.chess2d.ui.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Pgn
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.PieceOnBoard
import kma.game.chess2d.history.*
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.theme.GothicColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember(context) { MatchHistoryDatabase.get(context.applicationContext).matches() }
    val records by dao.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val stats by dao.observeStats().collectAsStateWithLifecycle(initialValue = MatchStats())
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    // Keep the snapshot while reviewing, even if another screen deletes the record.
    var reviewing by remember { mutableStateOf<MatchRecord?>(null) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var deleteAll by rememberSaveable { mutableStateOf(false) }
    val shown = reviewing
    if (shown != null) {
        MatchReview(shown, { reviewing = null }, modifier)
        return
    }
    HistorySurface(modifier) {
        SimpleSetupHeader(stringResource(R.string.history_title), stringResource(R.string.action_back), onBack, titleSize = 18.sp)
        Text(stringResource(R.string.history_stats, stats.total, stats.wins, stats.draws, stats.losses),
            color = GothicColors.Muted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 12.dp))
        if (records.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.history_empty), color = GothicColors.Parchment)
            }
        } else {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(records, key = { it.id }) { record ->
                    MatchRow(record, { reviewing = record }, { exportPgn(context, record) }, { deleteId = record.id })
                }
            }
            SimpleSetupButton(stringResource(R.string.history_delete_all), { deleteAll = true }, Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(10.dp))
        SimpleSetupButton(stringResource(R.string.history_back), onBack, Modifier.fillMaxWidth())
    }
    if (deleteId != null || deleteAll) {
        DrawnConfirmDialog(
            title = stringResource(R.string.history_delete_title),
            text = stringResource(if (deleteAll) R.string.history_delete_all_confirm else R.string.history_delete_match_confirm),
            confirmLabel = stringResource(R.string.history_delete),
            onConfirm = {
                val id = deleteId
                val all = deleteAll
                deleteId = null
                deleteAll = false
                scope.launch { withContext(Dispatchers.IO) { if (all) dao.deleteAll() else if (id != null) dao.deleteById(id) } }
            },
            onDismiss = { deleteId = null; deleteAll = false },
        )
    }
}

@Composable
private fun HistorySurface(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.bg_history, dim = 0.6f)
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().weight(1f), content = content)
        }
    }
}

@Composable
private fun MatchRow(record: MatchRecord, onReview: () -> Unit, onExport: () -> Unit, onDelete: () -> Unit) {
    SimpleSetupCard(Modifier.clickable(role = Role.Button, onClick = onReview)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(resultLabel(record.result)), color = if (record.result == MatchResult.LOSS) GothicColors.Alert else GothicColors.Gold,
                fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(formatPlayedAt(record.playedAtMillis), color = GothicColors.Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
        }
        Text(stringResource(modeLabel(record.mode)) + " · " + stringResource(R.string.history_moves_count, record.uciMoves.size),
            color = GothicColors.Parchment, fontSize = 14.sp)
        if (record.opponent.isNotBlank()) Text(record.opponent, color = GothicColors.Muted, fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SimpleSetupButton(stringResource(R.string.history_review), onReview, Modifier.weight(1f), maxLines = 2)
            SimpleSetupButton(stringResource(R.string.history_export_pgn), onExport, Modifier.weight(1f), maxLines = 2)
            SimpleSetupButton(stringResource(R.string.history_delete), onDelete, Modifier.weight(1f), maxLines = 2)
        }
    }
}

@Composable
private fun MatchReview(record: MatchRecord, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val frames = remember(record.id) { framesOf(record) }
    var ply by rememberSaveable(record.id) { mutableStateOf(frames.lastIndex) }
    val position = ply.coerceIn(0, frames.lastIndex)
    BackHandler(onBack = onBack)
    HistorySurface(modifier) {
        SimpleSetupHeader(stringResource(R.string.history_review_position, position, frames.lastIndex),
            stringResource(R.string.history_back), onBack, titleSize = 16.sp)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(minOf(maxWidth, maxHeight)).border(1.dp, GothicColors.Gold.copy(alpha = 0.5f)).padding(4.dp)) {
                ChessBoard(pieces = frames[position], selectedSquare = Squares.NONE, legalTargets = emptySet(),
                    lastMoveFrom = Squares.NONE, lastMoveTo = Squares.NONE, checkedKingSquare = Squares.NONE,
                    onSquareTap = {}, palette = settings.boardPalette, pieceTheme = settings.pieceTheme, modifier = Modifier.fillMaxSize())
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleSetupButton(stringResource(R.string.history_prev), { ply = position - 1 }, Modifier.weight(1f), enabled = position > 0, maxLines = 2)
            SimpleSetupButton(stringResource(R.string.history_next), { ply = position + 1 }, Modifier.weight(1f), enabled = position < frames.lastIndex, maxLines = 2)
        }
        Spacer(Modifier.height(10.dp))
        SimpleSetupButton(stringResource(R.string.history_back), onBack, Modifier.fillMaxWidth())
    }
}

internal fun framesOf(record: MatchRecord): List<List<PieceOnBoard>> {
    val board = Engine.fromFen(record.startFen)
    val frames = mutableListOf(piecesOf(board))
    for (uci in record.uciMoves) {
        val move = Engine.legalMoves(board).firstOrNull { it.toUci() == uci } ?: break
        board.makeMove(move)
        frames.add(piecesOf(board))
    }
    return frames
}

/** Ở màn xem lại, số ô làm luôn id quân: nhảy thế cờ không phải một nước đi để cần hoạt ảnh. */
private fun piecesOf(board: Board): List<PieceOnBoard> =
    (0 until Squares.COUNT).mapNotNull { square ->
        val piece = board.pieceAt(square)
        if (piece == Piece.NONE) null else PieceOnBoard(square, piece, square)
    }

/**
 * Xuất một ván đã lưu thành PGN rồi đẩy sang ứng dụng khác bằng [Intent.ACTION_SEND].
 *
 * Chia sẻ chuỗi văn bản chứ không tự ghi file: không phải xin quyền lưu trữ, và người
 * chơi tự chọn lưu vào đâu hay gửi cho ai.
 *
 * [Pgn.export] tính lại SAN bằng engine nên dãy nước hỏng trong DB sẽ ném lỗi; bắt lại
 * để báo một câu thay vì làm sập app.
 */
private fun exportPgn(context: Context, record: MatchRecord) {
    val pgn = runCatching {
        Pgn.export(
            Pgn.Game(
                event = "Chess 2D",
                site = "Android",
                date = SimpleDateFormat("yyyy.MM.dd", Locale.US).format(Date(record.playedAtMillis)),
                result = pgnResultOf(record.result),
                startFen = record.startFen,
                uciMoves = record.uciMoves,
            ),
        )
    }.getOrNull()

    if (pgn == null) {
        Toast.makeText(context, R.string.history_export_failed, Toast.LENGTH_SHORT).show()
        return
    }

    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, pgn)
    }
    context.startActivity(
        Intent.createChooser(send, context.getString(R.string.history_export_chooser)),
    )
}

/** Kết quả theo ký hiệu PGN, luôn nhìn từ phía Bên Trắng như chuẩn yêu cầu. */
internal fun pgnResultOf(result: String): String = when (result) {
    MatchResult.WIN -> "1-0"
    MatchResult.LOSS -> "0-1"
    else -> "1/2-1/2"
}

/** Ngày giờ ván đã đánh, theo định dạng ngắn kiểu Việt Nam. */
private fun formatPlayedAt(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

/** Huy hiệu kết quả: thắng `badge_victory`, thua `badge_defeat`, hòa `badge_complete`. */
private fun resultBadge(result: String): Int = when (result) {
    MatchResult.WIN -> R.drawable.badge_victory
    MatchResult.LOSS -> R.drawable.badge_defeat
    else -> R.drawable.badge_complete
}

/** Nhãn cho kết quả lưu dưới dạng chuỗi. */
internal fun resultLabel(result: String): Int = when (result) {
    MatchResult.WIN -> R.string.history_result_win
    MatchResult.LOSS -> R.string.history_result_loss
    else -> R.string.history_result_draw
}

/** Nhãn cho chế độ lưu dưới dạng chuỗi; chuỗi lạ thì coi như ván LAN. */
internal fun modeLabel(mode: String): Int = when (mode) {
    GameMode.TWO_PLAYERS.name -> R.string.history_mode_two_players
    GameMode.VS_COMPUTER.name -> R.string.history_mode_vs_computer
    else -> R.string.history_mode_lan
}

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// Preview support
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

private object HistoryPreviewData {
    val winRecord = MatchRecord(
        id = 1,
        playedAtMillis = 1728849600000L,
        mode = GameMode.VS_COMPUTER.name,
        startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        moves = "e2e4 e7e5 g1f3 b8c6 f1c4 g8f6 b1c3 f8c5",
        result = MatchResult.WIN,
    )

    val lossRecord = MatchRecord(
        id = 2,
        playedAtMillis = 1728763200000L,
        mode = GameMode.TWO_PLAYERS.name,
        startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        moves = "e2e4 e7e5 g1f3 b8c6",
        result = MatchResult.LOSS,
    )

    val drawRecord = MatchRecord(
        id = 3,
        playedAtMillis = 1728676800000L,
        mode = "LAN",
        startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        moves = "e2e4 e7e5 g1f3 g8f6",
        result = MatchResult.DRAW,
    )
}

@Preview(name = "Match Row - Win", showBackground = true)
@Composable
private fun MatchRowPreview_Win() {
    MatchRow(
        record = HistoryPreviewData.winRecord,
        onReview = {},
        onExport = {},
        onDelete = {},
    )
}

@Preview(name = "Match Row - Loss", showBackground = true)
@Composable
private fun MatchRowPreview_Loss() {
    MatchRow(
        record = HistoryPreviewData.lossRecord,
        onReview = {},
        onExport = {},
        onDelete = {},
    )
}

@Preview(name = "Match Row - Draw", showBackground = true)
@Composable
private fun MatchRowPreview_Draw() {
    MatchRow(
        record = HistoryPreviewData.drawRecord,
        onReview = {},
        onExport = {},
        onDelete = {},
    )
}
