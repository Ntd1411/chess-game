package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.MoveGenerator
import kma.game.chess2d.engine.MoveList
import kma.game.chess2d.engine.Rules

/** Tiến độ sau mỗi vòng đào sâu, để UI hiện "đang nghĩ — độ sâu 5". */
data class SearchProgress(
    val depth: Int,
    val bestMove: Move,
    val score: Int,
    val nodes: Long,
    val elapsedMillis: Long,
)

data class ScoredMove(val move: Move, val score: Int)

/**
 * @param completed false nếu search bị cắt giữa đường (hết giờ hoặc bị hủy).
 * @param rootMoves các nước ở gốc kèm điểm, đã xếp giảm dần.
 */
data class SearchResult(
    val bestMove: Move,
    val score: Int,
    val depth: Int,
    val nodes: Long,
    val rootMoves: List<ScoredMove>,
    val completed: Boolean,
)

/**
 * Search negamax + alpha-beta.
 *
 * Một thể hiện giữ sẵn toàn bộ bộ nhớ làm việc (danh sách nước, bảng điểm, TT) nên
 * một lượt nghĩ gần như không cấp phát gì. Đổi lại, class NÀY KHÔNG THREAD-SAFE:
 * mỗi luồng nghĩ phải có thể hiện riêng.
 */
class Search(tableSizeMb: Int = 8) {

    companion object {
        /** Điểm hết nước. Nhỏ hơn Int.MAX_VALUE nhiều để đảo dấu không tràn số. */
        const val MATE = 30_000

        /** Điểm trên mốc này chắc chắn là một thế hết nước có thật. */
        const val MATE_THRESHOLD = MATE - 1_000

        const val MAX_PLY = 64

        /** 2047 node mới xem đồng hồ một lần: gọi nanoTime ở mọi node là tự làm chậm. */
        private const val CHECK_MASK = 2047L
    }

    private val table = TranspositionTable(tableSizeMb)
    private val ordering = MoveOrdering(MAX_PLY)

    /** Mỗi độ sâu một danh sách riêng: để nhánh con không ghi đè nước của nhánh cha. */
    private val moveLists = Array(MAX_PLY) { MoveList() }
    private val scoreBuffers = Array(MAX_PLY) { IntArray(MoveList.MAX_MOVES) }

    /** Bộ đệm dùng chung an toàn: MoveGenerator tiêu thụ xong ngay, không đệ quy. */
    private val generatorBuffer = MoveList()

    private var nodes = 0L
    private var deadlineNanos = 0L
    private var stopped = false
    private var isActive: () -> Boolean = { true }

    /** Xóa bộ nhớ giữa hai ván: điểm của ván cũ không còn ý nghĩa. */
    fun newGame() {
        table.clear()
        ordering.clear()
    }

