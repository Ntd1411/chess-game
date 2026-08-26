package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.MoveGenerator
import kma.game.chess2d.engine.Rules
import kotlin.random.Random
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tiêu chí ở mục 5.4: chơi 100 ván AI đấu AI mà không nước nào phi luật và không
 * ván nào treo.
 *
 * Ván tự đấu là cách duy nhất tìm ra những thế cờ mà không ai nghĩ ra để viết test:
 * phong cấp khi ăn quân, hết nước ở đáy quiescence, hoàn nguyên sai sau nhập thành.
 * Dùng cấp Dễ để ngân sách thời gian không bao giờ bị chạm tới, nhưng gieo
 * ngẫu nhiên khác nhau để 100 ván đi theo 100 đường khác nhau.
 */
class SelfPlayTest {

    private companion object {
        const val GAMES = 100

        /** Chặn trên số nước: quá mức này coi như ván đã treo. */
        const val MAX_PLIES = 200
    }

    @Test
    fun aiVersusAiNeverPlaysAnIllegalMove() {
        var finished = 0
        var totalPlies = 0

        for (game in 0 until GAMES) {
            // Mỗi bên một thể hiện riêng: chia sẻ transposition table giữa hai đối thủ là
            // thứ sẽ không bao giờ xảy ra trong ván thật.
            val white = Ai(Random(1_000 + game))
            val black = Ai(Random(9_000 + game))
            white.newGame()
            black.newGame()

            val board = Board.startPosition()
            var ply = 0
            while (ply < MAX_PLIES && !Rules.isGameOver(board)) {
                val legal = MoveGenerator.legalMoves(board).map { it.raw }.toSet()
                val mover = if (board.whiteToMove) white else black
                val move = mover.chooseMove(board, Difficulty.EASY)

                assertNotEquals("ván $game nước $ply: AI không chọn được nước nào", Move.NONE, move)
                assertTrue(
                    "ván $game nước $ply: nước phi luật ${move.toUci()} ở thế ${board.toFen()}",
                    move.raw in legal,
                )

                board.makeMove(move)
                ply++
            }

            totalPlies += ply
            if (Rules.isGameOver(board)) finished++
        }

        // Không đòi mọi ván phải kết thúc trong 200 nước — cấp Dễ có yếu tố ngẫu nhiên
        // nên nhiều ván dài. Chỉ đòi AI thực sự đang chơi, không phải đẩy quân vô đính.
        assertTrue("chỉ $finished/$GAMES ván có kết cục", finished > 0)
        assertTrue("trung bình chỉ ${totalPlies / GAMES} nước mỗi ván", totalPlies / GAMES > 10)
    }

    /** Cùng hạt giống phải cho đúng cùng một ván — không thể thì không debug được gì. */
    @Test
    fun sameSeedReplaysTheSameGame() {
        val first = playScriptedGame(seed = 42)
        val second = playScriptedGame(seed = 42)
        assertTrue("ván rỗng", first.isNotEmpty())
        assertTrue("cùng hạt giống nhưng khác ván", first == second)
    }

    /** Cấp Khó không có ngẫu nhiên: cùng thế cờ phải luôn cho cùng nước đi. */
    @Test
    fun hardDifficultyIsDeterministic() {
        val board = Board.startPosition()
        val firstChoice = Ai().chooseMove(board, Difficulty.HARD)
        val secondChoice = Ai().chooseMove(board, Difficulty.HARD)
        assertTrue(firstChoice.raw == secondChoice.raw)
    }

    private fun playScriptedGame(seed: Int): List<String> {
        val ai = Ai(Random(seed))
        ai.newGame()
        val board = Board.startPosition()
        val played = ArrayList<String>()
        var ply = 0
        while (ply < 20 && !Rules.isGameOver(board)) {
            val move = ai.chooseMove(board, Difficulty.EASY)
            if (move == Move.NONE) break
            played.add(move.toUci())
            board.makeMove(move)
            ply++
        }
        return played
    }
}
