# CHANGELOG.md

Mỗi agent (Claude Code hoặc agent khác) khi chỉnh sửa project này **phải thêm 1 dòng
mới vào đầu danh sách bên dưới** (mới nhất lên trên), theo định dạng:

```
## YYYY-MM-DD HH:MM GMT+7 — <tóm tắt ngắn 1 dòng>
- Thay đổi cụ thể 1
- Thay đổi cụ thể 2
```

Không cần xin phép trước khi thêm dòng này — đây là log bắt buộc, không phải thay
đổi logic cần duyệt riêng.

---

## 2026-10-03 23:27 GMT+7 — Nền screen_chess_battle và chừa inset cho khung ván đấu dùng chung
- MatchScaffold dùng `gothicBackdrop` (mặc định screen_chess_battle, thêm tham số `backdrop`), nên GameScreen, CampaignBattleScreen và LanGameScreen cùng có nền gothic và chừa inset thanh hệ thống. SpectateScreen truyền `backdrop = screen_ai_vs_ai` thay vì tự gắn modifier để không vẽ nền hai lần
- `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-03 23:14 GMT+7 — Nền gothic cho sảnh LAN, Lịch sử, Máy vs Máy; chữ mặc định đọc được trên nền tối
- ui/art/GothicBackdrop.kt: Modifier `gothicBackdrop(res, dim)` vẽ ảnh nền cắt giữa (hàm thuần `centerCrop`) phủ tối, rồi mới chừa inset thanh trạng thái/điều hướng cho nội dung. Gắn vào LanLobbyScreen (screen_lan_lobby), HistoryScreen cả danh sách lẫn xem lại (screen_game_history), SpectateScreen cả bước chọn lẫn màn xem (screen_ai_vs_ai); các màn này trước đó chưa chừa inset. Không nạp trước ở splash để cache 64MB không bị đầy
- Theme: ChessTheme đặt `LocalContentColor` = Parchment vì app không còn Surface/Scaffold gốc (mặc định là đen, chữ đặt thẳng trên nền tối không đọc được)
- Thêm CenterCropTest (5 test). `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-03 22:46 GMT+7 — Bàn cờ gothic: ô marble, overlay phát sáng, quân ảnh làm mặc định
- BoardPalette thêm GOTHIC (textured) làm mặc định: ChessBoard vẽ tile_marble_light/dark, overlay tile_selected/move/attack/check, viền vàng mảnh; chưa nạp xong thì dùng màu phẳng dự phòng. Các bộ màu cũ giữ nguyên cách vẽ
- PieceTheme.IMAGE làm mặc định, quân Đen dùng piece_black_*_bright, quân nạp qua ArtCache (không còn painterResource 1254px) kèm quầng tối mờ phía sau
- SettingsStore mặc định GOTHIC + IMAGE, thêm nhãn palette_gothic trong SettingsPanel/strings, cập nhật 3 assert trong SettingsStoreTest. `:app:testDebugUnitTest` thành công

## 2026-10-03 22:42 GMT+7 — Splash mới, sau splash vào menu, menu dùng nút ảnh gothic
- AppRoot: SPLASH → MENU (trước đây vào thẳng sảnh LAN). Menu là màn gốc (back thoát app), sảnh LAN back về menu, hộp thoại tiếp tục ván dở chuyển sang menu
- SplashScreen: nền screen_splash_background, logo ui_logo_tower_of_chess, thanh tiến độ thật nạp trước ảnh menu/bàn cờ/quân cờ vào ArtCache (tối thiểu 1,4 giây)
- ui/art: thêm ArtAssets (danh sách nạp trước, ArtSizes) và PlateButton (nút dạng ảnh giữ tỉ lệ, hiệu ứng nhấn)
- MenuScreen: nền/nhân vật nạp qua ArtCache, quầng đỏ phía sau nhân vật, khung avatar, hai nút chính dùng ui_menu_button_primary_wide (+pressed), ba nút phụ dùng secondary_wide. LobbyLayoutTest vẫn pass. `:app:testDebugUnitTest` thành công

