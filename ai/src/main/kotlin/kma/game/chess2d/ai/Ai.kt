package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Move
import kotlin.random.Random

/**
 * Ba cấp độ, theo bảng ở mục 3.3.
 *
 * @param depth độ sâu tối đa.
 * @param timeBudgetMillis trần thời gian mỗi lượt nghĩ. Độ sâu một mình là không đủ:
 *        một thế cờ rối có thể sâu 6 mất vài chục giây trên điện thoại yếu.
 * @param topMoveChoices chọn ngẫu nhiên trong bao nhiêu nước tốt nhất. 1 = luôn đi
 *        nước tốt nhất.
 * @param blunderChance tỉ lệ bỏ hẳn nước tốt nhất.
 */
enum class Difficulty(
    val depth: Int,
    val timeBudgetMillis: Long,
    val topMoveChoices: Int,
    val blunderChance: Double,
) {
    EASY(depth = 2, timeBudgetMillis = 500, topMoveChoices = 3, blunderChance = 0.20),
    MEDIUM(depth = 4, timeBudgetMillis = 2_000, topMoveChoices = 1, blunderChance = 0.0),
    HARD(depth = 6, timeBudgetMillis = 5_000, topMoveChoices = 1, blunderChance = 0.0),
}

/**
 * Mặt tiền của AI — tầng duy nhất mà :app cần biết.
 *
 * Giữ trạng thái (transposition table) nên mỗi đối thủ phải có một thể hiện riêng,
 * và KHÔNG thread-safe: một thể hiện chỉ được nghĩ một lượt tại một thời điểm.
 *
 * @param random tiêm vào được để test tái lập được ván đấu của cấp Dễ.
 */
class Ai(
    private val random: Random = Random.Default,
    tableSizeMb: Int = 8,
) {

    private val search = Search(tableSizeMb)

    fun newGame() = search.newGame()

    /**
     * Chọn nước đi cho bên đang đến lượt. Trả về Move.NONE khi ván đã hết nước.
     *
     * Hàm này chặn luồng gọi cho tới khi nghĩ xong, nên BẮT BUỘC gọi ngoài main
     * thread. Truyền isActive để hủy được giữa đường khi người chơi thoát màn hình.
     *
     * Lưu ý: bàn cờ bị đi và hoàn nguyên rất nhiều lần bên trong rồi trả về nguyên
     * trạng, nên không được chia sẻ bàn cờ này với luồng UI trong lúc nghĩ.
     */
    fun chooseMove(
        board: Board,
        difficulty: Difficulty,
        isActive: () -> Boolean = { true },
        onProgress: (SearchProgress) -> Unit = {},
    ): Move {
        val needsExactScores = difficulty.topMoveChoices > 1
        val result = search.search(
            board = board,
            depth = difficulty.depth,
            timeBudgetMillis = difficulty.timeBudgetMillis,
            exactRootScores = needsExactScores,
            isActive = isActive,
            onProgress = onProgress,
        )

        val candidates = result.rootMoves
        if (candidates.isEmpty()) return Move.NONE
        if (!needsExactScores) return result.bestMove

        // Cấp Dễ phải yếu một cách TỰ NHIÊN. Chỉ hạ độ sâu xuống 2 là chưa đủ: nó vẫn
        // ăn sạch mọi quân bỏ không và vẫn quá khó với người mới. Nhưng chọn nước
        // hoàn toàn ngẫu nhiên thì lại thành vô nghĩa. Cách giữa: vẫn chọn trong nhóm
        // nước tốt, nhưng thỉnh thoảng bỏ qua chính nước tốt nhất.
        val pool = if (candidates.size > 1 && random.nextDouble() < difficulty.blunderChance) {
            candidates.subList(1, candidates.size)
        } else {
            candidates
        }
        val choices = minOf(difficulty.topMoveChoices, pool.size)
        return pool[random.nextInt(choices)].move
    }
}
