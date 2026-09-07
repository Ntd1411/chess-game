package kma.game.chess2d.net

import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import kma.game.chess2d.engine.Move
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phần chung của một đầu kết nối LAN, dùng cho cả host và khách.
 *
 * Giữ chung một lớp cho hai vai vì 90% hành vi là giống nhau (đi, xin hòa, đầu hàng,
 * heartbeat, đọc thông điệp). Phần khác biệt duy nhất là **quyền trọng tài**: host
 * từ chối nước đi sai và gửi lại trạng thái đúng, còn khách thấy lệch thì chỉ được
 * quyền xin đồng bộ. Các chỗ đó đều được đánh dấu bằng [isReferee].
 *
 * Vòng đời: [state] và [game] sống lâu hơn socket. Kết nối đứt thì ván đấu vẫn ở
 * đó, chỉ có `connected = false` — đây là điều kiện để nối lại được sau khi tắt
 * Wi-Fi 10 giây mà không mất ván.
 */
abstract class LanEndpoint(val role: LanRole, val localName: String) {

    protected val game = NetGame()

    /**
     * Ở khóa bảo vệ [game].
     *
     * [NetGame] không thread-safe, mà nó bị hai luồng chạm vào: thông điệp đến được
     * xử lý trên luồng đọc socket, còn cú chạm của người chơi đến từ luồng UI.
     * Mọi chỗ đọc/sửa [game] đều phải nằm trong ổ khóa này.
     */
    private val gameLock = Any()

    /** Chạy [block] trong ổ khóa của [game]. Dành cho lớp con (ví dụ lúc bắt tay). */
    protected fun <T> withGame(block: () -> T): T = synchronized(gameLock, block)

    private val _state = MutableStateFlow(LanGameState(role = role))
    val state: StateFlow<LanGameState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<LanEvent>(extraBufferCapacity = EVENT_BUFFER)
    val events: SharedFlow<LanEvent> = _events.asSharedFlow()

    /** Kênh đang mở, null khi chưa nối hoặc vừa đứt. */
    @Volatile
    protected var channel: MessageChannel? = null

    /**
     * Hàng đợi gửi đi, được một luồng ghi riêng rút ra.
     *
     * Đây không phải tối ưu hóa mà là điều kiện để chạy được trên Android: ghi socket
     * ngay tại luồng gọi nghĩa là ghi socket trên luồng UI, và Android ném thẳng
     * NetworkOnMainThreadException. Hàng đợi khiến [send] chỉ còn là một phép nối
     * vào danh sách, không chờ, không chạm vào dây.
     */
    @Volatile
    private var outbox: Channel<NetMessage>? = null

    /** Vé nhận diện để nối lại đúng ván cũ. Host sinh ra, khách nhận và gửi trả lại. */
    @Volatile
    protected var resumeToken: String? = null

    protected var gameId: Int = 0

    /** Màu của khách. Host mặc định giữ Trắng ván đầu, mỗi ván đấu lại thì đổi bên. */
    protected var guestPlaysWhite: Boolean = false

    /** Người dùng đã chủ động rời phòng: không được tự nối lại nữa. */
    @Volatile
    protected var closedByUser: Boolean = false

    /**
     * Phiên hiện tại đã kết thúc có chủ đích: đối thủ rời phòng, hoặc chính mình bỏ đối thủ.
     *
     * Trạng thái riêng, không dùng chung với [closedByUser]: [closedByUser] nói về cả
     * phòng (khách thì ngừng nối lại, host thì đóng cửa), còn cờ này chỉ nói về một kết
     * nối — host vẫn tiếp tục mở phòng cho người khác vào. Vì vậy host phải xóa cờ này
     * mỗi lần nhận người mới.
     */
    @Volatile
    protected var peerLeft: Boolean = false

    protected val isReferee: Boolean get() = role == LanRole.HOST

    protected fun youPlayWhite(): Boolean = if (isReferee) !guestPlaysWhite else guestPlaysWhite

