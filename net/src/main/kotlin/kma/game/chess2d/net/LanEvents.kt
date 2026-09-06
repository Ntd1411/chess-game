package kma.game.chess2d.net

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.GameStatus

/** Vai trong một phòng LAN. Host đồng thọi là trọng tài của ván đấu. */
enum class LanRole { HOST, GUEST }

/**
 * Trạng thái một phiên LAN, đủ để UI vẽ được mà không cần biết gì về socket.
 *
 * Hai cặp cờ "pending/awaiting" được tách riêng vì hai tình huống này hiển thị khác
 * nhau hoàn toàn: đối thủ xin hòa thì phải hiện hộp đồng ý/từ chối, còn mình xin hòa
 * thì chỉ hiện trạng thái đang chờ.
 */
data class LanGameState(
    val role: LanRole = LanRole.HOST,
    val connected: Boolean = false,
    val opponentName: String = "",
    val youPlayWhite: Boolean = true,
    val startFen: String = Engine.START_FEN,
    val moves: List<Int> = emptyList(),
    val whiteToMove: Boolean = true,
    val status: GameStatus = GameStatus.ONGOING,
    /** Ván kết thúc theo luật cờ (hết nước, hòa...), do engine quyết định. */
    val ruleFinished: Boolean = false,
    /** Ván kết thúc do người chơi: đầu hàng hoặc đồng ý hòa. */
    val outcome: LanOutcome? = null,
    /** Ai đã đầu hàng, chỉ có nghĩa khi [outcome] là RESIGNATION. */
    val resignedByWhite: Boolean? = null,
    val gameId: Int = 0,
    val opponentOffersDraw: Boolean = false,
    val waitingDrawReply: Boolean = false,
    val opponentOffersRematch: Boolean = false,
    val waitingRematchReply: Boolean = false,
    /** Độ trễ đo được từ heartbeat, -1 khi chưa có số đo. */
    val latencyMillis: Long = -1,
) {
    val finished: Boolean get() = ruleFinished || outcome != null

    /** Chỉ khi này UI mới cho phép chọn quân. */
    val yourTurn: Boolean get() = connected && !finished && whiteToMove == youPlayWhite

    val ply: Int get() = moves.size
}

/**
 * Những việc xảy ra một lần, không thuộc về trạng thái.
 *
 * Tách khỏi [LanGameState] vì nếu nhét vào state thì UI sẽ hiện lại cùng một thông báo
 * sau mỗi lần xoay màn hình, và phải tự nghĩ cách xoá cờ đã hiện.
 */
sealed interface LanEvent {

    data class Connected(val opponentName: String, val youPlayWhite: Boolean) : LanEvent

    /** Nước đi bị trọng tài từ chối; bàn cờ đã được trả về đúng trước khi sự kiện này bắn ra. */
    data class MoveRejected(val reason: String) : LanEvent

    /** Đã đồng bộ lại toàn bộ ván theo trọng tài. */
    data object Resynced : LanEvent

    data class DrawSettled(val accepted: Boolean) : LanEvent

    data class RematchSettled(val accepted: Boolean) : LanEvent

    /** Đối thủ chủ động rời phòng. Khác với mất kết nối. */
    data class OpponentLeft(val reason: String) : LanEvent

    /**
     * Kết nối đứt.
     *
     * @param canRetry true khi còn hy vọng nối lại ván đang chơi (mất mạng tạm thời).
     */
    data class Disconnected(val reason: String, val canRetry: Boolean) : LanEvent

    /** Lỗi không chơi tiếp được: lệch phiên bản, phòng đầy, bắt tay sai. */
    data class Failed(val code: String, val detail: String) : LanEvent
}
