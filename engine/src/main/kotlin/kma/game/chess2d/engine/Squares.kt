package kma.game.chess2d.engine

/**
 * Ô cờ được đánh số 0..63 theo thứ tự a1 = 0, b1 = 1, ..., h8 = 63.
 *
 * Cách đánh số này khiến "tiến một hàng" đúng bằng +8 cho Trắng và -8 cho Đen,
 * và file/rank lấy ra chỉ bằng một phép bit.
 */
object Squares {

    const val NONE = -1
    const val COUNT = 64

    const val A1 = 0
    const val C1 = 2
    const val E1 = 4
    const val G1 = 6
    const val H1 = 7
    const val A8 = 56
    const val C8 = 58
    const val E8 = 60
    const val G8 = 62
    const val H8 = 63

    fun fileOf(square: Int): Int = square and 7

    fun rankOf(square: Int): Int = square shr 3

    fun of(file: Int, rank: Int): Int = rank * 8 + file

    fun isValid(file: Int, rank: Int): Boolean = file in 0..7 && rank in 0..7

    fun name(square: Int): String =
        if (square == NONE) "-" else "${'a' + fileOf(square)}${rankOf(square) + 1}"

    fun fromName(name: String): Int {
        if (name == "-") return NONE
        require(name.length == 2) { "Tên ô không hợp lệ: $name" }
        val file = name[0] - 'a'
        val rank = name[1] - '1'
        require(isValid(file, rank)) { "Tên ô không hợp lệ: $name" }
        return of(file, rank)
    }
}
