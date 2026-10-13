package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.engine.CaptureTally
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.BattleButton
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.board.pieceImageRes
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Một người chơi trong khung ván đấu.
 *
 * @param subtitle dòng phụ đổi theo chế độ: LAN hiện độ trễ, đấu máy hiện "đang tính…", hai người
 *        một máy hiện bên Trắng/Đen.
 * @param active đang tới lượt bên này.
 * @param clock giờ còn lại dạng `mm:ss`; `null` là ván không bấm giờ nên không vẽ đồng hồ.
 * @param clockLow giờ sắp hết: đồng hồ đổi sang màu cảnh báo.
 * @param portrait chân dung vuông `portrait_*`; `null` thì vẽ quân Vua của [piece] trong khung.
 * @param hp phần "máu" còn lại 0..1 ([MatchHp.fromLost]); `null` thì không vẽ thanh máu.
 */
data class MatchPlayer(
    val name: String,
    val subtitle: String = "",
    val piece: Byte = Piece.NONE,
    val active: Boolean = false,
    val clock: String? = null,
    val clockLow: Boolean = false,
    @DrawableRes val portrait: Int? = null,
    val hp: Float? = null,
    val captured: List<Byte> = emptyList(),
    val advantage: Int = 0,
)

/**
 * Một nút ở hàng thao tác dưới bàn cờ.
 *
 * Nút không dùng được ở chế độ hiện tại thì **không truyền vào danh sách**; [enabled] chỉ dành cho
 * nút đúng là của chế độ này nhưng tạm thời chưa bấm được, ví dụ Đi lại khi chưa có nước nào.
 *
 * @param art ảnh nút tròn `battle_button_*`; `null` thì vẽ [icon] vàng trong khung `radio_unselected`.
 */
data class MatchAction(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
    val art: BattleButton? = null,
    val onClick: () -> Unit,
)

/** Kết quả ván nhìn từ phía người chơi, vẽ bằng `ui_result_banner_*` khi ván kết thúc. */
enum class ResultBanner(@DrawableRes val res: Int) {
    WIN(R.drawable.ui_result_banner_win),
    LOSS(R.drawable.ui_result_banner_loss),
    DRAW(R.drawable.ui_result_banner_draw),
    ;

    companion object {
        /**
         * Banner cho người cầm quân [youWhite] khi ván có [winnerWhite]: `null` là hòa,
         * [over] = false là ván chưa xong nên không có banner.
         */
        fun of(over: Boolean, winnerWhite: Boolean?, youWhite: Boolean): ResultBanner? = when {
            !over -> null
            winnerWhite == null -> DRAW
            winnerWhite == youWhite -> WIN
            else -> LOSS
        }
    }
}

/** "Máu" của mỗi bên suy từ quân đã mất: đủ 39 điểm quân (8 tốt, 2 mã, 2 tượng, 2 xe, hậu) là đầy. */
object MatchHp {
    const val FULL_MATERIAL = 39

    fun fromLost(lost: List<Byte>): Float =
        (1f - lost.sumOf { CaptureTally.valueOf(it) }.toFloat() / FULL_MATERIAL).coerceIn(0f, 1f)
}

/** Vị trí X của [count] nút tròn cỡ 110 căn giữa khung 1080 (4 nút trùng bản thiết kế 220/400/580/760). */
internal fun actionSlotsX(count: Int): List<Int> {
    if (count <= 0) return emptyList()
    val spacing = if (count == 1) 0 else minOf(180, 880 / (count - 1))
    val first = 540 - spacing * (count - 1) / 2
    return List(count) { first + it * spacing - ACTION_SIZE / 2 }
}

/**
 * Khung ván đấu dùng chung cho mọi chế độ, theo bản thiết kế Chess Battle 1080x1920:
 * hai khung `game_play_frame_player_top/bottom` chứa chân dung tròn, tên, quân đã ăn và đồng hồ,
 * bàn cờ đặt trong vùng rỗng của `chess_board_frame`, hàng nút tròn và vùng phụ cuộn được ở đáy
 * (quân đã ăn, danh sách nước đi), đặt theo [GamePlayLayout].
 * Nút không bao giờ che hàng cuối bàn cờ.
 *
 * @param headline dòng trạng thái ngắn giữa khung đối thủ và bàn cờ.
 * @param inCheck có vua đang bị chiếu: hiện `battle_check_indicator` + `fx_check` cạnh dòng trạng thái.
 * @param checkmate ván vừa kết thúc bằng chiếu hết: phủ `fx_checkmate` mờ lên giữa bàn cờ.
 * @param result banner kết quả nằm ngang giữa bàn cờ khi ván đã xong.
 * @param notice ô thông báo cần người dùng quyết định, nổi lên trên bàn cờ.
 * @param belowBoard phần phụ dưới hàng nút.
 */
