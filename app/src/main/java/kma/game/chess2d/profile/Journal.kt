package kma.game.chess2d.profile

import kma.game.chess2d.opponent.AiCharacter

/** Trạng thái của một nhân vật trong Nhật ký: ✓ đã gặp, ? chưa rõ, 🔒 khóa. */
enum class JournalState { MET, UNKNOWN, LOCKED }

/** Luật suy ra trạng thái Nhật ký. Thuần, không phụ thuộc Android. */
object Journal {

    /**
     * - Đã gặp (có trong [metIds]) → [JournalState.MET].
     * - Chưa gặp nhưng nhân vật đã mở theo tiến độ Tháp ([clearedUpTo]) → [JournalState.UNKNOWN]:
     *   người chơi đã có thể gặp mà chưa gặp.
     * - Còn lại → [JournalState.LOCKED].
     *
     * Dùng chung điều kiện mở của [AiCharacter.isUnlocked] để Nhật ký và màn chọn đối thủ
     * không bao giờ mâu thuẫn nhau.
     */
    fun stateOf(character: AiCharacter, metIds: Set<String>, clearedUpTo: Int): JournalState = when {
        character.id in metIds -> JournalState.MET
        character.isUnlocked(clearedUpTo) -> JournalState.UNKNOWN
        else -> JournalState.LOCKED
    }

    /**
     * Một trang Nhật ký: nhân vật, trạng thái và lịch sử gặp (chỉ có khi [JournalState.MET]).
     */
    data class Page(
        val character: AiCharacter,
        val state: JournalState,
        val entry: JournalEntry?,
    )

    /**
     * Dựng toàn bộ trang Nhật ký theo đúng thứ tự [characters].
     *
     * [entries] là bản ghi đã gặp, khóa theo id nhân vật. Chỉ nhân vật đã gặp mới kèm [Page.entry],
     * nhân vật chưa gặp không có lịch sử để lộ.
     */
    fun pages(
        characters: List<AiCharacter>,
        entries: Map<String, JournalEntry>,
        clearedUpTo: Int,
    ): List<Page> = characters.map { character ->
        val state = stateOf(character, entries.keys, clearedUpTo)
        Page(character, state, entry = if (state == JournalState.MET) entries[character.id] else null)
    }

    /** Số nhân vật đã gặp trong [pages]. */
    fun metCount(pages: List<Page>): Int = pages.count { it.state == JournalState.MET }
}
