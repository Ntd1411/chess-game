package kma.game.chess2d.net

import kma.game.chess2d.engine.Engine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProtocolTest {

    private fun sampleSync() = StateSync(
        startFen = Engine.START_FEN,
        moves = listOf(1, 2, 3),
        guestPlaysWhite = true,
        gameId = 7,
    )

    @Test
    fun `every message survives a round trip`() {
        val messages = listOf<NetMessage>(
            NetMessage.Hello(name = "phone"),
            NetMessage.Hello(name = "phone", resumeToken = "tok"),
            NetMessage.Welcome(hostName = "laptop", resumeToken = "tok", sync = sampleSync()),
            NetMessage.MoveMade(gameId = 1, ply = 0, raw = 1234, uci = "e2e4"),
            NetMessage.MoveAck(gameId = 1, ply = 0),
            NetMessage.MoveRejected(gameId = 1, reason = "not your turn", sync = sampleSync()),
            NetMessage.Resign(gameId = 1),
            NetMessage.DrawOffer(gameId = 1),
            NetMessage.DrawResponse(gameId = 1, accepted = true),
            NetMessage.RematchOffer(gameId = 1),
            NetMessage.RematchResponse(accepted = true, sync = sampleSync()),
            NetMessage.RematchResponse(accepted = false),
            NetMessage.SyncRequest("ply mismatch"),
            NetMessage.Sync(sampleSync()),
            NetMessage.Ping(nanos = 42L),
            NetMessage.Pong(nanos = 42L),
            NetMessage.Bye("user quit"),
            NetMessage.Error(LanErrorCode.ROOM_BUSY, "room already has a game"),
        )

        for (message in messages) {
            val encoded = ProtocolJson.encode(message)
            // Khung tin là JSON một dòng: có ký tự xuống dòng là vỡ toàn bộ tầng đọc.
            assertFalse("encoded message must stay on one line: $encoded", encoded.contains('\n'))
            assertEquals(message, ProtocolJson.decode(encoded))
        }
    }

    @Test
    fun `unknown fields do not break decoding`() {
        // Phiên bản sau thêm trường mới thì bản này vẫn phải đọc được, để lỗi lệch phiên
        // bản được báo bằng PROTOCOL_VERSION chứ không phải bằng một exception giải mã.
        val decoded = ProtocolJson.decode("""{"type":"ping","nanos":7,"futureField":"x"}""")
        assertEquals(NetMessage.Ping(7L), decoded)
    }

    @Test
    fun `beacon round trips and rejects foreign packets`() {
        val beacon = RoomBeacon(roomId = "r1", hostName = "laptop", gamePort = 45_123)
        assertEquals(beacon, ProtocolJson.decodeBeacon(ProtocolJson.encodeBeacon(beacon)))

        // Gói rác và gói của app khác trên cùng cổng đều phải bị bỏ qua.
        assertNull(ProtocolJson.decodeBeacon("not json at all"))
        assertNull(
            ProtocolJson.decodeBeacon(
                """{"magic":"some-other-app","protocolVersion":1,"roomId":"r","hostName":"h","gamePort":1}""",
            ),
        )
    }

    @Test
    fun `version mismatch is visible before anything else happens`() {
        // Bắt tay phải phát hiện lệch phiên bản từ thông điệp đầu tiên.
        val hello = ProtocolJson.decode(
            ProtocolJson.encode(NetMessage.Hello(protocolVersion = 99, name = "future")),
        )
        assertTrue(hello is NetMessage.Hello)
        assertEquals(99, (hello as NetMessage.Hello).protocolVersion)
        assertTrue(hello.protocolVersion != PROTOCOL_VERSION)
    }
}
