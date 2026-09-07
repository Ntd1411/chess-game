package kma.game.chess2d.lan

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.net.Inet4Address
import java.net.NetworkInterface
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.net.DiscoveredRoom
import kma.game.chess2d.net.LanEndpoint
import kma.game.chess2d.net.LanErrorCode
import kma.game.chess2d.net.LanEvent
import kma.game.chess2d.net.LanGameState
import kma.game.chess2d.net.LanGuest
import kma.game.chess2d.net.LanHost
import kma.game.chess2d.net.LanRole
import kma.game.chess2d.net.RoomScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Cầu nối duy nhất giữa giao diện và module <code>:net</code>.
 *
 * Phân vai rạch ròi: <code>:net</code> giữ luật chơi và đường truyền, lớp này chỉ làm ba
 * việc — đổi [LanGameState] thành thứ vẽ được, giữ trạng thái chọn quân của riêng màn
 * hình, và chuyển cú chạm thành những lời gọi có sẵn của <code>LanEndpoint</code>.
 *
 * Không lưu vào SavedStateHandle như <code>GameViewModel</code>: một phiên LAN không dựng
 * lại được từ danh sách nước đi, vì socket đã chết cùng tiến trình. Xoay máy không
 * mất ván vì ViewModel sống qua cả configuration change; bị hệ thống giải phóng tiến
 * trình thì phải vào lại phòng — đúng như mọi game LAN khác.
 */
class LanViewModel : ViewModel() {

    private val scanner = RoomScanner()
    private val mirror = BoardMirror()

    private var scanJob: Job? = null
    private var sessionJob: Job? = null
    private var endpoint: LanEndpoint? = null

    /** Trạng thái chọn quân là chuyện riêng của máy này, không gửi qua mạng. */
    private var selectedSquare = Squares.NONE
    private var pendingPromotion: PendingPromotion? = null

    private val _uiState = MutableStateFlow(
        LanUiState(
            lobby = LanLobbyUiState(localName = defaultLocalName()),
            localAddresses = localIpv4Addresses(),
        ),
    )
    val uiState: StateFlow<LanUiState> = _uiState.asStateFlow()

    init {
        startScan()
    }

    // --------------------------------------------------------------- sảnh chể

