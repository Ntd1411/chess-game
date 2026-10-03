package kma.game.chess2d.ai

import kma.game.chess2d.engine.Board
import kma.game.chess2d.engine.Move
import kma.game.chess2d.engine.MoveGenerator
import kma.game.chess2d.engine.Rules
import kotlin.random.Random
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
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

    /**
     * Các cặp cấp độ khác nhau đấu nhau (Dễ–Vừa, Vừa–Dễ, Vừa–Vừa): mọi nước đều hợp luật và
     * không ván nào treo. Giới hạn 30 nửa nước mỗi ván để cả bộ test còn chạy trong vài
     * chục giây; ván dài đã được `aiVersusAiNeverPlaysAnIllegalMove` phủ ở cấp Dễ.
     */
    @Test
    fun mixedDifficultiesPlayOnlyLegalMoves() {
        val pairs = listOf(
            Difficulty.EASY to Difficulty.MEDIUM,
            Difficulty.MEDIUM to Difficulty.EASY,
            Difficulty.MEDIUM to Difficulty.MEDIUM,
        )
        for ((index, pair) in pairs.withIndex()) {
            for (game in 0 until 3) {
                playChecked(
                    whiteLevel = pair.first,
                    blackLevel = pair.second,
                    seed = 100 * index + game,
                    maxPlies = 30,
                    label = "${pair.first}-${pair.second} ván $game",
                )
            }
        }
    }

    /**
     * Hai AI cấp Khó đấu nhau: chỉ kiểm vài nước mở đầu ở bản nhanh vì mỗi nước cấp Khó có thể nghĩ
     * tới 5 giây. Bản đầy đủ 50 ván chạy hàng giờ nên chỉ bật khi đặt biến môi trường
     * `SELFPLAY_LONG=1` (ví dụ trên máy rảnh qua đêm), để `gradlew test` thường không bị kéo chậm.
     */
    @Test
    fun hardVersusHardOpeningIsLegal() {
        playChecked(Difficulty.HARD, Difficulty.HARD, seed = 1, maxPlies = 8, label = "HARD-HARD mở đầu")
    }

    @Test
    fun hardVersusHardFiftyGamesNeverCrash() {
        assumeTrue("bật bằng SELFPLAY_LONG=1", System.getenv("SELFPLAY_LONG") == "1")
        for (game in 0 until 50) {
            playChecked(Difficulty.HARD, Difficulty.HARD, seed = game, maxPlies = 300, label = "HARD-HARD ván $game")
        }
    }

    /** Chơi một ván và đòi mọi nước đều có thật, hợp luật; trả về số nửa nước đã đi. */
    private fun playChecked(
        whiteLevel: Difficulty,
        blackLevel: Difficulty,
        seed: Int,
        maxPlies: Int,
        label: String,
    ): Int {
        val white = Ai(Random(seed))
        val black = Ai(Random(seed + 1))
        white.newGame()
        black.newGame()

        val board = Board.startPosition()
        var ply = 0
        while (ply < maxPlies && !Rules.isGameOver(board)) {
            val legal = MoveGenerator.legalMoves(board).map { it.raw }.toSet()
            val whiteMoves = board.whiteToMove
            val move = (if (whiteMoves) white else black)
                .chooseMove(board, if (whiteMoves) whiteLevel else blackLevel)

            assertNotEquals("$label nước $ply: AI không chọn được nước nào", Move.NONE, move)
            assertTrue(
                "$label nước $ply: nước phi luật ${move.toUci()} ở thế ${board.toFen()}",
                move.raw in legal,
            )
            board.makeMove(move)
            ply++
        }
        return ply
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
