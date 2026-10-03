package kma.game.chess2d.ui.screen

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kma.game.chess2d.R
import kma.game.chess2d.game.LocalSetup
import kma.game.chess2d.game.LocalTimeControl
import kma.game.chess2d.ui.theme.GothicColors

/**
 * Màn setup ván hai người cùng máy: tên hai người và thời gian mỗi bên, rồi BẮT ĐẦU.
 *
 * Người chơi 1 luôn cầm Trắng, Người chơi 2 cầm Đen (spec: "chọn quân Trắng"/"chọn quân Đen" cho
 * từng người). Ô tên để trống thì dùng tên mặc định, nên không bắt buộc nhập gì mới chơi được.
 *
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

    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        Image(
            painter = painterResource(R.drawable.screen_main_menu),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // Nền chỉ là phông mờ: phủ tối để form là trọng tâm.
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f)))

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
                    contentDescription = stringResource(R.string.local_setup_back),
                    modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onBack),
                )
                Text(
                    text = stringResource(R.string.local_setup_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = GothicColors.Gold,
                    fontWeight = FontWeight.Bold,
                )
            }

            // Phần giữa cuộn được: bàn phím hiện lên hoặc chữ lớn không được che mất nút BẮT ĐẦU.
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PlayerCard(
                    title = stringResource(R.string.local_setup_player_one),
                    value = whiteInput,
                    onValueChange = { whiteInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
                    placeholder = defaultWhite,
                    imeAction = ImeAction.Next,
                )
                PlayerCard(
                    title = stringResource(R.string.local_setup_player_two),
                    value = blackInput,
                    onValueChange = { blackInput = it.take(LocalSetup.MAX_NAME_LENGTH) },
                    placeholder = defaultBlack,
                    imeAction = ImeAction.Done,
                )
                TimeCard(selected = time, onSelect = { time = it })
            }

            Button(
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = GothicColors.Blood,
                    contentColor = GothicColors.Gold,
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                Text(
                    text = stringResource(R.string.local_setup_start),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Khung chung của một nhóm lựa chọn: viền vàng mảnh trên nền tối. */
@Composable
private fun SetupCard(content: @Composable () -> Unit) {
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

/** Một người chơi: tiêu đề kèm bên quân và ô nhập tên. */
@Composable
private fun PlayerCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    imeAction: ImeAction,
) {
    SetupCard {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = GothicColors.Gold,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder) },
            supportingText = { Text(stringResource(R.string.local_setup_name_hint)) },
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = GothicColors.Parchment,
                unfocusedTextColor = GothicColors.Parchment,
                focusedBorderColor = GothicColors.Gold,
                unfocusedBorderColor = GothicColors.Gold.copy(alpha = 0.5f),
                cursorColor = GothicColors.Gold,
                focusedPlaceholderColor = GothicColors.Parchment.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = GothicColors.Parchment.copy(alpha = 0.5f),
                focusedSupportingTextColor = GothicColors.Parchment.copy(alpha = 0.7f),
                unfocusedSupportingTextColor = GothicColors.Parchment.copy(alpha = 0.7f),
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Chọn thời gian mỗi bên: bốn mức nằm trên một hàng, mức đang chọn đổi sang đỏ-vàng. */
@Composable
private fun TimeCard(selected: LocalTimeControl, onSelect: (LocalTimeControl) -> Unit) {
    SetupCard {
        Text(
            text = stringResource(R.string.local_setup_time),
            style = MaterialTheme.typography.labelLarge,
            color = GothicColors.Gold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (option in LocalTimeControl.entries) {
                val isSelected = option == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) GothicColors.Blood else Color.Transparent)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) GothicColors.Gold else GothicColors.Locked,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .clickable { onSelect(option) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = timeLabel(option),
                        color = if (isSelected) GothicColors.Gold else GothicColors.Parchment,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.local_setup_time_note),
            style = MaterialTheme.typography.bodySmall,
            color = GothicColors.Parchment.copy(alpha = 0.7f),
        )
    }
}

/** Nhãn ngắn của một mức thời gian: "Không giới hạn" hoặc "N phút". */
@Composable
internal fun timeLabel(time: LocalTimeControl): String =
    if (time.limited) {
        stringResource(R.string.local_time_minutes, time.minutes)
    } else {
        stringResource(R.string.local_time_unlimited)
    }
