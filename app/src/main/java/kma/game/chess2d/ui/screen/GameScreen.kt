package kma.game.chess2d.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
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
import androidx.lifecycle.compose.LifecycleResumeEffect
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.game.ClockFormat
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.GameViewModel
import kma.game.chess2d.game.LocalStart
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.game.PauseMenu
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
 * @param localStart yêu cầu mở ván hai người mới từ màn setup (tên + thời gian); `null` khi vào
 *        ván theo cách khác (đấu máy, tiếp tục ván cũ).
 */
@Composable
fun GameScreen(
    playerName: String,
    mode: GameMode,
    difficulty: Difficulty,
    onExitToMenu: () -> Unit,
    modifier: Modifier = Modifier,
    resume: Boolean = false,
    localStart: LocalStart? = null,
    viewModel: GameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val clockTimes by viewModel.clockTimes.collectAsStateWithLifecycle()
    var showLevels by remember { mutableStateOf(false) }
    // Pause Menu mở/đóng là việc của màn hình; xoay máy không được làm nó tự đóng.
    var showPause by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
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

    // Ván hai người mới từ màn setup. ViewModel nhớ token đã xử lý nên xoay máy không mở thêm ván mới.
    LaunchedEffect(localStart) {
        localStart?.let { viewModel.startLocalGame(it.setup, it.token) }
    }

    // Đồng bộ lựa chọn từ menu vào ViewModel. Đặt trong LaunchedEffect để việc này chỉ
    // chạy khi lựa chọn đổi, chứ không chạy lại mỗi lần vẽ lại màn hình.
    LaunchedEffect(mode) { viewModel.setMode(mode) }
    LaunchedEffect(difficulty) { viewModel.setDifficulty(difficulty) }

    MatchSounds(state.soundCue)

    // Đồng hồ dừng khi mở Pause/Cài đặt và khi app xuống nền hoặc rời màn, chạy lại khi quay về.
    // Thời gian người chơi đang xem menu không được tính vào giờ của bất kỳ bên nào.
    val overlayOpen = showPause || showSettings
    LifecycleResumeEffect(overlayOpen) {
        viewModel.setClockPaused(overlayOpen)
        onPauseOrDispose { viewModel.setClockPaused(true) }
    }

    // Back trong ván mở Pause thay vì thoát ngay (tránh lỡ tay rời trận). Khi Pause đang mở,
    // Dialog tự xử lý Back thành "Tiếp tục", nên handler này chỉ cần lo lúc Pause đang đóng.
    BackHandler(enabled = !showPause) { showPause = true }

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
                icon = Icons.Filled.Menu,
                label = stringResource(R.string.action_menu),
                onClick = { showPause = true },
            ),
        )
    }

    // Ván hai người có giờ: đồng hồ hiện ở hàng của từng bên. Đen ở hàng trên, Trắng ở hàng dưới.
    val blackClock = clockTimes?.blackMillis
    val whiteClock = clockTimes?.whiteMillis
    val twoPlayers = state.mode == GameMode.TWO_PLAYERS

    MatchScaffold(
        opponent = MatchPlayer(
            name = opponentName(state),
            subtitle = opponentSubtitle(state),
            active = !state.whiteToMove,
            clock = blackClock?.let { ClockFormat.mmss(it) },
            clockLow = blackClock != null && blackClock <= LOW_CLOCK_MILLIS,
        ),
        you = MatchPlayer(
            name = if (twoPlayers && state.whiteName.isNotBlank()) {
                state.whiteName
            } else {
                playerName.ifBlank { stringResource(R.string.match_player_one) }
            },
            subtitle = stringResource(
                R.string.match_side,
                stringResource(R.string.side_white),
            ),
            active = state.whiteToMove,
            clock = whiteClock?.let { ClockFormat.mmss(it) },
            clockLow = whiteClock != null && whiteClock <= LOW_CLOCK_MILLIS,
        ),
        actions = actions,
        headline = state.reviewPly?.let {
            // Đang xem lại thì dòng trạng thái phải nói điều đó, không phải nói lượt ai:
            // bàn cờ đang không nhận đi, mà không nói thì người chơi tưởng app treo.
            stringResource(R.string.moves_reviewing, it + 1)
        } ?: timeoutLabel(state) ?: statusLabel(state),
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
            legalTargets = if (appearance.showLegalMoves) state.legalTargets else emptySet(),
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

    if (showPause) {
        PauseMenuOverlay(
            items = PauseMenu.itemsFor(state.mode),
            onItem = { item ->
                showPause = false
                when (item) {
                    PauseItem.RESUME -> Unit
                    PauseItem.RESTART -> viewModel.newGame()
                    PauseItem.SETTINGS -> showSettings = true
                    PauseItem.LEAVE -> onExitToMenu()
                }
            },
        )
    }

    if (showSettings) {
        // Không truyền tên: đổi tên giữa ván không có tác dụng, tên nhập ở Sảnh.
        SettingsPanel(onDismiss = { showSettings = false })
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
internal fun labelOf(difficulty: Difficulty): Int = when (difficulty) {
    Difficulty.EASY -> R.string.difficulty_easy
    Difficulty.MEDIUM -> R.string.difficulty_medium
    Difficulty.HARD -> R.string.difficulty_hard
}

/** Còn dưới mức này (30 giây) thì đồng hồ đổi sang màu cảnh báo. */
private const val LOW_CLOCK_MILLIS = 30_000L

/** Tên đối thủ theo chế độ: máy, hay người thứ hai ngồi cùng máy (tên nhập ở màn setup nếu có). */
@Composable
private fun opponentName(state: GameUiState): String = when (state.mode) {
    GameMode.VS_COMPUTER -> stringResource(R.string.match_computer)
    GameMode.TWO_PLAYERS ->
        state.blackName.ifBlank { stringResource(R.string.match_player_two) }
}

/** Dòng trạng thái khi ván kết thúc vì hết giờ; `null` nếu ván không kết thúc theo cách đó. */
@Composable
private fun timeoutLabel(state: GameUiState): String? {
    val timeout = state.timeout ?: return null
    if (timeout.drawn) return stringResource(R.string.status_timeout_draw)
    // Bên hết giờ thua nên bên thắng là bên còn lại.
    val winnerSide = stringResource(
        if (timeout.whiteFlagged) R.string.side_black else R.string.side_white,
    )
    return stringResource(R.string.status_timeout_win, winnerSide)
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
internal fun statusLabel(state: GameUiState): String {
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
