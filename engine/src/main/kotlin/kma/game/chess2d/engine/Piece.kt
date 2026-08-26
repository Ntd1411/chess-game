package kma.game.chess2d.engine

/**
 * Quân cờ được gói trong một Byte: 3 bit thấp là loại quân, bit thứ 4 là màu.
 *
 * Nhờ cách gói này cả bàn cờ chỉ là ByteArray(64) thay vì 64 object (mục 2.3).
 * Quân trắng có mã trùng luôn với mã loại quân, nên ô trống (0) không bao giờ
 * bị nhầm thành quân.
 */
object Piece {

    const val NONE: Byte = 0

    // Loại quân, nằm ở 3 bit thấp.
    const val PAWN = 1
    const val KNIGHT = 2
    const val BISHOP = 3
    const val ROOK = 4
    const val QUEEN = 5
    const val KING = 6

    /** Bit màu: bật là Đen. Trắng để 0. */
    const val BLACK_FLAG = 8

    const val TYPE_MASK = 7

    fun of(type: Int, white: Boolean): Byte =
        (if (white) type else type or BLACK_FLAG).toByte()

    fun typeOf(piece: Byte): Int = piece.toInt() and TYPE_MASK

    /**
     * Phải kiểm tra khác NONE trước: ô trống có bit màu bằng 0 giống quân trắng.
     */
    fun isWhite(piece: Byte): Boolean = piece != NONE && (piece.toInt() and BLACK_FLAG) == 0

    fun isBlack(piece: Byte): Boolean = (piece.toInt() and BLACK_FLAG) != 0

    fun isColor(piece: Byte, white: Boolean): Boolean = if (white) isWhite(piece) else isBlack(piece)

    /** Vị trí trong chuỗi này chính là mã loại quân, nên tra cứu hai chiều rất rẻ. */
    private const val SYMBOLS = " PNBRQK"

    fun toChar(piece: Byte): Char {
        val symbol = SYMBOLS[typeOf(piece)]
        return if (isBlack(piece)) symbol.lowercaseChar() else symbol
    }

    fun fromChar(symbol: Char): Byte {
        val type = SYMBOLS.indexOf(symbol.uppercaseChar())
        require(type > 0) { "Ký tự quân cờ không hợp lệ: $symbol" }
        return of(type, symbol.isUpperCase())
    }
}
