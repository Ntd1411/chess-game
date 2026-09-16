package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.GameViewModel
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Ván đấu offline: hai người một máy hoặc đấu máy.
 *
 * Chế độ và cấp độ được chọn từ menu rồi truyền xuống, không còn hàng nút chọn chế
 * độ nằm ngay trên bàn cờ như trước. Vẫn để [GameViewModel] giữ nguồn sự thật vì nó
 * lưu ván vào SavedStateHandle; menu chỉ đẩy lựa chọn vào đó qua setMode/setDifficulty.
 *
 * @param resume true khi người chơi vừa chọn "Tiếp tục" ở sảnh: ván lưu trên đĩa sẽ
 *        được đi lại từ đầu thay vì mở một bàn cờ mới.
 */
@Composable
fun GameScreen(
    playerName: String,
    mode: GameMode,
    difficulty: Difficulty,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier,
    resume: Boolean = false,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showLevels by remember { mutableStateOf(false) }
    // Bộ màu bàn và bộ quân đọc thẳng từ cài đặt: đây là sở thích của người chơi, không
    // phải trạng thái của ván, nên không đi qua ViewModel.
    val appearanceContext = LocalContext.current
    val appearanceStore = remember(appearanceContext) {
        SettingsStore(appearanceContext.applicationContext)
    }
    val appearance by appearanceStore.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )
    // Lật bàn là lựa chọn của người đang ngồi xem, không phải trạng thái của ván, nên
    // giữ ở màn hình bằng rememberSaveable chứ không đẩy vào ViewModel. Dùng lại đúng cờ
    // `flipped` của bàn cờ LAN nên không phải thêm đường vẽ nào mới.
    var flipped by rememberSaveable { mutableStateOf(false) }

    // Dựng lại ván cũ trước khi đồng bộ lựa chọn: ViewModel tự chọn đúng chế độ và cấp
    // độ của ván đó, và chính nó cũng chặn việc dựng lại lần thứ hai khi xoay máy.
    LaunchedEffect(resume) { if (resume) viewModel.resumeSavedGame() }

    // Đồng bộ lựa chọn từ menu vào ViewModel. Đặt trong LaunchedEffect để việc này chỉ
    // chạy khi lựa chọn đổi, chứ không chạy lại mỗi lần vẽ lại màn hình.
    LaunchedEffect(mode) { viewModel.setMode(mode) }
    LaunchedEffect(difficulty) { viewModel.setDifficulty(difficulty) }

    MatchSounds(state.soundCue)

    val actions = buildList {
        add(
            MatchAction(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                label = stringResource(R.string.action_undo),
                enabled = state.canUndo && !state.aiThinking,
                onClick = viewModel::undo,
            ),
        )
        add(
            MatchAction(
                icon = Icons.Filled.Refresh,
                label = stringResource(R.string.action_new_game),
                onClick = viewModel::newGame,
            ),
        )
        // Nút Đổi cấp chỉ có nghĩa khi đối thủ là máy, nên ở chế độ hai người thì ẩn hẳn
        // thay vì để một nút xám không bấm được.
        if (state.mode == GameMode.VS_COMPUTER) {
            add(
                MatchAction(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.action_level),
                    enabled = !state.aiThinking,
                    onClick = { showLevels = true },
                ),
            )
        }
        add(
            MatchAction(
                icon = Icons.Filled.KeyboardArrowUp,
                label = stringResource(R.string.action_flip),
                onClick = { flipped = !flipped },
            ),
        )
        add(
            MatchAction(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.action_menu),
                onClick = onExitToMenu,
            ),
        )
    }

    MatchScaffold(
        opponent = MatchPlayer(
            name = opponentName(state),
            subtitle = opponentSubtitle(state),
            active = !state.whiteToMove,
        ),
        you = MatchPlayer(
            name = playerName.ifBlank { stringResource(R.string.match_player_one) },
            subtitle = stringResource(
                R.string.match_side,
                stringResource(R.string.side_white),
            ),
            active = state.whiteToMove,
        ),
        actions = actions,
        headline = state.reviewPly?.let {
            // Đang xem lại thì dòng trạng thái phải nói điều đó, không phải nói lượt ai:
            // bàn cờ đang không nhận đi, mà không nói thì người chơi tưởng app treo.
            stringResource(R.string.moves_reviewing, it + 1)
        } ?: statusLabel(state),
        modifier = modifier,
        belowBoard = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Trắng ăn quân Đen nên hàng đầu là những quân Đen đã mất, và điểm hơn
                // của Trắng chính là chênh lệch dương.
                CapturedRow(
                    captured = state.takenFromBlack,
                    advantage = state.materialBalance,
                )
                CapturedRow(
                    captured = state.takenFromWhite,
                    advantage = -state.materialBalance,
                )
                MoveList(
                    sanMoves = state.sanMoves,
                    reviewPly = state.reviewPly,
                    onSelectPly = viewModel::reviewAt,
                )
            }
        },
    ) {
        ChessBoard(
            pieces = state.pieces,
            selectedSquare = state.selectedSquare,
            legalTargets = state.legalTargets,
            lastMoveFrom = state.lastMoveFrom,
            lastMoveTo = state.lastMoveTo,
            checkedKingSquare = state.checkedKingSquare,
            onSquareTap = viewModel::onSquareTap,
            flipped = flipped,
            palette = appearance.boardPalette,
            pieceTheme = appearance.pieceTheme,
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (showLevels) {
        LevelDialog(
            current = state.difficulty,
            onChosen = {
                viewModel.setDifficulty(it)
                showLevels = false
            },
            onDismiss = { showLevels = false },
        )
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
 * Hộp thoại đổi cấp độ máy ngay trong ván.
 *
 * Ba mức đều là một dòng bấm được trong thân hộp thoại, không nhét vào hai ô
 * confirm/dismiss của AlertDialog — nhét như thế thì mức giữa không còn chỗ để chọn.
 */
@Composable
private fun LevelDialog(
    current: Difficulty,
    onChosen: (Difficulty) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.menu_difficulty)) },
        text = {
            Column {
                for (level in Difficulty.entries) {
                    LevelRow(
                        label = stringResource(labelOf(level)),
                        selected = level == current,
                        onClick = { onChosen(level) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

/** Một dòng cấp độ; mức đang chọn được đánh dấu bằng dấu chấm đầu dòng. */
@Composable
private fun LevelRow(label: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = if (selected) "\u2022 $label" else label)
    }
}

/** Tên hiển thị của một cấp độ. */
private fun labelOf(difficulty: Difficulty): Int = when (difficulty) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
}

/** Tên đối thủ theo chế độ: máy, hay người thứ hai ngồi cùng máy. */
@Composable
private fun opponentName(state: GameUiState): String = when (state.mode) {
    GameMode.VS_COMPUTER -> stringResource(R.string.match_computer)
    GameMode.TWO_PLAYERS -> stringResource(R.string.match_player_two)
}

/** Phụ đề của đối thủ: máy đang tính, hoặc cấp độ đang chọn, hoặc bên quân. */
@Composable
private fun opponentSubtitle(state: GameUiState): String = when {
    state.mode == GameMode.VS_COMPUTER && state.aiThinking ->
        stringResource(R.string.match_thinking)

    state.mode == GameMode.VS_COMPUTER -> stringResource(labelOf(state.difficulty))
    else -> stringResource(R.string.match_side, stringResource(R.string.side_black))
}

/** Dòng trạng thái trên cùng: kết quả nếu đã xong, chưa xong thì lượt ai / thế chiếu. */
@Composable
private fun statusLabel(state: GameUiState): String {
    val sideToMove = stringResource(
        if (state.whiteToMove) R.string.side_white else R.string.side_black,
    )
    val winnerSide = stringResource(
        if (state.whiteToMove) R.string.side_black else R.string.side_white,
    )
    return when (state.status) {
        GameStatus.CHECKMATE -> stringResource(R.string.status_checkmate, winnerSide)
        GameStatus.STALEMATE -> stringResource(R.string.status_stalemate)
        GameStatus.DRAW_FIFTY_MOVES -> stringResource(R.string.status_draw_fifty)
        GameStatus.DRAW_REPETITION -> stringResource(R.string.status_draw_repetition)
        GameStatus.DRAW_INSUFFICIENT_MATERIAL -> stringResource(R.string.status_draw_material)
        GameStatus.CHECK -> stringResource(R.string.status_check, sideToMove)
        GameStatus.ONGOING -> stringResource(R.string.status_turn, sideToMove)
    }
}
