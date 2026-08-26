package kma.game.chess2d.engine

/**
 * Bảng tấn công tính sẵn một lần duy nhất khi nạp class.
 *
 * Đây là lý do không cần quét cả 64 ô cho mỗi quân: mỗi ô đã có sẵn danh sách ô
 * đích của mã, vua, tốt, và danh sách ô theo từng tia cho quân đi xa. Kiểm tra
 * biên bàn cờ chỉ làm một lần ở đây, không lặp lại trong vòng lặp nóng.
 */
object Attacks {

    private val KNIGHT_DELTAS = arrayOf(
        intArrayOf(1, 2), intArrayOf(2, 1), intArrayOf(2, -1), intArrayOf(1, -2),
        intArrayOf(-1, -2), intArrayOf(-2, -1), intArrayOf(-2, 1), intArrayOf(-1, 2),
    )

    private val KING_DELTAS = arrayOf(
        intArrayOf(1, 0), intArrayOf(1, 1), intArrayOf(0, 1), intArrayOf(-1, 1),
        intArrayOf(-1, 0), intArrayOf(-1, -1), intArrayOf(0, -1), intArrayOf(1, -1),
    )

    /** Bốn hướng đầu là hướng xe, bốn hướng sau là hướng tượng. Hậu dùng cả tám. */
    private val DIRECTIONS = arrayOf(
        intArrayOf(1, 0), intArrayOf(-1, 0), intArrayOf(0, 1), intArrayOf(0, -1),
        intArrayOf(1, 1), intArrayOf(1, -1), intArrayOf(-1, 1), intArrayOf(-1, -1),
    )

    const val ROOK_DIR_FIRST = 0
    const val ROOK_DIR_LAST = 3
    const val BISHOP_DIR_FIRST = 4
    const val BISHOP_DIR_LAST = 7

    /** rays[ô][hướng] = các ô theo hướng đó, xếp từ gần ra xa để dừng ngay khi gặp quân. */
    val rays: Array<Array<IntArray>> =
        Array(Squares.COUNT) { square -> Array(DIRECTIONS.size) { dir -> buildRay(square, dir) } }

    val knight: Array<IntArray> =
        Array(Squares.COUNT) { square -> buildJumps(square, KNIGHT_DELTAS) }

    val king: Array<IntArray> =
        Array(Squares.COUNT) { square -> buildJumps(square, KING_DELTAS) }

    /** Các ô mà một con tốt đứng ở ô này ăn chéo tới. */
    private val whitePawnAttacks: Array<IntArray> =
        Array(Squares.COUNT) { square -> buildPawnAttacks(square, white = true) }

    private val blackPawnAttacks: Array<IntArray> =
        Array(Squares.COUNT) { square -> buildPawnAttacks(square, white = false) }

    /**
     * Chiều ngược lại: các ô mà từ đó một con tốt tấn công vào ô này.
     * Dùng khi hỏi "ô này có bị tấn công không" — đi ngược hướng ăn rẻ hơn quét bàn cờ.
     */
    private val whitePawnAttackers: Array<IntArray> = invert(whitePawnAttacks)

    private val blackPawnAttackers: Array<IntArray> = invert(blackPawnAttacks)

    fun pawnAttacks(white: Boolean): Array<IntArray> = if (white) whitePawnAttacks else blackPawnAttacks

    fun pawnAttackers(white: Boolean): Array<IntArray> = if (white) whitePawnAttackers else blackPawnAttackers

    private fun buildJumps(square: Int, deltas: Array<IntArray>): IntArray {
        val file = Squares.fileOf(square)
        val rank = Squares.rankOf(square)
        val targets = ArrayList<Int>(deltas.size)
        for (delta in deltas) {
            val targetFile = file + delta[0]
            val targetRank = rank + delta[1]
            if (Squares.isValid(targetFile, targetRank)) targets.add(Squares.of(targetFile, targetRank))
        }
        return targets.toIntArray()
    }

    private fun buildRay(square: Int, direction: Int): IntArray {
        val delta = DIRECTIONS[direction]
        var file = Squares.fileOf(square) + delta[0]
        var rank = Squares.rankOf(square) + delta[1]
        val targets = ArrayList<Int>(7)
        while (Squares.isValid(file, rank)) {
            targets.add(Squares.of(file, rank))
            file += delta[0]
            rank += delta[1]
        }
        return targets.toIntArray()
    }

    private fun buildPawnAttacks(square: Int, white: Boolean): IntArray {
        val file = Squares.fileOf(square)
        val rank = Squares.rankOf(square) + if (white) 1 else -1
        val targets = ArrayList<Int>(2)
        for (targetFile in intArrayOf(file - 1, file + 1)) {
            if (Squares.isValid(targetFile, rank)) targets.add(Squares.of(targetFile, rank))
        }
        return targets.toIntArray()
    }

    private fun invert(table: Array<IntArray>): Array<IntArray> {
        val buckets = Array(Squares.COUNT) { ArrayList<Int>(2) }
        for (source in 0 until Squares.COUNT) {
            for (target in table[source]) buckets[target].add(source)
        }
        return Array(Squares.COUNT) { buckets[it].toIntArray() }
    }
}