    /** Bắt đầu nghe beacon. Gọi lại nhiều lần không sao: lần thứ hai bị bỏ qua. */
    fun startScan() {
        if (scanJob?.isActive == true) return
        scanJob = viewModelScope.launch {
            launch { scanner.run() }
            scanner.rooms.collect { rooms ->
                _uiState.update { it.copy(lobby = it.lobby.copy(rooms = rooms)) }
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        scanner.clear()
        _uiState.update { it.copy(lobby = it.lobby.copy(rooms = emptyList())) }
    }

    fun setLocalName(name: String) {
        _uiState.update { it.copy(lobby = it.lobby.copy(localName = name)) }
    }

    fun setManualAddress(address: String) {
        _uiState.update { it.copy(lobby = it.lobby.copy(manualAddress = address)) }
    }

    /** Mở phòng: vừa lắng nghe TCP vừa phát beacon, và làm trọng tài của ván. */
    fun host() {
        val host = LanHost(localName = currentName())
        startSession(host) {
            host.run { port ->
                _uiState.update { it.copy(hostPort = port) }
            }
        }
    }

    fun join(room: DiscoveredRoom) {
        // Chặn ngay tại đây thay vì để host từ chối: đỡ một vòng bắt tay và thông báo
        // cũng rõ hơn, vì beacon đã nói sẵn phiên bản giao thức của phòng đó.
        if (!room.compatible) {
            notify(LanNoticeKind.VERSION_MISMATCH, "room ${room.hostName} speaks v${room.protocolVersion}")
            return
        }
        val guest = LanGuest(localName = currentName())
        startSession(guest) { guest.run(room) }
    }

    /**
     * Vào phòng bằng địa chỉ gõ tay.
     *
     * Không phải tính năng cho vui: nhiều mạng Wi-Fi công cộng bật AP isolation hoặc
     * chặn broadcast, lúc đó danh sách phòng luôn rỗng dù hai máy đều ở cùng mạng.
     */
    fun joinManual() {
        val raw = _uiState.value.lobby.manualAddress.trim()
        val host = raw.substringBefore(':').trim()
        val port = raw.substringAfter(':', "").trim().toIntOrNull()
        if (host.isEmpty() || port == null || port !in 1..65_535) {
            notify(LanNoticeKind.BAD_ADDRESS, raw)
            return
        }
        val guest = LanGuest(localName = currentName())
        startSession(guest) { guest.run(host, port) }
    }

    // ------------------------------------------------------------------- phiên

    private fun startSession(target: LanEndpoint, block: suspend () -> Unit) {
        sessionJob?.cancel()
        endpoint = target
        selectedSquare = Squares.NONE
        pendingPromotion = null
        _uiState.update {
            it.copy(
                phase = LanPhase.SESSION,
                role = target.role,
                connected = false,
                waitingForOpponent = target.role == LanRole.HOST,
                hostPort = 0,
                opponentName = "",
                notice = null,
                board = LanBoardUiState(),
            )
        }

        sessionJob = viewModelScope.launch {
            launch { target.state.collect(::onNetworkState) }
            launch { target.events.collect(::onNetworkEvent) }
            block()
        }
    }

    /**
     * Host kết thúc ván đang chơi nhưng ở lại phòng, chờ đối thủ mới.
     *
     * Không dùng [leave] cho việc này: [leave] hủy cả phiên, nghĩa là beacon ngừng phát
     * và phòng biến mất khỏi danh sách của mọi máy khác, host phải mở lại từ đầu.
     */
    fun endGame() {
        val host = endpoint as? LanHost ?: return
        selectedSquare = Squares.NONE
        pendingPromotion = null
        host.dropOpponent("host ended the game")
        _uiState.update { it.copy(waitingForOpponent = true, notice = null) }
    }

    /** Đóng phòng (host) hoặc rời phòng (khách) rồi về sảnh chờ. */
    fun leave() {
        val leaving = endpoint
        val job = sessionJob
        endpoint = null
        sessionJob = null
        viewModelScope.launch {
            // Gửi Bye trước khi hủy job, để đối thủ biết là mình chủ động rời chứ không
            // phải mất mạng — hai việc này hiển thị khác nhau bên máy đối thủ.
            leaving?.leave("user left the room")
            job?.cancelAndJoin()
        }
        _uiState.update {
            it.copy(
                phase = LanPhase.LOBBY,
                role = null,
                connected = false,
                waitingForOpponent = false,
                hostPort = 0,
                board = LanBoardUiState(),
            )
        }
        startScan()
    }

    fun resign() = endpoint?.resign() ?: Unit

    fun offerDraw() = endpoint?.offerDraw() ?: Unit

    fun respondDraw(accepted: Boolean) = endpoint?.respondDraw(accepted) ?: Unit

    fun offerRematch() = endpoint?.offerRematch() ?: Unit

    fun respondRematch(accepted: Boolean) = endpoint?.respondRematch(accepted) ?: Unit

    fun requestSync() = endpoint?.requestSync("user asked") ?: Unit

    fun dismissNotice() {
        _uiState.update { it.copy(notice = null) }
    }

    // ---------------------------------------------------------------- bàn cờ

    /**
     * Người chơi chạm một ô.
     *
     * Giống <code>GameViewModel</code> nhưng có thêm một của: không phải lượt mình thì
     * không nhận chạm. Chặn ở đây chỉ để giao diện đử hạnh; trọng tài vẫn kiểm tra
     * lại mọi nước, nên một bản bị sửa cũng không đi được hai nước liền.
     */
    fun onSquareTap(square: Int) {
        val state = _uiState.value
        if (!state.yourTurn || pendingPromotion != null) return

        if (selectedSquare == Squares.NONE) {
            selectIfOwnPiece(square, state.youPlayWhite)
            publish()
            return
        }
        if (square == selectedSquare) {
            selectedSquare = Squares.NONE
            publish()
            return
        }

        val moves = mirror.legalMoves.filter { it.from == selectedSquare && it.to == square }
        when {
            moves.isEmpty() -> selectIfOwnPiece(square, state.youPlayWhite)
            moves.size == 1 -> submit(moves.first())
            // Nhiều nước cùng ô đi và ô đến thì chỉ có thể là phong cấp.
            else -> pendingPromotion =
                PendingPromotion(selectedSquare, square, mirror.whiteToMove, moves)
        }
        publish()
    }

    fun onPromotionChosen(move: Move) {
        pendingPromotion = null
        submit(move)
        publish()
    }

    fun onPromotionDismissed() {
        pendingPromotion = null
        publish()
    }

    private fun submit(move: Move) {
        selectedSquare = Squares.NONE
        // submitMove tự áp dụng nước tại máy rồi mới gửi đi, nên quân đi ngay không
        // chợ mạng. Nếu trọng tài từ chối, [LanEvent.MoveRejected] sẽ tới sau và bàn cờ
        // được đồng bộ lại.
        endpoint?.submitMove(move.raw)
    }

    private fun selectIfOwnPiece(square: Int, youPlayWhite: Boolean) {
        val ownPiece = mirror.isOwnPiece(square, youPlayWhite)
        selectedSquare =
            if (ownPiece && mirror.legalMoves.any { it.from == square }) square else Squares.NONE
    }

    // ------------------------------------------------------- tín hiệu từ :net

    private fun onNetworkState(state: LanGameState) {
        mirror.sync(state.startFen, state.moves, state.gameId)
        // Đối thủ vừa đi, hoặc ván vừa được đồng bộ lại: ô đang chọn không còn ý nghĩa.
        if (!state.yourTurn) {
            selectedSquare = Squares.NONE
            pendingPromotion = null
        }
        publish(state)
    }

    private fun onNetworkEvent(event: LanEvent) {
        when (event) {
            is LanEvent.Connected -> _uiState.update {
                it.copy(waitingForOpponent = false, notice = null)
            }

            is LanEvent.MoveRejected -> notify(LanNoticeKind.MOVE_REJECTED, event.reason)

            LanEvent.Resynced -> notify(LanNoticeKind.RESYNCED)

            is LanEvent.OpponentLeft -> {
                // Host ở lại chờ người tiếp theo; khách thì phiên này coi như hết.
                _uiState.update {
                    it.copy(waitingForOpponent = it.role == LanRole.HOST)
                }
                notify(LanNoticeKind.OPPONENT_LEFT, event.reason)
            }

            is LanEvent.Disconnected -> notify(
                if (event.canRetry) LanNoticeKind.DISCONNECTED_RETRYING else LanNoticeKind.DISCONNECTED_FINAL,
                event.reason,
            )

            is LanEvent.Failed -> notify(
                when (event.code) {
                    LanErrorCode.VERSION_MISMATCH -> LanNoticeKind.VERSION_MISMATCH
                    LanErrorCode.ROOM_BUSY -> LanNoticeKind.ROOM_BUSY
                    else -> LanNoticeKind.BAD_HANDSHAKE
                },
                event.detail,
            )

            is LanEvent.DrawSettled, is LanEvent.RematchSettled -> Unit // Trạng thái đã nói đủ.
        }
    }

    private fun notify(kind: LanNoticeKind, detail: String = "") {
        _uiState.update { it.copy(notice = LanNotice(kind, detail)) }
    }

    // ---------------------------------------------------------------- phát ra

    /** Dùng lại trạng thái mạng gần nhất khi chỉ có việc chọn quân thay đổi. */
    private fun publish() {
        _uiState.update { it.copy(board = boardState(it.youPlayWhite)) }
    }

    private fun publish(state: LanGameState) {
        _uiState.update {
            it.copy(
                connected = state.connected,
                waitingForOpponent = it.role == LanRole.HOST && !state.connected,
                opponentName = state.opponentName,
                youPlayWhite = state.youPlayWhite,
                whiteToMove = state.whiteToMove,
                yourTurn = state.yourTurn,
                status = state.status,
                finished = state.finished,
                outcome = state.outcome,
                resignedByWhite = state.resignedByWhite,
                latencyMillis = state.latencyMillis,
                opponentOffersDraw = state.opponentOffersDraw,
                waitingDrawReply = state.waitingDrawReply,
                opponentOffersRematch = state.opponentOffersRematch,
                waitingRematchReply = state.waitingRematchReply,
                board = boardState(state.youPlayWhite),
            )
        }
    }

    private fun boardState(youPlayWhite: Boolean): LanBoardUiState {
        val lastMove = mirror.lastMove
        return LanBoardUiState(
            pieces = mirror.pieces(),
            selectedSquare = selectedSquare,
            legalTargets = mirror.targetsFrom(selectedSquare),
            lastMoveFrom = lastMove?.from ?: Squares.NONE,
            lastMoveTo = lastMove?.to ?: Squares.NONE,
            checkedKingSquare = mirror.checkedKingSquare(),
            pendingPromotion = pendingPromotion,
            flipped = !youPlayWhite,
        )
    }

    private fun currentName(): String =
        _uiState.value.lobby.localName.trim().ifEmpty { defaultLocalName() }

    override fun onCleared() {
        // ViewModel chết thì socket và beacon phải chết theo, không để sót một ván "ảo"
        // vẫn hiện trong danh sách phòng của máy khác. viewModelScope tự hủy cả hai job.
        super.onCleared()
    }
}

/** Tên máy hiện cho đối thủ thấy. */
private fun defaultLocalName(): String = Build.MODEL?.takeIf { it.isNotBlank() } ?: "Android"

/**
 * Địa chỉ IPv4 của máy, để hiện cho người dùng đọc cho máy đối diện gõ tay khi
 * discovery bị mạng chặn.
 */
private fun localIpv4Addresses(): List<String> = runCatching {
    NetworkInterface.getNetworkInterfaces()
        .asSequence()
        .filter { it.isUp && !it.isLoopback }
        .flatMap { it.inetAddresses.asSequence() }
        .filterIsInstance<Inet4Address>()
        .mapNotNull { it.hostAddress }
        .toList()
}.getOrDefault(emptyList())
