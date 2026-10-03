package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.spectate.SpectateViewModel
import kma.game.chess2d.ui.board.ChessBoard

/**
 * Nhánh Máy vs Máy: chọn độ mạnh hai AI rồi ngồi xem chúng đấu.
 *
 * Luôn mở ở bước chọn, kể cả khi [SpectateViewModel] (sống theo Activity) còn giữ ván
 * của lần xem trước: người chơi vào từ menu là muốn một ván mới, không phải ván cũ.
 * Cờ [watching] lưu bằng `rememberSaveable` nên xoay máy vẫn ở lại ván đang xem.
 */
@Composable
fun SpectateRoute(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: SpectateViewModel = viewModel()
    var watching by rememberSaveable { mutableStateOf(false) }

    if (!watching) {
        SpectateSetupScreen(
            onStart = { white, black ->
                viewModel.newMatch(white, black)
                watching = true
            },
            onBack = onBack,
            modifier = modifier,
        )
    } else {
        SpectateMatchScreen(
            viewModel = viewModel,
            onExit = onBack,
            modifier = modifier,
        )
    }
}

/** Bước chọn cấp độ cho AI Trắng và AI Đen. */
@Composable
private fun SpectateSetupScreen(
    onStart: (Difficulty, Difficulty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var white by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    var black by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.spectate_setup_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.spectate_setup_hint),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        LevelPicker(
            title = stringResource(R.string.spectate_white_ai),
            current = white,
            onChosen = { white = it },
        )
        LevelPicker(
            title = stringResource(R.string.spectate_black_ai),
            current = black,
            onChosen = { black = it },
        )

        Button(onClick = { onStart(white, black) }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.spectate_start))
        }
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.spectate_back))
        }
    }
}

/** Một hàng ba nút cấp độ, nút đang chọn được tô đậm. */
@Composable
private fun LevelPicker(
    title: String,
    current: Difficulty,
    onChosen: (Difficulty) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (level in Difficulty.entries) {
                val label = stringResource(labelOf(level))
                if (level == current) {
                    Button(onClick = { onChosen(level) }) { Text(label) }
                } else {
                    OutlinedButton(onClick = { onChosen(level) }) { Text(label) }
                }
            }
        }
    }
}

/** Màn xem ván: bàn cờ chỉ để xem (chạm vào không làm gì), kèm hàng điều khiển. */
@Composable
private fun SpectateMatchScreen(
    viewModel: SpectateViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val game = state.game
    var flipped by rememberSaveable { mutableStateOf(false) }

    // Vòng lặp AI chạy khi màn hình đang hiện và dừng khi app xuống nền: không để hai AI
    // nghĩ ngầm (tốn pin) khi không ai xem. Ván được giữ nguyên nên quay lại là đi tiếp.
    LifecycleStartEffect(viewModel) {
        viewModel.attach()
        onStopOrDispose { viewModel.detach() }
    }

    val context = LocalContext.current
    val appearanceStore = remember(context) { SettingsStore(context.applicationContext) }
    val appearance by appearanceStore.settings.collectAsStateWithLifecycle(
        initialValue = AppSettings(),
    )

    val controls = state.controls
    val whiteName = stringResource(R.string.spectate_white_ai)
    val blackName = stringResource(R.string.spectate_black_ai)

    MatchScaffold(
        // Đen ở trên, Trắng ở dưới như bàn cờ thường; cả hai đều là máy nên không ai là "bạn".
        opponent = MatchPlayer(
            name = blackName,
            subtitle = if (game.aiThinking && !game.whiteToMove) {
                stringResource(R.string.match_thinking)
            } else {
                stringResource(labelOf(state.blackLevel))
            },
            active = !game.whiteToMove,
        ),
        you = MatchPlayer(
            name = whiteName,
            subtitle = if (game.aiThinking && game.whiteToMove) {
                stringResource(R.string.match_thinking)
            } else {
                stringResource(labelOf(state.whiteLevel))
            },
            active = game.whiteToMove,
        ),
        actions = listOf(
            MatchAction(
                icon = Icons.Filled.KeyboardArrowUp,
                label = stringResource(R.string.action_flip),
                onClick = { flipped = !flipped },
            ),
            MatchAction(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.action_menu),
                onClick = onExit,
            ),
        ),
        headline = if (state.hitMoveLimit) {
            stringResource(R.string.spectate_limit, game.sanMoves.size)
        } else {
            statusLabel(game)
        },
        modifier = modifier,
        belowBoard = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpectateControlRow(
                    paused = controls.paused,
                    autoPlay = controls.autoPlay,
                    speedFactor = controls.speed.factor,
                    finished = state.hitMoveLimit ||
                        (game.status != GameStatus.ONGOING && game.status != GameStatus.CHECK),
                    onTogglePause = viewModel::togglePause,
                    onStep = viewModel::stepOnce,
                    onToggleAuto = viewModel::toggleAutoPlay,
                    onCycleSpeed = viewModel::cycleSpeed,
                )
                CapturedRow(captured = game.takenFromBlack, advantage = game.materialBalance)
                CapturedRow(captured = game.takenFromWhite, advantage = -game.materialBalance)
                MoveList(sanMoves = game.sanMoves)
            }
        },
    ) {
        ChessBoard(
            pieces = game.pieces,
            selectedSquare = game.selectedSquare,
            legalTargets = game.legalTargets,
            lastMoveFrom = game.lastMoveFrom,
            lastMoveTo = game.lastMoveTo,
            checkedKingSquare = game.checkedKingSquare,
            onSquareTap = {},
            flipped = flipped,
            palette = appearance.boardPalette,
            pieceTheme = appearance.pieceTheme,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

/**
 * Hàng điều khiển của người xem: dừng/tiếp tục, đi 1 nước, tự động/thủ công, tốc độ.
 *
 * Nút "1 nước" chỉ có nghĩa khi máy không tự đi (thủ công hoặc đang dừng); khi máy
 * đang tự chạy thì tắt đi thay vì để bấm mà không thấy gì xảy ra. Hết ván thì mọi nút
 * điều khiển nước đi đều tắt.
 */
@Composable
private fun SpectateControlRow(
    paused: Boolean,
    autoPlay: Boolean,
    speedFactor: Int,
    finished: Boolean,
    onTogglePause: () -> Unit,
    onStep: () -> Unit,
    onToggleAuto: () -> Unit,
    onCycleSpeed: () -> Unit,
) {
    val running = autoPlay && !paused
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onTogglePause,
                enabled = autoPlay && !finished,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (paused) R.string.spectate_resume else R.string.spectate_pause,
                    ),
                )
            }
            OutlinedButton(
                onClick = onStep,
                enabled = !running && !finished,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.spectate_step)) }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = onToggleAuto,
                enabled = !finished,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    stringResource(
                        if (autoPlay) R.string.spectate_auto_on else R.string.spectate_auto_off,
                    ),
                )
            }
            OutlinedButton(
                onClick = onCycleSpeed,
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.spectate_speed, speedFactor)) }
        }
    }
}
