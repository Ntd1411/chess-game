package kma.game.chess2d.campaign

/** Màn hình đang hiện trong luồng chiến dịch. */
enum class TowerStage {
    MAP,

    /** Thoại trước tầng (chỉ tầng mốc). */
    INTRO,
    BATTLE,

    /** Thoại sau khi thắng (chỉ tầng cuối). */
    EPILOGUE,
    VICTORY,
    DEFEAT,
}

/**
 * Luật chuyển màn của chiến dịch: Tower Map → (Dialogue) → Battle → (Epilogue) → Victory/Defeat.
 *
 * Là hàm thuần để test JVM duyệt được cả 49 tầng mà không cần dựng giao diện. Tầng
 * [FloorGoal.Claim] (Story/Treasure) không có ván cờ nên đi thẳng từ thoại (nếu có) tới Victory.
 */
object TowerFlow {

    /** Người chơi chạm vào [floor] trên bản đồ. */
    fun onSelect(floor: Floor): TowerStage =
        if (floor.hasDialogue) TowerStage.INTRO else afterIntro(floor)

    /** Xem xong (hoặc bỏ qua) thoại mở đầu. */
    fun afterIntro(floor: Floor): TowerStage =
        if (floor.goal is FloorGoal.Claim) TowerStage.VICTORY else TowerStage.BATTLE

    /** Vừa thắng ván ở [floor]. */
    fun afterWin(floor: Floor): TowerStage =
        if (FloorDialogues.epilogue(floor.number).isNotEmpty()) TowerStage.EPILOGUE else TowerStage.VICTORY

    /** Tầng kế tiếp của [floor], `null` ở tầng cuối. [floors] đánh số liên tục từ 1 nên chỉ số chính là số tầng. */
    fun nextFloor(floors: List<Floor>, floor: Floor): Floor? = floors.getOrNull(floor.number)
}
