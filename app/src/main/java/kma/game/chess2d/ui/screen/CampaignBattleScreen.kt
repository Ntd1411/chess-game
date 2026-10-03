package kma.game.chess2d.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.campaign.CampaignViewModel
import kma.game.chess2d.campaign.Floor
import kma.game.chess2d.campaign.FloorGoal
import kma.game.chess2d.campaign.FloorResult
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.game.PauseItem
import kma.game.chess2d.game.PauseMenu
import kma.game.chess2d.puzzle.Puzzle
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.board.PromotionDialog
import kotlinx.coroutines.delay

/**
 * Ván đấu của một tầng trong Tháp Cờ.
 *
 * Người chơi cầm Trắng. Khi có kết quả, màn chờ một nhịp ngắn để người chơi kịp thấy nước
 * cuối cùng (nước chiếu hết) rồi mới báo [onWon] hoặc [onLost] cho luồng chiến dịch.
 *
 * @param attempt lượt chơi của tầng này. Tăng lên để mở ván mới (Thử lại, hoặc vào lại
 *        tầng); giữ nguyên thì xoay máy vẫn giữ ván đang chơi.
 * @param onRestart chơi lại tầng này từ đầu.
 * @param onExit rời ván về bản đồ, không thắng không thua.
 */
@Composable
fun CampaignBattleRoute(
    floor: Floor,
    attempt: Int,
    puzzles: List<Puzzle>,
    playerName: String,
    onWon: () -> Unit,
    onLost: (LossReason) -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CampaignViewModel = viewModel(),
) {
    // Dựng ván ngay trong lúc vẽ (không đợi LaunchedEffect) để khung hình đầu đã là ván mới,
    // không thấy nhầm kết quả của tầng trước. Gọi lại với cùng khóa thì không làm gì.
    remember(floor.number, attempt) { viewModel.start(floor, attempt, puzzles) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(viewModel, floor.number, attempt) {
        viewModel.attach()
        onDispose { viewModel.detach() }
    }

    LaunchedEffect(state.result, state.floorNumber, floor.number, attempt) {
        if (state.floorNumber != floor.number) return@LaunchedEffect
        when (state.result) {
            FloorResult.ONGOING -> Unit
            FloorResult.WON -> {
                delay(RESULT_PAUSE_MILLIS)
                onWon()
            }

            FloorResult.LOST -> {
                delay(RESULT_PAUSE_MILLIS)
                onLost(state.lossReason ?: LossReason.CHECKMATE)
            }
        }
    }

    // Bộ màu bàn và bộ quân đọc thẳng từ cài đặt, giống ván đấu thường.
    val context = LocalContext.current
    val settingsStore = remember(context) { SettingsStore(context.applicationContext) }
    val appearance by settingsStore.settings.collectAsStateWithLifecycle(initialValue = AppSettings())

    MatchSounds(state.game.soundCue)

    // Pause Menu mở/đóng là việc của màn hình; xoay máy không được làm nó tự đóng.
    var showPause by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    // Back trong ván mở Pause thay vì thoát ngay (tránh lỡ tay rời trận). Đăng ký sau handler của
    // TowerRoute nên được ưu tiên; khi Pause đang mở, Dialog tự xử lý Back thành "Tiếp tục".
    BackHandler(enabled = !showPause) { showPause = true }

    val goal = floor.goal
    val headline = when (goal) {
        is FloorGoal.DefeatAi -> stringResource(R.string.campaign_goal_defeat)
        is FloorGoal.SolvePuzzle -> if (state.mistakes > 0) {
            stringResource(R.string.campaign_goal_puzzle_mistakes, state.mistakes)
        } else {
            stringResource(R.string.campaign_goal_puzzle)
        }

        is FloorGoal.SurviveMoves ->
            stringResource(R.string.campaign_goal_survive, state.playerMoves.coerceAtMost(goal.moves), goal.moves)

        FloorGoal.Claim -> ""
    }
    val isPuzzle = goal is FloorGoal.SolvePuzzle

    MatchScaffold(
        opponent = MatchPlayer(
            name = if (isPuzzle) floor.title else stringResource(R.string.match_computer),
            subtitle = when {
                state.game.aiThinking -> stringResource(R.string.match_thinking)
                isPuzzle -> stringResource(R.string.tower_kind_puzzle)
                else -> stringResource(labelOf(state.game.difficulty))
            },
            active = !state.game.whiteToMove,
        ),
        you = MatchPlayer(
            name = playerName.ifBlank { stringResource(R.string.match_player_one) },
            subtitle = stringResource(R.string.match_side, stringResource(R.string.side_white)),
            active = state.game.whiteToMove,
        ),
        actions = listOf(
            MatchAction(
                icon = Icons.Filled.Refresh,
                label = stringResource(R.string.campaign_action_restart),
                onClick = onRestart,
            ),
            MatchAction(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.campaign_action_map),
                onClick = onExit,
            ),
            MatchAction(
                icon = Icons.Filled.Menu,
                label = stringResource(R.string.action_menu),
                onClick = { showPause = true },
            ),
        ),
        headline = headline,
        modifier = modifier,
        belowBoard = if (isPuzzle) {
            null
        } else {
            {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CapturedRow(
                        captured = state.game.takenFromBlack,
                        advantage = state.game.materialBalance,
                    )
                    CapturedRow(
                        captured = state.game.takenFromWhite,
                        advantage = -state.game.materialBalance,
                    )
                }
            }
        },
    ) {
        ChessBoard(
            pieces = state.game.pieces,
            selectedSquare = state.game.selectedSquare,
            legalTargets = if (appearance.showLegalMoves) state.game.legalTargets else emptySet(),
            lastMoveFrom = state.game.lastMoveFrom,
            lastMoveTo = state.game.lastMoveTo,
            checkedKingSquare = state.game.checkedKingSquare,
            onSquareTap = viewModel::onSquareTap,
            flipped = false,
            palette = appearance.boardPalette,
            pieceTheme = appearance.pieceTheme,
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (showPause) {
        PauseMenuOverlay(
            items = PauseMenu.itemsForCampaign(),
            onItem = { item ->
                showPause = false
                when (item) {
                    PauseItem.RESUME -> Unit
                    PauseItem.RESTART -> onRestart()
                    PauseItem.SETTINGS -> showSettings = true
                    PauseItem.LEAVE -> onExit()
                }
            },
        )
    }

    if (showSettings) {
        SettingsPanel(onDismiss = { showSettings = false })
    }

    state.game.pendingPromotion?.let { pending ->
        PromotionDialog(
            pending = pending,
            onChosen = viewModel::onPromotionChosen,
            onDismiss = viewModel::onPromotionDismissed,
        )
    }
}

/** Dừng một nhịp trước khi chuyển sang màn kết quả để người chơi thấy nước đi cuối. */
private const val RESULT_PAUSE_MILLIS = 1_000L