## 2026-10-03 22:39 GMT+7 — Thêm 35 asset gothic bổ sung, nền theme gothic cố định và bộ nạp ảnh ArtCache
- Đổi tên 35 ảnh trong asset_adding theo thời gian lưu khớp thứ tự prompt rồi chuyển vào res/drawable (screen_splash_background, ui_logo_tower_of_chess, ui_emblem_chess_tower, ic_launcher_art_*, ui_menu_button_*_wide(+_pressed), piece_black_*_bright, tile_*_overlay, tile_marble_light/dark, icon_* thanh chức năng và tiền tệ, ui_avatar_frame, screen_lan_lobby, screen_game_history, screen_ai_vs_ai)
- Theme gothic cố định (bỏ dynamic color), themes.xml nền tối chống nháy trắng lúc khởi động, MainActivity bỏ Scaffold, thêm ui/art/ArtCache (giải mã downsample có cache). Chưa nối vào các màn hình. `:app:testDebugUnitTest` và `:app:assembleDebug` đều thành công

## 2026-10-03 19:05 GMT+7 — Giai đoạn 6: đối chiếu thoại 6 tầng mốc, thử build release
- Đối chiếu `FloorDialogues` với story-bible: đủ 6 tầng mốc (1, 5, 21, 42, 45, 49) + epilogue tầng 49, đúng mạch Player #48 / Aria / Attempt #49. Đánh dấu xong trong completion-plan
- Thử `./gradlew :app:assembleRelease` qua MCP local hai lần nhưng connector báo server không phản hồi (nhiều khả năng build quá lâu so với giới hạn); chưa xác minh được. Cấu hình ký đã có sẵn trong app/build.gradle.kts (keystore.properties hoặc biến môi trường)

