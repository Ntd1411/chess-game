package kma.game.chess2d.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.R
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.FloorDialogues
import kma.game.chess2d.campaign.FloorResult
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.campaign.TowerCatalog
import kma.game.chess2d.campaign.TowerFlow
import kma.game.chess2d.campaign.TowerProgress
import kma.game.chess2d.campaign.TowerProgressStore
import kma.game.chess2d.campaign.TowerStage
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.SavedGame
import kma.game.chess2d.game.SavedGameStore
import kma.game.chess2d.lan.LanPhase
import kma.game.chess2d.lan.LanViewModel
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.profile.MatchRecording
import kma.game.chess2d.profile.ProfileDatabase
import kma.game.chess2d.profile.ProfileRepository
import kma.game.chess2d.puzzle.PuzzleCatalog
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Các màn hình cấp cao của app.
 *
 * Là enum chứ không phải sealed class để [rememberSaveable] lưu được trực tiếp vào
 * Bundle mà không cần viết Saver riêng. Các lựa chọn kèm theo (chế độ, cấp độ, tên)
 * được giữ thành state riêng bên cạnh.
 */
private enum class Screen { SPLASH, LOBBY, MENU, MATCH, HISTORY, TOWER, SPECTATE, AI_SELECT }

/**
 * Gốc cây giao diện: splash → sảnh phòng → (menu) → ván đấu.
 *
 * Thứ tự này là yêu cầu của mục 5.7: lấy **phòng chơi làm trung tâm**. Mở app lên là
 * vào thẳng sảnh, thấy ngay phòng của người khác trong mạng và mở được phòng của
 * mình; menu chơi offline tụt xuống thành màn thứ cấp sau một cú chạm.
 *
 * Điều hướng tự viết bằng một biến state chứ không dùng Navigation Compose: app chỉ
 * có bốn màn hình, không có deep link và không có back stack sâu, nên thêm một thư
 * viện điều hướng chỉ tốn dung lượng.
 *
 * Phím back được xử lý **riêng từng màn**: ván đấu về menu, menu về sảnh, sảnh đang
 * rảnh thì để hệ thống thoát app như bình thường, còn sảnh đang mở phòng thì back
 * là huỷ phòng. Trước đây back từ trong phòng LAN thoát luôn app, làm người chơi
 * rời phòng ngoài ý muốn.
 */
@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    var screen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
    var mode by rememberSaveable { mutableStateOf(GameMode.TWO_PLAYERS) }
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    // Tên máy làm tên mặc định để người chơi không bắt buộc phải nhập gì mới chơi được.
    var playerName by rememberSaveable { mutableStateOf(Build.MODEL ?: "Android") }
    // Ván sắp mở là ván cũ lưu trên đĩa, hay một ván mới từ menu.
    var resumeSaved by rememberSaveable { mutableStateOf(false) }

    when (screen) {
        Screen.SPLASH -> SplashScreen(
            onDone = { screen = Screen.LOBBY },
            modifier = modifier,
        )

        // Sảnh là màn chính: không đăng ký BackHandler ở đây để back lúc rảnh vẫn thoát app.
        // Riêng lúc đang mở phòng hoặc đang trong ván, [LanRoute] tự chặn back của nó.
        Screen.LOBBY -> {
            LanRoute(
                playerName = playerName,
                onOpenMenu = { screen = Screen.MENU },
                modifier = modifier,
            )
            // Câu hỏi "tiếp tục hay bỏ" đặt ở sảnh vì sảnh là màn đầu tiên người chơi thấy
            // sau splash, không phải menu.
            ResumePrompt(
                onResume = { saved ->
                    mode = saved.mode
                    difficulty = saved.difficulty
                    resumeSaved = true
                    screen = Screen.MATCH
                },
            )
        }

        Screen.MENU -> {
            // Back ở menu là về sảnh, vì sảnh mới là màn chính.
            BackHandler { screen = Screen.LOBBY }
            MenuScreen(
                name = playerName,
                onNameChange = { playerName = it },
                onOpenTower = { screen = Screen.TOWER },
                // Người vs Máy đi qua màn chọn đối thủ: cấp độ do nhân vật được chọn quyết định.
                onPlayComputer = { screen = Screen.AI_SELECT },
                onPlayTwoPlayers = {
                    mode = GameMode.TWO_PLAYERS
                    resumeSaved = false
                    screen = Screen.MATCH
                },
                onPlayLan = { screen = Screen.LOBBY },
                onOpenSpectate = { screen = Screen.SPECTATE },
                onOpenHistory = { screen = Screen.HISTORY },
                modifier = modifier,
            )
        }

        Screen.AI_SELECT -> {
            BackHandler { screen = Screen.MENU }
            AiSelectRoute(
                onStart = { character ->
                    difficulty = character.difficulty
                    mode = GameMode.VS_COMPUTER
                    resumeSaved = false
                    screen = Screen.MATCH
                },
                onBack = { screen = Screen.MENU },
                modifier = modifier,
            )
        }

        Screen.SPECTATE -> {
            // Màn Xem mở từ menu nên back ở đây là về menu; rời màn là dừng vòng lặp AI.
            BackHandler { screen = Screen.MENU }
            SpectateRoute(onBack = { screen = Screen.MENU }, modifier = modifier)
        }

        Screen.TOWER -> {
            // Bản đồ mở từ menu nên back ở đây là về menu.
            BackHandler { screen = Screen.MENU }
            TowerRoute(
                playerName = playerName,
                onBack = { screen = Screen.MENU },
                modifier = modifier,
            )
        }

        Screen.HISTORY -> {
            // Lịch sử mở từ menu nên back ở đây là về menu, giống đúng nút Quay lại trên màn.
            BackHandler { screen = Screen.MENU }
            HistoryScreen(
                onBack = { screen = Screen.MENU },
                modifier = modifier,
            )
        }

        Screen.MATCH -> {
            BackHandler { screen = Screen.MENU }
            GameScreen(
                playerName = playerName,
                mode = mode,
                difficulty = difficulty,
                onExitToMenu = { screen = Screen.MENU },
                modifier = modifier,
                resume = resumeSaved,
            )
        }
    }
}

