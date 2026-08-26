package kma.game.chess2d.engine

/**
 * Đọc và ghi FEN.
 *
 * Đây không phải tính năng phụ mà là công cụ test quan trọng nhất của Phase 1:
 * không có FEN thì mọi test luật đặc biệt đều phải đi lại từ đầu ván, và test
 * "hoàn nguyên đúng" không còn cách nào để so sánh hai trạng thái.
 */
object Fen {

    const val START = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    /**
     * Kiwipete — thế cờ chuẩn của cộng đồng để bắt lỗi nhập thành và bắt tốt qua
     * đường, hai nhóm lỗi mà vị trí khởi đầu không hề chạm tới.
     */
    const val KIWIPETE = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"

    fun parse(fen: String): Board {
        val parts = fen.trim().split(" ").filter { it.isNotEmpty() }
        require(parts.size >= 4) { "FEN thiếu thành phần: $fen" }

        val board = Board()
        var file = 0
        var rank = 7
        for (symbol in parts[0]) {
            when {
                symbol == '/' -> {
                    rank--
                    file = 0
                }
                symbol.isDigit() -> file += symbol - '0'
                else -> {
                    board.setPiece(Squares.of(file, rank), Piece.fromChar(symbol))
                    file++
                }
            }
        }

        board.whiteToMove = parts[1] == "w"
        board.castlingRights = parseCastling(parts[2])
        board.epSquare = Squares.fromName(parts[3])
        // Hai trường cuối là tùy chọn: nhiều bộ test perft ghi FEN không có chúng.
        board.halfmoveClock = parts.getOrNull(4)?.toIntOrNull() ?: 0
        board.fullmoveNumber = parts.getOrNull(5)?.toIntOrNull() ?: 1
        return board
    }

    fun format(board: Board): String {
        val result = StringBuilder()
        for (rank in 7 downTo 0) {
            var emptyRun = 0
            for (file in 0..7) {
                val piece = board.squares[Squares.of(file, rank)]
                if (piece == Piece.NONE) {
                    emptyRun++
                    continue
                }
                if (emptyRun > 0) {
                    result.append(emptyRun)
                    emptyRun = 0
                }
                result.append(Piece.toChar(piece))
            }
            if (emptyRun > 0) result.append(emptyRun)
            if (rank > 0) result.append('/')
        }

        result.append(if (board.whiteToMove) " w " else " b ")
        result.append(formatCastling(board.castlingRights))
        result.append(' ')
        result.append(Squares.name(board.epSquare))
        result.append(' ')
        result.append(board.halfmoveClock)
        result.append(' ')
        result.append(board.fullmoveNumber)
        return result.toString()
    }

    private fun parseCastling(field: String): Int {
        if (field == "-") return 0
        var rights = 0
        for (symbol in field) {
            rights = rights or when (symbol) {
                'K' -> Board.WHITE_KING_SIDE
                'Q' -> Board.WHITE_QUEEN_SIDE
                'k' -> Board.BLACK_KING_SIDE
                'q' -> Board.BLACK_QUEEN_SIDE
                else -> throw IllegalArgumentException("Quyền nhập thành không hợp lệ: $symbol")
            }
        }
        return rights
    }

    private fun formatCastling(rights: Int): String {
        if (rights == 0) return "-"
        val result = StringBuilder()
        if (rights and Board.WHITE_KING_SIDE != 0) result.append('K')
        if (rights and Board.WHITE_QUEEN_SIDE != 0) result.append('Q')
        if (rights and Board.BLACK_KING_SIDE != 0) result.append('k')
        if (rights and Board.BLACK_QUEEN_SIDE != 0) result.append('q')
        return result.toString()
    }
}
