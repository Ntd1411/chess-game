package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.opponent.AiCharacter
import kma.game.chess2d.opponent.AiCharacters
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Circular navigation also lets locked opponents be previewed. */
internal object AiSelectionNavigation {
    fun previous(index: Int, count: Int): Int {
        require(count > 0)
        return Math.floorMod(index - 1, count)
    }

    fun next(index: Int, count: Int): Int {
        require(count > 0)
        return Math.floorMod(index + 1, count)
    }
}

/** Portrait-only setup cards share character identity and unlock rules with the tower. */
@Composable
fun AiSelectionScreen(
    clearedUpTo: Int,
    onStart: (AiCharacter) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val characters = AiCharacters.all
    var index by rememberSaveable { mutableIntStateOf(0) }
    val selectedIndex = index.coerceIn(characters.indices)
    val current = characters[selectedIndex]
    val unlocked = current.isUnlocked(clearedUpTo)
    val carousel = rememberLazyListState()
    LaunchedEffect(selectedIndex) { carousel.animateScrollToItem(selectedIndex) }

    SimpleSetupPage(
        background = R.drawable.bg_ai_select,
        title = stringResource(R.string.ai_select_title),
        backLabel = stringResource(R.string.ai_select_back),
        onBack = onBack,
        modifier = modifier,
        footer = {
            SimpleSetupButton(stringResource(R.string.ai_select_start),
                onClick = { if (unlocked) onStart(current) }, primary = true,
                enabled = unlocked, modifier = Modifier.fillMaxWidth())
            SimpleSetupButton(stringResource(R.string.ai_select_back), onBack,
                modifier = Modifier.fillMaxWidth())
        },
    ) {
        SimpleSetupCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { index = AiSelectionNavigation.previous(selectedIndex, characters.size) },
                    modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.ai_select_prev),
                        tint = GothicColors.Gold)
                }
                Box(modifier = Modifier.weight(1f).height(200.dp), contentAlignment = Alignment.Center) {
                    ArtImage(res = current.portrait, maxEdge = ArtSizes.PORTRAIT,
                        contentDescription = stringResource(current.name),
                        contentScale = ContentScale.Crop, alpha = if (unlocked) 1f else 0.4f,
                        modifier = Modifier.size(168.dp).clip(RoundedCornerShape(16.dp)))
                    if (!unlocked) Icon(Icons.Default.Lock,
                        stringResource(R.string.ai_select_locked, current.unlockFloor ?: 0),
                        tint = GothicColors.Gold, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { index = AiSelectionNavigation.next(selectedIndex, characters.size) },
                    modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, stringResource(R.string.ai_select_next),
                        tint = GothicColors.Gold)
                }
            }
            Text(stringResource(current.name), modifier = Modifier.fillMaxWidth(),
                color = GothicColors.Gold, fontFamily = GameFontFamily,
                fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            val starsDescription = stringResource(R.string.ai_stars, current.stars)
            Row(modifier = Modifier.fillMaxWidth().semantics { contentDescription = starsDescription },
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { star ->
                    Icon(Icons.Default.Star, contentDescription = null,
                        tint = GothicColors.Gold.copy(alpha = if (star < current.stars) 1f else 0.25f),
                        modifier = Modifier.size(22.dp))
                }
                Text(stringResource(labelOf(current.difficulty)), color = GothicColors.Parchment,
                    fontFamily = GameFontFamily, fontSize = 14.sp)
            }
        }

        LazyRow(state = carousel, modifier = Modifier.fillMaxWidth().selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(characters, key = { _, character -> character.id }) { position, character ->
                CharacterThumb(character, selected = position == selectedIndex,
                    unlocked = character.isUnlocked(clearedUpTo), onClick = { index = position })
            }
        }

        SimpleSetupCard {
            StatLine(stringResource(R.string.ai_stat_style), stringResource(current.style))
            StatLine(stringResource(R.string.ai_stat_speed), stringResource(R.string.ai_speed_value, current.thinkSeconds))
            StatLine(stringResource(R.string.ai_stat_personality), stringResource(current.personality))
            Text(if (unlocked) stringResource(current.quote)
                else stringResource(R.string.ai_select_locked, current.unlockFloor ?: 0),
                modifier = Modifier.fillMaxWidth(), fontFamily = GameFontFamily, fontSize = 15.sp,
                color = if (unlocked) GothicColors.Parchment else GothicColors.Gold,
                fontStyle = if (unlocked) FontStyle.Italic else FontStyle.Normal)
        }
    }
}

@Composable
private fun CharacterThumb(character: AiCharacter, selected: Boolean, unlocked: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = Modifier.size(64.dp).clip(shape).background(GothicColors.Ink)
        .border(if (selected) 2.dp else 1.dp,
            GothicColors.Gold.copy(alpha = if (selected) 1f else 0.3f), shape)
        .selectable(selected = selected, role = Role.RadioButton, onClick = onClick).padding(4.dp),
        contentAlignment = Alignment.Center) {
        ArtImage(character.portrait, maxEdge = ArtSizes.PORTRAIT,
            contentDescription = stringResource(character.name), contentScale = ContentScale.Crop,
            alpha = if (unlocked) 1f else 0.35f,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
        if (!unlocked) Icon(Icons.Default.Lock,
            stringResource(R.string.ai_select_locked, character.unlockFloor ?: 0),
            tint = GothicColors.Gold, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Text("$label: $value", modifier = Modifier.fillMaxWidth(),
        color = GothicColors.Parchment, fontFamily = GameFontFamily, fontSize = 15.sp)
}
