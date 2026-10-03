package kma.game.chess2d.ui.board

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.rememberArt
import kma.game.chess2d.ui.theme.GothicColors
import kotlin.math.roundToInt
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
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
 *
 * @param flipped xoay bàn 180 độ để quân Đen ngồi phía dưới. Cần cho chế độ LAN: khách
 *        cầm Đen mà vẫn thấy Trắng ở dưới thì mọi trực giác về hướng tiến quân bị đảo
 *        ngược. Chỉ đổi cách <b>vẽ</b> và cách đọc cú chạm; số ô của engine không đổi.
 * @param palette bộ màu ô bàn cờ (mục 7.3). Màu ô luôn lấy từ đây chứ không lấy từ
 *        dynamic color của hệ thống, để độ tương phản sáng/tối không phụ thuộc hình nền.
 * @param pieceTheme bộ quân đang chọn; chỉ đổi ký tự được vẽ, không đổi luật gì cả.
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
    flipped: Boolean = false,
    palette: BoardPalette = BoardPalette.GOTHIC,
    pieceTheme: PieceTheme = PieceTheme.IMAGE,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val squareSize = maxWidth / BOARD_EDGE
        val squarePx = with(LocalDensity.current) { squareSize.toPx() }
        val occupied = remember(pieces) { pieces.mapTo(HashSet()) { it.square } }

        // Ảnh ô và lớp phủ của bộ gothic, đã được nạp trước ở splash nên thường có ngay. Chưa có (null)
        // thì Canvas vẽ màu phẳng dự phòng của bộ màu thay vì chờ.
        val tileLight = rememberArt(R.drawable.tile_marble_light, ArtSizes.OVERLAY)
        val tileDark = rememberArt(R.drawable.tile_marble_dark, ArtSizes.OVERLAY)
        val selectedOverlay = rememberArt(R.drawable.tile_selected_overlay, ArtSizes.OVERLAY)
        val moveOverlay = rememberArt(R.drawable.tile_move_overlay, ArtSizes.OVERLAY)
        val attackOverlay = rememberArt(R.drawable.tile_attack_overlay, ArtSizes.OVERLAY)
        val checkOverlay = rememberArt(R.drawable.tile_check_overlay, ArtSizes.OVERLAY)
        val textured = palette.textured

        // Ô đang được kéo và khoảng đã kéo (đơn vị px). Kéo thả chỉ là một lối vào khác
        // của đúng luồng tap: nhấc quân = chạm ô nguồn, nhả quân = chạm ô đích. Nhờ vậy
        // mọi luật chọn quân, phong cấp, chặn khi xem lại hay khi máy đang nghĩ đều dùng
        // chung một đường, không có đường thứ hai để lệch nhau.
        var dragSquare by remember { mutableStateOf(Squares.NONE) }
        var dragOffset by remember { mutableStateOf(Offset.Zero) }

        // Hai phép đổi tọa độ duy nhất trong file nằm ở [squareCol] và [squareRow]; ở đây
        // chỉ gọi lại cho gọn để việc lật bàn không rải rác thành tám phép trừ ở tám nơi.
        val screenCol = { square: Int -> squareCol(square, flipped) }
        val screenRow = { square: Int -> squareRow(square, flipped) }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(squarePx, flipped) {
                    detectTapGestures { tap ->
                        onSquareTap(squareAt(tap, squarePx, flipped))
                    }
                }
                .pointerInput(squarePx, flipped, occupied) {
                    detectDragGestures(
                        onDragStart = { start ->
                            val square = squareAt(start, squarePx, flipped)
                            // Chỉ nhấc được ô có quân; kéo từ ô trống là người chơi đang
                            // cuộn màn hình hoặc lỡ tay, không phải đang đi nước nào.
                            if (occupied.contains(square)) {
                                dragSquare = square
                                dragOffset = Offset.Zero
                                onSquareTap(square)
                            }
                        },
                        onDrag = { change, amount ->
                            if (dragSquare != Squares.NONE) {
                                change.consume()
                                dragOffset += amount
                            }
                        },
                        onDragEnd = {
                            if (dragSquare != Squares.NONE) {
                                // Ô nhả tính từ tâm quân đang kéo, không từ điểm ngón tay:
                                // ngón tay thường lệch xuống dưới quân khi kéo.
                                val center = Offset(
                                    squarePx * (squareCol(dragSquare, flipped) + 0.5f) + dragOffset.x,
                                    squarePx * (squareRow(dragSquare, flipped) + 0.5f) + dragOffset.y,
                                )
                                val target = squareAt(center, squarePx, flipped)
                                if (target != dragSquare) onSquareTap(target)
                                dragSquare = Squares.NONE
                                dragOffset = Offset.Zero
                            }
                        },
                        onDragCancel = {
                            dragSquare = Squares.NONE
                            dragOffset = Offset.Zero
                        },
                    )
                },
        ) {
            for (square in 0 until Squares.COUNT) {
                val topLeft = Offset(squarePx * screenCol(square), squarePx * screenRow(square))
                val size = Size(squarePx, squarePx)
                // Màu ô tính theo ô của engine, không theo vị trí màn hình: a1 phải luôn
                // là ô tối kể cả khi lật bàn.
                val isLight = (Squares.fileOf(square) + Squares.rankOf(square)) % 2 != 0
                val col = screenCol(square)
                val row = screenRow(square)
                val tile = if (isLight) tileLight else tileDark

                if (textured && tile != null) {
                    drawTile(tile, col, row, squarePx)
                } else {
                    drawRect(if (isLight) palette.lightSquare else palette.darkSquare, topLeft, size)
                }
                if (square == lastMoveFrom || square == lastMoveTo) {
                    drawRect(BoardColors.lastMove, topLeft, size)
                }
                if (square == checkedKingSquare) {
                    drawRect(BoardColors.check, topLeft, size)
                    if (textured && checkOverlay != null) drawTile(checkOverlay, col, row, squarePx)
                }
                if (square == selectedSquare) {
                    if (textured && selectedOverlay != null) {
                        drawTile(selectedOverlay, col, row, squarePx)
                    } else {
                        drawRect(BoardColors.selected, topLeft, size)
                    }
                }
            }

            // Viền vàng mảnh quanh bàn cờ gothic, nằm trong khung 8x8 nên không làm bàn lệch ô.
            if (textured) {
                val edge = squarePx * 0.05f
                drawRect(
                    color = GothicColors.Gold.copy(alpha = 0.85f),
                    topLeft = Offset(edge / 2f, edge / 2f),
                    size = Size(this.size.width - edge, this.size.height - edge),
                    style = Stroke(width = edge),
                )
            }

            // Gợi ý nước đi vẽ sau cùng để không bị màu ô nào phủ lên.
            for (target in legalTargets) {
                val capture = occupied.contains(target)
                val overlay = if (capture) attackOverlay else moveOverlay
                if (textured && overlay != null) {
                    drawTile(overlay, screenCol(target), screenRow(target), squarePx)
                    continue
                }
                val center = Offset(
                    squarePx * screenCol(target) + squarePx / 2f,
                    squarePx * screenRow(target) + squarePx / 2f,
                )
                if (capture) {
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
                    targetValue = squareSize * screenCol(piece.square),
                    animationSpec = tween(MOVE_ANIMATION_MILLIS),
                    label = "pieceX",
                )
                val y by animateDpAsState(
                    targetValue = squareSize * screenRow(piece.square),
                    animationSpec = tween(MOVE_ANIMATION_MILLIS),
                    label = "pieceY",
                )
                // Quân đang kéo đi theo ngón tay: cộng thêm khoảng đã kéo vào vị trí ô.
                val dragging = piece.square == dragSquare
                val dragX = if (dragging) with(LocalDensity.current) { dragOffset.x.toDp() } else 0.dp
                val dragY = if (dragging) with(LocalDensity.current) { dragOffset.y.toDp() } else 0.dp
                Box(
                    modifier = Modifier
                        .offset(x + dragX, y + dragY)
                        .size(squareSize)
                        .zIndex(if (dragging) 1f else 0f),
                    contentAlignment = Alignment.Center,
                ) {
                    PieceGlyph(piece.piece, squareSize, pieceTheme)
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
private fun PieceGlyph(piece: Byte, squareSize: Dp, theme: PieceTheme) {
    if (theme == PieceTheme.IMAGE) {
        // Bộ ảnh AI-generated đã có ảnh riêng cho từng quân/màu, không cần tô màu hay vẽ viền như
        // glyph. Một quầng tối mờ phía sau tách quân khỏi ô (quân Trắng trên ô ngà dễ bị chìm).
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier.size(squareSize * 0.8f).background(
                    Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)),
                ),
            )
            ArtImage(
                res = pieceImageRes(piece),
                maxEdge = ArtSizes.PIECE,
                modifier = Modifier.size(squareSize * 0.94f),
                contentScale = ContentScale.Fit,
            )
        }
        return
    }

    val glyph = theme.glyphOf(piece)
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