    protected fun currentSync(): StateSync = game.toSync(guestPlaysWhite, gameId)

    protected fun emit(event: LanEvent) {
        _events.tryEmit(event)
    }

    protected fun updateState(transform: (LanGameState) -> LanGameState) {
        _state.update(transform)
    }

    /** Đẩy trạng thái bàn cờ từ [game] ra [state]. Gọi sau **mọi** thay đổi ván đấu. */
    protected fun publishState() {
        _state.update {
            it.copy(
                youPlayWhite = youPlayWhite(),
                startFen = game.startFen,
                moves = game.rawMoves(),
                whiteToMove = game.whiteToMove,
                status = game.status,
                ruleFinished = game.isOver,
                gameId = gameId,
            )
        }
    }

    /**
     * Xếp một thông điệp vào hàng đợi gửi đi. Gọi được từ bất kỳ luồng nào.
     *
     * @return false khi chưa có kết nối nào để gửi. Lưu ý true chỉ có nghĩa "đã xếp
     *         hàng", không phải "đối thủ đã nhận" — TCP không cho ai biết điều đó ngay.
     */
    protected fun send(message: NetMessage): Boolean {
        val queue = outbox ?: return false
        return queue.trySend(message).isSuccess
    }

    /**
     * Báo mất kết nối, trừ khi phiên vừa kết thúc có chủ đích.
     *
     * Rời phòng cũng làm socket đóng, nên nếu báo vô điều kiện thì người dùng nhận hai
     * thông báo cho một sự việc, trong đó cái thứ hai ("mất kết nối") còn nói sai nguyên
     * nhân. [LanEvent.OpponentLeft] đã mô tả đúng chuyện xảy ra.
     */
    private fun emitDisconnected(reason: String, canRetry: Boolean = true) {
        if (peerLeft) return
        emit(LanEvent.Disconnected(reason, canRetry = canRetry && !closedByUser))
    }

    // ---------------------------------------------------------------- hành động của người chơi

    /**
     * Đi một nước.
     *
     * Áp dụng cục bộ trước rồi mới gửi: người chơi thấy quân chạy ngay, không phải
     * chờ một vòng mạng. Rủi ro lệch được bù bằng [NetMessage.MoveRejected]: nếu trọng
     * tài không đồng ý, bàn cờ được dụng lại theo trọng tài.
     *
     * @return false khi nước đi bị chính engine của mình từ chối, hoặc chưa tới lượt.
     */
    fun submitMove(raw: Int): Boolean = withGame {
        if (!state.value.yourTurn) return@withGame false
        val atPly = game.ply
        if (game.rejectionReason(raw, youPlayWhite(), atPly) != null) return@withGame false
        if (!game.apply(raw)) return@withGame false
        publishState()
        send(NetMessage.MoveMade(gameId, atPly, raw, Move(raw).toUci()))
        true
    }

    fun resign() {
        if (state.value.finished) return
        updateState {
            it.copy(outcome = LanOutcome.RESIGNATION, resignedByWhite = youPlayWhite())
        }
        send(NetMessage.Resign(gameId))
    }

    fun offerDraw() {
        if (state.value.finished || state.value.waitingDrawReply) return
        updateState { it.copy(waitingDrawReply = true) }
        send(NetMessage.DrawOffer(gameId))
    }

    fun respondDraw(accepted: Boolean) {
        if (!state.value.opponentOffersDraw) return
        updateState {
            it.copy(
                opponentOffersDraw = false,
                outcome = if (accepted) LanOutcome.DRAW_AGREED else it.outcome,
            )
        }
        send(NetMessage.DrawResponse(gameId, accepted))
        emit(LanEvent.DrawSettled(accepted))
    }

    fun offerRematch() {
        if (!state.value.finished || state.value.waitingRematchReply) return
        updateState { it.copy(waitingRematchReply = true) }
        send(NetMessage.RematchOffer(gameId))
    }

