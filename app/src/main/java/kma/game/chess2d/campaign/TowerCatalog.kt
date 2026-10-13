package kma.game.chess2d.campaign

import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.engine.Engine
import kma.game.chess2d.engine.Rules
import kma.game.chess2d.puzzle.Puzzle
import kma.game.chess2d.puzzle.PuzzleCatalog

/**
 * Bộ 49 tầng của Tháp Cờ.
 *
 * Each chapter has six checking puzzles followed by one full boss game.
 * The six story milestones are preserved. Like
 * [PuzzleCatalog], [build] và [validate] đều là hàm thuần nên test JVM duyệt được cả
 * 49 tầng bằng engine thật, không cần máy Android.
 */
object TowerCatalog {

    const val FLOOR_COUNT = 49
    const val FLOORS_PER_CHAPTER = 7

    /** Các tầng mốc có thoại cốt truyện: mở đầu, Player #48, Aria, true reveal, lý do của Aria, kết. */
    val MILESTONES: Set<Int> = setOf(1, 5, 21, 42, 45, 49)

    /** Thế khởi đầu của tầng bí mật: Trắng mất Hậu, người chơi phải cầm cự. */
    const val FEN_WHITE_NO_QUEEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"

    fun chapterOf(floor: Int): Int = (floor - 1) / FLOORS_PER_CHAPTER + 1

    /**
     * Độ khó thường của tầng, theo 3 lớp bí ẩn: tầng 1–14, 15–35, 36–49.
     */
    fun difficultyFor(floor: Int): Difficulty = when {
        floor <= 14 -> Difficulty.EASY
        floor <= 35 -> Difficulty.MEDIUM
        else -> Difficulty.HARD
    }

    const val PUZZLE_COUNT = 42

    /** Each non-boss floor gets a distinct puzzle, starting at an estimated Elo of 800. */
    fun build(puzzleCount: Int): List<Floor> {
        require(puzzleCount >= PUZZLE_COUNT) { "cần ít nhất $PUZZLE_COUNT câu đố khác nhau" }
        return (1..FLOOR_COUNT).map { floor(it) }
    }

    private fun floor(number: Int): Floor {
        val chapter = chapterOf(number)
        val position = (number - 1) % FLOORS_PER_CHAPTER + 1
        val isBoss = position == FLOORS_PER_CHAPTER
        val puzzleIndex = number - 1 - number / FLOORS_PER_CHAPTER
        val milestoneTitle = when (number) {
            1 -> "Cổng tháp"
            5 -> "Player #48"
            21 -> "Aria, Nữ Hoàng Ký Ức"
            42 -> "Sự thật bị chôn vùi"
            45 -> "Lời Aria"
            49 -> "Nước đi thứ 49"
            else -> null
        }
        return Floor(
            number = number,
            kind = if (isBoss) NodeKind.BOSS else NodeKind.PUZZLE,
            title = milestoneTitle ?: if (isBoss) "Người giữ cửa chương $chapter" else "Dấu vết $chapter · $position",
            goal = if (isBoss) FloorGoal.DefeatAi(
                when (chapter) {
                    1 -> Difficulty.EASY
                    2 -> Difficulty.MEDIUM
                    else -> Difficulty.HARD
                }, TowerBosses.strength(number),
            ) else FloorGoal.SolvePuzzle(puzzleIndex),
            hasDialogue = number in MILESTONES,
            estimatedElo = if (isBoss) null else 800 + puzzleIndex * 25,
            bossId = if (isBoss) TowerBosses.characterId(number) else null,
            teaser = teaser(number),
        )
    }

