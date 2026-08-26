package kma.game.chess2d.engine

/**
 * Bộ đệm nước đi dùng lại được, lưu dạng IntArray thô.
 *
 * Mỗi tầng của search giữ một MoveList riêng và gọi clear() thay vì tạo mới, nên
 * vòng lặp sinh nước đi không cấp phát gì. Đây là lý do không dùng
 * MutableList<Move>: List sẽ boxing từng Move và tạo rác ở mọi node.
 *
 * 256 là trần an toàn: thế cờ hợp lệ nhiều nước đi nhất từng biết có 218 nước.
 */
class MoveList(capacity: Int = MAX_MOVES) {

    private val raws = IntArray(capacity)

    var size = 0
        private set

    fun clear() {
        size = 0
    }

    fun add(move: Move) {
        raws[size] = move.raw
        size++
    }

    operator fun get(index: Int): Move = Move(raws[index])

    fun isEmpty(): Boolean = size == 0

    fun isNotEmpty(): Boolean = size > 0

    fun toList(): List<Move> = (0 until size).map { get(it) }

    companion object {
        const val MAX_MOVES = 256
    }
}
