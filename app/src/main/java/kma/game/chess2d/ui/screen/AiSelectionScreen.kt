package kma.game.chess2d.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** X của 5 ô chân dung trong dãy chọn (y=930, 160x160), theo bản thiết kế. */
internal val AiCarouselX = listOf(64, 248, 432, 616, 800)

/**
 * Màn "Chọn đối thủ AI" theo bản thiết kế: nền `bg_floor_select`, chân dung vuông lớn (500x500,
 * giữ 1:1, không phóng thành toàn thân), bảng tên, dãy 5 khung `ui_character_frame`, panel
 * mô tả và độ khó, nút Bắt đầu.
 *
 * Nhân vật chưa mở vẫn xem được nhưng nút Bắt đầu hiện ảnh trạng thái tắt kèm điều kiện mở.
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
    DesignCanvas(background = R.drawable.bg_floor_select, dim = 0.3f, modifier = modifier) {
        val d = LocalDesign.current
        SubScreenHeader(title = stringResource(R.string.ai_select_title), onBack = onBack, backY = 64)

        // Chân dung lớn, vuông.
        ArtImage(
            res = current.portrait,
            maxEdge = ArtSizes.PORTRAIT,
            contentDescription = stringResource(current.name),
            contentScale = ContentScale.Fit,
            alpha = if (unlocked) 1f else 0.35f,
            modifier = Modifier.at(290, 210, 500, 500),
        )
        if (!unlocked) {
            ArtImage(R.drawable.icon_locked, maxEdge = ArtSizes.ICON, trim = true, modifier = Modifier.at(470, 380, 140, 160))
        }

        SlicedArt(res = R.drawable.ui_name_plate, slice = 0.5f, modifier = Modifier.at(200, 735, 680, 90))
        DesignText(
            text = stringResource(current.name),
            size = 44,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(260, 735, 560, 90),
        )

        // Dãy 5 ô chọn.
        characters.take(AiCarouselX.size).forEachIndexed { i, character ->
            val isSelected = i == index
            Box(
                modifier = Modifier.at(AiCarouselX[i], 930, 160, 160).clip(CircleShape)
                    .clickable(role = Role.Button) { index = i },
            ) {
                PortraitInFrame(
                    portrait = character.portrait,
                    contentDescription = stringResource(character.name),
                    dimmed = !character.isUnlocked(clearedUpTo),
                    modifier = Modifier.padding(if (isSelected) d.dp(0) else d.dp(14)),
                )
                // Vầng sáng chọn nằm trên khung, dưới chữ.
                if (isSelected) {
                    ArtImage(
                        R.drawable.fx_selected_piece_glow,
                        maxEdge = ArtSizes.OVERLAY,
                        trim = true,
                        alpha = 0.8f,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
        }

        // Panel mô tả và độ khó.
        SlicedArt(res = R.drawable.ui_panel_sub, slice = 0.2f, modifier = Modifier.at(80, 1130, 920, 300))
        StatPanel(character = current, modifier = Modifier.at(80, 1130, 920, 300).padding(horizontal = d.dp(70), vertical = d.dp(36)))
        DesignText(
            text = stringResource(current.quote),
            size = 28,
            italic = true,
            maxLines = 2,
            modifier = Modifier.at(80, 1436, 920, 88),
        )

        GothicButton(
            label = stringResource(R.string.ai_select_start),
            onClick = { onStart(current) },
            enabled = unlocked,
            fontSize = d.sp(40),
            modifier = Modifier.at(150, 1530, 780, 115),
        )
        if (!unlocked) {
            DesignText(
                text = stringResource(R.string.ai_select_locked, current.unlockFloor ?: 0),
                size = 28,
                maxLines = 2,
                modifier = Modifier.at(100, 1660, 880, 90),
            )
        }
    }
}

/** Độ khó (sao `difficulty_star`), phong cách, tốc độ nghĩ, tính cách. */
@Composable
private fun StatPanel(character: AiCharacter, modifier: Modifier = Modifier) {
    val d = LocalDesign.current
    val stars = stringResource(R.string.ai_stars, character.stars)
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(d.dp(6))) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(d.dp(10))) {
            DesignText(stringResource(R.string.ai_stat_difficulty), size = 30, color = GothicColors.Gold, weight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.spacedBy(d.dp(6)),
                // Đọc "2/3 sao" thay vì từng ngôi sao.
                modifier = Modifier.semantics { contentDescription = stars },
            ) {
                repeat(3) { i ->
                    ArtImage(
                        res = R.drawable.difficulty_star,
                        maxEdge = ArtSizes.ICON,
                        trim = true,
                        alpha = if (i < character.stars) 1f else 0.25f,
                        modifier = Modifier.size(d.dp(46)),
                    )
                }
            }
        }
        StatLine(stringResource(R.string.ai_stat_style), stringResource(character.style))
        StatLine(stringResource(R.string.ai_stat_speed), stringResource(R.string.ai_speed_value, character.thinkSeconds))
        StatLine(stringResource(R.string.ai_stat_personality), stringResource(character.personality))
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    DesignText(
        text = "$label: $value",
        size = 27,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 2,
        modifier = Modifier.fillMaxWidth(),
    )
}
