package kma.game.chess2d.spectate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.game.GameUiState
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Mọi thứ màn Máy vs Máy cần để vẽ một khung hình. */
data class SpectateUiState(
    val game: GameUiState = GameUiState(),
    val whiteLevel: Difficulty = Difficulty.MEDIUM,
    val blackLevel: Difficulty = Difficulty.MEDIUM,
    /** Ván dừng vì chạm trần số nước chứ không phải vì luật cờ. */
    val hitMoveLimit: Boolean = false,
    val controls: SpectateControls = SpectateControls(),
)

/**
 * Chạy ván Máy vs Máy và nhận lệnh điều khiển từ người xem.
 *
 * Vòng lặp luôn chạy trong đúng một coroutine, mỗi lượt nghĩ trên [Dispatchers.Default]
 * (AI cấp Khó nghĩ tới 5 giây, chặn main thread là ANR). Giao diện chỉ đọc ảnh chụp bất
 * biến [SpectateMatch.snapshot], được chụp ở main thread **giữa** hai lượt nghĩ nên không
 * bao giờ đọc phải bàn cờ nửa vời.
 *
 * Tạm dừng có hiệu lực **sau** nước đang nghĩ: một lượt search không dừng giữa chừng một
 * cách sạch sẽ, và cũng không cần — nước đó sẽ đi xong rồi ván đứng yên.
 */
class SpectateViewModel : ViewModel() {

    private var match: SpectateMatch? = null
    private var job: Job? = null

    private val controls = MutableStateFlow(SpectateControls())
    private val frame = MutableStateFlow(Frame())

    private data class Frame(
        val game: GameUiState = GameUiState(),
        val whiteLevel: Difficulty = Difficulty.MEDIUM,
        val blackLevel: Difficulty = Difficulty.MEDIUM,
        val hitMoveLimit: Boolean = false,
    )

    val uiState: StateFlow<SpectateUiState> = combine(frame, controls) { f, c ->
        SpectateUiState(f.game, f.whiteLevel, f.blackLevel, f.hitMoveLimit, c)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SpectateUiState())

    /**
     * Bắt đầu một ván mới giữa hai AI. Chưa chạy: màn hình gọi [attach] khi hiện ra.
     * Ván cũ (nếu có) bị hủy, và vòng lặp mới sẽ đợi vòng cũ dừng hẳn mới chạy.
     */
    fun newMatch(white: Difficulty, black: Difficulty) {
        job?.cancel()
        val fresh = SpectateMatch(white, black)
        match = fresh
        controls.value = SpectateControls()
        publish(fresh, thinking = false)
    }

    /** Có ván đang giữ không (để biết nên mở thẳng ván đó hay cần chọn AI trước). */
    val hasMatch: Boolean get() = match != null

    /** Chạy (hoặc chạy tiếp) vòng lặp của ván hiện tại. An toàn khi gọi nhiều lần. */
    fun attach() {
        val current = match ?: return
        if (job?.isActive == true) return
        val previous = job
        job = viewModelScope.launch {
            // Vòng cũ có thể đang chờ search blocking kết thúc sau khi bị hủy; hai vòng mà
            // cùng chạm vào một bàn cờ là lỗi khó tìm nhất, nên chờ vòng cũ dừng hẳn.
            previous?.join()
            run(current)
        }
    }

    /** Dừng vòng lặp khi rời màn hình, giữ nguyên ván để quay lại (xoay máy) vẫn thấy ván cũ. */
    fun detach() {
        job?.cancel()
    }

    fun togglePause() = controls.update { it.togglePause() }

    fun toggleAutoPlay() = controls.update { it.toggleAutoPlay() }

    fun cycleSpeed() = controls.update { it.cycleSpeed() }

    fun stepOnce() = controls.update { it.requestStep() }

    private suspend fun run(current: SpectateMatch) {
        try {
            while (!current.isOver) {
                controls.first { it.canAdvance }
                publish(current, thinking = true)

                val self = coroutineContext[Job]
                val move = withContext(Dispatchers.Default) {
                    // Hủy job là điểm hủy thật sự: search tự dừng thay vì chạy tiếp trong nền.
                    current.step(isActive = { self?.isActive != false })
                } ?: break

                controls.update { it.afterMove() }
                publish(current, thinking = false)
                if (!current.isOver && controls.value.autoPlay) delay(controls.value.speed.pauseMillis)
            }
        } finally {
            publish(current, thinking = false)
        }
    }

    private fun publish(current: SpectateMatch, thinking: Boolean) {
        // Ván đã bị thay bằng ván khác thì ảnh chụp cũ không được ghi đè ván mới.
        if (match !== current) return
        frame.value = Frame(
            game = current.snapshot(thinking),
            whiteLevel = current.whiteLevel,
            blackLevel = current.blackLevel,
            hitMoveLimit = current.hitMoveLimit,
        )
    }

    override fun onCleared() {
        job?.cancel()
    }
}
