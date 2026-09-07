package kma.game.chess2d.ui.screen

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.lan.LanPhase
import kma.game.chess2d.lan.LanViewModel

/**
 * Các màn hình cấp cao của app.
 *
 * Là enum chứ không phải sealed class để [rememberSaveable] lưu được trực tiếp vào
 * Bundle mà không cần viết Saver riêng. Các lụa chọn kèm theo (chế độ, cấp độ, tên)
 * được giữ thành state riêng bên cạnh.
 */
private enum class Screen { SPLASH, MENU, GAME, LAN }

/**
 * Gốc cây giao diện: splash → menu → ván đấu.
 *
 * Điều hướng tự viết bằng một biến state chứ không dùng Navigation Compose: app chỉ
 * có bốn màn hình, không có deep link và không có back stack sâu, nên thêm một thư
 * viện điều hướng chỉ tốn dung lượng.
 *
 * Phím back được xử lý ở từng màn hình: trước đây back từ trong phòng LAN thoát
 * luôn app, làm người chơi rời phòng ngoài ý muốn.
 */
@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    var screen by rememberSaveable { mutableStateOf(Screen.SPLASH) }
    var mode by rememberSaveable { mutableStateOf(GameMode.TWO_PLAYERS) }
    var difficulty by rememberSaveable { mutableStateOf(Difficulty.MEDIUM) }
    // Tên máy làm tên mặc định để người chơi không bắt buộc phải nhập gì mới chơi được.
    var playerName by rememberSaveable { mutableStateOf(Build.MODEL ?: "Android") }

    when (screen) {
        Screen.SPLASH -> SplashScreen(
            onDone = { screen = Screen.MENU },
            modifier = modifier,
        )

        Screen.MENU -> MenuScreen(
            name = playerName,
            onNameChange = { playerName = it },
            difficulty = difficulty,
            onDifficultyChange = { difficulty = it },
            onPlayTwoPlayers = {
                mode = GameMode.TWO_PLAYERS
                screen = Screen.GAME
            },
            onPlayComputer = {
                mode = GameMode.VS_COMPUTER
                screen = Screen.GAME
            },
            onPlayLan = { screen = Screen.LAN },
            modifier = modifier,
        )

        Screen.GAME -> {
            BackHandler { screen = Screen.MENU }
            GameScreen(
                playerName = playerName,
                mode = mode,
                difficulty = difficulty,
                onExitToMenu = { screen = Screen.MENU },
                modifier = modifier,
            )
        }

        Screen.LAN -> LanRoute(
            playerName = playerName,
            onExit = { screen = Screen.MENU },
            modifier = modifier,
        )
    }
}

/**
 * Nhánh LAN: sảnh chờ hoặc bàn cờ, tùy pha của phiên.
 *
 * [LanViewModel] được tạo ở đây chứ không ở [AppRoot], nên rời hẳn chế độ LAN là huỷ
 * luôn ViewModel — tức là socket và coroutine quét mạng đều đóng theo, không để sót
 * một phiên chạy ngầm sau lưng menu.
 */
@Composable
private fun LanRoute(
    playerName: String,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LanViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Tên nhập ở menu là tên đối thủ sẽ thấy, nên đẩy sang ViewModel trước khi mở phòng.
    LaunchedEffect(playerName) { viewModel.setLocalName(playerName) }

    // Chỉ quét khi đang ở sảnh. Quét tiếp lúc đang chơi thì vừa tốn pin vừa làm ồn mạng
    // mà không ai đọc kết quả.
    LaunchedEffect(state.phase) {
        if (state.phase == LanPhase.LOBBY) viewModel.startScan() else viewModel.stopScan()
    }

    when (state.phase) {
        LanPhase.LOBBY -> {
            BackHandler(onBack = onExit)
            LanLobbyScreen(
                state = state.lobby,
                localAddresses = state.localAddresses,
                onHost = viewModel::host,
                onJoin = viewModel::join,
                onManualAddressChange = viewModel::setManualAddress,
                onManualJoin = viewModel::joinManual,
                onBack = onExit,
                modifier = modifier,
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
