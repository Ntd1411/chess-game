package kma.game.chess2d.spectate

import kma.game.chess2d.ai.Ai
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.CaptureTally
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.San
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.PieceOnBoard
import kotlin.random.Random

/**
 * Một ván Máy vs Máy: hai AI độc lập lần lượt đi cho tới khi hết ván.
 *
 * Là lớp thuần (không biết Android, không biết coroutine) để test JVM cho hai AI đấu
 * nhau hàng chục ván bằng đúng đoạn mã mà màn hình dùng. Việc chạy ngoài main thread,
 * tạm dừng và chỉnh tốc độ thuộc về `SpectateViewModel`.
 *
 * **Không thread-safe.** Mỗi lượt [step] đi và hoàn nguyên bàn cờ hàng triệu lần bên
 * trong AI, nên một thể hiện chỉ được gọi từ một luồng tại một thời điểm, và giao diện
 * chỉ được đọc [snapshot] khi không có [step] nào đang chạy.
 *
 * @param seed hạt giống ngẫu nhiên của hai AI (chỉ cấp Dễ có ngẫu nhiên); cùng hạt
 *        giống và cùng cấp độ thì ra đúng cùng một ván.
 * @param maxPlies trần số nửa nước. Hai AI yếu có thể đẩy quân qua lại rất lâu mà
 *        không ai thắng; chạm trần thì ván dừng, coi như hòa, để chế độ tự động không
 *        bao giờ chạy vô hạn.
 */
class SpectateMatch(
    val whiteLevel: Difficulty,
    val blackLevel: Difficulty,
    seed: Int = Random.nextInt(),
    val maxPlies: Int = DEFAULT_MAX_PLIES,
) {

    private val board = Engine.newGame()
    private val whiteAi = Ai(Random(seed))
    private val blackAi = Ai(Random(seed + 1))
    private val moves = mutableListOf<Move>()
    private val sanMoves = mutableListOf<String>()

    /** id của quân đang đứng ở từng ô; để Compose animate được quân đi chứ không nhấp nháy. */
    private val squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }

    init {
        var nextId = 0
        for (square in 0 until Squares.COUNT) {
            if (board.pieceAt(square) != Piece.NONE) squareIds[square] = nextId++
        }
        whiteAi.newGame()
        blackAi.newGame()
    }

    /** Số nửa nước đã đi. */
    val ply: Int get() = moves.size

    /** Các nước đã đi, theo thứ tự. */
    val playedMoves: List<Move> get() = moves.toList()

    /** Ván đã hết theo luật cờ (chiếu hết hoặc hòa). */
    val endedByRules: Boolean get() = Rules.isGameOver(board)

    /** Ván dừng vì chạm [maxPlies] chứ không phải vì luật cờ. */
    val hitMoveLimit: Boolean get() = !endedByRules && moves.size >= maxPlies

    val isOver: Boolean get() = endedByRules || hitMoveLimit

    /**
     * Để bên đang đến lượt nghĩ và đi một nước.
     *
     * Chặn luồng gọi cho tới khi AI nghĩ xong (tới 5 giây ở cấp Khó), nên phải gọi
     * ngoài main thread.
     *
     * @return nước vừa đi, hoặc `null` nếu ván đã hết hoặc lượt nghĩ bị hủy giữa chừng
     *         ([isActive] trả về false). Nước nghĩ dở bị hủy không bao giờ được áp dụng.
     * @throws IllegalStateException nếu AI trả về nước phi luật hoặc không chọn được nước
     *         nào trong khi ván còn tiếp tục — đó là lỗi của AI, phải lộ ra chứ không nuốt.
     */
    fun step(isActive: () -> Boolean = { true }): Move? {
        if (isOver) return null

        val whiteMoves = board.whiteToMove
        val move = (if (whiteMoves) whiteAi else blackAi).chooseMove(
            board = board,
            difficulty = if (whiteMoves) whiteLevel else blackLevel,
            isActive = isActive,
        )
        if (!isActive()) return null

        val fen = board.toFen()
        check(move != Move.NONE) { "AI không chọn được nước nào ở thế $fen" }
        val legal = Engine.legalMoves(board)
        check(legal.any { it.raw == move.raw }) { "AI đi nước phi luật ${move.toUci()} ở thế $fen" }

        val san = San.of(board, move, legal)
        val movingWhite = board.whiteToMove
        board.makeMove(move)

        // Bản đồ id phải phản chiếu đúng những gì makeMove vừa làm trên bàn cờ.
        if (move.isEnPassant) {
            squareIds[if (movingWhite) move.to - 8 else move.to + 8] = NO_PIECE_ID
        }
        squareIds[move.to] = squareIds[move.from]
        squareIds[move.from] = NO_PIECE_ID
        if (move.isCastle) {
            val rookFrom = if (move.flag == Move.CASTLE_KING) move.to + 1 else move.to - 2
            val rookTo = if (move.flag == Move.CASTLE_KING) move.to - 1 else move.to + 1
            squareIds[rookTo] = squareIds[rookFrom]
            squareIds[rookFrom] = NO_PIECE_ID
        }

        moves.add(move)
        sanMoves.add(san)
        return move
    }

    /**
     * Ảnh chụp bất biến để vẽ. Dùng lại [GameUiState] để các thành phần vẽ sẵn có
     * (bàn cờ, danh sách nước, hàng quân bị bắt) dùng được nguyên.
     *
     * @param thinking AI đang nghĩ; chỉ để hiện "đang tính…".
     */
    fun snapshot(thinking: Boolean = false): GameUiState {
        val last = moves.lastOrNull()
        val tally = CaptureTally.of(startFen = Engine.START_FEN, moves = moves, plies = moves.size)
        return GameUiState(
            pieces = (0 until Squares.COUNT).mapNotNull { square ->
                val piece = board.pieceAt(square)
                if (piece == Piece.NONE) null else PieceOnBoard(squareIds[square], piece, square)
            },
            whiteToMove = board.whiteToMove,
            status = Rules.status(board),
            lastMoveFrom = last?.from ?: Squares.NONE,
            lastMoveTo = last?.to ?: Squares.NONE,
            checkedKingSquare = if (board.isInCheck()) {
                board.kingSquare(board.whiteToMove)
            } else {
                Squares.NONE
            },
            aiThinking = thinking,
            sanMoves = sanMoves.toList(),
            takenFromWhite = tally.takenFromWhite,
            takenFromBlack = tally.takenFromBlack,
            materialBalance = tally.materialBalance,
        )
    }

    companion object {
        /** 300 nửa nước = 150 nước mỗi bên, dài hơn hầu hết ván thật. */
        const val DEFAULT_MAX_PLIES = 300

        private const val NO_PIECE_ID = -1
    }
}
