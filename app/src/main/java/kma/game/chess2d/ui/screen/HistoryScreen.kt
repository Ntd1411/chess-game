package kma.game.chess2d.ui.screen

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kma.game.chess2d.R
import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Pgn
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.PieceOnBoard
import kma.game.chess2d.history.MatchHistoryDatabase
import kma.game.chess2d.history.MatchRecord
import kma.game.chess2d.history.MatchResult
import kma.game.chess2d.history.MatchStats
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.ChessBoard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Lịch sử ván đấu (mục 7.3): danh sách ván đã xong, thống kê, xem lại và xoá.
 *
 * Đọc thẳng từ DAO thay vì qua một ViewModel riêng: màn này chỉ hiện dữ liệu đọc
 * được từ hai [kotlinx.coroutines.flow.Flow] của Room, không giữ trạng thái nào
 * phức tạp để đáng phải sống qua xoay máy.
 *
 * Ván được xem lại bằng cách đi lại dãy nước UCI trên một bàn cờ mới, đúng như
 * cách lưu đã chọn: không lưu thế cờ nên không bao giờ có thế cờ sai luật trong DB.
 */
@Composable
fun HistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val dao = remember(context) { MatchHistoryDatabase.get(context.applicationContext).matches() }
    val records by dao.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    val stats by dao.observeStats().collectAsStateWithLifecycle(initialValue = MatchStats())
    val scope = rememberCoroutineScope()

    // Ván đang xem lại. Giữ nguyên bản ghi chứ không chỉ giữ id: để xoá ván khác trong
    // lúc đang xem cũng không làm màn xem lại trống ruỗng.
    var reviewing by remember { mutableStateOf<MatchRecord?>(null) }

    val shown = reviewing
    if (shown != null) {
        MatchReview(record = shown, onBack = { reviewing = null }, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(
                R.string.history_stats,
                stats.total,
                stats.wins,
                stats.draws,
                stats.losses,
            ),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.history_back)) }
            if (records.isNotEmpty()) {
                OutlinedButton(
                    // Ghi đĩa là việc treo nên đẩy sang [Dispatchers.IO], không chặn main thread.
                    onClick = { scope.launch { withContext(Dispatchers.IO) { dao.deleteAll() } } },
                ) { Text(stringResource(R.string.history_delete_all)) }
            }
        }

        if (records.isEmpty()) {
            Text(
                text = stringResource(R.string.history_empty),
                style = MaterialTheme.typography.bodyMedium,
            )
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Khoá theo id để xoá một ván không làm vẽ lại cả danh sách.
            items(items = records, key = { it.id }) { record ->
                MatchRow(
                    record = record,
                    onReview = { reviewing = record },
                    onExport = { exportPgn(context, record) },
                    onDelete = {
                        scope.launch {
                            withContext(Dispatchers.IO) { dao.deleteById(record.id) }
                        }
                    },
                )
            }
        }
    }
}

/** Một dòng ván đã lưu: ngày, chế độ, kết quả, số nước và ba nút hành động. */
@Composable
private fun MatchRow(
    record: MatchRecord,
    onReview: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${formatPlayedAt(record.playedAtMillis)} · " +
                    stringResource(modeLabel(record.mode)),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(resultLabel(record.result)) + " · " +
                    stringResource(R.string.history_moves_count, record.uciMoves.size),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onReview) { Text(stringResource(R.string.history_review)) }
                TextButton(onClick = onExport) { Text(stringResource(R.string.history_export_pgn)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.history_delete)) }
            }
        }
    }
}

/**
 * Xem lại một ván đã lưu, đi từng nước một.
 *
 * Mọi nước đọc từ DB đều phải qua engine duyệt: gặp nước không hợp lệ thì dừng ở đó
 * thay vì cứ đặt quân theo dữ liệu, vì DB có thể là của một bản app cũ.
 */
@Composable
private fun MatchReview(record: MatchRecord, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    // Đi lại toàn bộ ván một lần rồi giữ lại ảnh chụp từng thế cờ: rẻ hơn đi lại mỗi lần
    // bấm, và một ván cờ thì cùng lắm vài trăm thế.
    val frames = remember(record.id) { framesOf(record) }
    var ply by remember(record.id) { mutableStateOf(frames.lastIndex) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.history_review_position, ply, frames.lastIndex),
            style = MaterialTheme.typography.titleMedium,
        )

        ChessBoard(
            pieces = frames[ply],
            selectedSquare = Squares.NONE,
            legalTargets = emptySet(),
            lastMoveFrom = Squares.NONE,
            lastMoveTo = Squares.NONE,
            checkedKingSquare = Squares.NONE,
            // Xem lại thì không đi được: bỏ qua mọi cú chạm.
            onSquareTap = {},
            palette = settings.boardPalette,
            pieceTheme = settings.pieceTheme,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = { ply = (ply - 1).coerceAtLeast(0) },
                enabled = ply > 0,
            ) { Text(stringResource(R.string.history_prev)) }
            OutlinedButton(
                onClick = { ply = (ply + 1).coerceAtMost(frames.lastIndex) },
                enabled = ply < frames.lastIndex,
            ) { Text(stringResource(R.string.history_next)) }
            OutlinedButton(onClick = onBack) { Text(stringResource(R.string.history_back)) }
        }
    }
}

/**
 * Dựng dãy thế cờ của một ván đã lưu: phần tử 0 là thế đầu, phần tử n là thế sau
 * nước thứ n.
 */
private fun framesOf(record: MatchRecord): List<List<PieceOnBoard>> {
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
private fun pgnResultOf(result: String): String = when (result) {
    MatchResult.WIN -> "1-0"
    MatchResult.LOSS -> "0-1"
    else -> "1/2-1/2"
}

/** Ngày giờ ván đã đánh, theo định dạng ngắn kiểu Việt Nam. */
private fun formatPlayedAt(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(millis))

/** Nhãn cho kết quả lưu dưới dạng chuỗi. */
private fun resultLabel(result: String): Int = when (result) {
    MatchResult.WIN -> R.string.history_result_win
    MatchResult.LOSS -> R.string.history_result_loss
    else -> R.string.history_result_draw
}

/** Nhãn cho chế độ lưu dưới dạng chuỗi; chuỗi lạ thì coi như ván LAN. */
private fun modeLabel(mode: String): Int = when (mode) {
    GameMode.TWO_PLAYERS.name -> R.string.history_mode_two_players
    GameMode.VS_COMPUTER.name -> R.string.history_mode_vs_computer
    else -> R.string.history_mode_lan
}
