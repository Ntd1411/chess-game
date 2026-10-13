package kma.game.chess2d.ui.screen

/** Source-pixel slots are transformed with the artwork, never guessed from viewport dimensions. */
internal object GamePlayLayout {
    const val OPPONENT_TOP = 36
    const val FRAME_WIDTH = 984f
    const val FRAME_HEIGHT = 280
    const val BOARD_TOP = 332
    const val BOARD_SIZE = 960f
    const val PLAYER_TOP = 1310
    const val ACTIONS_TOP = 1630
    const val DETAILS_TOP = 1810
    const val DETAILS_HEIGHT = 100

    data class Rect(val x: Float, val y: Float, val w: Float, val h: Float) {
        val right get() = x + w
        val bottom get() = y + h
        fun overlaps(other: Rect) = x < other.right && other.x < right && y < other.bottom && other.y < bottom
    }

    data class PlayerSlots(
        val crop: Rect,
        val avatarSource: Rect,
        val nameSource: Rect,
        val subtitleSource: Rect,
        val clockSource: Rect,
        val capturesSource: Rect,
    ) {
        val scale get() = FRAME_WIDTH / crop.w
        val height get() = crop.h * scale
        fun map(rect: Rect) = Rect((rect.x - crop.x) * scale, (rect.y - crop.y) * scale, rect.w * scale, rect.h * scale)
        val avatar get() = map(avatarSource)
        val name get() = map(nameSource)
        val subtitle get() = map(subtitleSource)
        val clock get() = map(clockSource)
        val captures get() = map(capturesSource)
    }

    fun player(upper: Boolean): PlayerSlots = if (upper) PlayerSlots(
        crop = Rect(12f, 301f, 1500f, 368f),
        avatarSource = Rect(79f, 364f, 237f, 237f),
        nameSource = Rect(360f, 358f, 710f, 62f),
        subtitleSource = Rect(360f, 423f, 710f, 40f),
        clockSource = Rect(1320f, 524f, 155f, 76f),
        capturesSource = Rect(360f, 514f, 755f, 80f),
    ) else PlayerSlots(
        crop = Rect(26f, 287f, 1479f, 418f),
        avatarSource = Rect(102f, 365f, 259f, 259f),
        nameSource = Rect(405f, 440f, 740f, 62f),
        subtitleSource = Rect(405f, 502f, 740f, 40f),
        clockSource = Rect(1360f, 444f, 110f, 76f),
        capturesSource = Rect(405f, 605f, 740f, 80f),
    )

    // The board frame is a full 1024x1024 canvas with its actual opening at (92,85)..(932,927).
    val boardSource = Rect(102f, 96f, 820f, 820f)
    val board get() = Rect(
        60f + boardSource.x * BOARD_SIZE / 1024f,
        BOARD_TOP + boardSource.y * BOARD_SIZE / 1024f,
        boardSource.w * BOARD_SIZE / 1024f,
        boardSource.h * BOARD_SIZE / 1024f,
    )
}
