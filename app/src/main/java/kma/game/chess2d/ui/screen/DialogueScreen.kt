package kma.game.chess2d.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.campaign.DialogueLine
import kma.game.chess2d.campaign.Speaker
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.BackIcon
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn thoại theo bản thiết kế: nền `bg_dialogue`, chân dung vuông của người nói (700x700, giữ 1:1),
 * hộp thoại `ui_dialogue_box` ở dưới với bảng tên chồng lên mép trên, chữ căn trái.
 *
 * Chạm vào hộp thoại cũng là tiếp tục. [onFinished] chạy khi hết thoại hoặc khi bấm Bỏ qua;
 * [onBack] (nếu có) hiện nút `icon_back` ở góc trái trên.
 */
@Composable
fun DialogueScreen(
    lines: List<DialogueLine>,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    // Không có thoại thì không có gì để hiện: đi tiếp luôn thay vì kẹt ở màn trống.
    if (lines.isEmpty()) {
        LaunchedEffect(Unit) { onFinished() }
        return
    }

    var index by rememberSaveable { mutableIntStateOf(0) }
    val current = index.coerceIn(0, lines.lastIndex)
    val line = lines[current]
    val isLast = current == lines.lastIndex
    val advance = { if (isLast) onFinished() else index = current + 1 }
    DesignCanvas(background = R.drawable.bg_dialogue, dim = 0.25f, modifier = modifier) {
        val d = LocalDesign.current
        if (onBack != null) BackIcon(onClick = onBack, modifier = Modifier.at(60, 70, 80, 80))

        ArtImage(R.drawable.decor_dialogue_top, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.at(340, 110, 400, 110))
        ArtImage(
            res = portraitRes(line.speaker),
            maxEdge = ArtSizes.PORTRAIT,
            contentDescription = line.speaker.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(190, 220, 700, 700),
        )

        // Hộp thoại: chạm vào đâu cũng là tiếp.
        SlicedArt(
            res = R.drawable.ui_dialogue_box,
            slice = 0.5f,
            modifier = Modifier.at(48, 1240, 984, 560).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = advance,
            ),
        )
        ArtImage(R.drawable.decor_dialogue_bottom, maxEdge = ArtSizes.PIECE, trim = true, modifier = Modifier.at(390, 1770, 300, 80))
        SlicedArt(res = R.drawable.ui_name_plate, slice = 0.5f, modifier = Modifier.at(80, 1190, 420, 90))
        DesignText(
            text = line.speaker.displayName,
            size = 36,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(120, 1190, 340, 90),
        )
        DesignText(
            text = line.text,
            size = 36,
            align = TextAlign.Start,
            alignment = Alignment.TopStart,
            modifier = Modifier.at(100, 1300, 860, 360),
        )

        GothicButton(
            label = stringResource(R.string.dialogue_skip),
            onClick = onFinished,
            art = ButtonArt.CHOICE,
            fontSize = d.sp(28),
            modifier = Modifier.at(100, 1672, 280, 84),
        )
        DesignText(
            text = "${current + 1}/${lines.size}",
            size = 28,
            color = GothicColors.Parchment.copy(alpha = 0.7f),
            modifier = Modifier.at(400, 1672, 280, 84),
        )
        GothicButton(
            label = stringResource(if (isLast) R.string.dialogue_continue else R.string.dialogue_next),
            onClick = advance,
            art = ButtonArt.SMALL,
            fontSize = d.sp(30),
            modifier = Modifier.at(680, 1672, 300, 84),
        )
    }
}

/** Chân dung vuông của người nói; giọng dẫn chuyện của tháp là Người Giữ Tháp. */
private fun portraitRes(speaker: Speaker): Int = when (speaker) {
    Speaker.NARRATOR -> R.drawable.portrait_tower_keeper
    Speaker.TRAVELER -> R.drawable.portrait_protagonist
    Speaker.PLAYER_48 -> R.drawable.portrait_unknown_03
    Speaker.ARIA -> R.drawable.portrait_golden_queen
}
