package kma.game.chess2d.campaign

import kma.game.chess2d.ai.Ai
import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.CaptureTally
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.GameStatus
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.engine.San
import kma.game.chess2d.engine.Squares
import kma.game.chess2d.game.GameMode
import kma.game.chess2d.game.GameUiState
import kma.game.chess2d.game.MoveSound
import kma.game.chess2d.game.PendingPromotion
import kma.game.chess2d.game.PieceOnBoard
import kma.game.chess2d.game.SoundCue
import kma.game.chess2d.puzzle.Puzzle
import kma.game.chess2d.puzzle.PuzzleCatalog
import kotlin.random.Random

/** Kết quả một ván ở một tầng. */
enum class FloorResult { ONGOING, WON, LOST }

/** Vì sao thua. */
enum class LossReason {
    /** Vua của người chơi bị chiếu hết. */
    CHECKMATE,

    /** Ván hòa trong khi tầng đòi phải thắng. */
    DRAW,
}

/**
 * Mọi thứ màn đấu của chiến dịch cần để vẽ một khung hình.
 *
 * @param floorNumber tầng của ván này, để phía nhận biết trạng thái cũ của ván trước.
 * @param playerMoves số nước người chơi đã đi.
 * @param mistakes số lần đi sai ở tầng giải thế cờ (nước sai không được đi).
 */
data class CampaignUiState(
    val game: GameUiState = GameUiState(),
    val result: FloorResult = FloorResult.ONGOING,
    val lossReason: LossReason? = null,
    val playerMoves: Int = 0,
    val mistakes: Int = 0,
    val floorNumber: Int = 0,
)

/**
 * Một ván ở một tầng của Tháp Cờ: người chơi luôn cầm Trắng, máy cầm Đen.
 *
 * Là lớp thuần (không biết Android, không biết coroutine) để test JVM chạy đúng đoạn mã
 * màn hình dùng. Luật thắng/thua theo [Floor.goal]:
 * - [FloorGoal.DefeatAi]: chiếu hết máy là thắng; bị chiếu hết hoặc hòa là thua.
 * - [FloorGoal.SolvePuzzle]: đi đúng chuỗi nước giải là thắng. Nước sai **không được đi**
 *   và chỉ tính vào [mistakes]: câu đố cho thử lại ngay chứ không bắt thua.
 * - [FloorGoal.SurviveMoves]: đi đủ N nước mà không bị chiếu hết là thắng. Hòa trước đó
 *   (hết nước đi, hết quân...) cũng tính là sống sót vì mục tiêu chỉ là không bị chiếu hết.
 *
 * **Không thread-safe.** Phần nghĩ của máy ([chooseAiMove]) chạy ngoài main thread và chỉ
 * *đọc* ván; nước tìm được phải đem về [applyAiMove] trên luồng chính. Trong lúc máy nghĩ
 * phía gọi không được đụng vào ván (ViewModel đã chặn chạm).
 *
 * @param seed hạt giống ngẫu nhiên của máy (chỉ cấp Dễ có ngẫu nhiên).
 */
