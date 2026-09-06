package kma.game.chess2d.net

import java.net.InetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test tìm phòng qua loopback.
 *
 * Scanner bind cổng 0 (hệ điều hành chọn) rồi test đọc [RoomScanner.boundPort] để
 * biết phải phát beacon vào đâu. Không dùng [DISCOVERY_PORT] thật: nếu dùng, test sẽ
 * bắt được beacon của một ván đấu thật đang chạy trong cùng mạng và hỏng theo cách
 * rất khó hiểu.
 */
class DiscoveryLoopbackTest {

    /** Chờ scanner bind xong để biết cổng thật trước khi phát beacon. */
    private suspend fun RoomScanner.awaitBoundPort(): Int {
        withTimeout(BIND_TIMEOUT) {
            while (boundPort <= 0) delay(20)
        }
        return boundPort
    }

    private fun CoroutineScope.advertise(port: Int, beacon: () -> RoomBeacon): Job = launch(
        Dispatchers.IO,
    ) {
        RoomAdvertiser(
            beacon = beacon,
            targets = listOf(InetAddress.getLoopbackAddress()),
            port = port,
        ).run()
    }

    private suspend fun RoomScanner.awaitFirstRoom(): DiscoveredRoom = withTimeout(FIND_TIMEOUT) {
        var found: DiscoveredRoom? = null
        while (found == null) {
            found = rooms.value.firstOrNull()
            if (found == null) delay(50)
        }
        found
    }

    @Test
    fun `scanner sees an advertised room within three seconds`() = runBlocking {
        val scanner = RoomScanner(port = 0)
        val scannerJob = launch(Dispatchers.IO) { scanner.run() }
        val advertiserJob = advertise(scanner.awaitBoundPort()) {
            RoomBeacon(roomId = "room-1", hostName = "laptop", gamePort = 45_123)
        }

        try {
            // FIND_TIMEOUT = 3 giây chính là tiêu chí "hai thiết bị thấy nhau" của Phase 4.
            val room = scanner.awaitFirstRoom()
            assertEquals("laptop", room.hostName)
            assertEquals(45_123, room.gamePort)
            assertTrue(room.compatible)
            assertTrue(room.joinable)
        } finally {
            advertiserJob.cancelAndJoin()
            scannerJob.cancelAndJoin()
        }
    }

    @Test
    fun `a room that stops broadcasting disappears from the list`() = runBlocking {
        var clock = 0L
        val scanner = RoomScanner(port = 0, now = { clock })
        val scannerJob = launch(Dispatchers.IO) { scanner.run() }
        val advertiserJob = advertise(scanner.awaitBoundPort()) {
            RoomBeacon(roomId = "room-2", hostName = "phone", gamePort = 45_124)
        }

        try {
            scanner.awaitFirstRoom()

            // Host biến mất không báo trước (hết pin, ra khỏi vùng phủ sóng).
            advertiserJob.cancelAndJoin()
            clock += LanTiming.ROOM_STALE_MILLIS + 1

            withTimeout(FIND_TIMEOUT) {
                while (scanner.rooms.value.isNotEmpty()) delay(50)
            }
        } finally {
            advertiserJob.cancelAndJoin()
            scannerJob.cancelAndJoin()
        }
    }

    @Test
    fun `a room with another protocol version is listed but not joinable`() = runBlocking {
        val scanner = RoomScanner(port = 0)
        val scannerJob = launch(Dispatchers.IO) { scanner.run() }
        val advertiserJob = advertise(scanner.awaitBoundPort()) {
            RoomBeacon(
                protocolVersion = PROTOCOL_VERSION + 1,
                roomId = "room-3",
                hostName = "future",
                gamePort = 45_125,
            )
        }

        try {
            val room = scanner.awaitFirstRoom()
            // Vẫn hiện trong danh sách để UI giải thích được vì sao không vào được,
            // thay vì ẩn đi rồi để người dùng tự đoán.
            assertFalse(room.compatible)
            assertFalse(room.joinable)
        } finally {
            advertiserJob.cancelAndJoin()
            scannerJob.cancelAndJoin()
        }
    }

    private companion object {
        const val BIND_TIMEOUT = 3_000L
        const val FIND_TIMEOUT = 3_000L
    }
}
