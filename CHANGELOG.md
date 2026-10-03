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