## 2026-10-03 18:41 GMT+7 — Giai đoạn 5 (phần 8): Pause Menu cho ván chiến dịch
- PauseMenu.itemsForCampaign() + test; CampaignBattleScreen thêm nút Menu và PauseMenuOverlay (Tiếp tục / Khởi động lại = chơi lại tầng / Cài đặt / Rời trận = về bản đồ). Back trong ván mở Pause thay vì thoát ngay
- LAN không thêm Pause: không thể tạm dừng đối thủ thật, `LanGameScreen` đã có Đầu hàng/Rời phòng
- Sửa comment lỗi thời trong GameScreen.timeoutLabel. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 18:20 GMT+7 — Giai đoạn 5 (phần 7): màn setup hai người cùng máy + đồng hồ cờ
- Thêm ui/screen/LocalSetupScreen.kt: nhập tên Người chơi 1 (Trắng) / Người chơi 2 (Đen), chọn thời gian Không giới hạn/5/10/30 phút, nút BẮT ĐẦU. Ô tên trống dùng tên mặc định. Chưa làm mục bật/tắt gợi ý và hiệu ứng của spec vì đã có cài đặt hiển thị nước đi hợp lệ toàn cục và chưa có hệ thống hiệu ứng
- AppRoot: thêm Screen.LOCAL_SETUP, menu "2 người cùng máy" đi qua setup rồi mới vào ván; GameScreen nhận `localStart` (token chống mở trùng khi xoay máy) và gọi `GameViewModel.startLocalGame`
- GameScreen/MatchScaffold: hiện tên hai bên, đồng hồ mm:ss ở hàng từng bên (đổi màu khi dưới 30 giây), dòng kết quả hết giờ. Đồng hồ tạm dừng khi mở Pause/Cài đặt, khi app xuống nền và khi rời màn
- Gom phần đã viết dở trước đó nhưng chưa commit: LocalSetup/LocalTimeControl/LocalClock/TimeoutRule, GameViewModel (đồng hồ, khôi phục, hết giờ, khóa Đi lại khi có giờ, ghi tên hai bên vào lịch sử), SavedGameStore lưu tên/thời gian/giờ còn lại
- Sửa LocalClockTest: `mmss(5 phút - 1ms)` phải là 05:00 vì làm tròn lên (kỳ vọng cũ 04:59 sai). Thêm string `local_*`, `status_timeout_*`. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:58 GMT+7 — Giai đoạn 5 (phần 6): màn hình Nhật ký nhân vật
- Thêm profile/Journal.kt (thuần): suy trạng thái ✓ đã gặp / ? chưa rõ / khóa cho từng nhân vật, dùng chung `isUnlocked` với màn chọn đối thủ để không mâu thuẫn. ProfileRepository thêm Flow `journal` (id → bản ghi gặp)
- Thêm ui/screen/JournalScreen.kt: danh sách nhân vật và trang chi tiết. Nhân vật chưa gặp bị che tên/ảnh. Trang đã gặp có câu thoại, độ khó, phong cách, tính cách, lịch sử gặp. Chưa có "bí mật đã phát hiện" vì chưa có hệ thống bí mật
- AppRoot thêm Screen.JOURNAL + JournalRoute (Room + tiến độ Tháp), back về menu. MenuScreen thêm `onOpenJournal` và nút Nhật ký cạnh Lịch sử
- Thêm string `journal_*`, test JournalPagesTest. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:49 GMT+7 — Giai đoạn 5 (phần 5): màn hình Hồ sơ (Profile)
- Thêm ui/screen/ProfileScreen.kt: ảnh nhân vật giữa, tên + cấp, thanh tiến độ lên cấp, bảng thống kê (thắng/thua/hòa, tổng ván, tỉ lệ thắng, chiếu hết, tầng cao nhất x/49, thời gian chơi). Chỉ có phần Thông tin, chưa dựng tab Trang phục/Kỹ năng/Ký ức vì chưa có hệ thống đằng sau
- PlayerStats: thêm `progressPoints`, `pointsIntoLevel`, `winRatePercent`; thêm profile/PlayTime.kt (tách giờ/phút/giây và chọn đơn vị hiển thị)
- Sảnh: chạm avatar/tên hoặc nhân vật để mở Hồ sơ; AppRoot thêm Screen.PROFILE + ProfileRoute (đọc Room qua Flow), back về menu
- Thêm 20 string `profile_*`; test ProfileDisplayTest (12); `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:42 GMT+7 — AGENTS.md: commit message theo Conventional Commits
- Quy tắc 5 bổ sung: bắt buộc mở đầu bằng type (`feat:` `fix:` `docs:` `refactor:` `test:` `chore:` `style:` `perf:`), tiêu đề mô tả chính xác, các `-m` sau đủ chi tiết để hiểu thay đổi mà không cần xem code; ví dụ trong file đổi sang dạng `feat: ...`

## 2026-10-03 17:40 GMT+7 — AGENTS.md: thêm quy tắc viết commit message
- Thêm quy tắc 5 vào `.claude/AGENTS.md`: commit message bằng tiếng Anh, `-m` đầu là tiêu đề ngắn, nhiều `-m` sau cho từng nhóm thay đổi, cấm `;` `&&` `|` `>` `<` và xuống dòng trực tiếp
- Nếu MCP từ chối command commit thì đưa nguyên câu lệnh đúng dạng cho người dùng tự chạy, không tự đổi sang message sai dạng

## 2026-10-03 17:31 GMT+7 — Giai đoạn 5 (phần 4): ghi thắng/thua/hòa/checkmate/thời gian chơi vào Hồ sơ
- Thêm profile/MatchRecording.kt (luật thuần): chế độ nào tính vào Hồ sơ (chỉ đấu máy; 2 người cùng máy và Máy vs Máy không tính), đo giây chơi (cắt trần 3 giờ/ván), kết quả ván chiến dịch (chỉ tầng đánh bại máy mới tính checkmate)
- GameViewModel (đấu máy): gọi `recordMatch` khi ván kết thúc, mỗi ván đúng 1 lần (Undo rồi thắng lại không cộng 2 lần; xoay máy sau khi ván xong không cộng lại nhờ cờ `replaying`)
- LanViewModel: ghi theo phía máy này; checkmate chỉ tính khi thắng bằng chiếu hết thật (đầu hàng/hết giờ không tính)
- AppRoot/TowerRoute: ghi ở `onWon`/`onLost` của ván chiến dịch, đồng hồ đo từ đầu mỗi lượt chơi (`newAttempt`); tất cả lần ghi chạy NonCancellable
- Test: MatchRecordingTest (10); `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:35 GMT+7 — Giai đoạn 5 (phần 3): Cài đặt dạng tab
- Thêm ui/screen/SettingsPanel.kt (Dialog toàn màn, 5 tab Chung/Âm thanh/Hình ảnh/Điều khiển/Khác) dùng chung cho Sảnh và Pause; settings/DataReset.kt (Xóa dữ liệu có xác nhận: cài đặt, tiến độ Tháp, ván dở, lịch sử, Hồ sơ, Nhật ký)
- SettingsStore nhận DataStore (test được trên JVM), thêm `showLegalMoves` + `resetToDefaults`; nối công tắc này vào bàn cờ GameScreen/CampaignBattle/LAN; TowerProgressStore.clear()
- MenuScreen: gỡ SettingsDialog cũ (chuyển sang SettingsPanel); Pause → Cài đặt mở SettingsPanel, mục này hiện ở mọi chế độ
- Test: SettingsStoreTest (8), PauseMenuTest cập nhật (4)

