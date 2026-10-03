package kma.game.chess2d.spectate

/**
 * Tốc độ xem: khoảng nghỉ giữa hai nước để mắt người kịp theo dõi.
 *
 * Khoảng nghỉ cộng thêm vào thời gian AI nghĩ, nên ở cấp Khó nước đi vẫn có thể chậm
 * hơn mức này; tốc độ chỉ là trần dưới, không phải đồng hồ.
 */
enum class SpectateSpeed(val factor: Int, val pauseMillis: Long) {
    X1(factor = 1, pauseMillis = 1_000),
    X2(factor = 2, pauseMillis = 500),
    X4(factor = 4, pauseMillis = 250),
    ;

    /** Vòng ×1 → ×2 → ×4 → ×1 cho nút đổi tốc độ. */
    fun next(): SpectateSpeed = entries[(ordinal + 1) % entries.size]
}

/**
 * Các nút điều khiển của người xem, tách thành lớp thuần để test luật mà không cần
 * coroutine.
 *
 * - [paused]: đóng băng chế độ tự động.
 * - [autoPlay]: tự đi nước kế tiếp. Tắt đi thì AI chỉ đi khi người xem bấm "đi 1 nước".
 * - [pendingSteps]: số lần bấm "đi 1 nước" chưa được thực hiện (tối đa 1: bấm dồn
 *   nhiều lần lúc AI còn đang nghĩ không được xếp hàng thành một loạt nước).
 */
data class SpectateControls(
    val paused: Boolean = false,
    val autoPlay: Boolean = true,
    val speed: SpectateSpeed = SpectateSpeed.X1,
    val pendingSteps: Int = 0,
) {

    /** Có được phép đi nước kế tiếp ngay bây giờ không. */
    val canAdvance: Boolean
        get() = pendingSteps > 0 || (autoPlay && !paused)

    /** Bấm "đi 1 nước". Không có tác dụng khi chế độ tự động đang chạy, vì máy vẫn tự đi. */
    fun requestStep(): SpectateControls =
        if (autoPlay && !paused) this else copy(pendingSteps = 1)

    /** Gọi sau mỗi nước đã đi xong: tiêu thụ lần bấm "đi 1 nước" nếu có. */
    fun afterMove(): SpectateControls =
        if (pendingSteps > 0) copy(pendingSteps = pendingSteps - 1) else this

    fun togglePause(): SpectateControls = copy(paused = !paused)

    fun toggleAutoPlay(): SpectateControls = copy(autoPlay = !autoPlay)

    fun cycleSpeed(): SpectateControls = copy(speed = speed.next())
}
