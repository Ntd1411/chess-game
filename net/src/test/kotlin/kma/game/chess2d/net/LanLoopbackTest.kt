package kma.game.chess2d.net

import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Squares
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test hai đầu LAN thật trên loopback.
 *
 * Dùng socket thật chứ không giả lập kênh truyền, vì phần dễ sai nhất của tầng mạng
 * nằm đúng ở chỗ socket: khung tin theo dòng, hạn chờ, EOF khi đứt kết nối. Một kênh
 * giả lập sẽ che mất đúng những lỗi cần bắt.
 *
 * "Kách giả" (rogue) là một [MessageChannel] trần, không qua [LanGuest]. Nhờ đó gửi
 * được cả những thông điệp mà một client đúng đắn không bao giờ gửi — cách duy
 * nhất để kiểm tra host có thật sự làm trọng tài hay chỉ tin lời đối phương.
 */
class LanLoopbackTest {

    // ------------------------------------------------------------------ tiện ích

    /**
     * Một cổng UDP không ai nghe, để beacon của test không đi vào cổng thật.
     *
     * Đọc cổng từ localSocketAddress chứ không dùng getLocalPort(): trên một số JDK,
     * getLocalPort() của DatagramSocket tạo bằng `DatagramSocket(0)` trả về -1, và
     * một cổng -1 thì bị từ chối ngay khi bind.
     */
    private fun freeUdpPort(): Int =
        DatagramSocket(InetSocketAddress(InetAddress.getLoopbackAddress(), 0)).use { socket ->
            (socket.localSocketAddress as InetSocketAddress).port
        }

    private fun dial(port: Int): MessageChannel {
        val socket = Socket()
        socket.connect(InetSocketAddress(InetAddress.getLoopbackAddress(), port), DIAL_TIMEOUT)
        return MessageChannel(socket)
    }

    /** Bỏ qua ping/pong để test chỉ nhìn các thông điệp thuộc về ván đấu. */
    private fun MessageChannel.nextGameMessage(): NetMessage {
        while (true) {
            val message = receive() ?: error("connection closed while waiting for a message")
            if (message is NetMessage.Ping || message is NetMessage.Pong) continue
            return message
        }
    }

    private fun rawMove(from: String, to: String, flag: Int): Int =
        Move.of(Squares.fromName(from), Squares.fromName(to), flag).raw

    private suspend fun waitUntil(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
        withTimeout(timeoutMillis) {
            while (!condition()) delay(20)
        }
    }

    /**
     * Mở một phòng trên cổng do hệ điều hành chọn, chỉ phát beacon vào loopback trên
     * một cổng UDP rỗi, rồi đóng phòng khi test xong.
     */
    private suspend fun withHost(block: suspend CoroutineScope.(LanHost, Int) -> Unit) =
        coroutineScope {
            val scope = this
            val host = LanHost(
                localName = "host",
                discoveryPort = freeUdpPort(),
                advertiseTargets = listOf(InetAddress.getLoopbackAddress()),
            )
            val ready = CompletableDeferred<Int>()
            val hostJob = launch(Dispatchers.IO) { host.run { port -> ready.complete(port) } }
            val port = withTimeout(5_000) { ready.await() }
            try {
                scope.block(host, port)
            } finally {
                hostJob.cancelAndJoin()
            }
        }

    // ------------------------------------------------------------------ ván đấu bình thường

    @Test
    fun `host and guest play over loopback`() = runBlocking {
        withHost { host, port ->
            val guest = LanGuest("guest")
            val guestJob = launch(Dispatchers.IO) {
                guest.run("127.0.0.1", port, reconnectAttempts = 0)
            }
            try {
                waitUntil { host.state.value.connected && guest.state.value.connected }

                // Host giữ Trắng ván đầu, nên khách phải là Đen.
                assertTrue(host.state.value.youPlayWhite)
                assertFalse(guest.state.value.youPlayWhite)
                assertTrue(host.state.value.yourTurn)
                assertFalse(guest.state.value.yourTurn)
                assertEquals("guest", host.state.value.opponentName)
                assertEquals("host", guest.state.value.opponentName)

                assertTrue(host.submitUci("e2e4"))
                waitUntil { guest.state.value.ply == 1 }
                assertTrue(guest.submitUci("e7e5"))
                waitUntil { host.state.value.ply == 2 }
                assertEquals(host.fen(), guest.fen())

                // Đi khi chưa tới lượt bị chặn ngay tại máy mình, không tốn vòng mạng nào.
                assertFalse(guest.submitUci("g8f6"))

                host.resign()
                waitUntil { guest.state.value.outcome == LanOutcome.RESIGNATION }
                assertEquals(true, guest.state.value.resignedByWhite)
                assertTrue(guest.state.value.finished)
            } finally {
                guest.leave("test over")
                guestJob.cancelAndJoin()
            }
        }
    }