## 2026-10-03 17:22 GMT+7 — Giai đoạn 5 (phần 2): Pause Menu trong GameScreen
- Thêm game/PauseMenu.kt (luật chọn mục theo chế độ: ẩn Cài đặt khi 2 người), ui/screen/PauseMenuOverlay.kt (Dialog gothic: Tiếp tục / Khởi động lại / Cài đặt / Rời trận) + 5 string `pause_*`
- GameScreen: nút Home đổi thành nút Menu mở Pause; Back trong ván mở Pause thay vì thoát ngay; mục Cài đặt mở hộp chọn cấp máy (chỉ khi đấu máy)
- Thêm test PauseMenuTest (4)

## 2026-10-03 17:15 GMT+7 — Giai đoạn 5 (phần 1): tầng dữ liệu Hồ sơ + Nhật ký (Room), chưa có màn hình
- Thêm profile/PlayerStats.kt (thống kê + luật cộng thắng/thua/hòa/checkmate/thời gian/tầng cao nhất/cấp), Journal.kt (trạng thái ✓/?/🔒 dùng chung điều kiện mở với AiCharacter), ProfileDatabase.kt (Room DB riêng `profile.db`, không đụng `match-history.db`) + ProfileRepository (ghi trong giao dịch)
- Nối AppRoot: vượt tầng → recordFloorCleared; bấm BẮT ĐẦU ở màn chọn đối thủ → recordMet (đều NonCancellable)
- Thêm test ProfileLogicTest (14)

## 2026-10-03 16:57 GMT+7 — Giai đoạn 4: nối trọn luồng Tower Map → Thoại → Ván đấu → Thắng/Thua
- Thêm campaign/FloorDialogue.kt (thoại 6 tầng mốc + đoạn kết tầng 49), TowerFlow.kt (luật chuyển màn), CampaignMatch.kt (luật thắng/thua theo mục tiêu tầng), CampaignViewModel.kt
- Thêm DialogueScreen, VictoryScreen/DefeatScreen (FloorResultScreens.kt), CampaignBattleScreen.kt; viết lại TowerRoute trong AppRoot để chạy cả luồng
- Thêm string chiến dịch; thêm test CampaignMatchTest (12) và TowerFlowTest (12)

