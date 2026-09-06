package kma.game.chess2d.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Client dòng lệnh cho tầng LAN.
 *
 * Lý do tồn tại: tầng mạng cần thử với người thật nhưng **máy ảo Android không
 * nhận được gói broadcast của máy thật**, nên không thể test emulator đối điện tậy.
 * Vậy nên chạy cái này trên PC rồi đấu với một điện thoại thật — chỉ cần một thiết bị.
 *
 * Cách dùng:
 * ```
 * ./gradlew :net:run --args="host Laptop"
 * ./gradlew :net:run --args="join Laptop 192.168.1.20:45123"
 * ./gradlew :net:run --args="scan"
 * ```
 * (Hoặc chạy trực tiếp `LanCliKt` từ Android Studio.)
 */
fun main(args: Array<String>) = runBlocking {
    val name = args.getOrNull(1) ?: "cli-${randomLanId()}"
    when (args.firstOrNull()) {
        "host" -> hostRoom(name)
        "join" -> joinRoom(name, args.getOrNull(2))
        "scan" -> scanRooms()
        else -> printUsage()
    }
}

private suspend fun hostRoom(name: String) = coroutineScope {
    val host = LanHost(localName = name)
    val server = launch {
        host.run { port -> println("[net] hosting room ${host.roomId} on tcp port $port") }
    }
    watch(host)
    readCommands(host)
    server.cancelAndJoin()
}

/** @param target để trống để tự tìm phòng, hoặc "host:port" khi muốn nối thẳng. */
private suspend fun joinRoom(name: String, target: String?) = coroutineScope {
    val (host, port) = when {
        target != null -> parseTarget(target) ?: run {
            println("[error] bad target '$target', expected host:port")
            return@coroutineScope
        }

        else -> {
            val room = findFirstRoom() ?: run {
                println("[error] no room found on this network")
                return@coroutineScope
            }
            println("[net] joining ${room.hostName} at ${room.host}:${room.gamePort}")
            room.host to room.gamePort
        }
    }

    val guest = LanGuest(localName = name)
    val client = launch { guest.run(host, port) }
    watch(guest)
    readCommands(guest)
    client.cancelAndJoin()
}

private suspend fun scanRooms() = coroutineScope {
    val scanner = RoomScanner()
    val job = launch { scanner.run() }
    println("[net] listening for rooms on udp $DISCOVERY_PORT for ${SCAN_SECONDS}s...")
    repeat(SCAN_SECONDS) {
        delay(1_000)
        for (room in scanner.rooms.value) {
            val note = when {
                !room.compatible -> "protocol v${room.protocolVersion}, incompatible"
                room.busy -> "busy"
                else -> "open"
            }
            println("  ${room.hostName} @ ${room.host}:${room.gamePort} ($note)")
        }
    }
    job.cancelAndJoin()
}

private suspend fun findFirstRoom(): DiscoveredRoom? = coroutineScope {
    val scanner = RoomScanner()
    val job = launch { scanner.run() }
    val room = withTimeoutOrNull(DISCOVERY_WAIT_MILLIS) {
        var found: DiscoveredRoom? = null
        while (found == null) {
            found = scanner.rooms.value.firstOrNull { it.joinable }
            if (found == null) delay(200)
        }
        found
    }
    job.cancelAndJoin()
    room
}

/** In mọi thay đổi ra stdout. Đủ để theo ván đấu mà không cần vẽ bàn cờ. */
private fun CoroutineScope.watch(endpoint: LanEndpoint) {
    launch { endpoint.events.collect { event -> println("[event] $event") } }
    launch {
        var lastPly = -1
        var lastConnected = false
        endpoint.state.collect { state ->
            if (state.connected != lastConnected) {
                lastConnected = state.connected
                println("[net] connected=${state.connected} opponent=${state.opponentName}")
            }
            if (state.ply != lastPly) {
                lastPly = state.ply
                println("[board] ${endpoint.fen()}")
                println(
                    "[turn] ply=${state.ply} " +
                        (if (state.yourTurn) "your move" else "waiting for opponent") +
                        " status=${state.status}",
                )
            }
        }
    }
}

/** Đọc lệnh từ bàn phím. Chạy trên Dispatchers.IO vì readln() là lời gọi chặn luồng. */
private suspend fun readCommands(endpoint: LanEndpoint) = withContext(Dispatchers.IO) {
    printHelp()
    while (true) {
        val line = readlnOrNull()?.trim() ?: break
        if (line.isEmpty()) continue
        when (line.lowercase()) {
            "quit", "exit" -> {
                endpoint.leave("user quit")
                return@withContext
            }

            "help" -> printHelp()
            "resign" -> endpoint.resign()
            "draw" -> endpoint.offerDraw()
            "rematch" -> endpoint.offerRematch()
            "sync" -> endpoint.requestSync("cli")
            "moves" -> println(endpoint.legalMoves().joinToString(" ") { it.toUci() })
            "fen" -> println(endpoint.fen())

            // Một lệnh "yes"/"no" cho cả hai loại đề nghị: chỉ có tối đa một đề nghị
            // đang treo tại một thời điểm (hòa khi đang chơi, đấu lại khi đã xong).
            "yes" -> respond(endpoint, accepted = true)
            "no" -> respond(endpoint, accepted = false)

            else -> if (!endpoint.submitUci(line)) println("[warn] move rejected locally: $line")
        }
    }
}

private fun respond(endpoint: LanEndpoint, accepted: Boolean) {
    val state = endpoint.state.value
    when {
        state.opponentOffersRematch -> endpoint.respondRematch(accepted)
        state.opponentOffersDraw -> endpoint.respondDraw(accepted)
        else -> println("[warn] nothing to answer")
    }
}

private fun parseTarget(target: String): Pair<String, Int>? {
    val host = target.substringBeforeLast(':', missingDelimiterValue = "")
    val port = target.substringAfterLast(':', missingDelimiterValue = "").toIntOrNull()
    if (host.isEmpty() || port == null) return null
    return host to port
}

private fun printHelp() {
    println(
        """
        [cli] commands:
          e2e4        make a move in UCI notation (e7e8q to promote)
          moves       list legal moves
          fen         print current position
          draw        offer a draw
          resign      resign the game
          rematch     offer a rematch after the game ends
          yes / no    answer the pending offer
          sync        ask the host to resend the game state
          quit        leave the room
        """.trimIndent(),
    )
}

private fun printUsage() {
    println(
        """
        usage:
          host <name>                  open a room and wait for an opponent
          join <name> [host:port]      join a room (auto-discovered when omitted)
          scan                         list rooms on this network
        """.trimIndent(),
    )
}

private const val SCAN_SECONDS = 5
private const val DISCOVERY_WAIT_MILLIS = 5_000L
