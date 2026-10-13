package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.BattleButton
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.theme.GothicColors
import kma.game.chess2d.ui.theme.GameFontFamily

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

/** Edge-anchored setup: safe header/footer and independently scrollable AI cards. */
@Composable
private fun SpectateSetupScreen(
    onStart: (Difficulty, Difficulty) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var white by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    var black by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.bg_battle_default, dim = 0.6f)
        // Anchor to the actual safe screen edges, not the centered 1080x1920 canvas.
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SimpleSetupHeader(
                title = stringResource(R.string.spectate_setup_title),
                backLabel = stringResource(R.string.spectate_back),
                onBack = onBack,
            )
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Text(
                        text = stringResource(R.string.spectate_setup_hint),
                        color = GothicColors.Parchment,
                        fontFamily = GameFontFamily,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    )
                    LevelPanel(
                        title = stringResource(R.string.spectate_white_ai),
                        current = white,
                        onChosen = { white = it },
                    )
                    LevelPanel(
                        title = stringResource(R.string.spectate_black_ai),
                        current = black,
                        onChosen = { black = it },
                    )
                }
            }
            Column(
                modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SimpleSetupButton(
                    label = stringResource(R.string.spectate_start),
                    onClick = { onStart(white, black) },
                    primary = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                SimpleSetupButton(
                    label = stringResource(R.string.spectate_back),
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** A drawn dark card with a circular portrait and a three-choice difficulty selector. */
@Composable
private fun LevelPanel(
    title: String,
    current: Difficulty,
    onChosen: (Difficulty) -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val opponent = AiCharacters.all.first { it.difficulty == current }
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape)
            .background(GothicColors.Ink.copy(alpha = 0.86f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.4f), shape)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ArtImage(
                res = portraitFor(current), maxEdge = ArtSizes.PORTRAIT,
                contentDescription = stringResource(opponent.name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(72.dp).clip(CircleShape)
                    .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), CircleShape),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = GothicColors.Gold, fontFamily = GameFontFamily,
                    fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                Text(stringResource(opponent.name), color = GothicColors.Muted,
                    fontFamily = GameFontFamily, fontSize = 14.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Difficulty.entries.forEach { level ->
                SimpleSetupButton(
                    label = stringResource(labelOf(level)),
                    onClick = { onChosen(level) },
                    selected = level == current,
                    modifier = Modifier.weight(1f),
                )
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
            captured = game.takenFromWhite,
            advantage = -game.materialBalance,
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
            captured = game.takenFromBlack,
            advantage = game.materialBalance,
        ),
        sanMoves = game.sanMoves,
        actions = listOf(
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
        backdrop = R.drawable.game_play_background,
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
            flipped = false,
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
