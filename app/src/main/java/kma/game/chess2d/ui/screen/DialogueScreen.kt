package kma.game.chess2d.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.coroutines.delay
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
    var autoMode by rememberSaveable { mutableStateOf(false) }
    val current = index.coerceIn(0, lines.lastIndex)
    val line = lines[current]
    val isLast = current == lines.lastIndex
    val advance = { if (isLast) onFinished() else index = current + 1 }

    // Auto-advance when auto mode is enabled
    LaunchedEffect(autoMode, current) {
        if (autoMode && !isLast) {
            delay(3000) // 3 seconds delay
            advance()
        }
    }

    DialogueScreenContent(
        line = line,
        currentIndex = current,
        totalLines = lines.size,
        isLast = isLast,
        autoMode = autoMode,
        onAdvance = advance,
        onSkip = onFinished,
        onToggleAuto = { autoMode = !autoMode },
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Stateless UI component cho màn thoại.
 */
@Composable
private fun DialogueScreenContent(
    line: DialogueLine,
    currentIndex: Int,
    totalLines: Int,
    isLast: Boolean,
    autoMode: Boolean,
    onAdvance: () -> Unit,
    onSkip: () -> Unit,
    onToggleAuto: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
) {
    DesignCanvas(background = R.drawable.bg_conversation, dim = 0.25f, modifier = modifier) {
        val d = LocalDesign.current
        if (onBack != null) BackIcon(onClick = onBack, modifier = Modifier.at(60, 70, 120, 120))

        // Auto and Skip buttons at top-right (reduced to 120x120)
        ArtImage(
            res = R.drawable.button_auto,
            maxEdge = ArtSizes.ICON,
            contentDescription = "Auto",
            alpha = if (autoMode) 1f else 0.5f,
            modifier = Modifier.at(764, 70, 120, 120).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggleAuto,
            ),
        )
        ArtImage(
            res = R.drawable.button_skip,
            maxEdge = ArtSizes.ICON,
            contentDescription = "Skip",
            modifier = Modifier.at(900, 70, 120, 120).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSkip,
            ),
        )
        ArtImage(
            res = portraitRes(line.speaker),
            maxEdge = ArtSizes.PORTRAIT,
            contentDescription = line.speaker.displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier.at(190, 220, 700, 700),
        )

        // Dialogue box frame with clickable area (960x900, taller)
        ArtImage(
            res = R.drawable.conversation_chat_frame,
            maxEdge = ArtSizes.PIECE,
            contentDescription = "Dialogue frame",
            modifier = Modifier.at(30, 980, 960, 900).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAdvance,
            ),
        )

        // Name plate (900x450, height reduced to 75%)
        ArtImage(
            res = R.drawable.chat_frame,
            maxEdge = ArtSizes.PIECE,
            contentDescription = "Name plate",
            modifier = Modifier.at(-20, 955, 900, 450),
        )

        // Character name
        DesignText(
            text = line.speaker.displayName,
            size = 42,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(-20, 1125, 800, 100),
        )

        // Dialogue text (90% of frame width = 864px)
        // Y-coordinate 1040 controls vertical position - increase this value to move text down
        DesignText(
            text = line.text,
            size = 40,
            align = TextAlign.Start,
            alignment = Alignment.TopStart,
            modifier = Modifier.at(108, 1350, 864, 780),
            color = Color.Black
        )

        // Next button at bottom-right corner of dialogue frame
        ArtImage(
            res = R.drawable.button_next,
            maxEdge = ArtSizes.ICON,
            contentDescription = "Next",
            modifier = Modifier.at(850, 1475, 100, 100).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAdvance,
            ),
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

// Preview data provider
private class DialoguePreviewData : PreviewParameterProvider<DialogueLine> {
    override val values = sequenceOf(
        DialogueLine(Speaker.NARRATOR, "Chào mừng đến với Tháp Chess. Tôi là Người Giữ Tháp."),
        DialogueLine(Speaker.TRAVELER, "Tôi đã đi một quãng đường dài để đến đây. Hãy cho tôi thử thách!"),
        DialogueLine(Speaker.ARIA, "Ta là Aria, Nữ Hoàng Vàng. Ngươi có đủ can đảm để đối mặt với ta không?"),
    )
}

@Preview
@Composable
private fun DialogueScreenPreview(
    @PreviewParameter(DialoguePreviewData::class) line: DialogueLine,
) {
    DialogueScreenContent(
        line = line,
        currentIndex = 0,
        totalLines = 3,
        isLast = false,
        autoMode = false,
        onAdvance = {},
        onSkip = {},
        onToggleAuto = {},
        onBack = {},
    )
}

@Preview
@Composable
private fun DialogueScreenLastLinePreview() {
    DialogueScreenContent(
        line = DialogueLine(Speaker.ARIA, "Trận chiến cuối cùng đang chờ đợi ngươi ở tầng cao nhất!"),
        currentIndex = 2,
        totalLines = 3,
        isLast = true,
        autoMode = false,
        onAdvance = {},
        onSkip = {},
        onToggleAuto = {},
        onBack = null,
    )
}
