package kma.game.chess2d.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kma.game.chess2d.game.MoveSound

/**
 * Phát tiếng và rung mỗi khi có nước đi.
 *
 * Dùng [SoundPool] chứ không phải `MediaPlayer`: các tiếng ở đây rất ngắn, được phát
 * liên tục và phải kêu ngay khi quân đặt xuống, nên cần giữ sẵn trong bộ nhớ thay vì
 * giải nén lại từ đầu mỗi lần.
 *
 * Tệp âm thanh được tìm theo **tên** trong `res/raw` chứ không tham chiếu thẳng
 * `R.raw.*`. Lý do: bộ âm thanh là asset tải từ ngoài vào (mục 7.1), chưa thêm tệp thì
 * app vẫn phải biên dịch và chạy bình thường — chỉ là chơi trong im lặng.
 */
class SoundEffects(context: Context) {

    private val appContext = context.applicationContext

    private val pool: SoundPool = SoundPool.Builder()
        // Hai luồng là đủ: tiếng cũ còn ngân thì tiếng mới vẫn vào được, không hơn.
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                // Xếp vào nhóm hiệu ứng game để hệ thống hạ âm lượng đúng cách khi
                // người chơi đang nghe nhạc hay có cuộc gọi.
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    /** id trong [SoundPool] của từng tiếng. Thiếu tệp thì khóa đó không có mặt. */
    private val loaded = HashMap<MoveSound, Int>()

    private val vibrator: Vibrator? = resolveVibrator()

    init {
        for (sound in MoveSound.entries) {
            val resId = appContext.resources.getIdentifier(
                resourceNameOf(sound),
                "raw",
                appContext.packageName,
            )
            if (resId != 0) loaded[sound] = pool.load(appContext, resId, PRIORITY)
        }
    }

    /**
     * Phát phản hồi cho một nước đi.
     *
     * Hai công tắc độc lập nhau: tắt tiếng mà vẫn muốn rung là nhu cầu thật khi đang
     * ở chỗ đông người.
     */
    fun play(sound: MoveSound, soundEnabled: Boolean, hapticEnabled: Boolean) {
        if (soundEnabled) {
            loaded[sound]?.let { id ->
                pool.play(id, VOLUME, VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
            }
        }
        if (hapticEnabled) vibrate(sound)
    }

    /**
     * Trả bộ nhớ âm thanh lại cho hệ thống.
     *
     * Bắt buộc gọi khi rời bàn cờ: [SoundPool] giữ dữ liệu đã giải nén và không tự
     * biến mất theo màn hình.
     */
    fun release() {
        loaded.clear()
        pool.release()
    }

    /** Rung ngắn cho nước thường, rung dài hơn cho những việc đáng chú ý hơn. */
    private fun vibrate(sound: MoveSound) {
        val device = vibrator ?: return
        if (!device.hasVibrator()) return
        val millis = when (sound) {
            MoveSound.MOVE -> 12L
            MoveSound.CAPTURE -> 20L
            MoveSound.CHECK -> 30L
            MoveSound.GAME_END -> 60L
        }
        device.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    /** Từ API 31, [Vibrator] lấy qua [VibratorManager]; bản cũ vẫn lấy trực tiếp. */
    private fun resolveVibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(VibratorManager::class.java)
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Vibrator::class.java)
        }

    private companion object {
        const val VOLUME = 1f
        const val PRIORITY = 1
        const val NO_LOOP = 0
        const val NORMAL_RATE = 1f

        /**
         * Tên tệp mong đợi trong `res/raw`.
         *
         * Giữ thành một chỗ duy nhất để người thêm asset biết chính xác phải đặt tên gì:
         * `move.ogg`, `capture.ogg`, `check.ogg`, `game_end.ogg`.
         */
        fun resourceNameOf(sound: MoveSound): String = when (sound) {
            MoveSound.MOVE -> "move"
            MoveSound.CAPTURE -> "capture"
            MoveSound.CHECK -> "check"
            MoveSound.GAME_END -> "game_end"
        }
    }
}
