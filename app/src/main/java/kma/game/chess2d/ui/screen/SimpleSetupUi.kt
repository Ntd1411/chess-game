package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Header and footer stay at the safe edges while the middle content scrolls. */
@Composable
internal fun SimpleSetupPage(
    @DrawableRes background: Int,
    title: String,
    backLabel: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    keyboardAware: Boolean = false,
    footer: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtBackdrop(background, dim = 0.6f)
        Column(
            modifier = Modifier.fillMaxSize().safeDrawingPadding()
                .then(if (keyboardAware) Modifier.imePadding() else Modifier)
                .padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SimpleSetupHeader(title, backLabel, onBack, titleSize = 18.sp)
            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 12.dp)) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp)
                        .fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    content = content,
                )
            }
            Column(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp), content = footer)
        }
    }
}

@Composable
internal fun SimpleSetupCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Column(modifier = modifier.fillMaxWidth().clip(shape)
        .background(GothicColors.Ink.copy(alpha = 0.86f))
        .border(1.dp, GothicColors.Gold.copy(alpha = 0.4f), shape).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}

/** Shared drawn header for setup screens. */
@Composable
internal fun SimpleSetupHeader(
    title: String,
    backLabel: String,
    onBack: () -> Unit,
    titleSize: TextUnit = 22.sp,
) {
    Row(modifier = Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = backLabel, tint = GothicColors.Gold)
        }
        Text(title, modifier = Modifier.weight(1f), color = GothicColors.Gold,
            fontFamily = GameFontFamily, fontWeight = FontWeight.Bold, fontSize = titleSize,
            textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.size(48.dp))
    }
}

/** Simple Compose-drawn buttons; selected difficulty remains exposed to accessibility. */
@Composable
internal fun SimpleSetupButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    selected: Boolean? = null,
    maxLines: Int = 1,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(12.dp)
    val highlighted = primary || selected == true
    val fill = when {
        primary -> GothicColors.Gold
        selected == true -> GothicColors.Gold.copy(alpha = 0.18f)
        else -> GothicColors.Ink.copy(alpha = 0.8f)
    }
    Box(
        modifier = modifier.height(52.dp).alpha(if (enabled) 1f else 0.45f).clip(shape).background(fill)
            .border(1.dp, GothicColors.Gold.copy(alpha = if (highlighted) 1f else 0.35f), shape)
            .then(if (selected == null) Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                else Modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = GameFontFamily, fontSize = 16.sp,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (primary) GothicColors.Ink else if (selected == true) GothicColors.Gold else GothicColors.Parchment,
            textAlign = TextAlign.Center, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

