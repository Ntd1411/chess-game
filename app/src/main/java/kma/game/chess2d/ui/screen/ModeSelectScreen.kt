package kma.game.chess2d.ui.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtPressable
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.BackIcon
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.ScreenTitle
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Một thẻ chế độ: tranh nền, tên, mô tả và việc cần làm khi chạm. */
private class ModeCard(
    @DrawableRes val art: Int,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val onClick: () -> Unit,
)

/**
 * Bố cục Chọn chế độ (đơn vị thiết kế 1080x1920). Khung `frame_mode_card` sau khi cắt viền trong
 * suốt có tỉ lệ ≈ 4,2:1, thẻ 920x236 giữ gần đúng tỉ lệ đó để hoa văn góc không bị kéo méo.
 */
internal object ModeSelectLayout {
    const val CARD_X = 80
    const val CARD_W = 920
    const val CARD_H = 236
    const val TOP = 220
    const val STEP = 264

    fun card(index: Int): LobbyLayout.Box = LobbyLayout.Box(CARD_X, TOP + index * STEP, CARD_W, CARD_H)

    val Ornament = LobbyLayout.Box(270, 1560, 540, 142)
}

/**
 * Chọn chế độ: nền `bg_mode_select` trong khung `frame_gothic_screen`, năm thẻ ngang. Mỗi thẻ là
 * một tranh `mode_card_*_art` (cắt vừa lòng thẻ) dưới khung `frame_mode_card`; chữ nằm bên trái
 * trên dải tối dần để luôn đọc được dù tranh sáng. Cả thẻ là vùng chạm. Hoa văn
 * `ornament_screen_bottom` khép màn ở dưới.
 */
@Composable
fun ModeSelectScreen(
    onOpenTower: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit,
    onOpenSpectate: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cards = listOf(
        ModeCard(R.drawable.mode_card_story_art, R.string.menu_tower, R.string.mode_story_desc, onOpenTower),
        ModeCard(R.drawable.mode_card_ai_battle_art, R.string.menu_vs_ai, R.string.mode_ai_desc, onPlayComputer),
        ModeCard(R.drawable.mode_card_local_art, R.string.menu_local, R.string.mode_local_desc, onPlayTwoPlayers),
        ModeCard(R.drawable.mode_card_lan_art, R.string.menu_lan, R.string.mode_lan_desc, onPlayLan),
        ModeCard(R.drawable.mode_card_ai_vs_ai_art, R.string.spectate_open, R.string.mode_spectate_desc, onOpenSpectate),
    )
    DesignCanvas(background = R.drawable.bg_mode_select, dim = 0.35f, modifier = modifier) {
        GothicScreenFrame()
        GothicScreenHeader(title = stringResource(R.string.modes_title), onBack = onBack)
        cards.forEachIndexed { i, card -> ModeCardView(ModeSelectLayout.card(i), card) }
        val o = ModeSelectLayout.Ornament
        ArtImage(R.drawable.ornament_screen_bottom, maxEdge = ArtSizes.BUTTON, trim = true,
            modifier = Modifier.at(o.x, o.y, o.w, o.h))
    }
}

@Composable
private fun ModeCardView(box: LobbyLayout.Box, card: ModeCard) {
    val d = LocalDesign.current
    ArtPressable(onClick = card.onClick, modifier = Modifier.at(box.x, box.y, box.w, box.h)) {
        // Tranh nằm trong lòng khung (viền khung dày khoảng 3% ngang, 9% dọc).
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = d.dp(box.w * 0.025f), vertical = d.dp(box.h * 0.08f))
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(d.dp(8))),
        ) {
            ArtImage(card.art, maxEdge = ArtSizes.BUTTON, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(
                modifier = Modifier.fillMaxHeight().fillMaxWidth(0.72f).background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.82f),
                        0.65f to Color.Black.copy(alpha = 0.45f),
                        1f to Color.Transparent,
                    ),
                ),
            )
        }
        ArtImage(R.drawable.frame_mode_card, maxEdge = ArtSizes.BUTTON, trim = true,
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxHeight().fillMaxWidth(0.66f)
                .padding(start = d.dp(56), top = d.dp(34), bottom = d.dp(30)),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            DesignText(
                text = stringResource(card.title),
                size = 42,
                color = GothicColors.Gold,
                weight = FontWeight.Bold,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(),
            )
            DesignText(
                text = stringResource(card.description),
                size = 26,
                align = TextAlign.Start,
                alignment = Alignment.CenterStart,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Khung viền gothic phủ cả màn thiết kế (dùng chung cho Chọn chế độ và Chọn đối thủ). */
@Composable
internal fun GothicScreenFrame() {
    ArtImage(
        R.drawable.frame_gothic_screen,
        maxEdge = ArtSizes.SCREEN,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.at(0, 0, 1080, 1920),
    )
}

/** Đầu màn nằm trong khung gothic: nút back `icon_back` lùi vào so với viền, tiêu đề giữa. */
@Composable
internal fun GothicScreenHeader(title: String, onBack: () -> Unit) {
    BackIcon(onClick = onBack, modifier = Modifier.at(70, 74, 100, 100))
    ScreenTitle(text = title, modifier = Modifier.at(190, 74, 700, 100))
}
