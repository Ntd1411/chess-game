package kma.game.chess2d.ui.board

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.PieceOnBoard

/**
 * Bàn cờ.
 *
 * 64 ô và toàn bộ highlight được vẽ trong <b>một</b> [Canvas] duy nhất — theo đúng mục
 * 5.3. Xếp 64 composable lồng nhau thì mỗi lần chọn quân sẽ kéo theo một lần
 * recomposition của cả cây layout, trong khi vẽ Canvas chỉ là một lần redraw.
 *
 * Riêng quân cờ là composable độc lập, vì chúng cần animate vị trí — thứ không làm
 * được nếu quân cũng nằm trong Canvas.
 */
@Composable
fun ChessBoard(
    pieces: List<PieceOnBoard>,
    selectedSquare: Int,
    legalTargets: Set<Int>,
    lastMoveFrom: Int,
    lastMoveTo: Int,
    checkedKingSquare: Int,
    onSquareTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val squareSize = maxWidth / BOARD_EDGE
        val squarePx = with(LocalDensity.current) { squareSize.toPx() }
        val occupied = remember(pieces) { pieces.mapTo(HashSet()) { it.square } }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(squarePx) {
                    detectTapGestures { tap ->
                        val file = (tap.x / squarePx).toInt().coerceIn(0, BOARD_EDGE - 1)
                        val row = (tap.y / squarePx).toInt().coerceIn(0, BOARD_EDGE - 1)
                        // Hàng 1 nằm dưới cùng trên màn hình nhưng là rank 0 trong engine.
                        onSquareTap(Squares.of(file, BOARD_EDGE - 1 - row))
                    }
                },
        ) {
            for (square in 0 until Squares.COUNT) {
                val topLeft = Offset(squarePx * Squares.fileOf(square), squarePx * screenRow(square))
                val size = Size(squarePx, squarePx)
                val isLight = (Squares.fileOf(square) + Squares.rankOf(square)) % 2 != 0

                drawRect(if (isLight) BoardColors.lightSquare else BoardColors.darkSquare, topLeft, size)
                if (square == lastMoveFrom || square == lastMoveTo) {
                    drawRect(BoardColors.lastMove, topLeft, size)
                }
                if (square == checkedKingSquare) drawRect(BoardColors.check, topLeft, size)
                if (square == selectedSquare) drawRect(BoardColors.selected, topLeft, size)
            }

            // Gợi ý nước đi vẽ sau cùng để không bị màu ô nào phủ lên.
            for (target in legalTargets) {
                val center = Offset(
                    squarePx * Squares.fileOf(target) + squarePx / 2f,
                    squarePx * screenRow(target) + squarePx / 2f,
                )
                if (occupied.contains(target)) {
                    // Ô có quân địch: vòng tròn viền để không che mất quân bên dưới.
                    drawCircle(
                        color = BoardColors.legalTarget,
                        radius = squarePx * 0.44f,
                        center = center,
                        style = Stroke(width = squarePx * 0.07f),
                    )
                } else {
                    drawCircle(BoardColors.legalTarget, squarePx * 0.15f, center)
                }
            }
        }

        for (piece in pieces) {
            // key theo id quân: đây chính là thứ biến một cú nhảy thành một chuyển động.
            key(piece.id) {
                val x by animateDpAsState(
                    targetValue = squareSize * Squares.fileOf(piece.square),
                    animationSpec = tween(MOVE_ANIMATION_MILLIS),
                    label = "pieceX",
                )
                val y by animateDpAsState(
                    targetValue = squareSize * screenRow(piece.square),
                    animationSpec = tween(MOVE_ANIMATION_MILLIS),
                    label = "pieceY",
                )
                Box(
                    modifier = Modifier.offset(x, y).size(squareSize),
                    contentAlignment = Alignment.Center,
                ) {
                    PieceGlyph(piece.piece, squareSize)
                }
            }
        }
    }
}

/**
 * Vẽ một quân cờ.
 *
 * Dùng ký tự Unicode thay vì bộ vector: không vướng license (mục 6 — nhiều bộ quân
 * phổ biến là CC BY-SA), không thêm asset, và đủ tốt để đạt mọi tiêu chí Phase 2.
 * Thay bằng VectorDrawable sau này chỉ cần sửa đúng hàm này.
 *
 * Cả hai màu đều dùng glyph đặc rồi tô màu, vì glyph quân trắng rỗng ruột của Unicode
 * gần như vô hình trên ô sáng. Viền được vẽ thành một lớp riêng bên dưới.
 */
@Composable
private fun PieceGlyph(piece: Byte, squareSize: Dp) {
    val glyph = glyphOf(piece)
    val white = Piece.isWhite(piece)
    val density = LocalDensity.current
    val fontSize = with(density) { (squareSize.toPx() * GLYPH_SCALE).toSp() }
    val outlineWidth = with(density) { squareSize.toPx() * OUTLINE_SCALE }

    Box(contentAlignment = Alignment.Center) {
        Text(
            text = glyph,
            style = TextStyle(
                color = if (white) BoardColors.whitePieceOutline else BoardColors.blackPieceOutline,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                drawStyle = Stroke(width = outlineWidth),
            ),
        )
        Text(
            text = glyph,
            style = TextStyle(
                color = if (white) BoardColors.whitePiece else BoardColors.blackPiece,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

/** Chỉ lấy glyph đặc theo loại quân; màu quân do [PieceGlyph] tô. */
internal fun glyphOf(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "\u265A"
    Piece.QUEEN -> "\u265B"
    Piece.ROOK -> "\u265C"
    Piece.BISHOP -> "\u265D"
    Piece.KNIGHT -> "\u265E"
    else -> "\u265F"
}

/** Hàng trên màn hình: rank 7 ở trên cùng nên Trắng ngồi phía dưới. */
private fun screenRow(square: Int): Int = BOARD_EDGE - 1 - Squares.rankOf(square)

private const val BOARD_EDGE = 8
private const val MOVE_ANIMATION_MILLIS = 180
private const val GLYPH_SCALE = 0.74f
private const val OUTLINE_SCALE = 0.03f
