package kma.game.chess2d.engine

/**
 * Thống kê quân bị bắt và chênh lệch vật chất của một ván (mục 7.2).
 *
 * @param takenFromWhite những quân Trắng đã mất, theo thứ tự bị bắt.
 * @param takenFromBlack những quân Đen đã mất, theo thứ tự bị bắt.
 */
data class CaptureTally(
    val takenFromWhite: List<Byte> = emptyList(),
    val takenFromBlack: List<Byte> = emptyList(),
) {

    /**
     * Chênh lệch vật chất theo góc nhìn của Trắng: dương là Trắng đang hơn quân.
     *
     * Tính từ danh sách quân bị bắt chứ không đếm quân còn trên bàn: phong cấp làm
     * tổng giá trị trên bàn tăng lên, nên đếm trên bàn sẽ ra con số khác với cái
     * người chơi mong đợi khi nhìn hàng quân đã ăn.
     */
    val materialBalance: Int
        get() = takenFromBlack.sumOf { valueOf(it) } - takenFromWhite.sumOf { valueOf(it) }

    companion object {

        /** Giá trị quy ước theo mã loại quân; Vua không bao giờ bị bắt nên để 0. */
        private val VALUES = intArrayOf(0, 1, 3, 3, 5, 9, 0)

        fun valueOf(piece: Byte): Int = VALUES[Piece.typeOf(piece)]

        /**
         * Đi lại ván từ [startFen] để biết những quân nào đã rời bàn.
         *
         * Tính từ **danh sách nước đi** thay vì ghi lại khi ăn: nhờ vậy Undo, đi lại ván
         * lưu trên đĩa hay xem lại một thế giữa ván đều dùng chung đúng một đường tính,
         * không có cách nào lệch nhau.
         *
         * @param plies số nước đầu tiên được tính; mặc định là cả ván.
         */
        fun of(
            startFen: String,
            moves: List<Move>,
            plies: Int = moves.size,
        ): CaptureTally {
            val board = Fen.parse(startFen)
            val takenFromWhite = mutableListOf<Byte>()
            val takenFromBlack = mutableListOf<Byte>()

            for (index in 0 until plies.coerceAtMost(moves.size)) {
                val move = moves[index]
                if (move.isCapture) {
                    // Bắt tốt qua đường: quân bị bắt không đứng ở ô đến mà ở ngay sau nó.
                    val target = when {
                        !move.isEnPassant -> move.to
                        board.whiteToMove -> move.to - 8
                        else -> move.to + 8
                    }
                    val taken = board.pieceAt(target)
                    if (taken != Piece.NONE) {
                        if (Piece.isWhite(taken)) takenFromWhite += taken else takenFromBlack += taken
                    }
                }
                board.makeMove(move)
            }

            return CaptureTally(takenFromWhite = takenFromWhite, takenFromBlack = takenFromBlack)
        }
    }
}
