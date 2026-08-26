package kma.game.chess2d.engine

/**
 * Một nước đi được gói trong một Int (mục 2.3): 6 bit ô đi, 6 bit ô đến, 4 bit cờ.
 *
 * Là value class nên khi search đi qua hàng triệu nước, không có object nào được
 * cấp phát — đây là điều kiện để cấp Khó chạy nổi trên điện thoại.
 */
@JvmInline
value class Move(val raw: Int) {

    val from: Int get() = raw and 0x3F
    val to: Int get() = (raw ushr 6) and 0x3F
    val flag: Int get() = (raw ushr 12) and 0xF

    val isCapture: Boolean get() = (flag and CAPTURE_BIT) != 0
    val isPromotion: Boolean get() = (flag and PROMOTION_BIT) != 0
    val isEnPassant: Boolean get() = flag == EN_PASSANT
    val isCastle: Boolean get() = flag == CASTLE_KING || flag == CASTLE_QUEEN

    /** Chỉ có nghĩa khi isPromotion bật. 2 bit thấp của cờ chọn loại quân phong cấp. */
    val promotionType: Int get() = PROMOTION_TYPES[flag and 3]

    /** Ký hiệu UCI, ví dụ "e2e4" hoặc "a7a8q". Dùng cho test, log và giao thức LAN. */
    fun toUci(): String {
        val suffix = if (isPromotion) Piece.toChar(Piece.of(promotionType, white = false)).toString() else ""
        return Squares.name(from) + Squares.name(to) + suffix
    }

    override fun toString(): String = toUci()

    companion object {

        // Cờ nước đi. Bit 4 (giá trị 4) nghĩa là có ăn quân, bit 8 nghĩa là phong cấp,
        // nên kiểm tra "có ăn quân không" chỉ là một phép AND thay vì liệt kê từng cờ.
        const val QUIET = 0
        const val DOUBLE_PAWN_PUSH = 1
        const val CASTLE_KING = 2
        const val CASTLE_QUEEN = 3
        const val CAPTURE = 4
        const val EN_PASSANT = 5
        const val PROMOTION_KNIGHT = 8
        const val PROMOTION_BISHOP = 9
        const val PROMOTION_ROOK = 10
        const val PROMOTION_QUEEN = 11
        const val PROMOTION_CAPTURE_KNIGHT = 12
        const val PROMOTION_CAPTURE_BISHOP = 13
        const val PROMOTION_CAPTURE_ROOK = 14
        const val PROMOTION_CAPTURE_QUEEN = 15

        const val CAPTURE_BIT = 4
        const val PROMOTION_BIT = 8

        /** Nước đi rỗng, dùng làm giá trị khởi tạo trong search. */
        val NONE = Move(0)

        private val PROMOTION_TYPES =
            intArrayOf(Piece.KNIGHT, Piece.BISHOP, Piece.ROOK, Piece.QUEEN)

        /** Bốn cờ phong cấp không ăn quân, và bốn cờ phong cấp có ăn quân. */
        val PROMOTION_FLAGS =
            intArrayOf(PROMOTION_KNIGHT, PROMOTION_BISHOP, PROMOTION_ROOK, PROMOTION_QUEEN)

        val PROMOTION_CAPTURE_FLAGS = intArrayOf(
            PROMOTION_CAPTURE_KNIGHT,
            PROMOTION_CAPTURE_BISHOP,
            PROMOTION_CAPTURE_ROOK,
            PROMOTION_CAPTURE_QUEEN,
        )

        fun of(from: Int, to: Int, flag: Int): Move = Move(from or (to shl 6) or (flag shl 12))
    }
}
