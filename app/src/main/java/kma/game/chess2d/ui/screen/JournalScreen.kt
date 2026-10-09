package kma.game.chess2d.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kma.game.chess2d.R
import kma.game.chess2d.profile.Journal
import kma.game.chess2d.profile.JournalState
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.DesignCanvas
import kma.game.chess2d.ui.art.DesignText
import kma.game.chess2d.ui.art.LocalDesign
import kma.game.chess2d.ui.art.SlicedArt
import kma.game.chess2d.ui.art.SubScreenHeader
import kma.game.chess2d.ui.art.at
import kma.game.chess2d.ui.theme.GothicColors

/** Lưới Nhật ký theo bản thiết kế: 3 cột, ô 260, khoảng cách 30, căn giữa khung 1080. */
internal object JournalLayout {
    const val COLUMNS = 3
    const val CELL = 260
    const val GAP = 30

    /** X của cột [col] (120, 410, 700). */
    fun cellX(col: Int): Int = (1080 - (COLUMNS * CELL + (COLUMNS - 1) * GAP)) / 2 + col * (CELL + GAP)

    /** Số hàng cần cho [count] nhân vật. */
    fun rows(count: Int): Int = (count + COLUMNS - 1) / COLUMNS
}

/**
 * Nhật ký nhân vật theo bản thiết kế: nền `bg_journal`, panel `ui_panel_main` (48,190,984,1500),
 * lưới chân dung 3 cột trong `ui_character_frame`; nhân vật khóa có `icon_locked` + `badge_locked`.
 * Chạm một ô mở trang chi tiết: chân dung lớn bên trái, `ui_name_plate` + thông tin bên phải,
 * mô tả đầy đủ bên dưới.
 *
 * Nhân vật chưa gặp hoặc còn khóa bị che tên và ảnh để không lộ trước. Back lúc đang xem một
 * trang là về lưới, back ở lưới là [onBack].
 *
 * @param pages các trang, dựng bởi [Journal.pages].
 */
@Composable
fun JournalScreen(
    pages: List<Journal.Page>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Giữ id thay vì cả trang: dữ liệu trang đổi theo Flow, còn id thì ổn định và lưu được qua xoay máy.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val page = pages.firstOrNull { it.character.id == selectedId }
    BackHandler(enabled = page != null) { selectedId = null }

    DesignCanvas(background = R.drawable.bg_journal, dim = 0.45f, modifier = modifier) {
        SubScreenHeader(
            title = stringResource(R.string.journal_title),
            onBack = { if (page != null) selectedId = null else onBack() },
        )
        SlicedArt(res = R.drawable.ui_panel_main, slice = 0.2f, modifier = Modifier.at(48, 190, 984, 1500))

        if (page == null) {
            DesignText(
                text = stringResource(R.string.journal_progress, Journal.metCount(pages), pages.size),
                size = 32,
                color = GothicColors.Gold,
                maxLines = 1,
                modifier = Modifier.at(90, 220, 900, 60),
            )
            PortraitGrid(pages = pages, onSelect = { selectedId = it.character.id })
        } else {
            PageDetail(page = page)
        }
    }
}

/** Lưới chân dung cuộn được trong panel. */
@Composable
private fun PortraitGrid(pages: List<Journal.Page>, onSelect: (Journal.Page) -> Unit) {
    val d = LocalDesign.current
    Column(
        modifier = Modifier.at(60, 300, 960, 1370).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(d.dp(JournalLayout.GAP)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        pages.chunked(JournalLayout.COLUMNS).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(d.dp(JournalLayout.GAP))) {
                row.forEach { entry -> PortraitCell(page = entry, onClick = { onSelect(entry) }) }
                // Ô trống giữ hàng cuối thẳng cột với các hàng trên.
                repeat(JournalLayout.COLUMNS - row.size) {
                    Box(Modifier.width(d.dp(JournalLayout.CELL)))
                }
            }
        }
    }
}

/** Một ô: chân dung trong khung, dấu trạng thái và tên (hoặc "???"). */
@Composable
private fun PortraitCell(page: Journal.Page, onClick: () -> Unit) {
    val d = LocalDesign.current
    val revealed = page.state == JournalState.MET
    val name = displayName(page)
    Column(
        modifier = Modifier.width(d.dp(JournalLayout.CELL)).clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(d.dp(JournalLayout.CELL))) {
            PortraitInFrame(
                portrait = page.character.portrait,
                contentDescription = if (revealed) name else null,
                dimmed = !revealed,
            )
            StateMark(
                state = page.state,
                modifier = Modifier.align(Alignment.TopEnd).size(d.dp(72)),
            )
            if (page.state == JournalState.LOCKED) {
                ArtImage(
                    res = R.drawable.icon_locked,
                    maxEdge = ArtSizes.ICON,
                    trim = true,
                    modifier = Modifier.align(Alignment.Center).size(d.dp(96)),
                )
            }
        }
        DesignText(
            text = name,
            size = 30,
            color = if (revealed) GothicColors.Parchment else GothicColors.Locked,
            weight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth().height(d.dp(60)),
        )
    }
}

/** Tên hiển thị: nhân vật đã gặp mới lộ tên thật, còn lại là "???". */
@Composable
private fun displayName(page: Journal.Page): String =
    if (page.state == JournalState.MET) {
        stringResource(page.character.name)
    } else {
        stringResource(R.string.ai_hidden_name)
    }

