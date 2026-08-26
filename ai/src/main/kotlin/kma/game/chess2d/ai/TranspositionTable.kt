package kma.game.chess2d.ai

/**
 * Transposition table: nhớ kết quả đã tính cho từng thế cờ.
 *
 * Cùng một thế cờ đến được bằng nhiều thứ tự nước khác nhau (1.d4 Nf6 2.Nf3 và
 * 1.Nf3 Nf6 2.d4 cho ra đúng một thế), nên không nhớ thì search tính lại rất nhiều
 * lần cùng một việc.
 *
 * Dùng các mảng nguyên thủy song song thay cho HashMap: HashMap sẽ cấp phát một
 * object ở mọi node, đủ để GC chạy liên tục giữa ván trên điện thoại.
 *
 * Bảng có mất mát: hai thế cờ có thể đụng cùng ô. So lại đủ 64 bit khóa trước khi
 * dùng nên xác suất nhầm thực tế coi như bằng không.
 */
class TranspositionTable(sizeMb: Int = 8) {

    companion object {
        /** Không dùng được giá trị này làm điểm thật, nên an toàn làm dấu "không có". */
        const val MISS = Int.MIN_VALUE

        /** Điểm chính xác: cửa sổ alpha-beta không bị cắt. */
        const val EXACT = 0

        /** Điểm thật >= giá trị lưu (gặp beta cutoff). */
        const val LOWER_BOUND = 1

        /** Điểm thật <= giá trị lưu (không nâng được alpha). */
        const val UPPER_BOUND = 2

        /** key 8 + score 4 + move 4 + depth 1 + flag 1, làm tròn lên cho chắc. */
        private const val BYTES_PER_ENTRY = 20
    }

    private val mask: Int
    private val keys: LongArray
    private val moves: IntArray
    private val scores: IntArray
    private val depths: ByteArray
    private val flags: ByteArray

    val capacity: Int

    init {
        val requested = (sizeMb.toLong() * 1024L * 1024L / BYTES_PER_ENTRY)
            .coerceAtLeast(1024L)
        // Làm tròn xuống lũy thừa của 2 để lấy chỉ số bằng phép AND thay cho phép chia.
        var size = 1
        while (size.toLong() * 2L <= requested) size *= 2
        capacity = size
        mask = size - 1
        keys = LongArray(size)
        moves = IntArray(size)
        scores = IntArray(size)
        depths = ByteArray(size)
        flags = ByteArray(size)
    }

    fun clear() {
        keys.fill(0L)
        moves.fill(0)
        scores.fill(0)
        depths.fill(0)
        flags.fill(0)
    }

    /** Nuớc tốt nhất đã biết của thế cờ, dạng raw. 0 nếu không có. */
    fun bestMove(key: Long): Int {
        val index = indexOf(key)
        return if (keys[index] == key) moves[index] else 0
    }

    /**
     * Tra điểm dùng được ngay.
     *
     * Điều kiện depth: chỉ tin kết quả được tính với độ sâu BẰNG HOẶC HƠN độ sâu
     * đang cần. Lấy kết quả nông hơn là tự làm yếu search.
     */
    fun probe(key: Long, depth: Int, alpha: Int, beta: Int): Int {
        val index = indexOf(key)
        if (keys[index] != key) return MISS
        if (depths[index] < depth) return MISS
        val score = scores[index]
        return when (flags[index].toInt()) {
            EXACT -> score
            LOWER_BOUND -> if (score >= beta) score else MISS
            UPPER_BOUND -> if (score <= alpha) score else MISS
            else -> MISS
        }
    }

    /**
     * Ghi kết quả. Giữ lại bản cũ nếu đó là cùng thế cờ nhưng được tính sâu hơn;
     * còn lại thì ghi đè, vì thế cờ cũ gần như không bao giờ được hỏi lại.
     */
    fun store(key: Long, depth: Int, score: Int, flag: Int, moveRaw: Int) {
        val index = indexOf(key)
        if (keys[index] == key && depths[index] > depth) return
        keys[index] = key
        moves[index] = moveRaw
        scores[index] = score
        depths[index] = depth.toByte()
        flags[index] = flag.toByte()
    }

    private fun indexOf(key: Long): Int = key.toInt() and mask
}
