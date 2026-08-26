package kma.game.chess2d.engine

/**
 * Kết cục của một thế cờ.
 *
 * CHECK không phải kết củc mà chỉ là trạng thái để UI báo đỏ ô vua — ván vẫn đang đi.
 */
enum class GameStatus {
    ONGOING,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVES,
    DRAW_REPETITION,
    DRAW_INSUFFICIENT_MATERIAL,
}

/**
 * Luật kết thúc ván.
 *
 * Tách khỏi Board và MoveGenerator có chủ ý: Board chỉ biết đi và hoàn nguyên,
 * còn "ván này đã xong chưa" là câu hỏi của tầng luật.
 */
object Rules {

    /** Đếm theo nửa nước: 50 nước của mỗi bên là 100 nửa nước. */
    const val FIFTY_MOVE_LIMIT = 100

    const val REPETITION_LIMIT = 3

    /**
     * Thứ tự xét có ý nghĩa luật: hết nước được xét TRƯỚC mọi luật hòa. Đứng
     * ở nước thứ 100 mà bị hết nước thì là THUA, không phải hòa.
     */
    fun status(board: Board): GameStatus {
        val hasLegalMove = MoveGenerator.legalMoves(board).isNotEmpty()
        val inCheck = board.isInCheck()
        return when {
            !hasLegalMove && inCheck -> GameStatus.CHECKMATE
            !hasLegalMove -> GameStatus.STALEMATE
            board.repetitionCount() >= REPETITION_LIMIT -> GameStatus.DRAW_REPETITION
            isInsufficientMaterial(board) -> GameStatus.DRAW_INSUFFICIENT_MATERIAL
            board.halfmoveClock >= FIFTY_MOVE_LIMIT -> GameStatus.DRAW_FIFTY_MOVES
            inCheck -> GameStatus.CHECK
            else -> GameStatus.ONGOING
        }
    }

    fun isGameOver(board: Board): Boolean = when (status(board)) {
        GameStatus.CHECKMATE,
        GameStatus.STALEMATE,
        GameStatus.DRAW_FIFTY_MOVES,
        GameStatus.DRAW_REPETITION,
        GameStatus.DRAW_INSUFFICIENT_MATERIAL,
        -> true

        GameStatus.ONGOING, GameStatus.CHECK -> false
    }

    /**
     * Hòa vì không bên nào còn đủ quân để hết nước đợc đối phương.
     *
     * Chỉ nhận bốn trường hợp chắc chắn theo FIDE: V-V, V+Mã-V, V+Tượng-V, và
     * V+Tượng-V+Tượng cùng màu ô. Cố tính thêm (ví dụ hai Mã) là sai: V+2Mã vẫn
     * hết nước được nếu đối phương phụ họa.
     *
     * Không cấp phát gì: hàm này được search gọi ở mọi node.
     */
    fun isInsufficientMaterial(board: Board): Boolean {
        var whiteMinors = 0
        var blackMinors = 0
        var whiteBishops = 0
        var blackBishops = 0
        var whiteBishopSquareColor = -1
        var blackBishopSquareColor = -1

        for (square in 0 until Squares.COUNT) {
            val piece = board.squares[square]
            if (piece == Piece.NONE) continue
            val white = Piece.isWhite(piece)
            when (Piece.typeOf(piece)) {
                // Còn Tốt thì còn phong cấp; còn Xe hay Hậu thì một mình nó đã hết nước được.
                Piece.PAWN, Piece.ROOK, Piece.QUEEN -> return false
                Piece.KNIGHT -> if (white) whiteMinors++ else blackMinors++
                Piece.BISHOP -> {
                    val squareColor = (Squares.fileOf(square) + Squares.rankOf(square)) and 1
                    if (white) {
                        whiteMinors++
                        whiteBishops++
                        whiteBishopSquareColor = squareColor
                    } else {
                        blackMinors++
                        blackBishops++
                        blackBishopSquareColor = squareColor
                    }
                }
            }
        }

        val minors = whiteMinors + blackMinors
        if (minors <= 1) return true
        // Hai Tượng đi cùng màu ô thì không bao giờ kiểm soát đủ ô để hết nước.
        return minors == 2 &&
            whiteBishops == 1 &&
            blackBishops == 1 &&
            whiteBishopSquareColor == blackBishopSquareColor
    }
}