    fun respondRematch(accepted: Boolean): Unit = withGame {
        if (!state.value.opponentOffersRematch) return@withGame
        updateState { it.copy(opponentOffersRematch = false) }
        if (accepted && isReferee) {
            // Trọng tài dụng ván mới rồi mới trả lời, để gói trả lời mang luôn ván mới.
            startNextGame()
            send(NetMessage.RematchResponse(true, currentSync()))
        } else {
            send(NetMessage.RematchResponse(accepted, null))
        }
        emit(LanEvent.RematchSettled(accepted))
    }

    /** Rời phòng có báo trước để đối thủ không phải ngồi chờ hết timeout. */
    fun leave(reason: String = "") {
        closedByUser = true
        // Không đóng socket ngay khi còn hàng đợi: luồng ghi sẽ đóng ngay sau khi Bye
        // ra được đến dây. Đóng trước thì đối thủ chỉ thấy kết nối chết và phải ngồi
        // chờ hết hạn timeout thay vì biết ngay là đối thủ đã rồi phòng.
        if (!send(NetMessage.Bye(reason))) {
            channel?.close()
            channel = null
        }
        updateState { it.copy(connected = false) }
    }

    /** Đi bằng ký hiệu UCI ("e2e4", "e7e8q"). Dành cho client dòng lệnh và cho test. */
    fun submitUci(uci: String): Boolean = withGame {
        val move = game.legalMoves().firstOrNull { it.toUci().equals(uci, ignoreCase = true) }
            ?: return@withGame false
        submitMove(move.raw)
    }

    /** Các nước đi hợp lệ ở thế cờ hiện tại. */
    fun legalMoves(): List<Move> = withGame { game.legalMoves() }

    /** FEN hiện tại, dùng để in log và để test kiểm tra nhanh. */
    fun fen(): String = withGame { game.fen() }

    /** Xin trọng tài gửi lại toàn bộ ván. Chỉ có nghĩa với khách. */
    fun requestSync(reason: String = "manual") {
        if (isReferee) return
        send(NetMessage.SyncRequest(reason))
    }

    // ---------------------------------------------------------------- vòng đời kết nối

    protected fun onConnected(opponentName: String) {
        updateState { it.copy(connected = true, opponentName = opponentName) }
        publishState()
        emit(LanEvent.Connected(opponentName, youPlayWhite()))
    }

