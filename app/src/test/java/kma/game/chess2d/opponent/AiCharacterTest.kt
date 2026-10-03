package kma.game.chess2d.opponent

import kma.game.chess2d.ai.Difficulty
import kma.game.chess2d.campaign.TowerCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Dữ liệu đối thủ AI: số sao và tốc độ phải suy ra từ độ khó thật, và luật mở nhân vật ẩn. */
class AiCharacterTest {

    private val characters = AiCharacters.all

    @Test
    fun `co du nam nhan vat voi id khac nhau`() {
        assertEquals(5, characters.size)
        assertEquals(characters.size, characters.map { it.id }.toSet().size)
    }

    @Test
    fun `moi nhan vat tro toi tai nguyen khac le`() {
        // Id tài nguyên bằng 0 nghĩa là chưa gắn chuỗi hoặc ảnh.
        for (character in characters) {
            assertNotEquals(character.id, 0, character.name)
            assertNotEquals(character.id, 0, character.portrait)
            assertNotEquals(character.id, 0, character.style)
            assertNotEquals(character.id, 0, character.personality)
            assertNotEquals(character.id, 0, character.quote)
        }
        assertEquals(characters.size, characters.map { it.portrait }.toSet().size)
    }

    @Test
    fun `so sao suy ra tu do kho that`() {
        for (character in characters) {
            assertEquals(character.difficulty.ordinal + 1, character.stars)
            assertTrue(character.stars in 1..3)
        }
        assertEquals(1, characters.first { it.difficulty == Difficulty.EASY }.stars)
        assertEquals(3, characters.first { it.difficulty == Difficulty.HARD }.stars)
    }

    @Test
    fun `toc do nghi lay thang tu ngan sach cua AI`() {
        for (character in characters) {
            assertEquals(character.difficulty.timeBudgetMillis / 1000f, character.thinkSeconds, 0f)
        }
    }

    @Test
    fun `thu tu carousel di tu de toi kho`() {
        val levels = characters.map { it.difficulty.ordinal }
        assertEquals(levels.sorted(), levels)
    }

    @Test
    fun `nhan vat mac dinh mo san`() {
        assertTrue(AiCharacters.default.isUnlocked(clearedUpTo = 0))
    }

    @Test
    fun `chi nhan vat an bi khoa luc moi choi`() {
        val locked = characters.filterNot { it.isUnlocked(clearedUpTo = 0) }
        assertEquals(1, locked.size)
        assertEquals("hidden", locked.single().id)
    }

    @Test
    fun `nhan vat an mo dung khi vuot het thap`() {
        val hidden = characters.single { it.id == "hidden" }
        assertFalse(hidden.isUnlocked(TowerCatalog.FLOOR_COUNT - 1))
        assertTrue(hidden.isUnlocked(TowerCatalog.FLOOR_COUNT))
    }
}
