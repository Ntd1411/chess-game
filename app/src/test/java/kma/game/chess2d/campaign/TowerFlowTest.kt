package kma.game.chess2d.campaign

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Thoại của 6 tầng mốc và luật chuyển màn của chiến dịch.
 *
 * Phần "thắng tầng N → mở tầng N+1" duyệt cả 49 tầng bằng đúng [TowerFlow] và [TowerProgress]
 * mà app dùng, nên một tầng bị kẹt không tới được màn Chiến thắng sẽ làm test đỏ.
 */
class TowerFlowTest {

    private val floors = TowerCatalog.build(puzzleCount = 42)

    @Test
    fun `thoai mo dau chi co o dung 6 tang moc`() {
        val withIntro = (1..TowerCatalog.FLOOR_COUNT).filter { FloorDialogues.intro(it).isNotEmpty() }.toSet()
        assertEquals(TowerCatalog.MILESTONES, withIntro)
    }

    @Test
    fun `co thoai khop voi co hasDialogue cua tung tang`() {
        for (floor in floors) {
            assertEquals(
                "tầng ${floor.number}",
                floor.hasDialogue,
                FloorDialogues.intro(floor.number).isNotEmpty(),
            )
        }
    }

    @Test
    fun `moi cau thoai co noi dung`() {
        for (floor in 1..TowerCatalog.FLOOR_COUNT) {
            val lines = FloorDialogues.intro(floor) + FloorDialogues.epilogue(floor)
            for (line in lines) {
                assertTrue("tầng $floor thiếu lời", line.text.isNotBlank())
            }
        }
    }

    @Test
    fun `thoai tang 45 gan vong lap voi that bai va khong goi Aria la ba`() {
        val lines = FloorDialogues.intro(45)
        assertTrue(lines.any { it.speaker == Speaker.ARIA && it.text.contains("thất bại ở đỉnh tháp") })
        assertTrue(lines.none { it.text.contains("mỗi lần ngươi lên tới đỉnh") })
        val traveler = lines.single { it.speaker == Speaker.TRAVELER }
        assertEquals("Vậy vì sao lần này tôi được biết sự thật?", traveler.text)
    }

    @Test
    fun newGuardiansHaveConsistentStoryAndNoRematchNarrative() {
        assertEquals("silent_bishop", floors[34].bossId)
        assertTrue(floors[34].teaser.contains("Giám Mục Tĩnh Lặng"))
        assertEquals("golden_queen", floors[41].bossId)
        assertTrue(floors[41].teaser.contains("Nữ Hoàng Vàng"))
        assertTrue(FloorDialogues.intro(21).any { it.speaker == Speaker.ARIA && it.text.contains("Nữ Hoàng Vàng") })
        val reveal = FloorDialogues.intro(42)
        assertTrue(reveal.any { it.text.contains("Attempt #49") })
        assertTrue(reveal.any { it.text.contains("Aria, Nữ Hoàng Vàng") })
        assertTrue(reveal.none { it.text.contains("Nữ Hoàng Máu") })
        assertTrue(!floors[34].teaser.contains("Hiệp Sĩ Bóng Đêm"))
        assertTrue(!floors[41].teaser.contains("Nữ Hoàng Máu"))
    }

    @Test
    fun `thoai ket chi co o tang cuoi`() {
        val withEpilogue = (1..TowerCatalog.FLOOR_COUNT).filter { FloorDialogues.epilogue(it).isNotEmpty() }
        assertEquals(listOf(TowerCatalog.FLOOR_COUNT), withEpilogue)
    }

    @Test
    fun `tang thuong vao thang tran dau`() {
        val floor = floors[2] // tầng 3: đấu cờ thường
        assertEquals(TowerStage.BATTLE, TowerFlow.onSelect(floor))
    }

    @Test
    fun `tang moc co thoai thi xem thoai roi moi danh`() {
        val floor = floors[0] // tầng 1
        assertEquals(TowerStage.INTRO, TowerFlow.onSelect(floor))
        assertEquals(TowerStage.BATTLE, TowerFlow.afterIntro(floor))
    }

    @Test
    fun `tang 45 xem thoai roi phai giai puzzle`() {
        val floor = floors[44]
        assertEquals(NodeKind.PUZZLE, floor.kind)
        assertEquals(TowerStage.INTRO, TowerFlow.onSelect(floor))
        assertEquals(TowerStage.BATTLE, TowerFlow.afterIntro(floor))
    }

    @Test
    fun `tang 4 khong con nhan thuong mien phi`() {
        val floor = floors[3] // tầng 4: kho báu
        assertEquals(NodeKind.PUZZLE, floor.kind)
        assertEquals(TowerStage.BATTLE, TowerFlow.onSelect(floor))
    }

    @Test
    fun `tang cuoi co thoai ket truoc khi chien thang`() {
        val floor = floors.last()
        assertEquals(TowerStage.EPILOGUE, TowerFlow.afterWin(floor))
        assertEquals(TowerStage.VICTORY, TowerFlow.afterWin(floors[0]))
    }

    @Test
    fun `tang ke tiep dung va tang cuoi khong co tang ke`() {
        assertEquals(2, TowerFlow.nextFloor(floors, floors[0])?.number)
        assertEquals(49, TowerFlow.nextFloor(floors, floors[47])?.number)
        assertNull(TowerFlow.nextFloor(floors, floors.last()))
    }

    @Test
    fun `thang lien tiep ca 49 tang thi moi tang mo khoa tang ke va khong tang nao ket`() {
        var progress = TowerProgress()
        for (floor in floors) {
            assertEquals("tầng ${floor.number} phải là tầng hiện tại", FloorStatus.CURRENT, progress.statusOf(floor.number))

            // Đi hết luồng như người chơi: chọn tầng → (thoại) → (ván, thắng) → (thoại kết) → Chiến thắng.
            var stage = TowerFlow.onSelect(floor)
            var guard = 0
            while (stage != TowerStage.VICTORY) {
                stage = when (stage) {
                    TowerStage.INTRO -> TowerFlow.afterIntro(floor)
                    TowerStage.BATTLE -> TowerFlow.afterWin(floor)
                    TowerStage.EPILOGUE -> TowerStage.VICTORY
                    else -> error("tầng ${floor.number} kẹt ở $stage")
                }
                check(++guard < 10) { "tầng ${floor.number} không tới được Chiến thắng" }
            }

            progress = progress.withCleared(floor.number)
            assertEquals(floor.number, progress.clearedUpTo)
            floors.getOrNull(floor.number)?.let { next ->
                assertEquals(FloorStatus.CURRENT, progress.statusOf(next.number))
            }
        }
        assertTrue(progress.isTowerCleared)
    }

    @Test
    fun `thang tang cu khong lam lui tien do`() {
        var progress = TowerProgress()
        for (n in 1..5) progress = progress.withCleared(n)
        progress = progress.withCleared(2)
        assertEquals(5, progress.clearedUpTo)
    }
}