## 2026-10-03 16:32 GMT+7 — Giai đoạn 3: Main Lobby mới + màn Chọn đối thủ AI
- Thêm `opponent/AiCharacter.kt`: 5 nhân vật AI (Pháp Sư Cờ, Hiệp Sĩ Bóng Đêm, Nữ Hoàng Máu,
  Vua Hắc Ám, nhân vật ẩn `???`). Sức mạnh **chỉ** đến từ `Difficulty` (Dễ/Vừa/Khó/Khó); số sao
  và tốc độ nghĩ suy ra từ độ khó, còn phong cách/tính cách/câu thoại chỉ là mô tả nhân vật
  (engine chưa có phong cách chơi riêng). Nhân vật ẩn tạm mở khi vượt hết 49 tầng vì story bible
  chưa chốt điều kiện; Pháp Sư Cờ tạm dùng art `char_tower_keeper`
- Thêm `AiSelectionScreen` (nhân vật lớn ở giữa + câu thoại, thẻ thông tin, carousel mũi tên +
  ảnh nhỏ, nút BẮT ĐẦU; nhân vật chưa mở hiện điều kiện mở thay vì nút bị xám)
- Viết lại `MenuScreen` thành Main Lobby: nền `screen_main_menu`, avatar + tên + nút Cài đặt ở
  trên, nhân vật chính ở giữa, 2 chế độ chính (Khám Phá Tháp Cờ, Người vs Máy) cao 72dp, 3 chế
  độ phụ (2 người, LAN, Máy vs Máy) cao 48dp. Tên người chơi, âm thanh, giao diện bàn cờ chuyển
  vào hộp thoại Cài đặt; hàng chọn cấp độ ở menu bị bỏ vì màn Chọn đối thủ đã thay thế
- Nối `Screen.AI_SELECT` vào `AppRoot`: Người vs Máy → Chọn đối thủ → ván đấu với cấp độ của nhân vật
- Test mới: `AiCharacterTest` (8) + `LobbyLayoutTest` (2) xanh; `:app:testDebugUnitTest` và
  `:app:assembleDebug` thành công
- Chưa làm: ba lối tắt Nhật ký/Túi đồ/Thành tựu ở Lobby (màn đích thuộc Giai đoạn 5, tránh nút
  ngõ cụt); Compose navigation test (project chưa có `ui-test`/Robolectric, và chưa có emulator)

## 2026-10-03 16:25 GMT+7 — Giai đoạn 2: chế độ Máy vs Máy (AI vs AI)
- Thêm package `spectate/`: `SpectateMatch` (lõi thuần, hai AI độc lập đấu tới hết ván hoặc
  chạm trần 300 nửa nước thì coi là hòa; giữ id quân ổn định để animate, tính cả nhập thành
  và bắt tốt qua đường), `SpectateControls` (dừng/tiếp tục, tự động/thủ công, "1 nước",
  tốc độ ×1/×2/×4) và `SpectateViewModel` (chạy AI trên `Dispatchers.Default`, tạm dừng có
  hiệu lực sau nước đang nghĩ, vòng cũ phải dừng hẳn mới chạy vòng mới)
- Thêm `SpectateScreen.kt` (`SpectateRoute`: chọn cấp độ AI Trắng/AI Đen → xem ván, tái dùng
  `MatchScaffold`/`ChessBoard`/`CapturedRow`/`MoveList`; tự dừng khi app xuống nền bằng
  `LifecycleStartEffect`); nối `Screen.SPECTATE` vào `AppRoot`, thêm nút "Máy vs Máy" vào
  `MenuScreen`; mở `labelOf`/`statusLabel` của `GameScreen` thành `internal` để dùng lại;
  thêm string `spectate_*`
- Mở rộng `SelfPlayTest`: cặp cấp độ hỗn hợp (Dễ–Vừa, Vừa–Dễ, Vừa–Vừa), Khó–Khó vài nước mở
  đầu, và bản 50 ván Khó–Khó chỉ chạy khi đặt `SELFPLAY_LONG=1` (mỗi nước Khó tới 5 giây nên
  50 ván chạy hàng giờ, không để trong `gradlew test` thường)
- Test mới: `SpectateMatchTest` (7) + `SpectateControlsTest` (8) + 2 test self-play đều xanh;
  `:app:testDebugUnitTest`, `:app:assembleDebug` thành công. `:ai:test` chạy lại được, không
  còn `EOFException` như ghi chú Giai đoạn 0
