package kma.game.chess2d.game

/**
 * Thời gian mỗi bên trong ván hai người cùng máy: Không giới hạn, 5, 10 hoặc 30 phút.
 *
 * Không có phần cộng thêm sau mỗi nước (khác thể thức 10+5 của LAN): spec chỉ yêu cầu bốn mức này.
 * Tách khỏi `TimeControl` của `:net` vì enum đó đi qua dây LAN, thêm giá trị vào đó sẽ đổi giao
 * thức mà ván offline không hề cần.
 */
enum class LocalTimeControl(val minutes: Int) {
    UNLIMITED(0),
    MIN_5(5),
    MIN_10(10),
    MIN_30(30),
    ;

    /** Thời gian ban đầu mỗi bên, tính bằng mili giây. 0 là không giới hạn. */
    val initialMillis: Long get() = minutes * 60_000L

    /** Có bấm giờ hay không. */
    val limited: Boolean get() = minutes > 0
}

/**
 * Lựa chọn của người chơi ở màn setup hai người cùng máy.
 *
 * Người chơi 1 luôn cầm Trắng, Người chơi 2 cầm Đen (đúng spec "chọn quân Trắng"/"chọn quân Đen"
 * cho từng người). Tên luôn không rỗng: ô để trống thì dùng tên mặc định.
 */
data class LocalSetup(
    val whiteName: String,
    val blackName: String,
    val time: LocalTimeControl = LocalTimeControl.UNLIMITED,
) {
    companion object {
        /** Tên dài hơn mức này bị cắt, để không phá bố cục thanh người chơi. */
        const val MAX_NAME_LENGTH = 20

        /**
         * Chuẩn hóa tên người dùng nhập: bỏ khoảng trắng đầu cuối, gộp các khoảng trắng liền nhau
         * thành một, cắt về [MAX_NAME_LENGTH]. Rỗng sau khi chuẩn hóa thì trả về `null`.
         */
        fun cleanName(raw: String): String? =
            raw.trim().replace(WHITESPACE_RUN, " ").take(MAX_NAME_LENGTH).trim().ifEmpty { null }

        /**
         * Dựng [LocalSetup] từ nội dung hai ô nhập. Ô trống hoặc chỉ có khoảng trắng dùng tên
         * mặc định tương ứng.
         */
        fun of(
            whiteInput: String,
            blackInput: String,
            time: LocalTimeControl,
            defaultWhite: String,
            defaultBlack: String,
        ): LocalSetup = LocalSetup(
            whiteName = cleanName(whiteInput) ?: defaultWhite,
            blackName = cleanName(blackInput) ?: defaultBlack,
            time = time,
        )

        private val WHITESPACE_RUN = Regex("\\s+")
    }
}

/**
 * Yêu cầu mở một ván hai người **mới** theo [setup].
 *
 * [token] tăng một mỗi lần người chơi bấm BẮT ĐẦU. ViewModel nhớ token đã xử lý nên xoay máy (dựng
 * lại màn hình, chạy lại hiệu ứng) không mở thêm ván mới đè lên ván đang chơi.
 */
data class LocalStart(val setup: LocalSetup, val token: Int)