@Composable
fun MatchScaffold(
    opponent: MatchPlayer,
    you: MatchPlayer,
    actions: List<MatchAction>,
    modifier: Modifier = Modifier,
    headline: String? = null,
    notice: (@Composable () -> Unit)? = null,
    belowBoard: (@Composable () -> Unit)? = null,
    @DrawableRes backdrop: Int = R.drawable.game_play_background,
    inCheck: Boolean = false,
    checkmate: Boolean = false,
    result: ResultBanner? = null,
    board: @Composable () -> Unit,
) {
    DesignCanvas(background = backdrop, dim = 0.2f, modifier = modifier) {
        // Đọc tỷ lệ bên trong DesignCanvas: bên ngoài chỉ có giá trị mặc định 1:1.
        val d = LocalDesign.current
        PlayerFrame(player = opponent, upper = true, top = GamePlayLayout.OPPONENT_TOP)

        if (inCheck) {
            ArtImage(R.drawable.fx_check, maxEdge = ArtSizes.OVERLAY, trim = true, alpha = 0.8f,
                modifier = Modifier.at(60, 266, 60, 60))
            ArtImage(R.drawable.battle_check_indicator, maxEdge = ArtSizes.ICON, trim = true,
                modifier = Modifier.at(68, 274, 44, 44))
        }
        if (headline != null) {
            DesignText(
                text = headline,
                size = 32,
                weight = FontWeight.Bold,
                color = if (inCheck) GothicColors.Blood else GothicColors.Parchment,
                maxLines = 2,
                modifier = Modifier.at(150, 282, 780, 40),
            )
        }

        ArtImage(R.drawable.chess_board_frame, maxEdge = ArtSizes.BOARD, trim = false,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.at(60, GamePlayLayout.BOARD_TOP, GamePlayLayout.BOARD_SIZE, GamePlayLayout.BOARD_SIZE))
        val boardRect = GamePlayLayout.board
        Box(modifier = Modifier.at(boardRect.x, boardRect.y, boardRect.w, boardRect.h)) { board() }
        if (checkmate) {
            ArtImage(R.drawable.fx_checkmate, maxEdge = ArtSizes.BOARD, trim = true, alpha = 0.75f,
                modifier = Modifier.at(290, 530, 500, 500))
        }
        // Chạm banner để cất đi, xem lại thế cờ cuối; ván mới (kết quả đổi) thì banner hiện lại.
        var resultHidden by remember(result) { mutableStateOf(false) }
        if (result != null && !resultHidden) {
            ArtImage(result.res, maxEdge = ArtSizes.BOARD, trim = true,
                modifier = Modifier.at(90, 600, 900, 360)
                    .clickable(role = Role.Button) { resultHidden = true })
        }
        if (notice != null) {
            Box(modifier = Modifier.at(100, 340, 880, 860), contentAlignment = Alignment.TopCenter) { notice() }
        }

        PlayerFrame(player = you, upper = false, top = GamePlayLayout.PLAYER_TOP)

        val xs = actionSlotsX(actions.size)
        actions.forEachIndexed { i, action -> ActionButton(action, x = xs[i]) }

        if (belowBoard != null) {
            Box(
                modifier = Modifier.at(60, GamePlayLayout.DETAILS_TOP, 960, GamePlayLayout.DETAILS_HEIGHT)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = d.dp(8)),
            ) { belowBoard() }
        }
    }
}

