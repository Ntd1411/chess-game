package kma.game.chess2d.campaign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kma.game.chess2d.engine.Move
import kma.game.chess2d.puzzle.Puzzle
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Giữ ván của một tầng và cho máy đáp lại ngoài main thread.
 *
 * Mỗi lượt máy nghĩ trên [Dispatchers.Default] (cấp Khó tới 5 giây, chặn main thread là
 * ANR). Nước tìm được được áp lại trên luồng chính, và trong lúc máy nghĩ mọi cú chạm bị
 * bỏ qua, nên giao diện không bao giờ đọc phải ván đang đổi.
 *
 * ViewModel gắn với Activity nên sống qua nhiều tầng: [start] nhận khóa `(tầng, lượt chơi)`
 * và chỉ dựng ván mới khi khóa đổi, nhờ vậy xoay máy giữ nguyên ván còn "Thử lại" thì có
 * ván mới.
 */
class CampaignViewModel : ViewModel() {

    private var match: CampaignMatch? = null
    private var key: Pair<Int, Int>? = null
    private var job: Job? = null
    private var thinking = false

    private val _uiState = MutableStateFlow(CampaignUiState())
    val uiState: StateFlow<CampaignUiState> = _uiState.asStateFlow()

    /**
     * Mở ván cho [floor]. Gọi lại với cùng ([floor], [attempt]) thì không làm gì.
     *
     * Ghi trạng thái ngay trong lúc gọi (không đợi coroutine) để màn hình đọc được ván mới
     * ngay khung hình đầu, không thấy nhầm kết quả của tầng trước.
     */
    fun start(floor: Floor, attempt: Int, puzzles: List<Puzzle>) {
        val newKey = floor.number to attempt
        if (key == newKey) return
        job?.cancel()
        job = null
        thinking = false
        key = newKey
        match = CampaignMatch(floor, puzzles)
        publish()
    }

    /** Màn hình hiện ra: nếu đang tới lượt máy (xoay máy giữa lúc máy nghĩ) thì cho máy nghĩ lại. */
    fun attach() = maybeStartAiReply()

    /** Rời màn hình: hủy lượt nghĩ đang chạy, giữ nguyên ván. */
    fun detach() {
        job?.cancel()
        job = null
        thinking = false
        publish()
    }

    fun onSquareTap(square: Int) {
        val current = match ?: return
        if (thinking) return
        val moved = current.onSquareTap(square)
        publish()
        if (moved) maybeStartAiReply()
    }

    fun onPromotionChosen(move: Move) {
        val current = match ?: return
        if (thinking) return
        val moved = current.onPromotionChosen(move)
        publish()
        if (moved) maybeStartAiReply()
    }

    fun onPromotionDismissed() {
        val current = match ?: return
        if (thinking) return
        current.onPromotionDismissed()
        publish()
    }

    private fun maybeStartAiReply() {
        val current = match ?: return
        if (thinking || !current.needsAiReply) return

        thinking = true
        publish()
        job = viewModelScope.launch {
            val me = coroutineContext[Job]
            try {
                val move = withContext(Dispatchers.Default) {
                    val self = coroutineContext[Job]
                    // Hủy job là điểm hủy thật sự: search tự dừng thay vì chạy tiếp trong nền.
                    current.chooseAiMove(isActive = { self?.isActive != false })
                }
                // Ván đã bị thay (sang tầng khác) trong lúc nghĩ thì bỏ nước này.
                if (move != null && match === current) current.applyAiMove(move)
            } finally {
                // Lượt nghĩ cũ (đã bị hủy rồi có lượt mới thay) không được tắt cờ của lượt mới.
                if (match === current && job === me) {
                    thinking = false
                    publish()
                }
            }
        }
    }

    private fun publish() {
        val current = match ?: return
        _uiState.value = current.snapshot(thinking)
    }

    override fun onCleared() {
        job?.cancel()
    }
}