    /**
     * Vòng đọc thông điệp, kèm một coroutine ping chạy song song.
     *
     * Ping là thứ duy nhất biến "mất mạng" thành một sự kiện quan sát được: cờ vua có
     * thể im lặng hàng phút khi đối thủ đang nghĩ, nên không thể lấy "lâu không có nước
     * đi" làm dấu hiệu mất kết nối.
     *
     * Hàm trả về khi kết nối đóng, và luôn dọn sạch trước khi trả về.
     */
    protected suspend fun pump(open: MessageChannel) {
        val queue = Channel<NetMessage>(Channel.UNLIMITED)
        outbox = queue
        try {
            coroutineScope {
                // Luồng ghi duy nhất của kết nối này: mọi thông điệp ra dây đều đi qua đây,
                // nên việc ghi socket không bao giờ xảy ra trên luồng UI.
                launch(Dispatchers.IO) {
                    for (message in queue) {
                        if (runCatching { open.send(message) }.isFailure) break
                        // Bye là thông điệp cuối cùng có nghĩa: đóng socket ngay sau khi nó đã
                        // thật sự ra đến dây.
                        if (message is NetMessage.Bye) {
                            open.close()
                            break
                        }
                    }
                }
                val heartbeat = launch(Dispatchers.IO) {
                    while (isActive) {
                        delay(LanTiming.PING_INTERVAL_MILLIS)
                        if (!send(NetMessage.Ping(System.nanoTime()))) return@launch
                    }
                }
                try {
                    withContext(Dispatchers.IO) {
                        while (true) {
                            val message = open.receive() ?: break
                            handle(message)
                        }
                    }
                    emitDisconnected("connection closed by peer")
                } catch (timeout: SocketTimeoutException) {
                    emitDisconnected("no data for ${LanTiming.CONNECTION_TIMEOUT_MILLIS} ms")
                } catch (bad: BadMessageException) {
                    // Không giải mã được thì gần như chắc là hai bên khác phiên bản: nối lại vô ích.
                    emit(LanEvent.Failed(LanErrorCode.VERSION_MISMATCH, bad.message ?: "bad message"))
                    emitDisconnected("protocol error", canRetry = false)
                } catch (io: IOException) {
                    emitDisconnected(io.message ?: "io error")
                } finally {
                    heartbeat.cancel()
                    // Đóng hàng đợi để luồng ghi tự kết thúc vòng lặp, thay vì bị hủy giữa
                    // lúc đang ghi dở một dòng JSON.
                    queue.close()
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } finally {
            queue.close()
            if (outbox === queue) outbox = null
            open.close()
            if (channel === open) channel = null
            updateState { it.copy(connected = false, latencyMillis = -1) }
        }
    }

    // ---------------------------------------------------------------- xử lý thông điệp

    /**
     * Điểm vào duy nhất của thông điệp đến.
     *
     * Khóa [game] ngay ở đây vì hàm này chạy trên luồng đọc socket, còn các hành động
     * của người chơi chạy trên luồng UI — hai luồng sửa cùng một bàn cờ.
     */
    private fun handle(message: NetMessage) = withGame { handleLocked(message) }

    private fun handleLocked(message: NetMessage) {
        when (message) {
            is NetMessage.Ping -> send(NetMessage.Pong(message.nanos))

            is NetMessage.Pong -> {
                val millis = (System.nanoTime() - message.nanos) / 1_000_000
                updateState { it.copy(latencyMillis = millis) }
            }

            is NetMessage.MoveMade -> onOpponentMove(message)

            is NetMessage.MoveAck -> Unit // Đã áp dụng cục bộ từ trước, không còn gì phải làm.

            is NetMessage.MoveRejected -> {
                applySync(message.sync)
                emit(LanEvent.MoveRejected(message.reason))
            }

            is NetMessage.Sync -> {
                applySync(message.state)
                emit(LanEvent.Resynced)
            }

            is NetMessage.SyncRequest -> if (isReferee) send(NetMessage.Sync(currentSync()))

            is NetMessage.Resign -> {
                if (message.gameId != gameId) return
                updateState {
                    it.copy(outcome = LanOutcome.RESIGNATION, resignedByWhite = !youPlayWhite())
                }
            }

            is NetMessage.DrawOffer -> {
                if (message.gameId != gameId || state.value.finished) return
                updateState { it.copy(opponentOffersDraw = true) }
            }

            is NetMessage.DrawResponse -> {
                if (message.gameId != gameId) return
                updateState {
                    it.copy(
                        waitingDrawReply = false,
                        outcome = if (message.accepted) LanOutcome.DRAW_AGREED else it.outcome,
                    )
                }
                emit(LanEvent.DrawSettled(message.accepted))
            }

            is NetMessage.RematchOffer -> updateState { it.copy(opponentOffersRematch = true) }

            is NetMessage.RematchResponse -> {
                updateState { it.copy(waitingRematchReply = false) }
                when {
                    !message.accepted -> Unit
                    // Trọng tài nhận đồng ý: tự dụng ván mới rồi thông báo cho khách.
                    isReferee -> {
                        startNextGame()
                        send(NetMessage.Sync(currentSync()))
                    }
                    message.sync != null -> applySync(message.sync)
                }
                emit(LanEvent.RematchSettled(message.accepted))
            }

            is NetMessage.Bye -> onPeerLeft(message.reason)

            is NetMessage.Error -> emit(LanEvent.Failed(message.code, message.detail))

            is NetMessage.Hello, is NetMessage.Welcome -> Unit // Chỉ hợp lệ trong lúc bắt tay.
        }
    }

    /**
     * Nước đi của đối thủ: tự kiểm tra lại bằng engine của mình.
     *
     * Hai vai xứ lý nước sai khác nhau, và đây là toàn bộ sự khác biệt giữa host và khách:
     * host là trọng tài nên từ chối và áp trạng thái của mình; khách không có quyền đó
     * nên chỉ xin đồng bộ lại.
     */
    private fun onOpponentMove(message: NetMessage.MoveMade) {
        if (message.gameId != gameId) return
        val reason = game.rejectionReason(message.raw, !youPlayWhite(), message.ply)
        if (reason != null) {
            if (isReferee) {
                send(NetMessage.MoveRejected(gameId, reason, currentSync()))
            } else {
                send(NetMessage.SyncRequest(reason))
            }
            return
        }
        game.apply(message.raw)
        publishState()
        if (isReferee) send(NetMessage.MoveAck(gameId, message.ply))
    }

    /**
     * Đối thủ chủ động rời phòng — khác với mất kết nối, nối lại không còn ý nghĩa.
     *
     * Trọng tài còn phải dụng lại ván sạch và bỏ vé nối lại. Nếu không, phòng vẫn bị
     * coi là "đang có ván" và mọi người vào sau đều bị từ chối bằng ROOM_BUSY.
     */
    private fun onPeerLeft(reason: String) {
        peerLeft = true
        emit(LanEvent.OpponentLeft(reason))
        if (isReferee) resetRoom()
        channel?.close()
    }

    /**
     * Dựng lại một phòng sạch cho người tiếp theo. Chỉ trọng tài được gọi.
     *
     * Bỏ vé nối lại là phần quan trọng nhất: còn vé và còn nước đi thì phòng vẫn bị coi
     * là "đang có ván", và mọi người vào sau đều bị từ chối bằng ROOM_BUSY.
     *
     * Phải gọi trong ổ khóa của [game] (xem [withGame]).
     */
    protected fun resetRoom() {
        resumeToken = null
        gameId += 1
        guestPlaysWhite = false
        game.reset()
        updateState { LanGameState(role = role) }
        publishState()
    }

    protected fun applySync(sync: StateSync) {
        if (!game.restore(sync)) {
            emit(LanEvent.Failed(LanErrorCode.BAD_HANDSHAKE, "cannot replay game state"))
            return
        }
        val startsNewGame = sync.gameId != gameId
        gameId = sync.gameId
        guestPlaysWhite = sync.guestPlaysWhite
        if (startsNewGame) updateState { it.copy(outcome = null, resignedByWhite = null) }
        updateState {
            it.copy(
                opponentOffersDraw = false,
                waitingDrawReply = false,
                opponentOffersRematch = false,
                waitingRematchReply = false,
            )
        }
        publishState()
    }

    /** Dụng ván mới. Đổi màu hai bên để không ai giữ Trắng mãi. Chỉ trọng tài được gọi. */
    protected fun startNextGame() {
        gameId += 1
        guestPlaysWhite = !guestPlaysWhite
        game.reset()
        updateState {
            it.copy(
                outcome = null,
                resignedByWhite = null,
                opponentOffersDraw = false,
                waitingDrawReply = false,
                opponentOffersRematch = false,
                waitingRematchReply = false,
            )
        }
        publishState()
    }

    private companion object {
        const val EVENT_BUFFER = 32
    }
}

/** Mã ngắn đủ để phân biệt phòng và vé nối lại trong phạm vi một mạng LAN. */
internal fun randomLanId(): String = UUID.randomUUID().toString().take(8)

/** Thời gian chờ giữa hai lần thử nối lại. */
internal const val RECONNECT_DELAY_MILLIS: Long = 1_000

/**
 * Khoảng thời gian cố nối lại trước khi coi như mất hẳn.
 *
 * 45 giây là con số đo từ hành vi thật: tắt rồi bật lại Wi-Fi trên Android mất
 * khoảng 5–15 giây để liên kết xong và xin được IP, chưa kể người dùng còn phải bấm.
 */
internal const val RECONNECT_WINDOW_MILLIS: Long = 45_000
