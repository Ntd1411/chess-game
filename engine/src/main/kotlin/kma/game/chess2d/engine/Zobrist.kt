package kma.game.chess2d.engine

import kotlin.random.Random

/**
 * Zobrist hashing: mỗi thế cờ có một khóa 64 bit.
 *
 * Ý tưởng: gán một số ngẫu nhiên cho từng cặp (quân, ô), rồi XOR tất cả những gì
 * đang có trên bàn. Vì XOR tự nghịch đảo, dời một quân chỉ tốn hai phép XOR thay
 * vì tính lại toàn bộ 64 ô — đó là lý do khóa này rẻ đến mức dùng được ở mọi node
 * của search (mục 3.2 bước 5).
 *
 * Dùng cho hai việc: transposition table, và luật lặp ba lần thế.
 */
object Zobrist {

    /**
     * Bảng phẳng 16 mã quân × 64 ô. Mã quân là 4 bit nên chỉ 12 trong 16 hàng được
     * dùng thật; giữ bảng phẳng để tra cứu chỉ là một phép nhân, không cần map lại mã.
     */
    private val pieces = Array(16) { LongArray(Squares.COUNT) }

    /** Một khóa cho mỗi tổ hợp quyền nhập thành (4 bit = 16 tổ hợp). */
    val castling = LongArray(16)

    /**
     * Ô bắt tốt qua đường chỉ cần phân biệt theo cột: hàng luôn suy ra được từ bên
     * đang đi.
     */
    val epFile = LongArray(8)

    val sideToMove: Long

    init {
        // Seed cố định, không dùng Random mặc định: cùng một thế cờ phải cho cùng một
        // khóa giữa các lần chạy, nếu không thì test không tái lập được.
        val random = Random(0x00C0FFEE)
        for (piece in 0 until 16) {
            for (square in 0 until Squares.COUNT) {
                pieces[piece][square] = random.nextLong()
            }
        }
        for (rights in 0 until 16) castling[rights] = random.nextLong()
        for (file in 0 until 8) epFile[file] = random.nextLong()
        sideToMove = random.nextLong()
    }

    fun pieceKey(piece: Byte, square: Int): Long = pieces[piece.toInt() and 0xF][square]

    /**
     * Tính khóa từ đầu.
     *
     * Chỉ dùng khi dựng thế cờ (FEN) và trong test đối chiếu với khóa tăng dần —
     * đây chính là cách bắt lỗi cập nhật thiếu trong makeMove.
     */
    fun compute(board: Board): Long {
        var key = 0L
        for (square in 0 until Squares.COUNT) {
            val piece = board.squares[square]
            if (piece != Piece.NONE) key = key xor pieceKey(piece, square)
        }
        key = key xor castling[board.castlingRights and 0xF]
        if (board.epSquare != Squares.NONE) key = key xor epFile[Squares.fileOf(board.epSquare)]
        // Chỉ XOR khi Đen đi, để thế cờ Trắng đi là mốc gốc.
        if (!board.whiteToMove) key = key xor sideToMove
        return key
    }
}