/**
 * Glyph đặc theo loại quân; màu quân do nơi vẽ tô.
 *
 * Giữ lại hàm này cho những chỗ chỉ cần một ký tự quân ngoài bàn cờ (hộp thoại phong
 * cấp, hàng quân bị bắt, ảnh đại diện người chơi) — ở đó luôn dùng glyph đặc để nhìn
 * rõ trên nền phẳng, không phụ thuộc bộ quân đang chọn.
 */
internal fun glyphOf(piece: Byte): String = PieceTheme.SOLID.glyphOf(piece)

/** Vẽ [image] kín ô (cột [col], hàng [row] trên màn hình). Làm tròn hai đầu để các ô liền nhau không hở khe. */
private fun DrawScope.drawTile(image: ImageBitmap, col: Int, row: Int, squarePx: Float) {
    val x0 = (col * squarePx).roundToInt()
    val y0 = (row * squarePx).roundToInt()
    val x1 = ((col + 1) * squarePx).roundToInt()
    val y1 = ((row + 1) * squarePx).roundToInt()
    drawImage(
        image = image,
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(x0, y0),
        dstSize = IntSize(x1 - x0, y1 - y0),
        filterQuality = FilterQuality.Medium,
    )
}

/** Cột trên màn hình của một ô engine. */
private fun squareCol(square: Int, flipped: Boolean): Int {
    val file = Squares.fileOf(square)
    return if (flipped) BOARD_EDGE - 1 - file else file
}

/** Hàng trên màn hình của một ô engine: hàng 1 ở dưới cùng nhưng là rank 0 trong engine. */
private fun squareRow(square: Int, flipped: Boolean): Int {
    val rank = Squares.rankOf(square)
    return if (flipped) rank else BOARD_EDGE - 1 - rank
}

/** Ô engine ứng với một điểm trên màn hình; điểm ngoài bàn bị kéo về ô gần nhất. */
private fun squareAt(point: Offset, squarePx: Float, flipped: Boolean): Int {
    val col = (point.x / squarePx).toInt().coerceIn(0, BOARD_EDGE - 1)
    val row = (point.y / squarePx).toInt().coerceIn(0, BOARD_EDGE - 1)
    val file = if (flipped) BOARD_EDGE - 1 - col else col
    val rank = if (flipped) row else BOARD_EDGE - 1 - row
    return Squares.of(file, rank)
}

private const val BOARD_EDGE = 8
private const val MOVE_ANIMATION_MILLIS = 180
private const val GLYPH_SCALE = 0.74f
private const val OUTLINE_SCALE = 0.03f
