package kma.game.chess2d.campaign

/**
 * Phần thưởng của một tầng: vàng, pha lê xanh và sách cổ.
 *
 * Earned rewards are derived from tower progress by [TowerRewards.total]; replaying a cleared
 * floor never earns them twice. Spendable gold subtracts the persistent shop spending ledger
 * in SettingsStore. Crystals and books are not spent in the cosmetic shop.
 */
data class Reward(val gold: Int = 0, val crystals: Int = 0, val books: Int = 0) {
    operator fun plus(other: Reward): Reward =
        Reward(gold + other.gold, crystals + other.crystals, books + other.books)
}

/** Bảng thưởng theo loại tầng. Hàm thuần, có test. */
object TowerRewards {

    /** Thưởng khi qua [floor] lần đầu: tầng càng cao càng nhiều, trùm và kho báu cho pha lê. */
    fun forFloor(floor: Floor): Reward {
        val n = floor.number
        return when (floor.kind) {
            NodeKind.BATTLE -> Reward(gold = 50 + 5 * n)
            NodeKind.PUZZLE -> Reward(gold = 40 + 5 * n, books = 1)
            NodeKind.STORY -> Reward(gold = 20, books = 1)
            NodeKind.TREASURE -> Reward(gold = 150 + 10 * n, crystals = 1)
            NodeKind.SECRET -> Reward(gold = 200 + 10 * n, crystals = 2)
            NodeKind.BOSS -> Reward(gold = 300 + 10 * n, crystals = 3, books = 1)
        }
    }

    /** Tổng thưởng của mọi tầng đã qua (số tầng `<= clearedUpTo`). */
    fun total(floors: List<Floor>, clearedUpTo: Int): Reward =
        floors.filter { it.number <= clearedUpTo }.fold(Reward()) { sum, floor -> sum + forFloor(floor) }
}

/**
 * Di vật cốt truyện trong Túi đồ: mỗi món mở khi qua tầng [unlockFloor].
 *
 * Chỉ là danh mục thuần; tên, mô tả và ảnh do tầng UI ánh xạ.
 */
enum class Relic(val unlockFloor: Int) {
    ANCIENT_KEY(4),
    SCROLL(8),
    POTION(12),
    MEMORY_CRYSTAL(16),
    BROKEN_PAWN(20),
    CHESS_FRAGMENT(24),
    WHITE_ROSE(28),
    RED_CRYSTAL(32),
    MAGIC_TOKEN(36),
    BLACK_ROSE(40),
    MAGIC_ORB(44),
    GOLDEN_FRAGMENT(48),

    /** Chỉ có khi chinh phục toàn bộ tháp. */
    TOWER_CROWN(TowerCatalog.FLOOR_COUNT),
    ;

    fun isUnlocked(clearedUpTo: Int): Boolean = clearedUpTo >= unlockFloor
}