/**
 * Nhánh chọn đối thủ AI: đọc tiến độ Tháp Cờ để biết nhân vật nào đã mở.
 */
@Composable
private fun AiSelectRoute(
    onStart: (AiCharacter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val store = remember(context) { TowerProgressStore(context.applicationContext) }
    val progress by store.progress.collectAsStateWithLifecycle(initialValue = TowerProgress())
    val profile = remember(context) { ProfileRepository(ProfileDatabase.get(context.applicationContext)) }
    val scope = rememberCoroutineScope()

    AiSelectionScreen(
        clearedUpTo = progress.clearedUpTo,
        onStart = { character ->
            // Bắt đầu đối đầu = đã gặp nhân vật: ghi vào Nhật ký rồi mới vào ván.
            scope.launch { withContext(NonCancellable) { profile.recordMet(character.id) } }
            onStart(character)
        },
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Nhánh chiến dịch: nạp 49 tầng và tiến độ rồi đi theo luồng
 * Tower Map → (Thoại) → Ván đấu → (Thoại kết) → Chiến thắng/Thất bại → quay lại bản đồ.
 *
 * Luật chuyển màn nằm ở [TowerFlow] (thuần, có test); ở đây chỉ giữ màn đang hiện, tầng
 * đang chơi và lượt chơi bằng `rememberSaveable` để xoay máy không đẩy người chơi về bản đồ.
 * Tiến độ chỉ được ghi khi vào màn Chiến thắng, nên bỏ dở giữa ván không mở khóa tầng nào.
 */
@Composable
private fun TowerRoute(playerName: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Dùng applicationContext: store sống lâu hơn một lần vẽ, không được giữ Activity.
    val puzzles = remember(context) { PuzzleCatalog.load(context.applicationContext) }
    val floors = remember(puzzles) { TowerCatalog.build(puzzles.size) }
    val store = remember(context) { TowerProgressStore(context.applicationContext) }
    val profile = remember(context) { ProfileRepository(ProfileDatabase.get(context.applicationContext)) }
    val progress by store.progress.collectAsStateWithLifecycle(initialValue = TowerProgress())

    var stage by rememberSaveable { mutableStateOf(TowerStage.MAP) }
    var floorNumber by rememberSaveable { mutableIntStateOf(1) }
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    var lossReason by rememberSaveable { mutableStateOf(LossReason.CHECKMATE) }
    // Lúc bắt đầu lượt chơi hiện tại, để tính thời gian chơi cho Hồ sơ.
    var attemptStartedAt by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    val scope = rememberCoroutineScope()
    val floor = floors[floorNumber - 1]

    /** Mở lượt chơi mới: đổi khóa ván và đặt lại đồng hồ đo thời gian. */
    fun newAttempt() {
        attempt++
        attemptStartedAt = System.currentTimeMillis()
    }

    /** Cộng ván chiến dịch vừa xong vào Hồ sơ (NonCancellable: rời màn ngay cũng không mất). */
    fun recordBattle(result: FloorResult, reason: LossReason?) {
        val outcome = MatchRecording.campaignOutcome(floor.goal, result, reason) ?: return
        val seconds = MatchRecording.elapsedSeconds(attemptStartedAt, System.currentTimeMillis())
        scope.launch {
            withContext(NonCancellable) {
                profile.recordMatch(outcome.result, outcome.byCheckmate, seconds)
            }
        }
    }

    when (stage) {
        TowerStage.MAP -> {
            // Bản đồ mở từ menu nên back ở đây là về menu.
            BackHandler(onBack = onBack)
            TowerMapScreen(
                floors = floors,
                progress = progress,
                onFloorClick = { picked ->
                    floorNumber = picked.number
                    // Lượt mới mỗi lần vào tầng để vào lại tầng cũ luôn là ván mới.
                    newAttempt()
                    stage = TowerFlow.onSelect(picked)
                },
                onBack = onBack,
                modifier = modifier,
            )
        }

        TowerStage.INTRO -> {
            BackHandler { stage = TowerStage.MAP }
            DialogueScreen(
                lines = FloorDialogues.intro(floor.number),
                onFinished = { stage = TowerFlow.afterIntro(floor) },
                modifier = modifier,
            )
        }

        TowerStage.BATTLE -> {
            BackHandler { stage = TowerStage.MAP }
            CampaignBattleRoute(
                floor = floor,
                attempt = attempt,
                puzzles = puzzles,
                playerName = playerName,
                onWon = {
                    recordBattle(FloorResult.WON, null)
                    stage = TowerFlow.afterWin(floor)
                },
                onLost = { reason ->
                    recordBattle(FloorResult.LOST, reason)
                    lossReason = reason
                    stage = TowerStage.DEFEAT
                },
                onRestart = { newAttempt() },
                onExit = { stage = TowerStage.MAP },
                modifier = modifier,
            )
        }

        TowerStage.EPILOGUE -> {
            BackHandler { stage = TowerStage.VICTORY }
            DialogueScreen(
                lines = FloorDialogues.epilogue(floor.number),
                onFinished = { stage = TowerStage.VICTORY },
                modifier = modifier,
            )
        }

        TowerStage.VICTORY -> {
            // NonCancellable: rời màn ngay sau khi thắng không được làm mất lần ghi tiến độ.
            LaunchedEffect(floor.number) {
                withContext(NonCancellable) {
                    store.markCleared(floor.number)
                    // Hồ sơ giữ tầng cao nhất riêng (không lùi), độc lập với tiến độ mở khóa.
                    profile.recordFloorCleared(floor.number)
                }
            }
            BackHandler { stage = TowerStage.MAP }
            VictoryScreen(
                floor = floor,
                nextFloor = TowerFlow.nextFloor(floors, floor),
                onNext = {
                    TowerFlow.nextFloor(floors, floor)?.let { next ->
                        floorNumber = next.number
                        newAttempt()
                        stage = TowerFlow.onSelect(next)
                    }
                },
                onMap = { stage = TowerStage.MAP },
                modifier = modifier,
            )
        }

        TowerStage.DEFEAT -> {
            BackHandler { stage = TowerStage.MAP }
            DefeatScreen(
                floor = floor,
                reason = lossReason,
                onRetry = {
                    newAttempt()
                    stage = TowerStage.BATTLE
                },
                onMap = { stage = TowerStage.MAP },
                modifier = modifier,
            )
        }
    }
}

/**
 * Hỏi người chơi có muốn tiếp tục ván offline đang dở hay không.
 *
 * Không tự động nhảy vào ván cũ: mở app lên mà bị đẩy thẳng vào một bàn cờ từ hôm
 * trước thì rất dễ lỡ tay đi một nước. Cũng không cho bấm ra ngoài để bỏ qua: hai
 * lựa chọn đều phải rõ ràng, vì "bỏ ván" là việc không hoàn nguyên được.
 *
 * @param onResume nhận ván đã lưu để phía gọi biết mở đúng chế độ và cấp độ cũ.
 */
@Composable
private fun ResumePrompt(onResume: (SavedGame) -> Unit) {
    val context = LocalContext.current
    // Dùng applicationContext: store sống lâu hơn một lần vẽ, không được giữ Activity.
    val store = remember(context) { SavedGameStore(context.applicationContext) }
    val saved by store.saved.collectAsStateWithLifecycle(initialValue = null)
    // Đã trả lời rồi thì không hỏi lại trong cùng một lần mở app, kể cả khi quay về sảnh.
    var answered by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val game = saved
    if (game == null || answered) return

    AlertDialog(
        onDismissRequest = {},
        title = { Text(stringResource(R.string.resume_title)) },
        text = { Text(stringResource(R.string.resume_text, game.uciMoves.size)) },
        confirmButton = {
            TextButton(
                onClick = {
                    answered = true
                    onResume(game)
                },
            ) { Text(stringResource(R.string.resume_continue)) }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    answered = true
                    scope.launch { store.clear() }
                },
            ) { Text(stringResource(R.string.resume_discard)) }
        },
    )
}

/**
 * Nhánh sảnh phòng: sảnh, sảnh đang mở phòng, hoặc bàn cờ — tùy pha của phiên.
 *
 * [LanViewModel] được tạo ở đây chứ không ở [AppRoot], nên rời hẳn sảnh (sang menu hay
 * ván offline) là huỷ luôn ViewModel — tức là socket và coroutine quét mạng đều đóng
 * theo, không để sót một phiên chạy ngầm sau lưng menu.
 */
@Composable
private fun LanRoute(
    playerName: String,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LanViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Tên nhập ở menu là tên đối thủ sẽ thấy, nên đẩy sang ViewModel trước khi mở phòng.
    LaunchedEffect(playerName) { viewModel.setLocalName(playerName) }

    // Chỉ quét khi còn ở sảnh (kể cả lúc đang mở phòng chờ người vào, vì danh sách phòng
    // khác vẫn hiện và người chơi vẫn đổi ý được). Quét tiếp lúc đang chơi thì vừa tốn
    // pin vừa làm ồn mạng mà không ai đọc kết quả.
    LaunchedEffect(state.phase) {
        if (state.phase == LanPhase.SESSION) viewModel.stopScan() else viewModel.startScan()
    }

    when (state.phase) {
        LanPhase.LOBBY -> {
            // Cố tình không có BackHandler: sảnh đang rảnh là màn gốc, back ở đây phải thoát
            // app đúng như người dùng Android nào cũng chờ đợi.
            LanLobbyScreen(
                state = state.lobby,
                localAddresses = state.localAddresses,
                onHost = viewModel::host,
                onJoin = viewModel::join,
                onManualAddressChange = viewModel::setManualAddress,
                onManualJoin = viewModel::joinManual,
                onOpenMenu = onOpenMenu,
                modifier = modifier,
                onTimeControlChange = viewModel::setTimeControl,
            )
        }

        LanPhase.HOSTING -> {
            // Vẫn là sảnh, chỉ khác là có thêm thẻ "Phòng của bạn" ở đầu. Back lúc này là
            // huỷ phòng rồi ở lại sảnh, không phải thoát hẳn chế độ LAN: bỏ phòng và rời
            // sảnh là hai ý định khác nhau.
            BackHandler(onBack = viewModel::cancelHosting)
            LanLobbyScreen(
                state = state.lobby,
                localAddresses = state.localAddresses,
                onHost = viewModel::host,
                onJoin = viewModel::join,
                onManualAddressChange = viewModel::setManualAddress,
                onManualJoin = viewModel::joinManual,
                onOpenMenu = onOpenMenu,
                modifier = modifier,
                hosting = true,
                hostPort = state.hostPort,
                onCancelHosting = viewModel::cancelHosting,
            )
        }

        LanPhase.SESSION -> {
            // Back trong phòng là rời phòng rồi về sảnh, không phải thoát app.
            BackHandler(onBack = viewModel::leave)
            LanGameScreen(
                state = state,
                onSquareTap = viewModel::onSquareTap,
                onPromotionChosen = viewModel::onPromotionChosen,
                onPromotionDismissed = viewModel::onPromotionDismissed,
                onResign = viewModel::resign,
                onOfferDraw = viewModel::offerDraw,
                onRespondDraw = viewModel::respondDraw,
                onOfferRematch = viewModel::offerRematch,
                onRespondRematch = viewModel::respondRematch,
                onRequestSync = { viewModel.requestSync() },
                onDismissNotice = viewModel::dismissNotice,
                onRetry = viewModel::retry,
                onLeave = viewModel::leave,
                modifier = modifier,
            )
        }
    }
}