    /**
     * Tìm nước đi.
     *
     * @param depth độ sâu tối đa.
     * @param timeBudgetMillis trần thời gian tính theo đồng hồ thật.
     * @param exactRootScores tính điểm chính xác cho MỌI nước ở gốc thay vì chỉ cho
     *        nước tốt nhất. Cần cho cấp Dễ (chọn trong nhóm 3 nước tốt nhất) và đổi
     *        lại là mất alpha-beta ở tầng gốc, nên cấp Khó KHÔNG bật.
     * @param isActive điểm hủy. Search hỏi định kỳ và dừng hẳn khi trả về false — đây
     *        là cách coroutine bên ngoài hủy được lượt nghĩ mà :ai không cần biết
     *        gì về coroutine.
     */
    fun search(
        board: Board,
        depth: Int,
        timeBudgetMillis: Long,
        exactRootScores: Boolean = false,
        isActive: () -> Boolean = { true },
        onProgress: (SearchProgress) -> Unit = {},
    ): SearchResult {
        nodes = 0L
        stopped = false
        this.isActive = isActive
        val startNanos = System.nanoTime()
        deadlineNanos = startNanos + timeBudgetMillis * 1_000_000L

        MoveGenerator.legal(board, moveLists[0], generatorBuffer)
        if (moveLists[0].isEmpty()) {
            return SearchResult(Move.NONE, 0, 0, 0L, emptyList(), true)
        }

        var bestMove = moveLists[0][0]
        var bestScore = 0
        var completedDepth = 0
        var rootMoves = listOf(ScoredMove(bestMove, 0))

        // Iterative deepening: đi từng bậc 1, 2, 3… thay vì nhảy thẳng tới độ sâu đích.
        // Nghe như lãng phí nhưng thực tế LÀM NHANH HƠN: kết quả vòng trước nằm trong
        // TT, giúp vòng sau thử đúng nước tốt trước và cắt nhánh sớm. Quan trọng hơn:
        // hết giờ lúc nào cũng có sẵn một nước đàng hoàng để dùng.
        for (currentDepth in 1..depth) {
            val scored = searchRoot(board, currentDepth, exactRootScores)
            // Hết giờ giữa vòng thì bỏ cả vòng đó: điểm của nó không đầy đủ nên
            // không so sánh được với vòng trước.
            if (stopped) break

            rootMoves = scored
            bestMove = scored[0].move
            bestScore = scored[0].score
            completedDepth = currentDepth
            onProgress(
                SearchProgress(
                    depth = currentDepth,
                    bestMove = bestMove,
                    score = bestScore,
                    nodes = nodes,
                    elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000L,
                ),
            )

            // Tìm ra đường hết nước rồi thì đào sâu thêm không để làm gì.
            if (bestScore > MATE_THRESHOLD) break
        }

        return SearchResult(bestMove, bestScore, completedDepth, nodes, rootMoves, !stopped)
    }

    private fun searchRoot(board: Board, depth: Int, exactRootScores: Boolean): List<ScoredMove> {
        val moves = moveLists[0]
        MoveGenerator.legal(board, moves, generatorBuffer)
        val scores = scoreBuffers[0]
        val ttMove = table.bestMove(board.hashKey)
        for (index in 0 until moves.size) {
            scores[index] = ordering.score(board, moves[index], ttMove, 0)
        }

        var alpha = -MATE
        val results = ArrayList<ScoredMove>(moves.size)
        for (index in 0 until moves.size) {
            ordering.pickBest(moves, scores, index)
            val move = moves[index]
            board.makeMove(move)
            val beta = if (exactRootScores) MATE else -alpha
            val score = -negamax(board, depth - 1, 1, -MATE, beta)
            board.unmakeMove()
            if (stopped) break
            results.add(ScoredMove(move, score))
            if (!exactRootScores && score > alpha) alpha = score
        }

        if (results.isEmpty()) return listOf(ScoredMove(moves[0], 0))
        results.sortByDescending { it.score }
        return results
    }

