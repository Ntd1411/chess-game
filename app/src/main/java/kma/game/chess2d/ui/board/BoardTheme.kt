package kma.game.chess2d.ui.board

import androidx.compose.ui.graphics.Color
import kma.game.chess2d.engine.Piece

/**
 * Bộ màu bàn cờ và bộ quań đổi được — mục 7.3.
 *
 * Tách thành enum thay vì những hằng số rải rác để ba việc sau đều rẻ: lưu lựa chọn
 * xuống DataStore (chỉ cần lưu tên enum), vẽ danh sách để người chơi chọn (duyệt
 * `entries`), và thêm bộ mới về sau (thêm một dòng).
 *
 * Màu Ổ **không** lấy từ dynamic color của hệ thống: bàn cờ cần độ tương phản ổn
 * định giữa ô sáng và ô tối, nên người chơi đổi bằng bộ màu ở đây chứ không bằng
 * hình nền điện thoại.
 */
enum class BoardPalette(val lightSquare: Color, val darkSquare: Color) {
    /** Bộ xanh lá quen thuộc, cũng là bộ mặc định từ Phase 2. */
    GREEN(Color(0xFFEEEED2), Color(0xFF769656)),

    /** Bộ gỗ nâu, giống bàn cờ gỗ thật. */
    WOOD(Color(0xFFF0D9B5), Color(0xFFB58863)),

    /** Bộ xanh dương, dễ nhìn với người khó phân biệt đỏ — lục. */
    OCEAN(Color(0xFFDEE3E6), Color(0xFF5C87A6)),

    /** Bộ xám trung tính, đỡ chói khi dùng ban đêm. */
    SLATE(Color(0xFFD8D8D8), Color(0xFF6E6E6E)),
}

/**
 * Bộ quań.
 *
 * Vẫn dùng glyph Unicode chứ không dùng ảnh, vì asset bộ quań còn phải đợi xác nhẩn
 * license (mục 6 — chỉ nhận MIT). Ba cách vẽ dưới đây đều lấy từ font hệ thống nên
 * không vướng license và không làm nặng APK. Khi có bộ vector MIT thì thêm một
 * giá trị enum nữa, phần còn lại của màn hình không phải sửa.
 */
enum class PieceTheme {
    /** Glyph đặc cho cả hai bên, phân biệt bằng màu thân và màu viền. */
    SOLID,

    /** Trắng dùng glyph rỗng ruột, Đen dùng glyph đặc — kiểu sách cờ in. */
    OUTLINE,

    /** Chứ cái K Q R B N P, dành cho người quen đọc ký hiệu hơn quen hình quań. */
    LETTER,
    ;

    /**
     * Ký tự cần vẽ cho [piece] theo bộ quań này.
     *
     * Biết màu quân là cần thiết: chỉ riêng bộ [OUTLINE] mới đổi glyph theo màu, hai
     * bộ còn lại dùng chung một ký tự rồi để [PieceGlyph] tô màu.
     */
    fun glyphOf(piece: Byte): String = when (this) {
        SOLID -> solidGlyph(piece)
        OUTLINE -> if (Piece.isWhite(piece)) hollowGlyph(piece) else solidGlyph(piece)
        LETTER -> letterGlyph(piece)
    }
}

/** Glyph đặc (khối quań Đen của Unicode), dùng cho cả hai bên rồi tô màu sau. */
private fun solidGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "\u265A"
    Piece.QUEEN -> "\u265B"
    Piece.ROOK -> "\u265C"
    Piece.BISHOP -> "\u265D"
    Piece.KNIGHT -> "\u265E"
    else -> "\u265F"
}

/** Glyph rỗng ruột (khối quań Trắng của Unicode). */
private fun hollowGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "\u2654"
    Piece.QUEEN -> "\u2655"
    Piece.ROOK -> "\u2656"
    Piece.BISHOP -> "\u2657"
    Piece.KNIGHT -> "\u2658"
    else -> "\u2659"
}

/** Chứ cái theo ký hiệu quốc tế; tốt viết là "P" cho dễ đọc trên màn hình nhỏ. */
private fun letterGlyph(piece: Byte): String = when (Piece.typeOf(piece)) {
    Piece.KING -> "K"
    Piece.QUEEN -> "Q"
    Piece.ROOK -> "R"
    Piece.BISHOP -> "B"
    Piece.KNIGHT -> "N"
    else -> "P"
}
