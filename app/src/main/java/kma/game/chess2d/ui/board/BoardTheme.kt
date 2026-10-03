package kma.game.chess2d.ui.board

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import kma.game.chess2d.R
import kma.game.chess2d.engine.Piece

/**
 * Bộ màu bàn cờ và bộ quân đổi được — mục 7.3.
 *
 * Tách thành enum thay vì những hằng số rải rác để ba việc sau đều rẻ: lưu lựa chọn
 * xuống DataStore (chỉ cần lưu tên enum), vẽ danh sách để người chơi chọn (duyệt
 * `entries`), và thêm bộ mới về sau (thêm một dòng).
 *
 * Màu ô **không** lấy từ dynamic color của hệ thống: bàn cờ cần độ tương phản ổn
 * định giữa ô sáng và ô tối, nên người chơi đổi bằng bộ màu ở đây chứ không bằng
 * hình nền điện thoại.
 */
enum class BoardPalette(val lightSquare: Color, val darkSquare: Color, val textured: Boolean = false) {
    /**
     * Bộ gothic: ô đá cẩm thạch ngà và obsidian vân vàng vẽ từ ảnh, có lớp phủ phát sáng cho
     * ô chọn, nước đi, nước ăn và vua bị chiếu. Hai màu phẳng chỉ là màu dự phòng lúc ảnh
     * chưa nạp xong. Là bộ mặc định.
     */
    GOTHIC(Color(0xFFD9CDB4), Color(0xFF26262C), textured = true),

    /** Bộ xanh lá quen thuộc từ Phase 2. */
    GREEN(Color(0xFFEEEED2), Color(0xFF769656)),

    /** Bộ gỗ nâu, giống bàn cờ gỗ thật. */
    WOOD(Color(0xFFF0D9B5), Color(0xFFB58863)),

    /** Bộ xanh dương, dễ nhìn với người khó phân biệt đỏ — lục. */
    OCEAN(Color(0xFFDEE3E6), Color(0xFF5C87A6)),

    /** Bộ xám trung tính, đỡ chói khi dùng ban đêm. */
    SLATE(Color(0xFFD8D8D8), Color(0xFF6E6E6E)),
}

/**
 * Bộ quân.
 *
 * Vẫn dùng glyph Unicode chứ không dùng ảnh, vì asset bộ quân còn phải đợi xác nhận
 * license (mục 6 — chỉ nhận MIT). Ba cách vẽ dưới đây đều lấy từ font hệ thống nên
 * không vướng license và không làm nặng APK. Khi có bộ vector MIT thì thêm một
 * giá trị enum nữa, phần còn lại của màn hình không phải sửa.
 */
enum class PieceTheme {
    /** Glyph đặc cho cả hai bên, phân biệt bằng màu thân và màu viền. */
    SOLID,

    /** Trắng dùng glyph rỗng ruột, Đen dùng glyph đặc — kiểu sách cờ in. */
    OUTLINE,

    /** Chữ cái K Q R B N P, dành cho người quen đọc ký hiệu hơn quen hình quân. */
    LETTER,

    /**
     * Ảnh PNG vẽ sẵn cho từng quân (bộ AI-generated, đã xác nhận dùng thoải mái —
     * không vướng yêu cầu license ở mục 6). Không dùng glyphOf: [PieceGlyph] rẽ
     * nhánh sang [pieceImageRes] trước khi gọi glyphOf.
     */
    IMAGE,
    ;

    /**
     * Ký tự cần vẽ cho [piece] theo bộ quân này.
     *
     * Biết màu quân là cần thiết: chỉ riêng bộ [OUTLINE] mới đổi glyph theo màu, hai
     * bộ còn lại dùng chung một ký tự rồi để [PieceGlyph] tô màu.
     */
    fun glyphOf(piece: Byte): String = when (this) {
        SOLID -> solidGlyph(piece)
        OUTLINE -> if (Piece.isWhite(piece)) hollowGlyph(piece) else solidGlyph(piece)
        LETTER -> letterGlyph(piece)
        // Không có nơi nào gọi glyphOf khi IMAGE (xem PieceGlyph), nhưng vẫn cần một
        // giá trị hợp lệ để when ở trên là exhaustive.
        IMAGE -> solidGlyph(piece)
    }
}

/**
 * Resource ảnh cho [piece] khi dùng [PieceTheme.IMAGE].
 *
 * Bộ ảnh AI-generated, đặt trong res/drawable/ với tên piece_<mau>_<loai>.png
 * (piece_white_king, ...). Quân Đen dùng bản sáng piece_black_<loai>_bright: bản cũ chỉ
 * sáng khoảng 70 trên ô tối khoảng 45 nên khó thấy, bản bright có viền ngà để nổi trên ô tối.
 */
@DrawableRes
internal fun pieceImageRes(piece: Byte): Int {
    val white = Piece.isWhite(piece)
    return when (Piece.typeOf(piece)) {
        Piece.KING -> if (white) R.drawable.piece_white_king else R.drawable.piece_black_king_bright
        Piece.QUEEN -> if (white) R.drawable.piece_white_queen else R.drawable.piece_black_queen_bright
        Piece.ROOK -> if (white) R.drawable.piece_white_rook else R.drawable.piece_black_rook_bright
        Piece.BISHOP -> if (white) R.drawable.piece_white_bishop else R.drawable.piece_black_bishop_bright
        Piece.KNIGHT -> if (white) R.drawable.piece_white_knight else R.drawable.piece_black_knight_bright
        else -> if (white) R.drawable.piece_white_pawn else R.drawable.piece_black_pawn_bright
    }
}

/** Glyph đặc (khối quân Đen của Unicode), dùng cho cả hai bên rồi tô màu sau. */
private fun solidGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "\u265A"
    Piece.QUEEN -> "\u265B"
    Piece.ROOK -> "\u265C"
    Piece.BISHOP -> "\u265D"
    Piece.KNIGHT -> "\u265E"
    else -> "\u265F"
}

/** Glyph rỗng ruột (khối quân Trắng của Unicode). */
private fun hollowGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "\u2654"
    Piece.QUEEN -> "\u2655"
    Piece.ROOK -> "\u2656"
    Piece.BISHOP -> "\u2657"
    Piece.KNIGHT -> "\u2658"
    else -> "\u2659"
}

/** Chữ cái theo ký hiệu quốc tế; tốt viết là "P" cho dễ đọc trên màn hình nhỏ. */
private fun letterGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "K"
    Piece.QUEEN -> "Q"
    Piece.ROOK -> "R"
    Piece.BISHOP -> "B"
    Piece.KNIGHT -> "N"
    else -> "P"
}
