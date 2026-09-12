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
private enum class Screen { SPLASH, LOBBY, MENU, MATCH }

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

    when (screen) {
        Screen.SPLASH -> SplashScreen(
            onDone = { screen = Screen.LOBBY },
            modifier = modifier,
        )

        // Sảnh là màn chính: không đăng ký BackHandler ở đây để back lúc rảnh vẫn thoát app.
        // Riêng lúc đang mở phòng hoặc đang trong ván, [LanRoute] tự chặn back của nó.
        Screen.LOBBY -> LanRoute(
            playerName = playerName,
            onOpenMenu = { screen = Screen.MENU },
            modifier = modifier,
        )

        Screen.MENU -> {
            // Back ở menu là về sảnh, vì sảnh mới là màn chính.
            BackHandler { screen = Screen.LOBBY }
            MenuScreen(
                name = playerName,
                onNameChange = { playerName = it },
                difficulty = difficulty,
                onDifficultyChange = { difficulty = it },
                onPlayTwoPlayers = {
                    mode = GameMode.TWO_PLAYERS
                    screen = Screen.MATCH
                },
                onPlayComputer = {
                    mode = GameMode.VS_COMPUTER
                    screen = Screen.MATCH
                },
                onPlayLan = { screen = Screen.LOBBY },
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
            )
        }
    }
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
