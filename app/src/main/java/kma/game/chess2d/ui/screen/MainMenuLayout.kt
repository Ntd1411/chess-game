package kma.game.chess2d.ui.screen

import kotlin.math.min

/** Bố cục theo kích thước vùng an toàn thực tế (dp), không letterbox khung 1080x1920. */
internal data class MainMenuLayout(
    val width: Float,
    val height: Float,
    val margin: Float,
    val profile: Rect,
    val gold: Rect,
    val crystals: Rect,
    val rail: Rect,
    val hero: Rect,
    val modes: List<Rect>,
    val sideItemHeight: Float,
    val landscape: Boolean,
) {
    data class Rect(val x: Float, val y: Float, val w: Float, val h: Float) {
        val right get() = x + w
        val bottom get() = y + h
        fun overlaps(other: Rect) = x < other.right && other.x < right && y < other.bottom && other.y < bottom
    }

    companion object {
        fun calculate(width: Float, height: Float): MainMenuLayout {
            require(width > 0f && height > 0f)
            val margin = (width * 0.035f).coerceIn(8f, 20f)
            val gap = (width * 0.018f).coerceIn(6f, 12f)
            val landscape = width >= height * 1.2f
            val railWidth = (width * 0.17f).coerceIn(60f, 88f)
            val profileWidth = min(min((width - margin * 2) * 0.60f, 360f), height * 0.26f * 1967f / 799f)
            val profile = Rect(margin, margin, profileWidth, profileWidth * 799f / 1967f)
            val currencyX = profile.right + gap
            val currencyWidth = min(width - margin - currencyX, 200f)
            val currencyHeight = min(30f, profile.h * 0.34f)
            val gold = Rect(width - margin - currencyWidth, margin + 2f, currencyWidth, currencyHeight)
            val crystals = gold.copy(y = gold.bottom + gap)
            val modes = if (landscape) {
                // Màn ngang: một hàng giữ đủ năm mode và không đè lên cột nút bên phải.
                val available = width - railWidth - margin * 3 - gap
                val rowWidth = min(available, 720f)
                val smallWidth = (rowWidth - gap * 4) / 5.9f
                val cardHeight = min(smallWidth * 1.15f, height * 0.32f).coerceAtLeast(60f)
                var x = margin + (available - rowWidth) / 2
                (0 until 5).map { index ->
                    val cardWidth = smallWidth * if (index < 2) 1.45f else 1f
                    Rect(x, height - margin - cardHeight, cardWidth, cardHeight).also { x += cardWidth + gap }
                }
            } else {
                val gridWidth = min(width - margin * 2, 680f)
                val x = (width - gridWidth) / 2
                val bigWidth = (gridWidth - gap) / 2
                val smallWidth = (gridWidth - gap * 2) / 3
                val naturalBig = bigWidth * 0.68f
                val naturalSmall = smallWidth * 1.02f
                val shrink = min(1f, height * 0.36f / (naturalBig + naturalSmall + gap))
                val bigHeight = (naturalBig * shrink).coerceAtLeast(48f)
                val smallHeight = (naturalSmall * shrink).coerceAtLeast(48f)
                val top = height - margin - bigHeight - gap - smallHeight
                listOf(
                    Rect(x, top, bigWidth, bigHeight),
                    Rect(x + bigWidth + gap, top, bigWidth, bigHeight),
                    Rect(x, top + bigHeight + gap, smallWidth, smallHeight),
                    Rect(x + smallWidth + gap, top + bigHeight + gap, smallWidth, smallHeight),
                    Rect(x + (smallWidth + gap) * 2, top + bigHeight + gap, smallWidth, smallHeight),
                )
            }
            val railTop = maxOf(profile.bottom, crystals.bottom) + gap
            val railBottom = if (landscape) height - margin else modes.first().y - gap
            val rail = Rect(width - margin - railWidth, railTop, railWidth, railBottom - railTop)
            val hero = Rect(margin, railTop, rail.x - margin - gap, (modes.first().y - gap - railTop).coerceAtLeast(0f))
            val sideItemHeight = if (rail.h < 90f) {
                rail.h.coerceAtLeast(48f)
            } else if (rail.h >= 5 * 72f) {
                (rail.h / 5f).coerceAtMost(94f)
            } else {
                // Chừa 18 dp cho dấu cuộn; ở trạng thái nghỉ không cắt nửa nhãn của nút kế tiếp.
                val visibleHeight = (rail.h - 18f).coerceAtLeast(48f)
                val visibleItems = (visibleHeight / 72f).toInt().coerceAtLeast(1)
                (visibleHeight / visibleItems).coerceAtLeast(72f)
            }
            return MainMenuLayout(
                width, height, margin, profile, gold, crystals, rail, hero, modes,
                sideItemHeight, landscape,
            )
        }
    }
}
