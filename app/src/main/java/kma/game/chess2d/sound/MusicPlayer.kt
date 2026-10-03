package kma.game.chess2d.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer

/**
 * Phát nhạc nền và đoạn nhạc báo kết quả.
 *
 * Khác [SoundEffects]: đây là tệp dài (nhiều chục MB nếu để WAV), nên dùng [MediaPlayer] đọc
 * thẳng từ tệp chứ không nạp cả vào bộ nhớ như [android.media.SoundPool]. Mọi hàm đều gọi từ
 * luồng chính.
 *
 * Trạng thái được gom thành ba yếu tố độc lập rồi mới quyết định phát gì ([apply]):
 * - bản nhạc **muốn phát** ([setTrack]), do màn hình đang hiện quyết định;
 * - công tắc **nhạc nền** trong Cài đặt ([setEnabled]);
 * - app có đang **ở nền** không ([setSuspended]): xuống nền là tạm dừng, về lại thì phát tiếp
 *   đúng chỗ, không bắt đầu lại từ đầu.
 *
 * Đổi sang cùng bản đang phát thì **không làm gì**: đi từ menu sang bản đồ tháp vẫn là nhạc menu
 * và phải liền mạch, không được khởi động lại.
 *
 * Thiếu tệp hoặc máy không giải mã được thì im lặng, không bao giờ làm sập app.
 */
class MusicPlayer(context: Context) {

    private val appContext = context.applicationContext

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private var wanted: MusicTrack? = null
    private var enabled = true
    private var suspended = false

    private var player: MediaPlayer? = null
    private var playing: MusicTrack? = null

    fun setTrack(track: MusicTrack?) {
        wanted = track
        apply()
    }

    fun setEnabled(value: Boolean) {
        enabled = value
        apply()
    }

    fun setSuspended(value: Boolean) {
        suspended = value
        apply()
    }

    /** Trả tài nguyên cho hệ thống. Gọi khi cây giao diện gốc rời đi. */
    fun release() {
        wanted = null
        stopPlayer()
    }

    private fun apply() {
        val target = if (enabled) wanted else null
        if (target != playing) {
            stopPlayer()
            if (target != null) startPlayer(target)
        }
        val current = player ?: return
        runCatching {
            if (suspended) {
                if (current.isPlaying) current.pause()
            } else if (!current.isPlaying) {
                current.start()
            }
        }
    }

    private fun startPlayer(track: MusicTrack) {
        val resId = appContext.resources.getIdentifier(track.resourceName, "raw", appContext.packageName)
        if (resId == 0) return
        val created = runCatching {
            MediaPlayer.create(appContext, resId, audioAttributes, AudioManager.AUDIO_SESSION_ID_GENERATE)
        }.getOrNull() ?: return
        created.isLooping = track.loops
        created.setVolume(VOLUME, VOLUME)
        // Đoạn không lặp phát xong thì chuyển sang bản nối tiếp (vd. thắng → nhạc menu).
        created.setOnCompletionListener {
            if (player === it) {
                wanted = track.next
                apply()
            }
        }
        player = created
        playing = track
    }

    private fun stopPlayer() {
        val old = player
        player = null
        playing = null
        if (old != null) {
            old.setOnCompletionListener(null)
            runCatching { old.stop() }
            runCatching { old.release() }
        }
    }

    private companion object {
        /** Nhạc nền nhỏ hơn hiệu ứng để tiếng quân cờ vẫn nghe rõ. */
        const val VOLUME = 0.5f
    }
}
