package kma.game.chess2d.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.IOException
import kma.game.chess2d.R
import kma.game.chess2d.engine.*
import kma.game.chess2d.game.PieceOnBoard
import kma.game.chess2d.settings.AppSettings
import kma.game.chess2d.settings.SettingsStore
import kma.game.chess2d.shop.*
import kma.game.chess2d.ui.board.ChessBoard
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.launch

@Composable
fun ShopScreen(earnedGold: Int, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { SettingsStore(context.applicationContext) }
    val state by store.cosmetics.collectAsStateWithLifecycle(initialValue = ShopState())
    val settings by store.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    val scope = rememberCoroutineScope()
    var boards by rememberSaveable { mutableStateOf(true) }
    var pending by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf<Int?>(null) }
    val previewPieces = remember {
        val board = Engine.fromFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        (0 until Squares.COUNT).mapNotNull { square -> board.pieceAt(square).takeUnless { it == Piece.NONE }?.let { PieceOnBoard(square, it, square) } }
    }
    SimpleSetupPage(R.drawable.bg_inventory, stringResource(R.string.shop_title), stringResource(R.string.action_back), onBack, modifier,
        footer = { SimpleSetupButton(stringResource(R.string.action_back), onBack, Modifier.fillMaxWidth()) }) {
        Text(stringResource(R.string.shop_balance, state.availableGold(earnedGold)), color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(stringResource(R.string.shop_hint), color = GothicColors.Muted, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SimpleSetupButton(stringResource(R.string.shop_boards), { boards = true }, Modifier.weight(1f), selected = boards)
            SimpleSetupButton(stringResource(R.string.shop_pieces), { boards = false }, Modifier.weight(1f), selected = !boards)
        }
        if (!state.readable) Text(stringResource(R.string.shop_save_failed), color = GothicColors.Alert)
        notice?.let { Text(stringResource(it), color = GothicColors.Gold, fontSize = 14.sp) }
        ShopCatalog.items.filter { (it.palette != null) == boards }.forEach { item ->
            val owned = item.id in state.owned
            val equipped = item.palette?.let { it == settings.boardPalette } ?: (item.pieces == settings.pieceTheme)
            SimpleSetupCard {
                Text(stringResource(item.palette?.let(::paletteLabel) ?: pieceThemeLabel(requireNotNull(item.pieces))), color = GothicColors.Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                ChessBoard(pieces = previewPieces, selectedSquare = Squares.NONE, legalTargets = emptySet(), lastMoveFrom = Squares.NONE,
                    lastMoveTo = Squares.NONE, checkedKingSquare = Squares.NONE, onSquareTap = {},
                    palette = item.palette ?: settings.boardPalette, pieceTheme = item.pieces ?: settings.pieceTheme,
                    modifier = Modifier.size(192.dp).align(Alignment.CenterHorizontally))
                Text(stringResource(if (owned) R.string.shop_owned else R.string.shop_price, item.goldPrice), color = GothicColors.Muted, fontSize = 14.sp)
                SimpleSetupButton(stringResource(when { equipped -> R.string.shop_equipped; owned -> R.string.shop_apply; else -> R.string.shop_buy }),
                    onClick = {
                        if (!owned) pending = item.id else {
                            busy = true
                            scope.launch {
                                try {
                                    item.palette?.let { store.setBoardPalette(it) }
                                    item.pieces?.let { store.setPieceTheme(it) }
                                    notice = R.string.shop_applied
                                } catch (error: IOException) { notice = R.string.shop_save_failed }
                                finally { busy = false }
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth(), primary = owned && !equipped,
                    enabled = state.readable && !busy && !equipped && (owned || state.availableGold(earnedGold) >= item.goldPrice))
                if (!owned && state.availableGold(earnedGold) < item.goldPrice) Text(stringResource(R.string.shop_insufficient), color = GothicColors.Muted, fontSize = 13.sp)
            }
        }
    }
    pending?.let(ShopCatalog::item)?.let { item ->
        AlertDialog(onDismissRequest = { if (!busy) pending = null }, containerColor = GothicColors.Ink,
            title = { Text(stringResource(R.string.shop_buy), color = GothicColors.Gold) },
            text = { Text(stringResource(R.string.shop_confirm, stringResource(item.palette?.let(::paletteLabel) ?: pieceThemeLabel(requireNotNull(item.pieces))), item.goldPrice), color = GothicColors.Parchment) },
            confirmButton = { TextButton(enabled = !busy, onClick = {
                busy = true
                scope.launch {
                    try {
                        notice = when (store.purchaseCosmetic(item.id, earnedGold)) {
                            PurchaseResult.BOUGHT -> R.string.shop_bought
                            PurchaseResult.ALREADY_OWNED -> R.string.shop_owned
                            PurchaseResult.INSUFFICIENT_GOLD -> R.string.shop_insufficient
                        }
                        pending = null
                    } catch (error: IOException) { notice = R.string.shop_save_failed; pending = null }
                    finally { busy = false }
                }
            }) { Text(stringResource(R.string.shop_buy), color = GothicColors.Gold) } },
            dismissButton = { TextButton(enabled = !busy, onClick = { pending = null }) { Text(stringResource(R.string.action_cancel), color = GothicColors.Muted) } })
    }
}
