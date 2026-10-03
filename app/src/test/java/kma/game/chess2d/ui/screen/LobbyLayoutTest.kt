package kma.game.chess2d.ui.screen

import org.junit.Assert.assertTrue
import org.junit.Test

/** Yêu cầu bố cục sảnh: hai chế độ chính phải to hơn ba chế độ phụ (kiểm bằng giá trị trong code). */
class LobbyLayoutTest {

    @Test
    fun `nut che do chinh cao hon nut che do phu`() {
        assertTrue(
            LobbyLayout.PrimaryButtonHeight.value > LobbyLayout.SecondaryButtonHeight.value,
        )
    }

    @Test
    fun `nut phu van du lon de cham`() {
        // 48dp là cỡ chạm tối thiểu khuyến nghị của Material.
        assertTrue(LobbyLayout.SecondaryButtonHeight.value >= 48f)
    }
}
