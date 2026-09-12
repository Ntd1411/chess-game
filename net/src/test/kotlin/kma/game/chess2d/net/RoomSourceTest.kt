package kma.game.chess2d.net

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm tra tầng trữu tượng nguồn phòng: định danh bằng mã phòng, không bằng IP.
 *
 * Đây là điểm dễ sai nhất của sảnh chờ: điện thoại đổi IP thì phòng phải được cập
 * nhật tại chỗ, không được nhân đôi thành hai thẻ.
 */
class RoomSourceTest {

    @Test
    fun `cung ma phong nhung doi IP thi khong nhan doi`() = runTest {
        val source = FakeRoomSource(initialRooms = listOf(fakeRoom("room-1", address = "192.168.1.10")))

        source.publish(fakeRoom("room-1", address = "192.168.1.77"))

        val rooms = source.rooms.first()
        assertEquals(1, rooms.size)
        assertEquals("192.168.1.77", rooms.single().address)
    }

    @Test
    fun `hai ma phong khac nhau la hai phong`() = runTest {
        val source = FakeRoomSource()

        source.publish(fakeRoom("room-1"))
        source.publish(fakeRoom("room-2"))

        assertEquals(listOf("room-1", "room-2"), source.rooms.first().map { it.roomId })
    }

    @Test
    fun `khoa cua phong khong chua dia chi`() {
        val first = fakeRoom("room-1", address = "192.168.1.10", port = 45_455)
        val second = fakeRoom("room-1", address = "10.0.0.4", port = 51_000)

        assertEquals(first.key, second.key)
        assertEquals("fake/room-1", first.key)
    }

    @Test
    fun `phong lech phien ban van hien ra nhung khong vao duoc`() {
        val room = fakeRoom("room-1", protocolVersion = PROTOCOL_VERSION + 1)

        assertFalse(room.compatible)
        assertFalse(room.joinable)
    }

    @Test
    fun `phong dang co van thi khong vao duoc`() {
        val room = fakeRoom("room-1", busy = true)

        assertTrue(room.compatible)
        assertFalse(room.joinable)
    }

    @Test
    fun `xoa danh sach thi sanh khong con phong nguoi`() = runTest {
        val source = FakeRoomSource(initialRooms = listOf(fakeRoom("room-1")))

        source.clear()

        assertTrue(source.rooms.first().isEmpty())
    }

    @Test
    fun `beacon LAN doi sang phong hien thi giu nguyen ma phong`() {
        val discovered = DiscoveredRoom(
            roomId = "abc12345",
            hostName = "Pixel",
            host = "192.168.1.23",
            gamePort = 45_460,
            busy = false,
            protocolVersion = PROTOCOL_VERSION,
            lastSeenMillis = 1_000,
        )

        val info = discovered.toRoomInfo()

        assertEquals("lan/abc12345", info.key)
        assertEquals("192.168.1.23", info.address)
        assertEquals(45_460, info.port)
        assertTrue(info.joinable)
    }
}