- Chưa làm: chạy thử trên emulator/`adb` (MCP local không cho chạy `adb`) — cần chạy tay
  hoặc ở Giai đoạn 6

## 2026-10-03 16:05 GMT+7 — Giai đoạn 1: dữ liệu 49 tầng + màn Tower Map
- Thêm package `campaign/`: `Floor`/`NodeKind`/`FloorGoal`, `TowerCatalog` (sinh 49 tầng
  theo 7 chương × 7 tầng, 6 tầng mốc 1/5/21/42/45/49 có cờ thoại, độ khó theo 3 lớp
  bí ẩn, boss mạnh hơn một bậc), `validate`/`validateAll` chạy qua engine thật
- Thêm `TowerProgress` (luật mở khóa: qua N mở N+1, không nhảy cóc, không lùi) và
  `TowerProgressStore` (DataStore riêng `tower_progress`)
- Thêm `TowerMapScreen` (nền `screen_tower_map`, node khóa/hiện tại phát sáng vàng/đã qua)
  và `ui/theme/GothicColors`; nối `Screen.TOWER` vào `AppRoot`, thêm nút "Khám Phá Tháp Cờ"
  vào `MenuScreen`; thêm string `tower_*` (chuỗi `???` phải escape thành `\?\?\?`)
- Test mới: `TowerCatalogTest` (14) + `TowerProgressTest` (9) đều xanh;
  `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL
- Đánh dấu Giai đoạn 1 hoàn thành trong `docs/plan/completion-plan.md`
- Chưa làm (đúng kế hoạch): chạm vào tầng chưa dẫn đến thoại/ván đấu — thuộc Giai đoạn 4

## 2026-10-03 15:39 GMT+7 — Giải đoạn 0: audit asset + vá build lỗi
- Phát hiện bộ asset PNG đầy đủ cho gần hết 17 màn hình đã có sẵn trong
  `res/drawable/` (chưa commit) — ghi audit vào `docs/design/ui-screens.md`
- Phát hiện code dở dáng chưa commit: `PieceTheme.IMAGE` + `pieceImageRes()` trong
  `BoardTheme.kt`/`ChessBoard.kt` nối bộ ảnh quân cờ thật
- `./gradlew :app:compileDebugKotlin` FAIL do `MenuScreen.kt` thiếu nhánh `IMAGE`
  trong `when` — đã sửa: thêm `piece_theme_image` string + nhánh `when`
- Xác nhận lại: `:engine:test`, `:net:test` xanh; `:app:assembleDebug` build
  thành công (APK debug)
- **Vấn đề môi trường ghi nhận (chưa sửa):** `:ai:test` fail với
  `java.io.EOFException` ngay cả khi không động vào module `ai` — nghi do JDK 25
  trên máy (Kotlin fallback về JVM_24) xung đột với test worker process của
  Gradle, không phải lỗi logic test. Cần kiểm tra lại trên máy thật/CI.
- Commit: `feat: wire IMAGE piece theme to AI-generated piece assets, fix MenuScreen
  exhaustive when`

## 2026-10-03 15:31 GMT+7 — Tạo kế hoạch hoàn thành dự án
- Tạo `docs/plan/completion-plan.md` (6 giai đoạn, tiêu chí tự xác minh từng giai
  đoạn, chỉ yêu cầu người dùng test ở Giai đoạn 6)
- Cập nhật `.claude/AGENTS.md` thêm link đến file kế hoạch

## 2026-10-03 15:20 GMT+7 — Khởi tạo tài liệu dự án cho agent
- Tạo `.claude/AGENTS.md` (hướng dẫn ngắn cho agent)
- Tạo `CHANGELOG.md` (file này)
- Tạo `docs/design/story-bible.md` (cốt truyện Tower of Chess)
- Tạo `docs/design/ui-screens.md` (spec 17 màn hình + navigation flow)
- Tạo `docs/context/tower-of-chess.md` (context tổng hợp dự án)