@Composable
private fun stateLabel(state: JournalState): String = when (state) {
    JournalState.MET -> stringResource(R.string.journal_state_met)
    JournalState.UNKNOWN -> stringResource(R.string.journal_state_unknown)
    JournalState.LOCKED -> stringResource(R.string.journal_state_locked)
}

/** Dấu trạng thái ở góc ô: `badge_complete` đã gặp, `badge_new` chưa rõ, `badge_locked` khóa. */
@Composable
private fun StateMark(state: JournalState, modifier: Modifier = Modifier) {
    ArtImage(
        res = when (state) {
            JournalState.MET -> R.drawable.badge_complete
            JournalState.UNKNOWN -> R.drawable.badge_new
            JournalState.LOCKED -> R.drawable.badge_locked
        },
        maxEdge = ArtSizes.ICON,
        trim = true,
        contentDescription = stateLabel(state),
        modifier = modifier,
    )
}

/** Trang chi tiết của một nhân vật. Nội dung lộ ra tùy trạng thái. */
@Composable
private fun PageDetail(page: Journal.Page) {
    val d = LocalDesign.current
    val character = page.character
    val revealed = page.state == JournalState.MET
    val name = displayName(page)

    Box(modifier = Modifier.at(90, 240, 400, 400)) {
        PortraitInFrame(
            portrait = character.portrait,
            contentDescription = if (revealed) name else null,
            dimmed = !revealed,
        )
        if (page.state == JournalState.LOCKED) {
            ArtImage(
                res = R.drawable.icon_locked,
                maxEdge = ArtSizes.ICON,
                trim = true,
                modifier = Modifier.align(Alignment.Center).size(d.dp(130)),
            )
        }
    }

    SlicedArt(res = R.drawable.ui_name_plate, slice = 0.5f, modifier = Modifier.at(510, 260, 490, 100))
    DesignText(
        text = name,
        size = 38,
        color = GothicColors.Gold,
        weight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier.at(550, 260, 410, 100),
    )
    Row(
        modifier = Modifier.at(520, 380, 480, 70),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(d.dp(16)),
    ) {
        StateMark(state = page.state, modifier = Modifier.size(d.dp(60)))
        DesignText(
            text = stateLabel(page.state),
            size = 32,
            color = GothicColors.Parchment,
            align = TextAlign.Start,
            maxLines = 1,
        )
    }
    if (revealed) {
        val stars = stringResource(R.string.ai_stars, character.stars)
        Row(
            modifier = Modifier.at(520, 470, 480, 80).semantics { contentDescription = stars },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(d.dp(10)),
        ) {
            repeat(3) { i ->
                ArtImage(
                    res = R.drawable.difficulty_star,
                    maxEdge = ArtSizes.ICON,
                    trim = true,
                    alpha = if (i < character.stars) 1f else 0.25f,
                    modifier = Modifier.size(d.dp(64)),
                )
            }
        }
    }

    Column(
        modifier = Modifier.at(100, 690, 880, 960).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(d.dp(20)),
    ) {
        when (page.state) {
            JournalState.MET -> MetDetail(page)
            JournalState.UNKNOWN -> BodyText(stringResource(R.string.journal_unknown_hint))
            JournalState.LOCKED -> BodyText(
                stringResource(R.string.ai_select_locked, character.unlockFloor ?: 0),
            )
        }
    }
}

/** Nội dung đầy đủ của nhân vật đã gặp: câu thoại, phong cách, tính cách, lịch sử gặp mặt. */
@Composable
private fun MetDetail(page: Journal.Page) {
    val character = page.character
    DesignText(
        text = stringResource(character.quote),
        size = 32,
        italic = true,
        color = GothicColors.Parchment,
        modifier = Modifier.fillMaxWidth(),
    )
    Separator()
    DetailBlock(stringResource(R.string.ai_stat_style), stringResource(character.style))
    DetailBlock(stringResource(R.string.ai_stat_personality), stringResource(character.personality))
    page.entry?.let { entry ->
        Separator()
        DetailBlock(
            label = stringResource(R.string.journal_history_title),
            value = stringResource(
                R.string.journal_first_met,
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.firstMetAtMillis)),
            ) + "\n" + stringResource(R.string.journal_times_met, entry.timesMet),
        )
    }
}

@Composable
private fun Separator() {
    ArtImage(
        res = R.drawable.ui_separator,
        maxEdge = ArtSizes.BUTTON,
        trim = true,
        modifier = Modifier.fillMaxWidth().height(LocalDesign.current.dp(24)),
    )
}

@Composable
private fun DetailBlock(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        DesignText(
            text = label,
            size = 32,
            color = GothicColors.Gold,
            weight = FontWeight.Bold,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxWidth(),
        )
        DesignText(
            text = value,
            size = 30,
            align = TextAlign.Start,
            alignment = Alignment.CenterStart,
            modifier = Modifier.fillMaxWidth().padding(top = LocalDesign.current.dp(6)),
        )
    }
}

@Composable
private fun BodyText(text: String) {
    DesignText(text = text, size = 32, modifier = Modifier.fillMaxWidth())
}
