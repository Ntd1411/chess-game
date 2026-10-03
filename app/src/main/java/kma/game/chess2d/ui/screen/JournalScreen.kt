package kma.game.chess2d.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kma.game.chess2d.R
import kma.game.chess2d.profile.Journal
import kma.game.chess2d.profile.JournalState
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Nhật ký nhân vật: danh sách nhân vật (✓ đã gặp / ? chưa rõ / 🔒 khóa) và trang chi tiết.
 *
 * Nhân vật chưa gặp hoặc còn khóa bị che tên và ảnh để không lộ trước. Trang chi tiết của
 * nhân vật đã gặp có ảnh, tên, câu thoại, phong cách, tính cách, độ khó và lịch sử gặp mặt.
 * Chưa có "bí mật đã phát hiện" vì chưa có hệ thống bí mật nào để ghi nhận.
 *
 * Back lúc đang xem một trang là về danh sách, back ở danh sách là [onBack].
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

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_character_journal),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Phủ tối để chữ đọc được trên mọi vùng của ảnh nền.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_back),
                    contentDescription = stringResource(R.string.journal_back),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { if (page != null) selectedId = null else onBack() },
                )
                Text(
                    text = stringResource(R.string.journal_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = GothicColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (page == null) {
                    Text(
                        text = stringResource(
                            R.string.journal_progress,
                            Journal.metCount(pages),
                            pages.size,
                        ),
                        style = MaterialTheme.typography.labelLarge,
                        color = GothicColors.Parchment,
                    )
                    pages.forEach { entry ->
                        PageRow(page = entry, onClick = { selectedId = entry.character.id })
                    }
                } else {
                    PageDetail(page = page)
                }
            }
        }
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

/** Dấu trạng thái: ✓ đã gặp, ? chưa rõ, ổ khóa là khóa. */
@Composable
private fun StateMark(state: JournalState) {
    val label = stateLabel(state)
    if (state == JournalState.LOCKED) {
        Image(
            painter = painterResource(R.drawable.icon_locked),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
        )
    } else {
        Text(
            text = if (state == JournalState.MET) "\u2713" else "?",
            style = MaterialTheme.typography.titleMedium,
            color = GothicColors.Gold,
            fontWeight = FontWeight.Bold,
            // Đọc "Đã gặp"/"Chưa rõ" thay vì đọc ký tự.
            modifier = Modifier.semantics { contentDescription = label },
        )
    }
}

/** Một dòng trong danh sách: ảnh tròn, tên, dấu trạng thái. */
@Composable
private fun PageRow(page: Journal.Page, onClick: () -> Unit) {
    val revealed = page.state == JournalState.MET
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(page.character.portrait),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(1.dp, if (revealed) GothicColors.Gold else GothicColors.Locked, CircleShape)
                .alpha(if (revealed) 1f else 0.3f),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName(page),
                style = MaterialTheme.typography.titleMedium,
                color = GothicColors.Parchment,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            Text(
                text = stateLabel(page.state),
                style = MaterialTheme.typography.labelMedium,
                color = GothicColors.Parchment.copy(alpha = 0.7f),
            )
        }
        StateMark(page.state)
    }
}

/** Trang chi tiết của một nhân vật. Nội dung lộ ra tùy trạng thái. */
@Composable
private fun PageDetail(page: Journal.Page) {
    val character = page.character
    val revealed = page.state == JournalState.MET

    Box(modifier = Modifier.height(260.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(character.portrait),
            contentDescription = if (revealed) stringResource(character.name) else null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize().alpha(if (revealed) 1f else 0.3f),
        )
        if (page.state == JournalState.LOCKED) {
            Image(
                painter = painterResource(R.drawable.icon_locked),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )
        }
    }
    Text(
        text = displayName(page),
        style = MaterialTheme.typography.headlineSmall,
        color = GothicColors.Parchment,
        fontWeight = FontWeight.Bold,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StateMark(page.state)
        Text(
            text = stateLabel(page.state),
            style = MaterialTheme.typography.labelLarge,
            color = GothicColors.Gold,
        )
    }

    when (page.state) {
        JournalState.MET -> MetDetail(page)
        JournalState.UNKNOWN -> HintText(stringResource(R.string.journal_unknown_hint))
        JournalState.LOCKED -> HintText(
            stringResource(R.string.ai_select_locked, character.unlockFloor ?: 0),
        )
    }
}

@Composable
private fun HintText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = GothicColors.Parchment,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    )
}

/** Nội dung đầy đủ của nhân vật đã gặp: câu thoại, thông tin, lịch sử gặp mặt. */
@Composable
private fun MetDetail(page: Journal.Page) {
    val character = page.character
    val stars = stringResource(R.string.ai_stars, character.stars)
    Text(
        text = stringResource(character.quote),
        style = MaterialTheme.typography.bodyMedium,
        color = GothicColors.Parchment,
        fontStyle = FontStyle.Italic,
        textAlign = TextAlign.Center,
    )
    DetailCard {
        DetailRow(label = stringResource(R.string.ai_stat_difficulty)) {
            Text(
                text = "\u2605".repeat(character.stars) + "\u2606".repeat(3 - character.stars),
                color = GothicColors.Gold,
                style = MaterialTheme.typography.titleMedium,
                // Đọc "2/3 sao" thay vì đọc từng ký tự sao.
                modifier = Modifier.semantics { contentDescription = stars },
            )
        }
        DetailRow(label = stringResource(R.string.ai_stat_style)) {
            DetailValue(stringResource(character.style))
        }
        DetailRow(label = stringResource(R.string.ai_stat_personality)) {
            DetailValue(stringResource(character.personality))
        }
    }
    page.entry?.let { entry ->
        DetailCard {
            DetailRow(label = stringResource(R.string.journal_history_title)) {
                DetailValue(
                    stringResource(
                        R.string.journal_first_met,
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            .format(Date(entry.firstMetAtMillis)),
                    ),
                )
                DetailValue(stringResource(R.string.journal_times_met, entry.timesMet))
            }
        }
    }
}

@Composable
private fun DetailCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, GothicColors.Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = GothicColors.Gold,
        )
        value()
    }
}

@Composable
private fun DetailValue(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = GothicColors.Parchment,
    )
}