    /** One distinct, spoiler-light introduction per floor, never a shared chapter template. */
    private fun teaser(number: Int): String = when (number) {
        1 -> "Ngươi cũng chỉ là một kẻ lạc lối sao? Ở nơi này, chỉ có chiến thắng mới đưa ngươi lên…"
        2 -> "Bụi phủ kín cầu thang, trừ một vệt chân còn mới. Có người vừa đi qua đây."
        3 -> "Ngọn nến bên bàn cờ bỗng sáng lên khi ngươi đến gần. Căn phòng dường như đã chờ từ lâu."
        4 -> "Một chiếc chìa khóa nằm dưới quân xe. Ổ khóa của nó ở đâu đó phía trên."
        5 -> "Một bóng người đứng bên bàn cờ. Vì sao ánh mắt của hắn quen thuộc đến vậy?"
        6 -> "Những ký hiệu trên tường tỏa sáng nhè nhẹ. Từ tầng trên vọng xuống tiếng lật sách."
        7 -> "Pháp Sư Cờ khép quyển sách và mỉm cười. Ngươi có tin một nước đi cũng có thể là phép thuật?"
        8 -> "Cuộn giấy bên cửa chỉ còn đọc được vài chữ. Phần tên người viết đã bị xóa sạch."
        9 -> "Một bộ giáp rỗng đứng giữa hành lang. Khi ngươi quay lưng, nó khẽ chuyển động."
        10 -> "Mưa gõ lên khung cửa sổ vỡ. Ngoài kia không có lấy một ngọn đèn."
        11 -> "Trên bàn có một ván cờ bị bỏ dở. Chiếc ghế đối diện vẫn còn ấm."
        12 -> "Lọ thuốc xanh nằm cạnh một lá thư chưa gửi. Nét chữ run rẩy dừng ở lời chào."
        13 -> "Cánh cửa sắt hé mở, để lộ ánh kiếm bên trong. Người canh gác đã biết ngươi sẽ đến."
        14 -> "Hiệp Sĩ Bóng Đêm đặt kiếm xuống cạnh bàn. Không một tiếng động nào lọt qua chiếc mũ giáp."
        15 -> "Tấm gương phản chiếu căn phòng, nhưng thiếu bóng ngươi. Một vết nứt đang lan từ chính giữa."
        16 -> "Viên pha lê phát ra tiếng thì thầm. Ngươi chỉ nghe rõ một cái tên: Aria."
        17 -> "Những bức chân dung đều bị che mặt. Một đôi mắt vẫn nhìn ngươi qua lớp vải đỏ."
        18 -> "Tiếng nhạc vang lên từ cây đàn không người chạm. Giai điệu này khiến ngươi thấy nhớ một điều gì đó."
        19 -> "Cánh hoa trắng rơi trên bàn cờ đen. Chưa ai tìm thấy khu vườn nơi nó mọc."
        20 -> "Một quân tốt vỡ nằm trong góc phòng. Bên cạnh là một dấu tay mờ trên đá."
        21 -> "Aria chờ sau tấm màn đỏ. Nữ Hoàng Máu giữ cửa căn phòng chứa những ký ức đã mất."
        22 -> "Sau cánh cửa của nữ hoàng là một hành lang đầy gương. Mỗi tấm phản chiếu một bầu trời khác nhau."
        23 -> "Một chiếc đồng hồ đã mất cả hai kim. Tiếng tích tắc vẫn vang đều trong phòng."
        24 -> "Mảnh quân cờ khắc một ký hiệu quen thuộc. Ngươi không nhớ đã nhìn thấy nó ở đâu."
        25 -> "Bức thư trên ngai trống không đề tên người nhận. Chỉ có lời hẹn gặp ở đỉnh tháp."
        26 -> "Bóng của một vương miện trải dài trên tường. Chiếc vương miện thật đã biến mất."
        27 -> "Hai hàng tượng cúi đầu trước cánh cửa đen. Phía sau cửa, có người đang chờ trong im lặng."
        28 -> "Vua Hắc Ám ngồi bất động trên ngai. Với hắn, sự kiên nhẫn cũng là một thứ vũ khí."
        29 -> "Tiếng chuông từ ngôi điện phía trên bỗng tắt. Trong bụi hiện ra dấu một quân tượng."
        30 -> "Một chiếc ghế đã được kéo sẵn cho ngươi. Trên lưng ghế có vết khắc giống tên mình."
        31 -> "Tấm bản đồ dừng lại ở tầng này. Những bậc thang phía trước chưa từng được ghi lên giấy."
        32 -> "Một quân tượng bằng đá đứng giữa tro tàn. Đường chéo dưới chân nó dẫn vào bóng tối."
        33 -> "Một lời cảnh báo bị cào khỏi cánh cửa. Chỉ còn hai chữ: đừng quên."
        34 -> "Hai cánh cửa nhà nguyện mở ra không một tiếng động. Người giữ điện đã ngồi sẵn bên bàn cờ."
        35 -> "Giám Mục Tĩnh Lặng đặt một quân tượng xuống bàn. Trong sự im lặng của hắn, không có nước đi nào là vô nghĩa."
        36 -> "Sau nhà nguyện là một hành lang phủ ánh vàng. Những mảnh ký ức đang tìm đường về với chủ nhân."
        37 -> "Ngọn nến cuối phòng cháy bằng lửa trắng. Dưới chân nến là một trang giấy trắng tinh."
        38 -> "Ngươi nghe thấy tiếng mình gọi từ bên kia cửa. Nhưng căn phòng phía sau hoàn toàn trống rỗng."
        39 -> "Hồ sơ trên bàn đã bị nhòe gần hết. Số 48 là dòng duy nhất còn đọc được."
        40 -> "Một vương miện vàng nằm trên chiếc gối bạc. Dưới nó là nét chữ của Aria."
        41 -> "Ánh vàng lọt qua cánh cửa ký ức. Một hồ sơ mang số 48 nằm ngay trước ngưỡng cửa."
        42 -> "Nữ Hoàng Vàng chờ bên bàn cờ sáng rực. Aria sẽ thử xem ngươi có đủ sức giữ lấy ký ức của mình hay không."
        43 -> "Những bậc thang phía sau chìm dần trong sương. Tiếng gọi của Player #48 vẫn còn vang trong đầu."
        44 -> "Quả cầu phép soi lên những căn phòng ngươi đã đi qua. Có một cánh cửa trong đó chưa từng được mở."
        45 -> "Aria để lại một thế cờ cùng lời nhắn: hãy tìm lối ra, không phải thêm một chiếc ngai."
        46 -> "Một bàn cờ trống nằm dưới ánh trăng đỏ. Bên cạnh nó chỉ có một chiếc ghế."
        47 -> "Gió từ đỉnh tháp mang theo những cánh hoa trắng. Lần đầu tiên, không khí ở đây bớt lạnh."
        48 -> "Mảnh cờ vàng nằm trên bậc thang cuối. Cánh cửa phía trước không có ổ khóa."
        49 -> "Kẻ không tên chờ ở đỉnh tháp. Nước đi cuối cùng sẽ khép lại điều gì?"
        else -> error("floor $number has no introduction")
    }

