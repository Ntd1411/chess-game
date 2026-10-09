package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.engine.Piece
import kma.game.chess2d.game.LocalSetup
import kma.game.chess2d.game.LocalTimeControl
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.ButtonArt
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.GothicButton
import kma.game.chess2d.ui.art.GothicTextField
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.board.pieceImageRes
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn setup ván hai người cùng máy: tên hai người và thời gian mỗi bên, rồi BẮT ĐẦU.
 *
 * Dựng trên khung thiết kế 1080x1920 như các màn phụ: mỗi người chơi là một tấm `ui_panel_sub`
 * có chân dung, quân Vua của bên mình và ô nhập tên; tấm thứ ba chọn thời gian bằng bốn nút
 * (nút đang chọn là nút chính); nút Bắt đầu ở vị trí nút chính của bản thiết kế.
 *
 * Người chơi 1 luôn cầm Trắng, Người chơi 2 cầm Đen. Ô tên để trống thì dùng tên mặc định.
 * Nội dung các ô nhập dùng `rememberSaveable` để xoay máy không làm mất chữ đang gõ.
 *
 * @param initialWhiteName tên gợi ý cho Người chơi 1 (thường là tên đã nhập ở Sảnh).
 * @param onStart nhận [LocalSetup] đã chuẩn hóa (tên không rỗng, đã cắt độ dài).
 */
@Composable
fun LocalSetupScreen(
    initialWhiteName: String,
    onStart: (LocalSetup) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var whiteInput by rememberSaveable { mutableStateOf(initialWhiteName) }
    var blackInput by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf(LocalTimeControl.UNLIMITED) }

    val defaultWhite = stringResource(R.string.match_player_one)
    val defaultBlack = stringResource(R.string.match_player_two)

    DesignCanvas(background = R.drawable.bg_main_menu, dim = 0.6f, modifier = modifier) {
        SubScreenHeader(title = stringResource(R.string.local_setup_title), onBack = onBack)

        PlayerPanel(
            top = 210,
            title = stringResource(R.string.local_setup_player_one),
            portrait = R.drawable.portrait_protagonist,
            white = true,
            value = whiteInput,
            onValueChange = { whiteInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
            placeholder = defaultWhite,
            imeAction = ImeAction.Next,
        )
        PlayerPanel(
            top = 580,
            title = stringResource(R.string.local_setup_player_two),
            portrait = R.drawable.portrait_unknown_01,
            white = false,
            value = blackInput,
            onValueChange = { blackInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
            placeholder = defaultBlack,
            imeAction = ImeAction.Done,
        )

        SlicedArt(res = R.drawable.ui_panel_sub, slice = 0.25f, modifier = Modifier.at(60, 950, 960, 430))
        DesignText(
            text = stringResource(R.string.local_setup_time),
            size = 38,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.at(100, 985, 880, 70),
        )
        LocalTimeControl.entries.forEachIndexed { i, option ->
            GothicButton(
                label = timeLabel(option),
                onClick = { time = option },
                art = if (option == time) ButtonArt.PRIMARY else ButtonArt.SECONDARY,
                maxLines = 2,
                modifier = Modifier.at(95 + i * 225, 1080, 210, 110),
            )
        }
        DesignText(
            text = stringResource(R.string.local_setup_time_note),
            size = 28,
            color = GothicColors.Parchment.copy(alpha = 0.8f),
            maxLines = 3,
            modifier = Modifier.at(110, 1220, 860, 130),
        )

        GothicButton(
            label = stringResource(R.string.local_setup_start),
            onClick = {
                onStart(
                    LocalSetup.of(
                        whiteInput = whiteInput,
                        blackInput = blackInput,
                        time = time,
                        defaultWhite = defaultWhite,
                        defaultBlack = defaultBlack,
                    ),
                )
            },
            modifier = Modifier.at(150, 1530, 780, 115),
        )
    }
}

/** Một người chơi: chân dung + quân Vua của bên mình, tiêu đề, ô nhập tên và dòng gợi ý. */
@Composable
private fun PlayerPanel(
    top: Int,
    title: String,
    @DrawableRes portrait: Int,
    white: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
) {
    SlicedArt(res = R.drawable.ui_panel_sub, slice = 0.25f, modifier = Modifier.at(60, top, 960, 330))
    Box(modifier = Modifier.at(100, top + 55, 200, 200)) {
        PortraitInFrame(portrait = portrait)
    }
    ArtImage(
        res = pieceImageRes(Piece.of(Piece.KING, white)),
        maxEdge = ArtSizes.PIECE,
        trim = true,
        modifier = Modifier.at(235, top + 190, 80, 80),
    )
    DesignText(
        text = title,
        size = 36,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 1,
        modifier = Modifier.at(340, top + 35, 640, 70),
    )
    GothicTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        modifier = Modifier.at(330, top + 115, 660, 110),
    )
    DesignText(
        text = stringResource(R.string.local_setup_name_hint),
        size = 26,
        color = GothicColors.Parchment.copy(alpha = 0.75f),
        align = TextAlign.Start,
        alignment = Alignment.CenterStart,
        maxLines = 2,
        modifier = Modifier.at(340, top + 235, 640, 70),
    )
}

/** Nhãn ngắn của một mức thời gian: "Không giới hạn" hoặc "N phút". */
@Composable
internal fun timeLabel(time: LocalTimeControl): String =
    if (time.limited) {
        stringResource(R.string.local_time_minutes, time.minutes)
    } else {
        stringResource(R.string.local_time_unlimited)
    }
