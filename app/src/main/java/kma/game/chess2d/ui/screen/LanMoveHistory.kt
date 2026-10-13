package kma.game.chess2d.ui.screen

import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Fen
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.San

/** Build display notation on a separate board without touching the live LAN position. */
internal fun lanSanMoves(startFen: String, moves: List<Int>): List<String> {
    if (moves.isEmpty()) return emptyList()
    val board = Fen.parse(startFen)
    return moves.map { raw ->
        val move = Move(raw)
        val legal = Engine.legalMoves(board)
        require(move in legal) { "Invalid LAN move in history: ${move.toUci()}" }
        val san = San.of(board, move, legal)
        board.makeMove(move)
        san
    }
}
