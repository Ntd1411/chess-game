package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R

/**
 * Danh sách nước đi hai cột kèm chế độ xem lại (mục 7.2): mỗi dòng là một nước đôi
 * Trắng – Đen, bấm vào một nước là xem thế cờ ngay sau nước đó.
 *
 * Nhận sẵn ký hiệu SAN từ [kma.game.chess2d.game.GameUiState] chứ không tự sinh: sinh
 * SAN cần thế cờ trước từng nước, đó là việc của engine và ViewModel, không phải
 * việc của một hàm vẽ.
 *
 * Không dùng LazyColumn vì khung ván đấu đã cuộn dọc sẵn; lồng hai vùng cuộn cùng
 * chiều vào nhau chỉ làm ngón tay của người chơi bị tranh chấp.
 *
 * @param sanMoves danh sách nước đã đi theo thứ tự, Trắng ở các chỉ số chẵn.
 * @param reviewPly nước đang xem lại, `null` khi đang ở thế hiện tại.
 * @param onSelectPly báo lên ViewModel nước vừa chọn; `null` nghĩa là về thế hiện tại.
 */
@Composable
fun MoveList(
    sanMoves: List<String>,
    modifier: Modifier = Modifier,
    reviewPly: Int? = null,
    onSelectPly: (Int?) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.moves_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                // Nút thoát xem lại chỉ hiện khi đang xem lại: đó cũng là cách báo cho người
                // chơi biết vì sao bàn cờ đang không cho đi.
                if (reviewPly != null) {
                    TextButton(onClick = { onSelectPly(null) }) {
                        Text(stringResource(R.string.moves_back_to_live))
                    }
                }
            }

            if (sanMoves.isEmpty()) {
                Text(
                    text = stringResource(R.string.moves_empty),
                    style = MaterialTheme.typography.bodySmall,
                )
                return@Column
            }

            // Đi từng bước 2: nước của Trắng và nước trả lời của Đen nằm chung một dòng,
            // giống cách biên bản cờ vẫn đánh số — và cũng là cách PGN đánh số ở Chặng 3.
            for (index in sanMoves.indices step 2) {
                MoveRow(
                    number = index / 2 + 1,
                    whitePly = index,
                    white = sanMoves[index],
                    black = sanMoves.getOrNull(index + 1),
                    reviewPly = reviewPly,
                    onSelectPly = onSelectPly,
                )
            }
        }
    }
}

/** Một dòng: số thứ tự, nước của Trắng, nước của Đen. */
@Composable
private fun MoveRow(
    number: Int,
    whitePly: Int,
    white: String,
    black: String?,
    reviewPly: Int?,
    onSelectPly: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(NUMBER_WIDTH.dp),
        )
        // Hai cột chia đôi phần còn lại bằng weight, nhờ vậy cột Đen không nhảy qua
        // nhảy lại khi nước của Trắng dài ngắn khác nhau ("e4" so với "Qxd7+").
        MoveCell(
            text = white,
            ply = whitePly,
            reviewPly = reviewPly,
            onSelectPly = onSelectPly,
            modifier = Modifier.weight(1f),
        )
        if (black == null) {
            // Giữ chỗ trống cho nước của Đen chưa đi, để dòng cuối không bị trải rộng ra.
            Text(text = "", modifier = Modifier.weight(1f))
        } else {
            MoveCell(
                text = black,
                ply = whitePly + 1,
                reviewPly = reviewPly,
                onSelectPly = onSelectPly,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Một ô nước đi bấm được.
 *
 * Bấm lại đúng nước đang xem thì về thế hiện tại, nhờ vậy không cần đi tìm nút
 * thoát mới ra khỏi chế độ xem lại được.
 */
@Composable
private fun MoveCell(
    text: String,
    ply: Int,
    reviewPly: Int?,
    onSelectPly: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selected = ply == reviewPly
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = if (selected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        modifier = modifier
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                shape = MaterialTheme.shapes.small,
            )
            .clickable { onSelectPly(if (selected) null else ply) }
            .padding(horizontal = 4.dp, vertical = 2.dp),
    )
}

/** Bề ngang cố định cho số thứ tự để hai cột nước đi luôn thẳng hàng. */
private const val NUMBER_WIDTH = 28
