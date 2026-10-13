package kma.game.chess2d.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kma.game.chess2d.R
import kma.game.chess2d.profile.Journal
import kma.game.chess2d.profile.JournalState
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

internal object JournalLayout {
    const val MIN_CELL_DP = 144
    const val GAP_DP = 12
    const val MAX_WIDTH_DP = 560
    const val NAME_LINES = 2
    const val FOOTER_LINES = 1

    fun columnsFor(availableWidthDp: Float): Int =
        ((availableWidthDp + GAP_DP) / (MIN_CELL_DP + GAP_DP)).toInt().coerceIn(1, 3)
}

/** Unmet characters keep their identity hidden in both cards and detail pages. */
internal fun journalNameRes(page: Journal.Page): Int =
    if (page.state == JournalState.MET) page.character.name else R.string.ai_hidden_name

internal fun journalPortraitRes(page: Journal.Page): Int? =
    page.character.portrait.takeIf { page.state == JournalState.MET }

internal fun journalUnlockFloor(page: Journal.Page): Int? =
    page.character.unlockFloor.takeIf { page.state == JournalState.LOCKED }

internal fun journalStateLabel(state: JournalState): Int = when (state) {
    JournalState.MET -> R.string.journal_state_met
    JournalState.UNKNOWN -> R.string.journal_state_unknown
    JournalState.LOCKED -> R.string.journal_state_locked
}

/** Responsive drawn cards and details, preserving encounter data and nested Back behavior. */
@Composable
fun JournalScreen(pages: List<Journal.Page>, onBack: () -> Unit, modifier: Modifier = Modifier) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val page = pages.firstOrNull { it.character.id == selectedId }
    val gridState = rememberLazyGridState()
    val back = { if (page != null) selectedId = null else onBack() }
    BackHandler(enabled = page != null) { selectedId = null }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(R.drawable.bg_journal, dim = 0.6f)
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()
            .padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            SimpleSetupHeader(stringResource(R.string.journal_title), stringResource(R.string.journal_back), back,
                titleSize = 18.sp)
            Text(stringResource(R.string.journal_progress, Journal.metCount(pages), pages.size),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                color = GothicColors.Muted, fontFamily = GameFontFamily, fontSize = 14.sp,
                textAlign = TextAlign.Center)
            BoxWithConstraints(modifier = Modifier.weight(1f).widthIn(max = JournalLayout.MAX_WIDTH_DP.dp)
                .fillMaxWidth()) {
                if (page == null) {
                    LazyVerticalGrid(columns = GridCells.Fixed(JournalLayout.columnsFor(maxWidth.value)),
                        state = gridState, modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(JournalLayout.GAP_DP.dp),
                        verticalArrangement = Arrangement.spacedBy(JournalLayout.GAP_DP.dp)) {
                        items(pages, key = { it.character.id }) { entry ->
                            PortraitCell(entry, onClick = { selectedId = entry.character.id })
                        }
                    }
                } else {
                    key(page.character.id) { PageDetail(page) }
                }
            }
            SimpleSetupButton(stringResource(R.string.journal_back), back,
                modifier = Modifier.widthIn(max = JournalLayout.MAX_WIDTH_DP.dp).fillMaxWidth().padding(top = 12.dp))
        }
    }
}

