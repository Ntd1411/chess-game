package kma.game.chess2d.engine

/**
 * Kết cục của một thế cờ.
 *
 * Chưa có lặp ba lần thế và không đủ quân để thắng: hai luật đó cần Zobrist hash,
 * nên để sang Phase 3 khi đã có hash sẵn cho transposition table (mục 5.4).
 */
enum class GameStatus {
    ONGOING,
    CHECK,
    CHECKMATE,
    STALEMATE,
    DRAW_FIFTY_MOVES,
}

object Rules {

    /** Luật 50 nước đếm theo nửa nước, nên ngưỡng là 100. */
    const val FIFTY_MOVE_LIMIT = 100

    fun status(board: Board): GameStatus {
        val hasLegalMove = MoveGenerator.legalMoves(board).isNotEmpty()
        val inCheck = board.isInCheck()
        return when {
            // Hết nước và hòa do bí chỉ khác nhau ở chỗ vua có đang bị chiếu hay không.
            !hasLegalMove && inCheck -> GameStatus.CHECKMATE
            !hasLegalMove -> GameStatus.STALEMATE
            board.halfmoveClock >= FIFTY_MOVE_LIMIT -> GameStatus.DRAW_FIFTY_MOVES
            inCheck -> GameStatus.CHECK
            else -> GameStatus.ONGOING
        }
    }

    fun isGameOver(board: Board): Boolean = when (status(board)) {
        GameStatus.CHECKMATE, GameStatus.STALEMATE, GameStatus.DRAW_FIFTY_MOVES -> true
        GameStatus.ONGOING, GameStatus.CHECK -> false
    }
}
