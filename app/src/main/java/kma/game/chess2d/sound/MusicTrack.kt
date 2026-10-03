package kma.game.chess2d.sound

import kma.game.chess2d.campaign.NodeKind
import kma.game.chess2d.campaign.TowerStage

/**
 * Các bản nhạc của app, mỗi bản ứng với một tệp trong `res/raw` (tìm theo **tên**, không theo
 * `R.raw.*`, để thiếu tệp thì app vẫn biên dịch và chạy, chỉ im lặng).
 *
 * @param resourceName tên tệp không kèm đuôi (`music_menu` ứng với `music_menu.wav`).
 * @param loops phát lặp mãi (nhạc nền) hay chỉ một lần (đoạn nhạc báo kết quả).
 * @param next bản nối tiếp khi bản này phát xong; chỉ có nghĩa với bản không lặp. Đoạn báo
 *        thắng/thua ngắn, nên hết là quay lại nhạc menu thay vì để màn kết quả im bặt.
 */
enum class MusicTrack(
    val resourceName: String,
    val loops: Boolean,
    private val nextName: String? = null,
) {
    MENU("music_menu", loops = true),
    BATTLE("music_battle", loops = true),
    BOSS("music_boss", loops = true),
    VICTORY("victory", loops = false, nextName = "MENU"),
    DEFEAT("defeat", loops = false, nextName = "MENU");

    val next: MusicTrack? get() = nextName?.let { valueOf(it) }
}

/**
 * Luật chọn nhạc theo ngữ cảnh, tách khỏi Compose để kiểm bằng test thuần.
 */
object MusicPlan {

    /**
     * Nhạc cho từng chặng của Tháp Cờ.
     *
     * Bản đồ và thoại dùng nhạc menu (không khí khám phá, không căng thẳng); ván đấu dùng nhạc
     * chiến đấu, riêng tầng trùm có bản riêng; thắng/thua phát đoạn báo kết quả.
     */
    fun forTowerStage(stage: TowerStage, kind: NodeKind): MusicTrack = when (stage) {
        TowerStage.MAP, TowerStage.INTRO, TowerStage.EPILOGUE -> MusicTrack.MENU
        TowerStage.BATTLE -> if (kind == NodeKind.BOSS) MusicTrack.BOSS else MusicTrack.BATTLE
        TowerStage.VICTORY -> MusicTrack.VICTORY
        TowerStage.DEFEAT -> MusicTrack.DEFEAT
    }
}