    @Test
    fun `a draw offer needs both sides to agree`() = runBlocking {
        withHost { host, port ->
            val guest = LanGuest("guest")
            val guestJob = launch(Dispatchers.IO) {
                guest.run("127.0.0.1", port, reconnectAttempts = 0)
            }
            try {
                waitUntil { host.state.value.connected && guest.state.value.connected }

                host.offerDraw()
                waitUntil { guest.state.value.opponentOffersDraw }
                guest.respondDraw(accepted = false)
                waitUntil { !host.state.value.waitingDrawReply }
                assertFalse(host.state.value.finished)

                guest.offerDraw()
                waitUntil { host.state.value.opponentOffersDraw }
                host.respondDraw(accepted = true)
                waitUntil { guest.state.value.outcome == LanOutcome.DRAW_AGREED }
                assertEquals(LanOutcome.DRAW_AGREED, host.state.value.outcome)
            } finally {
                guest.leave("test over")
                guestJob.cancelAndJoin()
            }
        }
    }

    @Test
    fun `a rematch starts a new game with the colours swapped`() = runBlocking {
        withHost { host, port ->
            val guest = LanGuest("guest")
            val guestJob = launch(Dispatchers.IO) {
                guest.run("127.0.0.1", port, reconnectAttempts = 0)
            }
            try {
                waitUntil { host.state.value.connected && guest.state.value.connected }
                assertTrue(host.submitUci("e2e4"))
                waitUntil { guest.state.value.ply == 1 }

                guest.resign()
                waitUntil { host.state.value.finished }

                guest.offerRematch()
                waitUntil { host.state.value.opponentOffersRematch }
                host.respondRematch(accepted = true)

                // Ván mới: bàn cờ sạch, kết quả cũ được xóa, và hai bên đổi màu.
                waitUntil { guest.state.value.ply == 0 && guest.state.value.youPlayWhite }
                assertFalse(host.state.value.youPlayWhite)
                assertTrue(guest.state.value.outcome == null)
                assertTrue(guest.state.value.yourTurn)
                assertTrue(guest.submitUci("d2d4"))
                waitUntil { host.state.value.ply == 1 }
            } finally {
                guest.leave("test over")
                guestJob.cancelAndJoin()
            }
        }
    }

    // ------------------------------------------------------------------ host làm trọng tài

    @Test
    fun `referee rejects moves a rogue client should not be able to make`() = runBlocking {
        withHost { host, port ->
            val rogue = dial(port)
            try {
                rogue.send(NetMessage.Hello(name = "rogue"))
                val welcome = rogue.nextGameMessage() as NetMessage.Welcome
                assertFalse(welcome.sync.guestPlaysWhite)
                waitUntil { host.state.value.connected }

                // Chưa tới lượt: khách là Đen mà nước đầu tiên là của Trắng.
                rogue.send(
                    NetMessage.MoveMade(
                        gameId = welcome.sync.gameId,
                        ply = 0,
                        raw = rawMove("e7", "e5", Move.DOUBLE_PAWN_PUSH),
                        uci = "e7e5",
                    ),
                )
                val rejected = rogue.nextGameMessage()
                assertTrue("expected a rejection, got $rejected", rejected is NetMessage.MoveRejected)
                // Kèm trạng thái đúng để khách tự dụng lại bàn cờ.
                assertTrue((rejected as NetMessage.MoveRejected).sync.moves.isEmpty())
                assertEquals(0, host.state.value.ply)

                assertTrue(host.submitUci("e2e4"))
                assertTrue(rogue.nextGameMessage() is NetMessage.MoveMade)

                // Đúng lượt nhưng sai luật: tốt đen không đi được ba ô.
                rogue.send(
                    NetMessage.MoveMade(
                        gameId = welcome.sync.gameId,
                        ply = 1,
                        raw = rawMove("a7", "a4", Move.QUIET),
                        uci = "a7a4",
                    ),
                )
                assertTrue(rogue.nextGameMessage() is NetMessage.MoveRejected)
                assertEquals(1, host.state.value.ply)

                // Xin đồng bộ thì trọng tài gửi lại toàn bộ ván.
                rogue.send(NetMessage.SyncRequest("test"))
                val sync = rogue.nextGameMessage()
                assertTrue(sync is NetMessage.Sync)
                assertEquals(1, (sync as NetMessage.Sync).state.moves.size)
            } finally {
                rogue.close()
            }
        }
    }

