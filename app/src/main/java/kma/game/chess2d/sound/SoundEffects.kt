package kma.game.chess2d.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors
import kma.game.chess2d.game.MoveSound
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Phát tiếng và rung mỗi khi có nước đi.
 *
 * Có hai đường phát tiếng, xếp theo thứ tự ưu tiên:
 *
 * 1. Tệp trong `res/raw` (`move.ogg`, `capture.ogg`, `check.ogg`, `game_end.ogg`), tìm
 *    theo **tên** chứ không tham chiếu thẳng `R.raw.*`, để chưa thêm asset thì app vẫn
 *    biên dịch được. Nạp bằng [SoundPool] vì các tiếng này rất ngắn và phải kêu ngay.
 * 2. Không có tệp nào thì **tự sinh** một tiếng bằng [AudioTrack]. Đây không phải cho
 *    vui: bản đang chạy không kèm asset âm thanh, nên nếu chỉ dựa vào `res/raw` thì
 *    công tắc âm thanh trong menu bật cũng không kêu gì — người chơi thấy như tính năng
 *    bị hỏng. Tiếng sinh ra là một nốt sin tắt dần, đủ để xác nhận cú đi.
 */
class SoundEffects(context: Context) {

    private val appContext = context.applicationContext

    private val audioAttributes = AudioAttributes.Builder()
        // Xếp vào nhóm hiệu ứng game để hệ thống hạ âm lượng đúng cách khi người chơi
        // đang nghe nhạc hay có cuộc gọi.
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val pool: SoundPool = SoundPool.Builder()
        // Hai luồng là đủ: tiếng cũ còn ngân thì tiếng mới vẫn vào được, không hơn.
        .setMaxStreams(2)
        .setAudioAttributes(audioAttributes)
        .build()

    /** id trong [SoundPool] của từng tiếng. Thiếu tệp thì khóa đó không có mặt. */
    private val loaded = HashMap<MoveSound, Int>()

    /** Mẫu PCM tự sinh, tính một lần rồi giữ lại vì mỗi ván dùng lại hàng chục lần. */
    private val synthesized = HashMap<MoveSound, ShortArray>()

    /**
     * Luồng riêng để phát tiếng tự sinh.
     *
     * [AudioTrack] phải được ghi, chờ phát xong rồi mới giải phóng, nên không thể làm
     * việc đó trên luồng UI. Một luồng duy nhất cũng đủ: các tiếng đều rất ngắn và
     * việc xếp chúng nối tiếp nhau còn dễ nghe hơn là cho chồng lên nhau.
     */
    private val player = Executors.newSingleThreadExecutor()

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
            val id = loaded[sound]
            if (id != null) {
                pool.play(id, VOLUME, VOLUME, PRIORITY, NO_LOOP, NORMAL_RATE)
            } else {
                playSynthesized(sound)
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
        synthesized.clear()
        pool.release()
        player.shutdown()
    }

    /** Phát nốt tự sinh. Lỗi âm thanh không bao giờ được làm sập ván đấu. */
    private fun playSynthesized(sound: MoveSound) {
        val samples = synthesized.getOrPut(sound) { synthesize(sound) }
        if (player.isShutdown) return
        runCatching {
            player.execute {
                runCatching {
                    val track = AudioTrack.Builder()
                        .setAudioAttributes(audioAttributes)
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(SAMPLE_RATE)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build(),
                        )
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .setBufferSizeInBytes(samples.size * BYTES_PER_SAMPLE)
                        .build()
                    track.write(samples, 0, samples.size)
                    track.play()
                    // Chờ đúng độ dài của mẫu rồi mới thả: thả sớm thì tiếng bị cắt ngang.
                    Thread.sleep(samples.size * 1_000L / SAMPLE_RATE + TAIL_MILLIS)
                    track.stop()
                    track.release()
                }
            }
        }
    }

    /**
     * Sinh một nốt sin tắt dần theo hàm mũ.
     *
     * Tắt dần là phần bắt buộc: sin bị cắt đột ngột nghe thành tiếng "bốp" rất khó chịu
     * vì dạng sóng nhảy bậc ở cuối mẫu.
     */
    private fun synthesize(sound: MoveSound): ShortArray {
        val (frequency, millis) = when (sound) {
            MoveSound.MOVE -> 660.0 to 70
            MoveSound.CAPTURE -> 320.0 to 110
            MoveSound.CHECK -> 880.0 to 150
            MoveSound.GAME_END -> 440.0 to 320
        }
        val count = SAMPLE_RATE * millis / 1_000
        val step = 2.0 * PI * frequency / SAMPLE_RATE
        return ShortArray(count) { index ->
            val progress = index.toDouble() / count
            val envelope = exp(-DECAY * progress)
            // Hết ván thì thêm nốt thứ hai ở quãng năm để nghe ra là một kết thúc.
            val wave = if (sound == MoveSound.GAME_END) {
                (sin(step * index) + HARMONY_GAIN * sin(step * HARMONY_RATIO * index)) /
                    (1.0 + HARMONY_GAIN)
            } else {
                sin(step * index)
            }
            (wave * envelope * PEAK).toInt().toShort()
        }
    }

    /**
     * Rung ngắn cho nước thường, rung dài hơn cho những việc đáng chú ý hơn.
     *
     * Các mốc đều từ 25 ms trở lên: rung 10–15 ms gần như không cảm nhận được trên
     * phần lớn máy Android, nên bật công tắc rung mà không thấy gì.
     */
    private fun vibrate(sound: MoveSound) {
        val device = vibrator ?: return
        if (!device.hasVibrator()) return
        val millis = when (sound) {
            MoveSound.MOVE -> 25L
            MoveSound.CAPTURE -> 45L
            MoveSound.CHECK -> 70L
            MoveSound.GAME_END -> 140L
        }
        runCatching {
            device.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
        }
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

        /** 22,05 kHz là quá đủ cho một nốt đơn và tốn nửa bộ nhớ so với 44,1 kHz. */
        const val SAMPLE_RATE = 22_050
        const val BYTES_PER_SAMPLE = 2
        const val TAIL_MILLIS = 40L
        const val DECAY = 6.0
        const val PEAK = 9_000.0
        const val HARMONY_GAIN = 0.6
        const val HARMONY_RATIO = 1.5

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
