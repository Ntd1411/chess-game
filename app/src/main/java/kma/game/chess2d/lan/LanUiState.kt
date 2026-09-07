package kma.game.chess2d.lan

import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.game.PieceOnBoard
import kma.game.chess2d.net.DiscoveredRoom
import kma.game.chess2d.net.LanOutcome
import kma.game.chess2d.net.LanRole

/** Màn hình LAN đang ở đâu. */
enum class LanPhase {
    /** Sảnh chể: quét phòng, mở phòng, hoặc vào bằng địa chỉ. */
    LOBBY,

    /** Đã mở hoặc đã vào một phiên; bàn cờ được hiển thị. */
    SESSION,
}

/**
 * Những thông báo cần nói thắng cho người dùng.
 *
 * Dùng enum chứ không giữ sẵn câu chữ: chuỗi hiển thị nằm trong strings.xml, còn
 * ViewModel không được biết gì về tài nguyên Android.
 */
enum class LanNoticeKind {
    VERSION_MISMATCH,
    ROOM_BUSY,
    BAD_HANDSHAKE,
    MOVE_REJECTED,
    RESYNCED,
    OPPONENT_LEFT,
    BECAME_HOST,
    DISCONNECTED_RETRYING,
    DISCONNECTED_FINAL,
    CONNECT_FAILED,
    BAD_ADDRESS,
}

/** @param detail phần chi tiết bằng tiếng Anh từ tầng mạng, chỉ dùng để gợi ý chẩn đoán. */
data class LanNotice(val kind: LanNoticeKind, val detail: String = "")

/** Sảnh chể. */
data class LanLobbyUiState(
    val localName: String = "",
    val manualAddress: String = "",
    val rooms: List<DiscoveredRoom> = emptyList(),
)

/**
 * Phần bàn cờ, đúng những gì <code>ChessBoard</code> cần.
 *
 * @param flipped khách cầm Đen thì lật bàn để quân mình nằm phía dưới.
 */
data class LanBoardUiState(
    val pieces: List<PieceOnBoard> = emptyList(),
    val selectedSquare: Int = Squares.NONE,
    val legalTargets: Set<Int> = emptySet(),
    val lastMoveFrom: Int = Squares.NONE,
    val lastMoveTo: Int = Squares.NONE,
    val checkedKingSquare: Int = Squares.NONE,
    val pendingPromotion: PendingPromotion? = null,
    val flipped: Boolean = false,
)

/**
 * Toàn bộ trạng thái màn hình LAN.
 *
 * @param role null khi chưa mở/chưa vào phiên nào.
 * @param waitingForOpponent host đã mở phòng nhưng chưa ai vào.
 * @param hostPort cổng TCP đang mở, để hiện cho người dùng gõ tay khi discovery bị chặn.
 */
data class LanUiState(
    val phase: LanPhase = LanPhase.LOBBY,
    val lobby: LanLobbyUiState = LanLobbyUiState(),
    val board: LanBoardUiState = LanBoardUiState(),
    val role: LanRole? = null,
    val connected: Boolean = false,
    val waitingForOpponent: Boolean = false,
    val hostPort: Int = 0,
    val localAddresses: List<String> = emptyList(),
    val opponentName: String = "",
    val youPlayWhite: Boolean = true,
    val whiteToMove: Boolean = true,
    val yourTurn: Boolean = false,
    val status: GameStatus = GameStatus.ONGOING,
    val finished: Boolean = false,
    val outcome: LanOutcome? = null,
    val resignedByWhite: Boolean? = null,
    val latencyMillis: Long = -1,
    val opponentOffersDraw: Boolean = false,
    val waitingDrawReply: Boolean = false,
    val opponentOffersRematch: Boolean = false,
    val waitingRematchReply: Boolean = false,
    val notice: LanNotice? = null,
)