    @Test
    fun `host refuses a client speaking another protocol version`() = runBlocking {
        withHost { _, port ->
            val rogue = dial(port)
            try {
                rogue.send(
                    NetMessage.Hello(protocolVersion = PROTOCOL_VERSION + 1, name = "future"),
                )
                val reply = rogue.nextGameMessage()
                assertTrue("expected an error, got $reply", reply is NetMessage.Error)
                assertEquals(LanErrorCode.VERSION_MISMATCH, (reply as NetMessage.Error).code)
            } finally {
                rogue.close()
            }
        }
    }

    @Test
    fun `host refuses a third player while a game is running`() = runBlocking {
        withHost { host, port ->
            val guest = LanGuest("guest")
            val guestJob = launch(Dispatchers.IO) {
                guest.run("127.0.0.1", port, reconnectAttempts = 0)
            }
            try {
                waitUntil { host.state.value.connected }

                val third = dial(port)
                try {
                    third.send(NetMessage.Hello(name = "third"))
                    val reply = third.nextGameMessage()
                    assertTrue("expected an error, got $reply", reply is NetMessage.Error)
                    assertEquals(LanErrorCode.ROOM_BUSY, (reply as NetMessage.Error).code)
                } finally {
                    third.close()
                }

                // Người thứ ba bị từ chối không được làm ảnh hưởng tới ván đang chơi.
                assertTrue(host.state.value.connected)
                assertTrue(host.submitUci("e2e4"))
                waitUntil { guest.state.value.ply == 1 }
            } finally {
                guest.leave("test over")
                guestJob.cancelAndJoin()
            }
        }
    }

    // ------------------------------------------------------------------ nối lại

    @Test
    fun `a guest can rejoin the same game with its resume token`() = runBlocking {
        withHost { host, port ->
            val first = dial(port)
            first.send(NetMessage.Hello(name = "phone"))
            val welcome = first.nextGameMessage() as NetMessage.Welcome
            waitUntil { host.state.value.connected }

            assertTrue(host.submitUci("d2d4"))
            waitUntil { host.state.value.ply == 1 }

            // Rút dây: đúng như tắt Wi-Fi, không có Bye nào được gửi.
            first.close()
            waitUntil { !host.state.value.connected }
            // Ván đấu phải còn nguyên, đây là điều kiện để nối lại có nghĩa.
            assertEquals(1, host.state.value.ply)

            val second = dial(port)
            try {
                second.send(
                    NetMessage.Hello(name = "phone", resumeToken = welcome.resumeToken),
                )
                val resumed = second.nextGameMessage()
                assertTrue("expected a welcome, got $resumed", resumed is NetMessage.Welcome)
                resumed as NetMessage.Welcome
                assertEquals(welcome.sync.gameId, resumed.sync.gameId)
                assertEquals(1, resumed.sync.moves.size)
                waitUntil { host.state.value.connected }
            } finally {
                second.close()
            }
        }
    }

    @Test
    fun `a new guest cannot take over a game in progress`() = runBlocking {
        withHost { host, port ->
            val first = dial(port)
            first.send(NetMessage.Hello(name = "phone"))
            first.nextGameMessage() as NetMessage.Welcome
            waitUntil { host.state.value.connected }
            assertTrue(host.submitUci("d2d4"))
            waitUntil { host.state.value.ply == 1 }
            first.close()
            waitUntil { !host.state.value.connected }

            // Không có vé nối lại thì không được vào giữa ván của người khác.
            val stranger = dial(port)
            try {
                stranger.send(NetMessage.Hello(name = "stranger"))
                val reply = stranger.nextGameMessage()
                assertTrue("expected an error, got $reply", reply is NetMessage.Error)
                assertEquals(LanErrorCode.ROOM_BUSY, (reply as NetMessage.Error).code)
                assertEquals(1, host.state.value.ply)
            } finally {
                stranger.close()
            }
        }
    }

    private companion object {
        const val DIAL_TIMEOUT = 2_000
    }
}