    private fun negamax(board: Board, depth: Int, ply: Int, alphaIn: Int, beta: Int): Int {
        if (stopped) return 0
        nodes++
        if (nodes and CHECK_MASK == 0L && shouldStop()) {
            stopped = true
            return 0
        }

        // Trong search chỉ cần LẶP LẦN THỨ HAI đã coi như hòa, không chờ đủ ba lần: nếu
        // một bên lặp được thế thì phải giả định nó sẽ lặp tiếp để cứu hòa.
        if (board.repetitionCount() >= 2) return 0
        if (board.halfmoveClock >= Rules.FIFTY_MOVE_LIMIT) return 0
        if (Rules.isInsufficientMaterial(board)) return 0

        if (depth <= 0) return quiescence(board, ply, alphaIn, beta)
        if (ply >= MAX_PLY - 1) return Evaluation.evaluate(board)

        var alpha = alphaIn
        val cached = table.probe(board.hashKey, depth, alpha, beta)
        if (cached != TranspositionTable.MISS) return cached

        val moves = moveLists[ply]
        MoveGenerator.legal(board, moves, generatorBuffer)
        if (moves.isEmpty()) {
            // Cộng ply vào điểm hết nước để engine thích đường hết nước NGẮN hơn; thiếu
            // cái này thì AI thấy "hết nước sau 5 nước" ngang "ngay bây giờ" và chần chừ.
            return if (board.isInCheck()) -MATE + ply else 0
        }

        val scores = scoreBuffers[ply]
        val ttMove = table.bestMove(board.hashKey)
        for (index in 0 until moves.size) {
            scores[index] = ordering.score(board, moves[index], ttMove, ply)
        }

        var bestScore = -MATE - 1
        var bestMoveRaw = 0
        for (index in 0 until moves.size) {
            ordering.pickBest(moves, scores, index)
            val move = moves[index]
            val quiet = !move.isCapture && !move.isPromotion
            board.makeMove(move)
            val score = -negamax(board, depth - 1, ply + 1, -beta, -alpha)
            board.unmakeMove()
            if (stopped) return 0

            if (score > bestScore) {
                bestScore = score
                bestMoveRaw = move.raw
            }
            if (score > alpha) alpha = score
            if (alpha >= beta) {
                // Nhánh bị cắt: đối phương sẽ không bao giờ cho ta vào đây, khỏi xét tiếp.
                if (quiet) ordering.rememberKiller(ply, move.raw)
                store(board.hashKey, depth, bestScore, TranspositionTable.LOWER_BOUND, bestMoveRaw)
                return bestScore
            }
        }

        val flag = if (bestScore > alphaIn) {
            TranspositionTable.EXACT
        } else {
            TranspositionTable.UPPER_BOUND
        }
        store(board.hashKey, depth, bestScore, flag, bestMoveRaw)
        return bestScore
    }

    /**
     * Quiescence — tìm tiếp cho tới khi hết chuỗi ăn quân.
     *
     * Không có nó thì AI bị "hiệu ứng chân trời": search hết độ sâu đúng lúc vừa ăn
     * Hậu và tưởng mình hơn quân, không thấy Hậu bị ăn lại ở nước ngay sau đó.
     */
    private fun quiescence(board: Board, ply: Int, alphaIn: Int, beta: Int): Int {
        if (stopped) return 0
        nodes++
        if (nodes and CHECK_MASK == 0L && shouldStop()) {
            stopped = true
            return 0
        }
        if (ply >= MAX_PLY - 1) return Evaluation.evaluate(board)

        val moves = moveLists[ply]
        MoveGenerator.legal(board, moves, generatorBuffer)
        if (moves.isEmpty()) return if (board.isInCheck()) -MATE + ply else 0

        // Stand pat: bên đi có quyền KHÔNG ăn gì cả, nên điểm tĩnh là sàn để so.
        var best = Evaluation.evaluate(board)
        if (best >= beta) return best
        var alpha = if (best > alphaIn) best else alphaIn

        val scores = scoreBuffers[ply]
        val ttMove = table.bestMove(board.hashKey)
        for (index in 0 until moves.size) {
            val move = moves[index]
            scores[index] = if (move.isCapture || move.isPromotion) {
                ordering.score(board, move, ttMove, ply)
            } else {
                MoveOrdering.SKIP
            }
        }

        for (index in 0 until moves.size) {
            ordering.pickBest(moves, scores, index)
            // Đã xếp giảm dần, nên gặp nước không ăn quân đầu tiên là hết việc.
            if (scores[index] == MoveOrdering.SKIP) break
            val move = moves[index]
            board.makeMove(move)
            val score = -quiescence(board, ply + 1, -beta, -alpha)
            board.unmakeMove()
            if (stopped) return 0

            if (score > best) best = score
            if (score > alpha) alpha = score
            if (alpha >= beta) break
        }
        return best
    }

    /**
     * Điểm hết nước KHÔNG được ghi vào TT: nó được tính kèm ply của nhánh hiện tại,
     * nên đọc lại ở nhánh khác sẽ cho số nước đến hết nước sai.
     */
    private fun store(key: Long, depth: Int, score: Int, flag: Int, moveRaw: Int) {
        if (score > MATE_THRESHOLD || score < -MATE_THRESHOLD) return
        table.store(key, depth, score, flag, moveRaw)
    }

    private fun shouldStop(): Boolean =
        !isActive() || System.nanoTime() >= deadlineNanos
}
