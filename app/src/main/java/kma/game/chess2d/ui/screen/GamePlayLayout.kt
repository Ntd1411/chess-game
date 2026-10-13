package kma.game.chess2d.ui.screen

/** Simple, matching player cards and a horizontal move strip above the board. */
internal object GamePlayLayout {
    const val MOVES_TOP = 16
    const val MOVES_HEIGHT = 80
    const val OPPONENT_TOP = 112
    const val FRAME_WIDTH = 984f
    const val FRAME_HEIGHT = 176
    const val HEADLINE_TOP = 300
    const val HEADLINE_HEIGHT = 48
    const val BOARD_TOP = 360
    const val BOARD_SIZE = 960f
    const val PLAYER_TOP = 1338
    const val ACTIONS_TOP = 1570
    const val DETAILS_TOP = 1750
    const val DETAILS_HEIGHT = 160

    data class Rect(val x: Float, val y: Float, val w: Float, val h: Float) {
        val right get() = x + w
        val bottom get() = y + h
        fun overlaps(other: Rect) = x < other.right && other.x < right && y < other.bottom && other.y < bottom
    }

    data class PlayerSlots(
        val avatar: Rect = Rect(20f, 24f, 128f, 128f),
        val name: Rect = Rect(172f, 16f, 540f, 52f),
        val subtitle: Rect = Rect(172f, 72f, 540f, 32f),
        val clock: Rect = Rect(744f, 36f, 216f, 104f),
        val captures: Rect = Rect(172f, 116f, 540f, 44f),
    ) {
        val height get() = FRAME_HEIGHT.toFloat()
    }

    val player = PlayerSlots()

    // Preserve the existing board artwork and its square opening.
    val boardSource = Rect(102f, 96f, 820f, 820f)
    val board get() = Rect(
        60f + boardSource.x * BOARD_SIZE / 1024f,
        BOARD_TOP + boardSource.y * BOARD_SIZE / 1024f,
        boardSource.w * BOARD_SIZE / 1024f,
        boardSource.h * BOARD_SIZE / 1024f,
    )
}
