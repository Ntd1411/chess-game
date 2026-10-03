package kma.game.chess2d.profile

import kma.game.chess2d.campaign.FloorGoal
import kma.game.chess2d.campaign.FloorResult
import kma.game.chess2d.campaign.LossReason
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.history.MatchResult

/**
 * Luật quyết định một ván có được ghi vào Hồ sơ hay không, và ghi như thế nào.
 *
 * Thuần (không biết Android/Room) để test được trên JVM; các nơi kết thúc ván
 * (đấu máy, LAN, chiến dịch) đều đi qua đây để số liệu thống nhất.
 */
object MatchRecording {

    /**
     * Kết quả ván theo góc nhìn người chơi.
     *
     * @param byCheckmate người chơi thắng bằng cách chiếu hết (đầu hàng/hết giờ thì không).
     */
    data class Outcome(val result: String, val byCheckmate: Boolean)

    /**
     * Thời lượng tối đa tính cho một ván (3 giờ).
     *
     * Thời gian đo bằng đồng hồ treo tường nên để app ở nền qua đêm sẽ ra số vô lý; cắt trần
     * để một ván bỏ quên không làm hỏng tổng thời gian chơi.
     */
    const val MAX_SECONDS_PER_MATCH = 3 * 60 * 60L

    /**
     * Chế độ nào được tính vào Hồ sơ.
     *
     * Đấu máy: có. Hai người cùng máy và Máy vs Máy: không, vì không có một "người chơi"
     * duy nhất để gán thắng/thua. LAN và chiến dịch có đường riêng nên không qua hàm này.
     */
    fun countsForProfile(mode: GameMode): Boolean = mode == GameMode.VS_COMPUTER

    /** Số giây từ [startMillis] đến [nowMillis]; chưa biết lúc bắt đầu thì là 0, luôn trong `0..MAX`. */
    fun elapsedSeconds(startMillis: Long?, nowMillis: Long): Long {
        if (startMillis == null) return 0L
        return ((nowMillis - startMillis) / 1000L).coerceIn(0L, MAX_SECONDS_PER_MATCH)
    }

    /**
     * Kết quả của một ván chiến dịch.
     *
     * @return `null` nếu ván chưa xong. Chỉ tầng [FloorGoal.DefeatAi] mới chắc chắn là
     *         thắng bằng chiếu hết; câu đố và tầng sống sót thì không tính là checkmate.
     */
    fun campaignOutcome(goal: FloorGoal, result: FloorResult, lossReason: LossReason?): Outcome? =
        when (result) {
            FloorResult.ONGOING -> null
            FloorResult.WON -> Outcome(MatchResult.WIN, byCheckmate = goal is FloorGoal.DefeatAi)
            FloorResult.LOST -> Outcome(
                if (lossReason == LossReason.DRAW) MatchResult.DRAW else MatchResult.LOSS,
                byCheckmate = false,
            )
        }
}
