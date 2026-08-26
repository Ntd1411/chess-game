package kma.game.chess2d.net

import kotlinx.serialization.Serializable

/** Placeholder cua Phase 0. Phase 4 se hien thuc discovery + transport. */
const val PROTOCOL_VERSION: Int = 1

@Serializable
sealed interface NetMessage {
    @Serializable
    data class Hello(val protocolVersion: Int = PROTOCOL_VERSION, val name: String) : NetMessage
}
