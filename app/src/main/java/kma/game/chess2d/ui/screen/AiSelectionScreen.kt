package kma.game.chess2d.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtPressable
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Bố cục màn Chọn đối thủ AI (đơn vị thiết kế 1080x1920). */
internal object AiSelectLayout {
    /** Vùng nhân vật: ảnh toàn thân 2:3 nếu có, không thì chân dung vuông trong khung. */
    val Hero = LobbyLayout.Box(240, 190, 600, 800)
    val HeroPortrait = LobbyLayout.Box(270, 300, 540, 540)
    val Prev = LobbyLayout.Box(70, 500, 100, 180)
    val Next = LobbyLayout.Box(910, 500, 100, 180)
    val Name = LobbyLayout.Box(90, 1000, 900, 76)
    val Stars = LobbyLayout.Box(400, 1080, 280, 76)

    const val THUMB = 150
    const val THUMB_Y = 1180
    val Description = LobbyLayout.Box(60, 1356, 960, 307)
    val Start = LobbyLayout.Box(215, 1676, 650, 186)
}

/** X của 5 ô chân dung trong dãy chọn (150x150, cách nhau 28), căn giữa khung 1080. */
internal val AiCarouselX = listOf(109, 287, 465, 643, 821)

/**
 * Màn "Chọn đối thủ AI": nền `bg_ai_select` trong khung `frame_gothic_screen`. Nhân vật đang chọn
 * đứng giữa (ảnh toàn thân nếu có, không thì chân dung trong `frame_character_thumb`), hai mũi
 * tên `icon_character_prev_next` để lật, tên + sao độ khó `icon_difficulty_star(_empty)`, dãy 5
 * ô chân dung (ô chưa mở phủ `overlay_character_locked`), panel `panel_ai_description` và nút
 * `button_ai_start`.
 *
 * Nhân vật chưa mở vẫn xem được nhưng nút Bắt đầu tắt và panel ghi điều kiện mở.
 *
 * @param clearedUpTo số tầng Tháp Cờ đã vượt qua, để biết nhân vật nào đã mở.
 * @param onStart báo nhân vật được chọn; chỉ gọi với nhân vật đã mở.
 */
