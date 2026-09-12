package kma.game.chess2d.engine

/**
 * Sinh ký hiệu SAN (Standard Algebraic Notation) cho một nước đi (mục 7.2).
 *
 * Đặt trong `:engine` chứ không trong `:app` vì SAN là chuyện của luật cờ, không
 * phải chuyện của giao diện: muốn biết có phải viết thêm ô đi để phân biệt hay
 * không thì phải sinh được danh sách nước hợp lệ, mà đó là việc của engine. Nhờ
 * vậy xuất PGN ở Chặng 3 và danh sách nước đi trên màn hình dùng chung một bộ sinh,
 * và test được bằng JVM thuần.
 */
object San {

    /**
     * Ký hiệu SAN của [move] trong thế cờ [board].
     *
     * Phải gọi **trước** khi đi nước đó: SAN phụ thuộc vào những quân còn đứng
     * trên bàn ở thế hiện tại (để biết có nhập nhằng hai quân cùng đi được tới
     * một ô hay không), chứ không phụ thuộc thế sau nước đi.
     *
     * Hàm có đi thử một nước để biết có `+` hay `#`, nhưng luôn hoàn nguyên, nên
     * [board] khi trả về y nguyên như lúc vào.
     */
    fun of(board: Board, move: Move): String {
        val legal = Engine.legalMoves(board)
        return of(board, move, legal)
    }

    /**
     * Bản nhận sẵn danh sách nước hợp lệ.
     *
     * Dùng khi cần SAN cho nhiều nước trong cùng một thế (ví dụ hiện gợi ý), khỏi
     * sinh lại danh sách cho từng nước.
     */
    fun of(board: Board, move: Move, legalMoves: List<Move>): String {
        // Nhập thành có ký hiệu riêng, không viết theo ô đi ô đến.
        if (move.flag == Move.CASTLE_KING) return "O-O" + suffixOf(board, move)
        if (move.flag == Move.CASTLE_QUEEN) return "O-O-O" + suffixOf(board, move)

        val piece = board.pieceAt(move.from)
        val type = Piece.typeOf(piece)
        val builder = StringBuilder()

        if (type == Piece.PAWN) {
            // Quân tốt không có chữ đầu. Ăn quân thì viết cột xuất phát: "exd5".
            if (move.isCapture) {
                builder.append('a' + Squares.fileOf(move.from))
                builder.append('x')
            }
        } else {
            builder.append(Piece.toChar(Piece.of(type, white = true)))
            builder.append(disambiguationOf(board, move, legalMoves, type))
            if (move.isCapture) builder.append('x')
        }

        builder.append(Squares.name(move.to))

        if (move.isPromotion) {
            builder.append('=')
            builder.append(Piece.toChar(Piece.of(move.promotionType, white = true)))
        }

        builder.append(suffixOf(board, move))
        return builder.toString()
    }

    /**
     * Phần phân biệt hai nước trùng nghĩa.
     *
     * Quy tắc SAN: nếu còn quân cùng loại khác cũng đi hợp lệ tới đúng ô đó thì phải
     * viết thêm: ưu tiên cột, cột vẫn trùng thì viết hàng, trùng cả hai thì viết hẳn
     * tên ô. So sánh trên **danh sách nước hợp lệ** chứ không phải trên những quân
     * đứng đâu: một con mã đang ghìm vì vốn không đi được thì không gây nhập nhằng.
     */
    private fun disambiguationOf(
        board: Board,
        move: Move,
        legalMoves: List<Move>,
        type: Int,
    ): String {
        val rivals = legalMoves.filter { other ->
            other.to == move.to &&
                other.from != move.from &&
                Piece.typeOf(board.pieceAt(other.from)) == type
        }
        if (rivals.isEmpty()) return ""

        val file = Squares.fileOf(move.from)
        val rank = Squares.rankOf(move.from)
        val sameFile = rivals.any { Squares.fileOf(it.from) == file }
        val sameRank = rivals.any { Squares.rankOf(it.from) == rank }

        return when {
            !sameFile -> "${'a' + file}"
            !sameRank -> "${rank + 1}"
            else -> Squares.name(move.from)
        }
    }

    /**
     * `#` khi chiếu hết, `+` khi chỉ chiếu, còn lại là chuỗi rỗng.
     *
     * Cách duy nhất để biết là đi thử rồi xem thế sau nước đi, nên ở đây dùng
     * make/unmake trên chính bàn cờ đó — không copy bàn cờ, và luôn hoàn nguyên
     * kể cả khi giữa đường có ngoại lệ.
     */
    private fun suffixOf(board: Board, move: Move): String {
        board.makeMove(move)
        val suffix = try {
            when (Rules.status(board)) {
                GameStatus.CHECKMATE -> "#"
                GameStatus.CHECK -> "+"
                else -> ""
            }
        } finally {
            board.unmakeMove()
        }
        return suffix
    }
}
