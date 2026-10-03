package kma.game.chess2d.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kma.game.chess2d.R
import kma.game.chess2d.ui.art.ArtBackdrop
import kma.game.chess2d.ui.art.ArtImage
import kma.game.chess2d.ui.art.ArtSizes
import kma.game.chess2d.ui.art.PlateButton
import kma.game.chess2d.ui.theme.GothicColors

/** Kích thước các nút chế độ ở sảnh: hai chế độ chính phải to hơn ba chế độ phụ. */
internal object LobbyLayout {
    val PrimaryButtonHeight = 92.dp

    /** Cũng là vùng chạm tối thiểu: ảnh nút phụ nhỏ hơn thế, nhưng chạm vào đâu trong hộp 48dp cũng ăn. */
    val SecondaryButtonHeight = 48.dp
}

/**
 * Sảnh chính: tên người chơi và nút cài đặt ở trên, nhân vật ở giữa, năm chế độ ở dưới.
 *
 * **Khám Phá Tháp Cờ** và **Người vs Máy** là hai chế độ chính nên to hơn; hai người cùng
 * máy, LAN và Máy vs Máy là ba chế độ phụ. Cấp độ máy không còn chọn ở đây mà chọn ở màn
 * Chọn đối thủ AI. Tên người chơi, âm thanh và giao diện bàn cờ nằm trong hộp thoại Cài đặt.
 * Các nút vẽ bằng ảnh bảng gothic ([PlateButton]) thay cho nút Material mặc định.
 *
 * Lối tắt Nhật ký nằm cạnh Lịch sử ở cuối. Túi đồ và Thành tựu chưa có vì các màn đó chưa làm,
 * và một nút bấm không dẫn đi đâu là ngõ cụt.
 */
@Composable
fun MenuScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onOpenTower: () -> Unit,
    onPlayComputer: () -> Unit,
    onPlayTwoPlayers: () -> Unit,
    onPlayLan: () -> Unit,
    onOpenSpectate: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSettings by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        // Phủ tối nhẹ để tên và nút luôn đọc được trên mọi vùng của ảnh nền.
        ArtBackdrop(res = R.drawable.screen_main_menu, dim = 0.4f)

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        ) {
            LobbyHeader(
                name = name,
                onOpenProfile = onOpenProfile,
                onOpenSettings = { showSettings = true },
            )

            // Nhân vật chiếm phần còn lại giữa màn hình, Fit để không bao giờ bị cắt đầu/chân.
            // Quầng đỏ máu phía sau tách nhân vật khỏi nền. Chạm vào nhân vật mở Hồ sơ.
            val profileLabel = stringResource(R.string.profile_open)
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.radialGradient(
                            colors = listOf(GothicColors.Blood.copy(alpha = 0.55f), Color.Transparent),
                        ),
                    ),
                )
                ArtImage(
                    res = R.drawable.char_protagonist_full_body,
                    maxEdge = ArtSizes.BOARD,
                    contentDescription = profileLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .clickable(onClick = onOpenProfile),
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                PrimaryModeButton(
                    label = stringResource(R.string.tower_open),
                    onClick = onOpenTower,
                )
                PrimaryModeButton(
                    label = stringResource(R.string.mode_vs_computer),
                    onClick = onPlayComputer,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    SecondaryModeButton(
                        label = stringResource(R.string.mode_two_players),
                        onClick = onPlayTwoPlayers,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryModeButton(
                        label = stringResource(R.string.mode_lan),
                        onClick = onPlayLan,
                        modifier = Modifier.weight(1f),
                    )
                    SecondaryModeButton(
                        label = stringResource(R.string.spectate_open),
                        onClick = onOpenSpectate,
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onOpenJournal, modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.journal_open),
                            color = GothicColors.Parchment,
                        )
                    }
                    TextButton(onClick = onOpenHistory, modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.history_open),
                            color = GothicColors.Parchment,
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsPanel(
            name = name,
            onNameChange = onNameChange,
            onDismiss = { showSettings = false },
        )
    }
}

/** Hàng trên cùng: avatar trong khung vàng và tên người chơi bên trái (chạm để mở Hồ sơ), nút cài đặt bên phải. */
@Composable
private fun LobbyHeader(name: String, onOpenProfile: () -> Unit, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f).clip(CircleShape).clickable(onClick = onOpenProfile),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Khung có phần giữa trong suốt: avatar nhỏ hơn khung để lọt vào lỗ tròn.
            Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                PlayerAvatar(name = name, size = 38.dp)
                ArtImage(
                    res = R.drawable.ui_avatar_frame,
                    maxEdge = ArtSizes.PIECE,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                text = name.ifBlank { stringResource(R.string.match_player_one) },
                style = MaterialTheme.typography.titleMedium,
                color = GothicColors.Parchment,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
        }
        ArtImage(
            res = R.drawable.icon_settings,
            maxEdge = ArtSizes.ICON,
            contentDescription = stringResource(R.string.lobby_settings),
            modifier = Modifier.size(44.dp).clip(CircleShape).clickable(onClick = onOpenSettings),
        )
    }
}

/** Nút chế độ chính: bảng gothic lớn, có ảnh trạng thái nhấn riêng. */
@Composable
private fun PrimaryModeButton(label: String, onClick: () -> Unit) {
    PlateButton(
        label = label,
        onClick = onClick,
        normalRes = R.drawable.ui_menu_button_primary_wide,
        pressedRes = R.drawable.ui_menu_button_primary_wide_pressed,
        fontSize = 20.sp,
        textPadding = 56.dp,
        modifier = Modifier.fillMaxWidth().height(LobbyLayout.PrimaryButtonHeight),
    )
}

/**
 * Nút chế độ phụ: bảng đá nhỏ hơn. Cho phép xuống hai dòng vì ba nút chia chung một hàng.
 * Ảnh pressed của nút phụ có nền đặc nên không dùng; nhấn thì làm tối ảnh thường.
 */
@Composable
private fun SecondaryModeButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PlateButton(
        label = label,
        onClick = onClick,
        normalRes = R.drawable.ui_menu_button_secondary_wide,
        fontSize = 11.sp,
        maxLines = 2,
        textColor = GothicColors.Parchment,
        textPadding = 16.dp,
        modifier = modifier.height(LobbyLayout.SecondaryButtonHeight),
    )
}