@Composable
fun AiSelectionScreen(
    clearedUpTo: Int,
    onStart: (AiCharacter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val characters = AiCharacters.all
    var index by rememberSaveable { mutableIntStateOf(0) }
    val current = characters[index.coerceIn(characters.indices)]
    val unlocked = current.isUnlocked(clearedUpTo)
    DesignCanvas(background = R.drawable.bg_ai_select, dim = 0.3f, modifier = modifier) {
        val d = LocalDesign.current
        GothicScreenFrame()
        GothicScreenHeader(title = stringResource(R.string.ai_select_title), onBack = onBack)

        Hero(character = current, unlocked = unlocked)

        val count = characters.size
        ArrowButton(AiSelectLayout.Prev, mirrored = true, label = stringResource(R.string.ai_select_prev)) {
            index = (index - 1 + count) % count
        }
        ArrowButton(AiSelectLayout.Next, mirrored = false, label = stringResource(R.string.ai_select_next)) {
            index = (index + 1) % count
        }

        val n = AiSelectLayout.Name
        DesignText(
            text = stringResource(current.name),
            size = 54,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(n.x, n.y, n.w, n.h),
        )
        DifficultyStars(stars = current.stars, box = AiSelectLayout.Stars)

        // Dãy 5 ô chọn.
        characters.take(AiCarouselX.size).forEachIndexed { i, character ->
            Thumb(
                character = character,
                x = AiCarouselX[i],
                selected = i == index,
                unlocked = character.isUnlocked(clearedUpTo),
                onClick = { index = i },
            )
        }

        // Panel mô tả: ba dòng thông số và câu thoại (hoặc điều kiện mở nếu chưa mở).
        val p = AiSelectLayout.Description
        ArtImage(R.drawable.panel_ai_description, maxEdge = ArtSizes.BUTTON, trim = true,
            contentScale = ContentScale.FillBounds, modifier = Modifier.at(p.x, p.y, p.w, p.h))
        Column(
            modifier = Modifier.at(p.x, p.y, p.w, p.h).padding(horizontal = d.dp(92), vertical = d.dp(46)),
            verticalArrangement = Arrangement.spacedBy(d.dp(4), Alignment.CenterVertically),
        ) {
            StatLine(stringResource(R.string.ai_stat_style), stringResource(current.style))
            StatLine(stringResource(R.string.ai_stat_speed), stringResource(R.string.ai_speed_value, current.thinkSeconds))
            StatLine(stringResource(R.string.ai_stat_personality), stringResource(current.personality))
            DesignText(
                text = if (unlocked) {
                    stringResource(current.quote)
                } else {
                    stringResource(R.string.ai_select_locked, current.unlockFloor ?: 0)
                },
                size = 25,
                color = if (unlocked) GothicColors.Muted else GothicColors.Gold,
                italic = unlocked,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth().padding(top = d.dp(6)),
            )
        }

        val s = AiSelectLayout.Start
        ArtPressable(
            onClick = { onStart(current) },
            enabled = unlocked,
            modifier = Modifier.at(s.x, s.y, s.w, s.h),
        ) {
            ArtImage(R.drawable.button_ai_start, maxEdge = ArtSizes.BUTTON, trim = true,
                contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            DesignText(
                text = stringResource(R.string.ai_select_start),
                size = 46,
                color = GothicColors.Parchment,
                weight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

/** Nhân vật đang chọn: ảnh toàn thân nếu có, không thì chân dung vuông trong khung lớn. */
@Composable
private fun Hero(character: AiCharacter, unlocked: Boolean) {
    val full = character.fullBody
    if (full != null) {
        val h = AiSelectLayout.Hero
        ArtImage(
            res = full,
            maxEdge = ArtSizes.BOARD,
            trim = true,
            contentDescription = stringResource(character.name),
            contentScale = ContentScale.Fit,
            alpha = if (unlocked) 1f else 0.35f,
            modifier = Modifier.at(h.x, h.y, h.w, h.h),
        )
        if (!unlocked) {
            ArtImage(R.drawable.overlay_character_locked, maxEdge = ArtSizes.PIECE, trim = true,
                modifier = Modifier.at(h.x + 180, h.y + 280, 240, 240))
        }
    } else {
        val b = AiSelectLayout.HeroPortrait
        FramedPortrait(
            character = character,
            unlocked = unlocked,
            maxEdge = ArtSizes.PORTRAIT,
            modifier = Modifier.at(b.x, b.y, b.w, b.h),
        )
    }
}

/** Chân dung vuông trong `frame_character_thumb`; chưa mở thì phủ `overlay_character_locked`. */
@Composable
private fun FramedPortrait(character: AiCharacter, unlocked: Boolean, maxEdge: Int, modifier: Modifier) {
    BoxWithConstraints(modifier = modifier, contentAlignment = Alignment.Center) {
        ArtImage(
            res = character.portrait,
            maxEdge = maxEdge,
            contentDescription = stringResource(character.name),
            contentScale = ContentScale.Crop,
            alpha = if (unlocked) 1f else 0.4f,
            modifier = Modifier.fillMaxSize().padding(maxWidth * 0.11f).clip(RoundedCornerShape(maxWidth * 0.03f)),
        )
        if (!unlocked) {
            ArtImage(R.drawable.overlay_character_locked, maxEdge = ArtSizes.PIECE, trim = true,
                modifier = Modifier.fillMaxSize().padding(maxWidth * 0.08f))
        }
        ArtImage(R.drawable.frame_character_thumb, maxEdge = ArtSizes.PIECE, trim = true,
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
    }
}

/** Một ô trong dãy chọn: ô đang chọn to hơn và sáng, ô khác thu nhỏ và mờ bớt. */
@Composable
private fun Thumb(character: AiCharacter, x: Int, selected: Boolean, unlocked: Boolean, onClick: () -> Unit) {
    val size = AiSelectLayout.THUMB
    Box(
        modifier = Modifier.at(x, AiSelectLayout.THUMB_Y, size, size)
            .graphicsLayer {
                val s = if (selected) 1.08f else 0.9f
                scaleX = s
                scaleY = s
                alpha = if (selected) 1f else 0.75f
            }
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        if (selected) {
            ArtImage(R.drawable.fx_selected_piece_glow, maxEdge = ArtSizes.OVERLAY, trim = true, alpha = 0.7f,
                modifier = Modifier.fillMaxSize())
        }
        FramedPortrait(character = character, unlocked = unlocked, maxEdge = ArtSizes.PORTRAIT, modifier = Modifier.fillMaxSize())
    }
}

/** Mũi tên lật nhân vật; ảnh gốc chỉ chỉ sang phải nên mũi trái lật ngang. */
@Composable
private fun ArrowButton(box: LobbyLayout.Box, mirrored: Boolean, label: String, onClick: () -> Unit) {
    ArtPressable(onClick = onClick, modifier = Modifier.at(box.x, box.y, box.w, box.h)) {
        ArtImage(
            R.drawable.icon_character_prev_next,
            maxEdge = ArtSizes.ICON,
            trim = true,
            contentDescription = label,
            modifier = Modifier.fillMaxSize().graphicsLayer { scaleX = if (mirrored) -1f else 1f },
        )
    }
}

/** Độ khó: 3 sao, sao đầy `icon_difficulty_star`, sao rỗng `icon_difficulty_star_empty`. */
@Composable
private fun DifficultyStars(stars: Int, box: LobbyLayout.Box) {
    val d = LocalDesign.current
    val description = stringResource(R.string.ai_stars, stars)
    Row(
        modifier = Modifier.at(box.x, box.y, box.w, box.h).semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(d.dp(12), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(3) { i ->
            ArtImage(
                res = if (i < stars) R.drawable.icon_difficulty_star else R.drawable.icon_difficulty_star_empty,
                maxEdge = ArtSizes.ICON,
                trim = true,
                modifier = Modifier.size(d.dp(72)),
            )
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    DesignText(
        text = "$label: $value",
        size = 26,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
    )
}
