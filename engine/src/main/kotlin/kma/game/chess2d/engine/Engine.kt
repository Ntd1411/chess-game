package kma.game.chess2d.engine

/**
 * Placeholder cua Phase 0.
 *
 * Phase 1 se thay the file nay bang:
 *  - Board: ByteArray(64), make/unmake + undo stack
 *  - Move: value class dong goi vao mot Int
 *  - MoveGenerator, Fen, Perft
 */
object Engine {
    const val START_FEN: String =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

    /** So o tren ban co. Chi dung de xac nhan module build va test chay duoc. */
    const val BOARD_SIZE: Int = 64
}