@Composable
private fun PortraitCell(page: Journal.Page, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = Modifier.fillMaxWidth().clip(shape).background(GothicColors.Ink.copy(alpha = 0.9f))
        .border(1.dp, GothicColors.Gold.copy(alpha = 0.35f), shape)
        .clickable(role = Role.Button, onClick = onClick).padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)) {
        JournalPortrait(page, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        Text(stringResource(journalNameRes(page)), modifier = Modifier.fillMaxWidth(),
            fontFamily = GameFontFamily, color = GothicColors.Parchment, fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center,
            minLines = JournalLayout.NAME_LINES, maxLines = JournalLayout.NAME_LINES,
            lineHeight = 20.sp, overflow = TextOverflow.Ellipsis)
        StateBadge(page.state)
        // Reserve the same footer line even when there is no unlock condition.
        val unlockFloor = journalUnlockFloor(page)
        Text(if (unlockFloor != null) stringResource(R.string.journal_unlock_floor, unlockFloor) else " ",
            modifier = Modifier.fillMaxWidth(), color = GothicColors.Muted,
            fontFamily = GameFontFamily, fontSize = 12.sp, lineHeight = 16.sp,
            minLines = JournalLayout.FOOTER_LINES, maxLines = JournalLayout.FOOTER_LINES,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun JournalPortrait(page: Journal.Page, modifier: Modifier) {
    val portrait = journalPortraitRes(page)
    Box(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(GothicColors.Gold.copy(alpha = 0.07f)),
        contentAlignment = Alignment.Center) {
        if (portrait != null) {
            ArtImage(portrait, maxEdge = ArtSizes.PORTRAIT, contentScale = ContentScale.Crop,
                contentDescription = null, modifier = Modifier.fillMaxSize())
        } else if (page.state == JournalState.LOCKED) {
            Icon(Icons.Default.Lock, contentDescription = null, tint = GothicColors.Muted, modifier = Modifier.size(32.dp))
        } else {
            Text("?", color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 42.sp)
        }
    }
}

@Composable
private fun StateBadge(state: JournalState) {
    Text(stringResource(journalStateLabel(state)),
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GothicColors.Gold.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        fontFamily = GameFontFamily, fontSize = 12.sp, lineHeight = 16.sp,
        maxLines = 1, overflow = TextOverflow.Ellipsis,
        color = if (state == JournalState.MET) GothicColors.Gold else GothicColors.Muted)
}

@Composable
private fun PageDetail(page: Journal.Page) {
    val character = page.character
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SimpleSetupCard {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JournalPortrait(page, modifier = Modifier.size(144.dp))
                Text(stringResource(journalNameRes(page)), color = GothicColors.Gold,
                    fontFamily = GameFontFamily, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
                StateBadge(page.state)
                if (page.state == JournalState.MET) {
                    val description = stringResource(R.string.ai_stars, character.stars)
                    Row(modifier = Modifier.semantics { contentDescription = description },
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(3) { star ->
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(22.dp),
                                tint = GothicColors.Gold.copy(alpha = if (star < character.stars) 1f else 0.25f))
                        }
                    }
                }
            }
        }
        when (page.state) {
            JournalState.MET -> {
                SimpleSetupCard {
                    Text(stringResource(character.quote), color = GothicColors.Parchment,
                        fontFamily = GameFontFamily, fontSize = 16.sp, fontStyle = FontStyle.Italic)
                }
                SimpleSetupCard {
                    DetailBlock(stringResource(R.string.ai_stat_difficulty), stringResource(labelOf(character.difficulty)))
                    DetailBlock(stringResource(R.string.ai_stat_style), stringResource(character.style))
                    DetailBlock(stringResource(R.string.ai_stat_personality), stringResource(character.personality))
                    DetailBlock(stringResource(R.string.ai_stat_speed), stringResource(R.string.ai_speed_value, character.thinkSeconds))
                }
                page.entry?.let { entry ->
                    SimpleSetupCard {
                        DetailBlock(stringResource(R.string.journal_history_title),
                            stringResource(R.string.journal_first_met,
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(entry.firstMetAtMillis))) +
                                "\n" + stringResource(R.string.journal_times_met, entry.timesMet))
                    }
                }
            }
            JournalState.UNKNOWN -> SimpleSetupCard { BodyText(stringResource(R.string.journal_unknown_hint)) }
            JournalState.LOCKED -> SimpleSetupCard {
                BodyText(stringResource(R.string.ai_select_locked, character.unlockFloor ?: 0))
            }
        }
    }
}

@Composable
private fun DetailBlock(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = GothicColors.Gold, fontFamily = GameFontFamily, fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold)
        BodyText(value)
    }
}

@Composable
private fun BodyText(text: String) {
    Text(text, color = GothicColors.Parchment, fontFamily = GameFontFamily, fontSize = 15.sp, lineHeight = 22.sp)
}
