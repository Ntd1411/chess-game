package kma.game.chess2d.engine

/**
 * Perft: đếm số thế cờ lá ở một độ sâu nhất định.
 *
 * Đây là công cụ duy nhất bắt được lỗi sinh nước đi một cách tin cậy: đối chiếu với
 * con số đã được cộng đồng xác nhận, lệch một node là biết ngay có lỗi (mục 5.2).
 *
 * Bộ đệm được cấp trước một lần cho mỗi tầng độ sâu, nên cả triệu node không sinh
 * thêm rác. Perft cũng chính là phép đo tốc độ make/unmake cho Phase 3.
 */
object Perft {

    fun perft(board: Board, depth: Int): Long {
        require(depth >= 0) { "Độ sâu phải không âm" }
        if (depth == 0) return 1L
        val moveBuffers = Array(depth + 1) { MoveList() }
        val scratchBuffers = Array(depth + 1) { MoveList() }
        return search(board, depth, moveBuffers, scratchBuffers)
    }

    /**
     * Chia nhỏ theo từng nước đi đầu tiên. Khi perft lệch, so bảng này với engine
     * khác sẽ chỉ ra chính xác nhánh nào sai thay vì phải đoán.
     */
    fun divide(board: Board, depth: Int): Map<String, Long> {
        require(depth >= 1) { "divide cần độ sâu tối thiểu 1" }
        val result = LinkedHashMap<String, Long>()
        for (move in MoveGenerator.legalMoves(board)) {
            board.makeMove(move)
            result[move.toUci()] = perft(board, depth - 1)
            board.unmakeMove()
        }
        return result
    }

    private fun search(
        board: Board,
        depth: Int,
        moveBuffers: Array<MoveList>,
        scratchBuffers: Array<MoveList>,
    ): Long {
        val moves = moveBuffers[depth]
        MoveGenerator.legal(board, moves, scratchBuffers[depth])

        // Ở độ sâu 1 chỉ cần đếm, không cần đi thử từng nước nữa.
        if (depth == 1) return moves.size.toLong()

        var nodes = 0L
        for (index in 0 until moves.size) {
            board.makeMove(moves[index])
            nodes += search(board, depth - 1, moveBuffers, scratchBuffers)
            board.unmakeMove()
        }
        return nodes
    }
}
