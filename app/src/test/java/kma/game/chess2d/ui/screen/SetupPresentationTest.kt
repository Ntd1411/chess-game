package kma.game.chess2d.ui.screen

import kma.game.chess2d.R
import kma.game.chess2d.net.RoomInfo
import kma.game.chess2d.net.TimeControl
import kma.game.chess2d.opponent.AiCharacters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetupPresentationTest {
    @Test
    fun aiNavigationWrapsAtBothEnds() {
        val count = AiCharacters.all.size
        assertEquals(count - 1, AiSelectionNavigation.previous(0, count))
        assertEquals(0, AiSelectionNavigation.next(count - 1, count))
        for (index in 0 until count) {
            assertEquals(index, AiSelectionNavigation.previous(AiSelectionNavigation.next(index, count), count))
        }
    }

    @Test
    fun singleAiNavigationStaysAtZero() {
        assertEquals(0, AiSelectionNavigation.previous(0, 1))
        assertEquals(0, AiSelectionNavigation.next(0, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun aiNavigationRejectsEmptyRoster() {
        AiSelectionNavigation.next(0, 0)
    }

    @Test
    fun lockedAiCanBeReachedButOnlyUnlocksAtRequiredFloor() {
        val characters = AiCharacters.all
        val lockedIndex = characters.indexOfFirst { it.unlockFloor != null }
        assertTrue(lockedIndex >= 0)
        val character = characters[lockedIndex]
        assertEquals(lockedIndex, AiSelectionNavigation.previous((lockedIndex + 1) % characters.size, characters.size))
        val requiredFloor = requireNotNull(character.unlockFloor)
        assertFalse(character.isUnlocked(requiredFloor - 1))
        assertTrue(character.isUnlocked(requiredFloor))
    }

    @Test
    fun openCompatibleRoomCanBeJoined() {
        val room = room(busy = false)
        assertEquals(R.string.lan_badge_open, lanRoomBadge(room))
        assertTrue(room.joinable)
    }

    @Test
    fun busyRoomIsDisabledWithOccupancyReason() {
        val room = room(busy = true)
        assertEquals(R.string.lan_badge_playing, lanRoomBadge(room))
        assertFalse(room.joinable)
    }

    @Test
    fun versionMismatchTakesPriorityOverOccupancy() {
        val room = room(busy = true)
        val mismatch = room.copy(protocolVersion = room.protocolVersion + 1)
        assertEquals(R.string.lan_badge_version, lanRoomBadge(mismatch))
        assertFalse(mismatch.joinable)
        assertEquals(R.string.lan_badge_version, lanRoomBadge(mismatch.copy(busy = false)))
    }

    @Test
    fun allLanTimeControlsKeepTheirFullLabels() {
        assertEquals(R.string.time_control_unlimited, timeControlLabel(TimeControl.UNLIMITED))
        assertEquals(R.string.time_control_blitz, timeControlLabel(TimeControl.BLITZ))
        assertEquals(R.string.time_control_rapid, timeControlLabel(TimeControl.RAPID))
    }

    private fun room(busy: Boolean) = RoomInfo(
        sourceId = "lan", roomId = "test-room", hostName = "Host",
        address = "192.168.1.5", port = 45455, busy = busy,
    )
}
