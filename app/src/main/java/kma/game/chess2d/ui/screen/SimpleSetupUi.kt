package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.ui.theme.GameFontFamily
import kma.game.chess2d.ui.theme.GothicColors

/** Shared drawn header for the local and spectate setup screens. */
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
) {
    val shape = RoundedCornerShape(12.dp)
    val highlighted = primary || selected == true
    val fill = when {
        primary -> GothicColors.Gold
        selected == true -> GothicColors.Gold.copy(alpha = 0.18f)
        else -> GothicColors.Ink.copy(alpha = 0.8f)
    }
    Box(
        modifier = modifier.height(52.dp).clip(shape).background(fill)
            .border(1.dp, GothicColors.Gold.copy(alpha = if (highlighted) 1f else 0.35f), shape)
            .then(if (selected == null) Modifier.clickable(role = Role.Button, onClick = onClick)
                else Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick))
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = GameFontFamily, fontSize = 16.sp,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            color = if (primary) GothicColors.Ink else if (selected == true) GothicColors.Gold else GothicColors.Parchment,
            textAlign = TextAlign.Center, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
    }
}

