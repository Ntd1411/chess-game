package kma.game.chess2d.spectate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Luật của các nút điều khiển người xem, kiểm bằng lớp thuần không cần coroutine. */
class SpectateControlsTest {

    @Test
    fun `mac dinh tu dong chay va duoc phep di tiep`() {
        val controls = SpectateControls()
        assertTrue(controls.autoPlay)
        assertFalse(controls.paused)
        assertTrue(controls.canAdvance)
    }

    @Test
    fun `dung thi khong di tiep`() {
        val controls = SpectateControls().togglePause()
        assertTrue(controls.paused)
        assertFalse(controls.canAdvance)
        assertTrue(controls.togglePause().canAdvance)
    }

    @Test
    fun `che do thu cong chi di khi bam 1 nuoc`() {
        val manual = SpectateControls().toggleAutoPlay()
        assertFalse(manual.canAdvance)

        val requested = manual.requestStep()
        assertTrue(requested.canAdvance)

        val after = requested.afterMove()
        assertEquals(0, after.pendingSteps)
        assertFalse(after.canAdvance)
    }

    @Test
    fun `bam 1 nuoc nhieu lan luc dang nghi khong xep hang`() {
        val manual = SpectateControls().toggleAutoPlay()
        val spammed = manual.requestStep().requestStep().requestStep()
        assertEquals(1, spammed.pendingSteps)
        assertFalse(spammed.afterMove().canAdvance)
    }

    @Test
    fun `bam 1 nuoc khi may dang tu chay thi bo qua`() {
        val running = SpectateControls()
        assertEquals(running, running.requestStep())
    }

    @Test
    fun `dang dung van bam duoc 1 nuoc`() {
        val paused = SpectateControls().togglePause()
        assertTrue(paused.requestStep().canAdvance)
    }

    @Test
    fun `toc do quay vong 1 2 4 roi ve 1`() {
        var controls = SpectateControls()
        val seen = mutableListOf(controls.speed.factor)
        repeat(3) {
            controls = controls.cycleSpeed()
            seen.add(controls.speed.factor)
        }
        assertEquals(listOf(1, 2, 4, 1), seen)
    }

    @Test
    fun `toc do cao hon thi nghi ngan hon`() {
        assertTrue(SpectateSpeed.X2.pauseMillis < SpectateSpeed.X1.pauseMillis)
        assertTrue(SpectateSpeed.X4.pauseMillis < SpectateSpeed.X2.pauseMillis)
    }
}
