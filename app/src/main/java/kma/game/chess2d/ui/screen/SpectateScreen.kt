package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.spectate.SpectateViewModel
import kma.game.chess2d.ui.art.BattleButton
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.theme.GothicColors

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

/** Chân dung nhân vật máy ứng với một cấp độ; dùng chung cho bước chọn và khung ván. */
private fun portraitFor(level: Difficulty): Int =
    AiCharacters.all.firstOrNull { it.difficulty == level }?.portrait ?: R.drawable.portrait_unknown_01

/**
 * Bước chọn cấp độ cho AI Trắng và AI Đen, dựng trên khung 1080x1920 như các màn phụ khác:
 * mỗi bên là một tấm `ui_panel_main` có chân dung nhân vật máy và ba nút cấp độ (nút đang chọn
 * là nút chính), dưới cùng là nút Bắt đầu và nút Quay lại.
 */
@Composable
private fun SpectateSetupScreen(
    onStart: (Difficulty, Difficulty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var white by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    var black by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }

    DesignCanvas(background = R.drawable.bg_battle_default, dim = 0.4f, modifier = modifier) {
        SubScreenHeader(title = stringResource(R.string.spectate_setup_title), onBack = onBack)
        DesignText(
            text = stringResource(R.string.spectate_setup_hint),
            size = 30,
            maxLines = 3,
            modifier = Modifier.at(90, 180, 900, 110),
        )
        LevelPanel(
            top = 320,
            title = stringResource(R.string.spectate_white_ai),
            current = white,
            onChosen = { white = it },
        )
        LevelPanel(
            top = 820,
            title = stringResource(R.string.spectate_black_ai),
            current = black,
            onChosen = { black = it },
        )
        GothicButton(
            label = stringResource(R.string.spectate_start),
            onClick = { onStart(white, black) },
            modifier = Modifier.at(150, 1400, 780, 110),
        )
        GothicButton(
            label = stringResource(R.string.spectate_back),
            onClick = onBack,
            art = ButtonArt.SECONDARY,
            modifier = Modifier.at(150, 1530, 780, 100),
        )
    }
}

/** Tấm chọn cấp độ của một bên: chân dung, tên bên và hàng ba nút cấp độ. */
@Composable
private fun LevelPanel(
    top: Int,
    title: String,
    current: Difficulty,
    onChosen: (Difficulty) -> Unit,
) {
    SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(60, top, 960, 440))
    PortraitInFrame(
        portrait = portraitFor(current),
        contentDescription = title,
        modifier = Modifier.at(110, top + 50, 220, 220),
    )
    DesignText(
        text = title,
        size = 44,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(370, top + 70, 610, 80),
    )
    DesignText(
        text = stringResource(labelOf(current)),
        size = 32,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(370, top + 160, 610, 70),
    )
    Difficulty.entries.forEachIndexed { i, level ->
        GothicButton(
            label = stringResource(labelOf(level)),
            onClick = { onChosen(level) },
            art = if (level == current) ButtonArt.PRIMARY else ButtonArt.SECONDARY,
            modifier = Modifier.at(100 + i * 300, top + 300, 280, 100),
        )
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
            piece = Piece.of(Piece.KING, false),
            active = !game.whiteToMove,
            portrait = portraitFor(state.blackLevel),
            hp = MatchHp.fromLost(game.takenFromBlack),
        ),
        you = MatchPlayer(
            name = whiteName,
            subtitle = if (game.aiThinking && game.whiteToMove) {
                stringResource(R.string.match_thinking)
            } else {
                stringResource(labelOf(state.whiteLevel))
            },
            piece = Piece.of(Piece.KING, true),
            active = game.whiteToMove,
            portrait = portraitFor(state.whiteLevel),
            hp = MatchHp.fromLost(game.takenFromWhite),
        ),
        actions = listOf(
            MatchAction(
                icon = Icons.Filled.KeyboardArrowUp,
                label = stringResource(R.string.action_flip),
                art = BattleButton.FLIP,
                onClick = { flipped = !flipped },
            ),
            MatchAction(
                icon = Icons.Filled.Home,
                label = stringResource(R.string.action_menu),
                art = BattleButton.LEAVE,
                onClick = onExit,
            ),
        ),
        headline = if (state.hitMoveLimit) {
            stringResource(R.string.spectate_limit, game.sanMoves.size)
        } else {
            statusLabel(game)
        },
        modifier = modifier,
        backdrop = R.drawable.bg_battle_default,
        inCheck = game.status == GameStatus.CHECK,
        checkmate = game.status == GameStatus.CHECKMATE,
        belowBoard = {
            Column(verticalArrangement = Arrangement.spacedBy(LocalDesign.current.dp(12))) {
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
    val d = LocalDesign.current
    val running = autoPlay && !paused
    val rowHeight = Modifier.fillMaxWidth().height(d.dp(90))
    Column(verticalArrangement = Arrangement.spacedBy(d.dp(12))) {
        Row(modifier = rowHeight, horizontalArrangement = Arrangement.spacedBy(d.dp(20))) {
            GothicButton(
                label = stringResource(if (paused) R.string.spectate_resume else R.string.spectate_pause),
                onClick = onTogglePause,
                enabled = autoPlay && !finished,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
            GothicButton(
                label = stringResource(R.string.spectate_step),
                onClick = onStep,
                art = ButtonArt.SECONDARY,
                enabled = !running && !finished,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }
        Row(modifier = rowHeight, horizontalArrangement = Arrangement.spacedBy(d.dp(20))) {
            GothicButton(
                label = stringResource(if (autoPlay) R.string.spectate_auto_on else R.string.spectate_auto_off),
                onClick = onToggleAuto,
                art = ButtonArt.SECONDARY,
                enabled = !finished,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
            GothicButton(
                label = stringResource(R.string.spectate_speed, speedFactor),
                onClick = onCycleSpeed,
                art = ButtonArt.SECONDARY,
                modifier = Modifier.weight(1f).fillMaxSize(),
            )
        }
    }
}
