package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.ui.art.gothicBackdrop

/**
 * Một người chơi trong khung ván đấu.
 *
 * @param subtitle dòng phụ đề đổi theo chế độ: LAN hiện độ trễ, đấu máy hiện
 *        "đang tính…", hai người một máy hiện bên Trắng/Đen.
 * @param active đang tới lượt bên này.
 * @param clock giờ còn lại dạng `mm:ss`; `null` là ván không bấm giờ nên không vẽ đồng hồ.
 * @param clockLow giờ sắp hết: đồng hồ đổi sang màu cảnh báo.
 */
data class MatchPlayer(
    val name: String,
    val subtitle: String = "",
    val piece: Byte = Piece.NONE,
    val active: Boolean = false,
    val clock: String? = null,
    val clockLow: Boolean = false,
)

/**
 * Một nút ở hàng dưới cùng.
 *
 * Nút không dùng được ở chế độ hiện tại thì **không truyền vào danh sách** chứ không
 * phải truyền kèm enabled = false: một hàng nút xám rải rác khó đọc hơn là không có.
 * [enabled] chỉ dành cho nút đúng là của chế độ này nhưng tạm thời chưa bấm được,
 * ví dụ Đi lại khi chưa có nước nào.
 */
data class MatchAction(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

/**
 * Khung ván đấu dùng chung cho cả ba chế độ (hai người một máy, đấu máy, LAN).
 *
 * Từ trên xuống: đối thủ → bàn cờ → mình → hàng nút icon. Đổi chế độ chỉ đổi tên,
 * phụ đề và hàng nút; bố cục giữ nguyên để người chơi không phải học lại màn hình.
 *
 * Hai ràng buộc bố cục được cố ý đặt ở đây chứ không ở từng màn hình con:
 * 1. Toàn bộ cột **cuộn được**. Máy nhỏ hoặc cỡ chữ lớn nhất từng đẩy hàng nút ra
 *    khỏi màn hình, mà đó là hàng chứa Rời phòng — tức là kẹt trong phòng.
 * 2. Bàn cờ đi kèm `aspectRatio(1f)` nên luôn vuông, không bị bóp méo theo chiều cao
 *    còn lại.
 *
 * @param headline dòng trạng thái ngắn ở trên cùng (lượt ai, chiếu, kết quả).
 * @param notice ô thông báo cần người dùng quyết định, ví dụ thử nối lại ở chế độ LAN.
 * @param belowBoard phần phụ nằm dưới hàng nút, ví dụ danh sách nước đi. Đặt ở đây
 *        để mọi chế độ dùng chung một chỗ, không mỗi màn hình đặt một kiểu.
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
    @DrawableRes backdrop: Int = R.drawable.screen_chess_battle,
    board: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .gothicBackdrop(backdrop)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (headline != null) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
        }

        notice?.invoke()

        PlayerRow(player = opponent)

        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
            board()
        }

        PlayerRow(player = you)

        ActionRow(actions = actions)

        belowBoard?.invoke()
    }
}

/** Một hàng người chơi: avatar, tên, phụ đề. */
@Composable
private fun PlayerRow(player: MatchPlayer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlayerAvatar(name = player.name, piece = player.piece, active = player.active)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (player.subtitle.isNotEmpty()) {
                Text(
                    text = player.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        player.clock?.let { clock ->
            Text(
                text = clock,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = if (player.active) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    player.clockLow -> MaterialTheme.colorScheme.error
                    player.active -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
            )
        }
    }
}

/**
 * Hàng nút icon.
 *
 * Mỗi nút là icon kèm nhãn chữ nhỏ: chỉ icon thì người chơi phải đoán, mà đoán sai
 * ở nút Đầu hàng là mất luôn ván.
 */
@Composable
private fun ActionRow(actions: List<MatchAction>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Top,
    ) {
        for (action in actions) {
            Column(
                modifier = Modifier.width(ACTION_WIDTH.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                IconButton(onClick = action.onClick, enabled = action.enabled) {
                    Icon(imageVector = action.icon, contentDescription = action.label)
                }
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}

/** Bề ngang cố định cho mỗi nút để nhãn dài ngắn khác nhau không làm hàng nút nhảy. */
private const val ACTION_WIDTH = 64