/** Paint the circular portrait behind the intact frame and map every slot from source pixels. */
@Composable
private fun PlayerFrame(player: MatchPlayer, upper: Boolean, top: Int) {
    val d = LocalDesign.current
    val slots = GamePlayLayout.player(upper)
    val frame = if (upper) R.drawable.game_play_frame_player_top else R.drawable.game_play_frame_player_bottom
    @Composable
    fun GamePlayLayout.Rect.placed(): Modifier = Modifier.at(48f + x, top + y, w, h)

    val avatar = slots.avatar
    Box(modifier = avatar.placed().clip(CircleShape)) {
        val portrait = player.portrait ?: pieceImageRes(
            if (player.piece == Piece.NONE) Piece.of(Piece.KING, true) else player.piece,
        )
        ArtImage(portrait, maxEdge = ArtSizes.PORTRAIT, trim = true,
            contentDescription = player.name, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize())
    }
    // Preserve the original canvas transform rather than recalculating crop after bitmap sampling.
    Box(modifier = Modifier.at(48, top, GamePlayLayout.FRAME_WIDTH, slots.height).clipToBounds()) {
        ArtImage(frame, maxEdge = ArtSizes.BUTTON, trim = false, contentScale = ContentScale.FillBounds,
            modifier = Modifier.offset(d.dp(-slots.crop.x * slots.scale), d.dp(-slots.crop.y * slots.scale))
                .size(d.dp(1536f * slots.scale), d.dp(1024f * slots.scale)))
    }
    DesignText(text = player.name, size = 30, weight = FontWeight.Bold,
        color = if (player.active) GothicColors.Gold else GothicColors.Parchment,
        align = TextAlign.Start, alignment = Alignment.CenterStart, maxLines = 1,
        modifier = slots.name.placed())
    if (player.subtitle.isNotEmpty()) {
        DesignText(text = player.subtitle, size = 20,
            color = GothicColors.Parchment.copy(alpha = 0.85f),
            align = TextAlign.Start, alignment = Alignment.CenterStart, maxLines = 1,
            modifier = slots.subtitle.placed())
    }
    DesignText(text = player.clock ?: "∞", size = if (upper) 28 else 24,
        weight = if (player.active) FontWeight.Bold else FontWeight.Normal,
        color = when {
            player.clockLow -> GothicColors.Blood
            player.active -> GothicColors.Gold
            else -> GothicColors.Parchment
        }, maxLines = 1, modifier = slots.clock.placed())

    if (player.captured.isNotEmpty()) {
        PlayerCaptureRow(player, modifier = slots.captures.placed())
    }
}

/** Each header shows pieces captured BY that player, with scroll access for a full set. */
@Composable
private fun PlayerCaptureRow(player: MatchPlayer, modifier: Modifier) {
    val d = LocalDesign.current
    val pieces = player.captured.sortedByDescending { CaptureTally.valueOf(it) }
    Row(modifier = modifier.horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(d.dp(4))) {
        pieces.forEach { piece ->
            ArtImage(pieceImageRes(piece), maxEdge = ArtSizes.PIECE, trim = true,
                contentScale = ContentScale.Fit, modifier = Modifier.size(d.dp(42)))
        }
        if (player.advantage > 0) {
            DesignText(text = "+${player.advantage}", size = 24,
                color = GothicColors.Gold, modifier = Modifier.size(d.dp(64), d.dp(42)))
        }
    }
}

/**
 * Nút tròn 110x110 kèm nhãn nhỏ bên dưới: chỉ icon thì người chơi phải đoán, mà đoán sai
 * ở nút Đầu hàng là mất luôn ván.
 */
@Composable
private fun ActionButton(action: MatchAction, x: Int) {
    val d = LocalDesign.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Nút có ảnh disabled riêng thì không làm mờ thêm; chỉ làm mờ nút một ảnh và nhãn chữ.
    val dim = if (action.enabled) 1f else 0.4f
    val artDim = if (action.art != null && !action.art.singleState) 1f else dim
    val pressScale by animateFloatAsState(
        targetValue = if (pressed && action.enabled && action.art?.singleState == true) 0.94f else 1f,
        label = "gameplayActionPress",
    )
    Box(
        modifier = Modifier.at(x, ACTION_TOP, ACTION_SIZE, ACTION_SIZE)
            .clip(CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = if (action.art != null && !action.art.singleState) null else LocalIndication.current,
                enabled = action.enabled,
                role = Role.Button,
                onClick = action.onClick,
            )
            .alpha(artDim)
            .scale(pressScale),
        contentAlignment = Alignment.Center,
    ) {
        if (action.art != null) {
            ArtImage(action.art.resFor(action.enabled, pressed), maxEdge = ArtSizes.ICON, trim = true,
                contentDescription = action.label, modifier = Modifier.fillMaxSize())
        } else {
            ArtImage(R.drawable.radio_unselected, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.fillMaxSize())
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = GothicColors.Gold,
                modifier = Modifier.size(d.dp(52)),
            )
        }
    }
    DesignText(
        text = action.label,
        size = 24,
        maxLines = 2,
        modifier = Modifier.at(x - 30, ACTION_TOP + ACTION_SIZE + 4, ACTION_SIZE + 60, 56).alpha(dim),
    )
}

private const val ACTION_TOP = GamePlayLayout.ACTIONS_TOP
private const val ACTION_SIZE = 110