class CampaignMatch(
    val floor: Floor,
    puzzles: List<Puzzle>,
    seed: Int = Random.nextInt(),
) {

    private val goal = floor.goal
    private val puzzle: Puzzle? = (goal as? FloorGoal.SolvePuzzle)?.let { puzzles[it.puzzleIndex] }

    /** Các nước giải, đã được engine duyệt. Rỗng nếu tầng này không phải câu đố. */
    private val solution: List<Move> = puzzle?.let { PuzzleCatalog.validate(it) } ?: emptyList()

    private val startFen: String = floor.startFen ?: puzzle?.startFen ?: Engine.START_FEN
    private val board = Engine.fromFen(startFen)

    private val difficulty: Difficulty = when (goal) {
        is FloorGoal.DefeatAi -> goal.difficulty
        is FloorGoal.SurviveMoves -> goal.difficulty
        else -> Difficulty.EASY
    }

    private val ai = Ai(Random(seed))
    private val moves = mutableListOf<Move>()
    private val sanMoves = mutableListOf<String>()

    /** id của quân ở từng ô, để Compose animate được quân đi chứ không nhấp nháy. */
    private val squareIds = IntArray(Squares.COUNT) { NO_PIECE_ID }

    private var legalMoves: List<Move> = emptyList()
    private var selectedSquare = Squares.NONE
    private var pendingPromotion: PendingPromotion? = null
    private var puzzleStep = 0
    private var soundSerial = 0
    private var soundCue: SoundCue? = null

    var result: FloorResult = FloorResult.ONGOING
        private set
    var lossReason: LossReason? = null
        private set
    var playerMoves: Int = 0
        private set
    var mistakes: Int = 0
        private set

    init {
        require(goal !is FloorGoal.Claim) { "tầng ${floor.number} không có ván cờ" }
        require(board.whiteToMove) { "tầng ${floor.number}: người chơi cầm Trắng nên thế đầu phải tới lượt Trắng" }
        var nextId = 0
        for (square in 0 until Squares.COUNT) {
            if (board.pieceAt(square) != Piece.NONE) squareIds[square] = nextId++
        }
        legalMoves = Engine.legalMoves(board)
        ai.newGame()
    }

    /** Đến lượt máy và ván còn tiếp tục. */
    val needsAiReply: Boolean get() = result == FloorResult.ONGOING && !board.whiteToMove

    /**
     * Người chơi chạm vào một ô.
     *
     * @return `true` nếu vừa đi một nước (phía gọi nên cho máy đáp lại), `false` nếu chỉ
     *         chọn/bỏ chọn quân, mở hộp phong cấp hoặc không có gì xảy ra.
     */
    fun onSquareTap(square: Int): Boolean {
        if (result != FloorResult.ONGOING || !board.whiteToMove || pendingPromotion != null) return false

        if (selectedSquare == Squares.NONE) {
            selectIfOwnPiece(square)
            return false
        }
        if (square == selectedSquare) {
            selectedSquare = Squares.NONE
            return false
        }

        val candidates = legalMoves.filter { it.from == selectedSquare && it.to == square }
        return when {
            // Không đi được tới đó: coi như người chơi định chọn quân khác.
            candidates.isEmpty() -> {
                selectIfOwnPiece(square)
                false
            }

            candidates.size == 1 -> {
                selectedSquare = Squares.NONE
                play(candidates.first())
            }

            // Nhiều nước cùng ô đi và ô đến thì chỉ có thể là phong cấp.
            else -> {
                pendingPromotion = PendingPromotion(selectedSquare, square, true, candidates)
                false
            }
        }
    }

    fun onPromotionChosen(move: Move): Boolean {
        pendingPromotion = null
        selectedSquare = Squares.NONE
        return play(move)
    }

    fun onPromotionDismissed() {
        pendingPromotion = null
    }

    /**
     * Người chơi đi [move].
     *
     * @return `true` nếu nước đi được thực hiện. `false` nếu ván đã hết, không tới lượt
     *         Trắng, nước phi luật, hoặc (ở câu đố) nước không phải nước giải.
     */
    fun play(move: Move): Boolean {
        if (result != FloorResult.ONGOING || !board.whiteToMove) return false
        if (legalMoves.none { it.raw == move.raw }) return false

        if (puzzle != null && move.raw != solution[puzzleStep].raw) {
            mistakes++
            selectedSquare = Squares.NONE
            return false
        }

        apply(move)
        playerMoves++

        if (puzzle != null) {
            puzzleStep++
            // Máy đáp lại theo đúng kịch bản của câu đố, không nghĩ.
            while (puzzleStep < solution.size && !board.whiteToMove) {
                apply(solution[puzzleStep])
                puzzleStep++
            }
            if (puzzleStep >= solution.size) result = FloorResult.WON
        } else {
            evaluate()
        }
        return true
    }

    /**
     * Để máy chọn nước. Chặn luồng gọi (cấp Khó tới 5 giây) nên phải gọi ngoài main thread.
     *
     * Máy nghĩ trên **bản sao** dựng bằng cách đi lại cả ván, nên search hàng triệu
     * nước đi/hoàn nguyên không chạm vào bàn cờ giao diện đang đọc, và máy vẫn thấy lịch sử
     * lặp thế để tính luật hòa ba lần lặp. Không đổi trạng thái ván.
     *
     * @return nước đã chọn, hoặc `null` nếu chưa đến lượt máy hoặc lượt nghĩ bị hủy
     *         ([isActive] trả về false).
     */
    fun chooseAiMove(isActive: () -> Boolean = { true }): Move? {
        if (!needsAiReply) return null
        val copy = Engine.fromFen(startFen)
        for (move in moves) copy.makeMove(move)

        val move = ai.chooseMove(board = copy, difficulty = difficulty, isActive = isActive)
        return if (isActive()) move else null
    }

    /**
     * Áp nước máy vừa chọn.
     *
     * @throws IllegalStateException nếu máy trả về nước phi luật hoặc không chọn được nước
     *         nào trong khi ván còn tiếp tục: đó là lỗi của AI, phải lộ ra chứ không nuốt.
     */
    fun applyAiMove(move: Move) {
        if (!needsAiReply) return
        val fen = board.toFen()
        check(move != Move.NONE) { "AI không chọn được nước nào ở thế $fen" }
        check(legalMoves.any { it.raw == move.raw }) { "AI đi nước phi luật ${move.toUci()} ở thế $fen" }
        apply(move)
        evaluate()
    }

    /** Nghĩ rồi đi luôn trên cùng luồng (tiện cho test). */
    fun aiStep(isActive: () -> Boolean = { true }): Move? =
        chooseAiMove(isActive)?.also { applyAiMove(it) }

    /** Ảnh chụp bất biến để vẽ; dùng lại [GameUiState] nên bàn cờ, danh sách nước đi dùng được nguyên. */
    fun snapshot(thinking: Boolean = false): CampaignUiState {
        val last = moves.lastOrNull()
        val tally = CaptureTally.of(startFen = startFen, moves = moves, plies = moves.size)
        val game = GameUiState(
            pieces = (0 until Squares.COUNT).mapNotNull { square ->
                val piece = board.pieceAt(square)
                if (piece == Piece.NONE) null else PieceOnBoard(squareIds[square], piece, square)
            },
            whiteToMove = board.whiteToMove,
            status = Rules.status(board),
            selectedSquare = selectedSquare,
            legalTargets = targetsFrom(selectedSquare),
            lastMoveFrom = last?.from ?: Squares.NONE,
            lastMoveTo = last?.to ?: Squares.NONE,
            checkedKingSquare = if (board.isInCheck()) board.kingSquare(board.whiteToMove) else Squares.NONE,
            pendingPromotion = pendingPromotion,
            mode = GameMode.VS_COMPUTER,
            difficulty = difficulty,
            aiThinking = thinking,
            soundCue = soundCue,
            sanMoves = sanMoves.toList(),
            takenFromWhite = tally.takenFromWhite,
            takenFromBlack = tally.takenFromBlack,
            materialBalance = tally.materialBalance,
        )
        return CampaignUiState(
            game = game,
            result = result,
            lossReason = lossReason,
            playerMoves = playerMoves,
            mistakes = mistakes,
            floorNumber = floor.number,
        )
    }

    /** Thắng/thua sau mỗi nước (trừ câu đố, có luật riêng ở [play]). */
    private fun evaluate() {
        if (result != FloorResult.ONGOING) return
        when (Rules.status(board)) {
            GameStatus.CHECKMATE ->
                // Bên đến lượt là bên bị chiếu hết.
                if (board.whiteToMove) finish(FloorResult.LOST, LossReason.CHECKMATE) else finish(FloorResult.WON)

            GameStatus.ONGOING, GameStatus.CHECK -> {
                val g = goal
                // Máy đã đáp lại nước thứ N của người chơi mà vẫn chưa bị chiếu hết: sống sót.
                if (g is FloorGoal.SurviveMoves && playerMoves >= g.moves && board.whiteToMove) {
                    finish(FloorResult.WON)
                }
            }

            // Các kiểu hòa: tầng sống sót coi là qua, tầng đòi thắng thì thua.
            else ->
                if (goal is FloorGoal.SurviveMoves) finish(FloorResult.WON) else finish(FloorResult.LOST, LossReason.DRAW)
        }
    }

    private fun finish(newResult: FloorResult, reason: LossReason? = null) {
        result = newResult
        lossReason = reason
    }

    private fun apply(move: Move) {
        // Sinh SAN trước khi đi: sau makeMove thì không còn biết quân nào khác cũng đi tới được ô đó.
        val san = San.of(board, move, legalMoves)
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
        legalMoves = Engine.legalMoves(board)
        selectedSquare = Squares.NONE
        pendingPromotion = null

        val sound = when {
            Rules.isGameOver(board) -> MoveSound.GAME_END
            board.isInCheck() -> MoveSound.CHECK
            move.isCapture || move.isEnPassant -> MoveSound.CAPTURE
            else -> MoveSound.MOVE
        }
        soundCue = SoundCue(++soundSerial, sound)
    }

    private fun selectIfOwnPiece(square: Int) {
        val piece = board.pieceAt(square)
        val own = piece != Piece.NONE && Piece.isColor(piece, board.whiteToMove)
        // Chỉ chọn khi quân đó thực sự còn nước đi, tránh highlight một ô rồi không làm gì.
        selectedSquare = if (own && legalMoves.any { it.from == square }) square else Squares.NONE
    }

    private fun targetsFrom(square: Int): Set<Int> {
        if (square == Squares.NONE) return emptySet()
        // Phong cấp sinh bốn nước cùng ô đến, Set tự gộp lại thành một gợi ý.
        return legalMoves.filter { it.from == square }.mapTo(HashSet()) { it.to }
    }

    private companion object {
        const val NO_PIECE_ID = -1
    }
}
