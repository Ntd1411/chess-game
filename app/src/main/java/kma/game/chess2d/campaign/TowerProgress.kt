package kma.game.chess2d.campaign

/** Trạng thái của một tầng trên bản đồ. */
enum class FloorStatus {
    /** Chưa mở: phải qua tầng trước đã. */
    LOCKED,

    /** Tầng kế tiếp cần chinh phục, phát sáng vàng trên bản đồ. */
    CURRENT,

    /** Đã qua, vào chơi lại được. */
    CLEARED,
}

/**
 * Tiến độ chiến dịch: tầng cao nhất đã qua.
 *
 * Chỉ cần một con số vì tháp đi tuyến tính: qua tầng N là mở tầng N+1. Phần logic nằm
 * ở lớp thuần này (không biết Android) để test JVM kiểm được luật mở khóa mà không
 * cần DataStore.
 */
data class TowerProgress(val clearedUpTo: Int = 0) {

    init {
        require(clearedUpTo in 0..TowerCatalog.FLOOR_COUNT) { "tiến độ ngoài khoảng: $clearedUpTo" }
    }

    /** Tầng đang chinh phục; dừng ở tầng cuối khi đã qua hết. */
    val currentFloor: Int
        get() = (clearedUpTo + 1).coerceAtMost(TowerCatalog.FLOOR_COUNT)

    val isTowerCleared: Boolean
        get() = clearedUpTo >= TowerCatalog.FLOOR_COUNT

    fun statusOf(floor: Int): FloorStatus = when {
        floor <= clearedUpTo -> FloorStatus.CLEARED
        floor == clearedUpTo + 1 -> FloorStatus.CURRENT
        else -> FloorStatus.LOCKED
    }

    /**
     * Ghi nhận vừa qua tầng [floor].
     *
     * Chỉ tiến khi đó là tầng đang chinh phục. Qua lại một tầng cũ, hoặc báo một tầng
     * còn khóa, đều không đổi gì: tiến độ không bao giờ lùi và không nhảy cóc.
     */
    fun withCleared(floor: Int): TowerProgress =
        if (floor == clearedUpTo + 1 && floor <= TowerCatalog.FLOOR_COUNT) {
            TowerProgress(floor)
        } else {
            this
        }
}
