package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R

/**
 * Danh sách nước đi hai cột (mục 7.2): mỗi dòng là một nước đôi Trắng – Đen.
 *
 * Nhận sẵn ký hiệu SAN từ [kma.game.chess2d.game.GameUiState] chứ không tự sinh: sinh
 * SAN cần thế cờ trước từng nước, đó là việc của engine và ViewModel, không phải
 * việc của một hàm vẽ.
 *
 * Không dùng LazyColumn vì khủng ván đấu đã cuộn dọc sẵn; lồng hai vùng cuộn cùng
 * chiều vào nhau chỉ làm ngón tay của người chơi bị tranh chấp.
 *
 * @param sanMoves danh sách nước đã đi theo thứ tự, Trắng ở các chỉ số chẵn.
 */
@Composable
fun MoveList(sanMoves: List<String>, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.moves_title),
                style = MaterialTheme.typography.labelLarge,
            )

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
                    white = sanMoves[index],
                    black = sanMoves.getOrNull(index + 1),
                )
            }
        }
    }
}

/** Một dòng: số thứ tự, nước của Trắng, nước của Đen. */
@Composable
private fun MoveRow(number: Int, white: String, black: String?) {
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
        Text(
            text = white,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = black ?: "",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Bề ngang cố định cho số thứ tự để hai cột nước đi luôn thẳng hàng. */
private const val NUMBER_WIDTH = 28
