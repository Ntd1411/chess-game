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
}
