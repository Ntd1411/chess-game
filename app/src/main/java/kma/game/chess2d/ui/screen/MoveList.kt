package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import kma.game.chess2d.R
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Fen
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.theme.GothicColors

/** Labels retain PGN numbering, including games starting with Black from a custom FEN. */
internal fun moveStripLabels(sanMoves: List<String>, firstMoveNumber: Int = 1, whiteStarts: Boolean = true): List<String> =
    sanMoves.mapIndexed { index, san ->
        val white = (index % 2 == 0) == whiteStarts
        val number = firstMoveNumber + (index + if (whiteStarts) 0 else 1) / 2
        when {
            white -> "$number. $san"
            index == 0 -> "$number... $san"
            else -> san
        }
    }

/** Follow new moves only at the live end, never pull the user away while browsing older moves. */
internal fun moveStripScrollTarget(
    moveCount: Int,
    reviewPly: Int?,
    previousMoveCount: Int,
    lastVisibleIndex: Int,
    previousReviewPly: Int?,
): Int? = when {
    moveCount == 0 -> null
    reviewPly != null -> reviewPly.coerceIn(0, moveCount - 1)
    previousReviewPly != null || previousMoveCount == 0 || moveCount < previousMoveCount -> moveCount - 1
    lastVisibleIndex >= previousMoveCount - 2 -> moveCount - 1
    else -> null
}

/** One swipeable SAN strip. Review taps are enabled only when the caller supports board review. */
@Composable
fun MoveList(
    sanMoves: List<String>,
    modifier: Modifier = Modifier,
    reviewPly: Int? = null,
    onSelectPly: ((Int?) -> Unit)? = null,
    startFen: String = Engine.START_FEN,
) {
    val d = LocalDesign.current
    val shape = RoundedCornerShape(d.dp(18))
    val initial = remember(startFen) { Fen.parse(startFen) }
    val labels = remember(sanMoves, startFen) {
        moveStripLabels(sanMoves, initial.fullmoveNumber, initial.whiteToMove)
    }
    val scroll = rememberLazyListState()
    var previousCount by remember { mutableStateOf(0) }
    var previousReview by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(sanMoves.size, reviewPly) {
        val lastVisible = scroll.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
        val target = moveStripScrollTarget(sanMoves.size, reviewPly, previousCount, lastVisible, previousReview)
        if (target != null) scroll.animateScrollToItem(target)
        previousCount = sanMoves.size
        previousReview = reviewPly
    }

    Box(modifier = modifier.background(GothicColors.Ink.copy(alpha = 0.95f), shape)
        .border(d.dp(2), GothicColors.GoldDim.copy(alpha = 0.45f), shape)) {
        if (sanMoves.isEmpty()) {
            DesignText(text = stringResource(R.string.moves_empty), size = 28, color = GothicColors.Muted,
                modifier = Modifier.fillMaxSize(), maxLines = 1)
        } else {
            Row(modifier = Modifier.fillMaxSize().padding(horizontal = d.dp(10)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(d.dp(10))) {
                if (reviewPly != null && onSelectPly != null) {
                    DesignText(text = stringResource(R.string.moves_back_to_live), size = 28,
                        color = GothicColors.Gold, weight = FontWeight.Bold, maxLines = 1,
                        modifier = Modifier.height(d.dp(64))
                            .clickable(role = Role.Button) { onSelectPly(null) }
                            .padding(horizontal = d.dp(12)))
                }
                LazyRow(state = scroll, modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(d.dp(8))) {
                    itemsIndexed(labels, key = { index, _ -> index }) { index, label ->
                        val selected = reviewPly == index
                        val latest = reviewPly == null && index == sanMoves.lastIndex
                        val chipShape = RoundedCornerShape(d.dp(12))
                        val click = if (onSelectPly == null) Modifier else Modifier.clickable(role = Role.Button) {
                            onSelectPly(if (selected) null else index)
                        }
                        DesignText(text = label, size = 30, maxLines = 1,
                            weight = if (selected || latest) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected || latest) GothicColors.Gold else GothicColors.Parchment,
                            modifier = Modifier.height(d.dp(60))
                                .background(if (selected || latest) GothicColors.Gold.copy(alpha = 0.14f)
                                    else GothicColors.Ink, chipShape)
                                .then(click).padding(horizontal = d.dp(16)))
                    }
                }
            }
        }
    }
}
