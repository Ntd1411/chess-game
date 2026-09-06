package kma.game.chess2d.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kma.game.chess2d.lan.LanPhase
import kma.game.chess2d.lan.LanViewModel

/**
 * Điểm vào của giao diện: chơi máy/hai người tại chỗ, hay chơi qua LAN.
 *
 * Đây là một quyết định thiết kế có cân nhắc: LAN <b>không</b> được thêm vào
 * <code>GameMode</code>. Nếu thêm, <code>GameViewModel</code> sẽ phải vừa giữ ván đấu cục bộ
 * vừa giữ socket, lịch bắt tay và vòng nhặt lại kết nối — hai trách nhiệm chẳng liên
 * quan trong một lớp. Tách thành hai nhánh với hai ViewModel riêng khiến mỗi lớp
 * chỉ có một lý do để thay đổi, và ván đấu cục bộ không bị ảnh hưởng gì bởi Phase 4.
 */
@Composable
fun AppRoot(modifier: Modifier = Modifier) {
    var lanOpen by rememberSaveable { mutableStateOf(false) }

    if (lanOpen) {
        LanRoute(onExit = { lanOpen = false }, modifier = modifier)
    } else {
        GameScreen(onOpenLan = { lanOpen = true }, modifier = modifier)
    }
}

/**
 * Nhánh LAN. [LanViewModel] được tạo ở đây nên nó sống đúng bằng thời gian người
 * dùng ở trong nhánh này: thoát ra là socket và beacon đều đóng.
 */
@Composable
private fun LanRoute(onExit: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: LanViewModel = viewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (state.phase) {
        LanPhase.LOBBY -> LanLobbyScreen(
            state = state.lobby,
            onNameChange = viewModel::setLocalName,
            onHost = viewModel::host,
            onJoin = viewModel::join,
            onManualAddressChange = viewModel::setManualAddress,
            onManualJoin = viewModel::joinManual,
            onBack = onExit,
            modifier = modifier,
        )

        LanPhase.SESSION -> LanGameScreen(
            state = state,
            onSquareTap = viewModel::onSquareTap,
            onPromotionChosen = viewModel::onPromotionChosen,
            onPromotionDismissed = viewModel::onPromotionDismissed,
            onResign = viewModel::resign,
            onOfferDraw = viewModel::offerDraw,
            onRespondDraw = viewModel::respondDraw,
            onOfferRematch = viewModel::offerRematch,
            onRespondRematch = viewModel::respondRematch,
            onDismissNotice = viewModel::dismissNotice,
            onLeave = viewModel::leave,
            modifier = modifier,
        )
    }
}