    /**
     * Kiểm tra một tầng bằng engine thật; ném lỗi nếu dữ liệu sai.
     *
     * Nhắm vào lỗi của người viết dữ liệu (FEN hỏng, chỉ số câu đố lệch, loại node
     * không khớp mục tiêu), nên sai là báo ngay chứ không âm thầm bỏ qua.
     */
    fun validate(floor: Floor, puzzles: List<Puzzle>) {
        val label = "tầng ${floor.number}"
        require(floor.number in 1..FLOOR_COUNT) { "$label ngoài khoảng 1..$FLOOR_COUNT" }
        require(floor.title.isNotBlank()) { "$label thiếu tên" }

        floor.startFen?.let { fen ->
            val board = Engine.fromFen(fen)
            check(!Rules.isGameOver(board)) { "$label: thế cờ bắt đầu đã kết thúc ($fen)" }
        }

        val goal = floor.goal
        val kindMatchesGoal = when (floor.kind) {
            NodeKind.BATTLE, NodeKind.BOSS -> goal is FloorGoal.DefeatAi
            NodeKind.PUZZLE -> goal is FloorGoal.SolvePuzzle
            NodeKind.SECRET -> goal is FloorGoal.SurviveMoves
            NodeKind.STORY, NodeKind.TREASURE -> goal is FloorGoal.Claim
        }
        check(kindMatchesGoal) { "$label: loại ${floor.kind} không khớp mục tiêu $goal" }

        when (goal) {
            is FloorGoal.SolvePuzzle -> {
                check(goal.puzzleIndex in puzzles.indices) {
                    "$label: câu đố số ${goal.puzzleIndex} không tồn tại (có ${puzzles.size} câu)"
                }
                PuzzleCatalog.validate(puzzles[goal.puzzleIndex])
            }

            is FloorGoal.SurviveMoves -> {
                check(goal.moves > 0) { "$label: số nước phải sống sót phải dương" }
                check(floor.startFen != null) { "$label: tầng sống sót cần thế cờ riêng" }
            }

            is FloorGoal.DefeatAi, FloorGoal.Claim -> Unit
        }
    }

    /**
     * Kiểm tra cả bộ: đủ [FLOOR_COUNT] tầng đánh số liên tục, thoại chỉ ở tầng mốc,
     * và từng tầng qua [validate].
     */
    fun validateAll(floors: List<Floor>, puzzles: List<Puzzle>) {
        check(floors.size == FLOOR_COUNT) { "cần $FLOOR_COUNT tầng, có ${floors.size}" }
        for ((index, floor) in floors.withIndex()) {
            check(floor.number == index + 1) { "tầng ở vị trí ${index + 1} đánh số ${floor.number}" }
            check(floor.hasDialogue == (floor.number in MILESTONES)) {
                "tầng ${floor.number}: cờ thoại không khớp danh sách tầng mốc"
            }
            check((floor.kind == NodeKind.BOSS) == (floor.number % FLOORS_PER_CHAPTER == 0)) {
                "tầng ${floor.number}: only multiples of seven may be bosses"
            }
            validate(floor, puzzles)
            if (floor.kind == NodeKind.BOSS) {
                val boss = requireNotNull(TowerBosses.character(floor))
                check(floor.bossId == TowerBosses.characterId(floor.number))
                check(boss.unlockFloor == null || boss.unlockFloor == floor.number) {
                    "floor ${floor.number}: boss unlock does not match their guardian floor"
                }
            }
            (floor.goal as? FloorGoal.SolvePuzzle)?.let { goal ->
                check(floor.estimatedElo == puzzles[goal.puzzleIndex].estimatedElo) {
                    "tầng ${floor.number}: puzzle rating differs from the preview"
                }
            }
        }
        check(floors.filter { it.kind == NodeKind.BOSS }.map { it.bossId }.distinct().size == 7) {
            "each chapter must have a distinct guardian"
        }
        val puzzleFloors = floors.filter { it.goal is FloorGoal.SolvePuzzle }
        check(puzzleFloors.size == PUZZLE_COUNT)
        check(puzzleFloors.map { (it.goal as FloorGoal.SolvePuzzle).puzzleIndex }.toSet().size == PUZZLE_COUNT)
        check(puzzleFloors.map { puzzles[(it.goal as FloorGoal.SolvePuzzle).puzzleIndex].startFen }.toSet().size == PUZZLE_COUNT)
    }
}
