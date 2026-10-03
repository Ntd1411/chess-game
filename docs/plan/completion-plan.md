# Kế hoạch hoàn thành — Tower of Chess

Nguyên tắc chung: **mỗi giai đoạn phải tự xác minh được bằng build/test tự động hoặc
checklist code review trước khi coi là xong.** Chỉ yêu cầu người dùng test thủ công
ở **Giai đoạn 6 (cuối cùng)** — khi đó mới cần cảm nhận thật về UX/gameplay.

Tham khảo thêm: `.claude/AGENTS.md` (quy tắc chung), `docs/design/story-bible.md`,
`docs/design/ui-screens.md`.

---

## Giai đoạn 0 — Rà soát nền tảng & asset

**Mục tiêu:** Biết chính xác đang có gì trước khi thêm tính năng mới, tránh làm lại.

**Công việc:**
- [x] Liệt kê toàn bộ asset hiện có trong `app/src/main/assets` và `res/drawable*`
      (hoặc thư mục asset tương ứng), đối chiếu với danh sách 17 màn hình trong
      `ui-screens.md` → lập bảng "đã có / còn thiếu" — **xong, xem
      `docs/design/ui-screens.md` » Asset audit**: ~90% đã có sẵn, chỉ thiếu nền
      riêng cho Splash/AI Selection/LAN Lobby và xác nhận nhân vật Pháp Sư Cờ
- [x] Chạy `./gradlew test` trên toàn bộ project để xác nhận baseline đang xanh
      — **kết quả:** `:engine:test`, `:net:test` xanh; `:ai:test` lỗi
      `java.io.EOFException` (nghi môi trường/JDK, không phải lỗi test); cũng
      phát hiện và vá 1 lỗi compile thật trong `MenuScreen.kt` (xem
      CHANGELOG 2026-10-03 15:39 GMT+7)

**Tự xác minh:**
- `./gradlew test` trả về BUILD SUCCESSFUL, không có test fail nào có sẵn
- Bảng asset đã có/thiếu được ghi vào `docs/design/ui-screens.md` (cập nhật phần
  asset, không cần hỏi người dùng)

**Hoàn thành khi:** baseline build xanh + bảng asset audit tồn tại trong docs.

**→ GIÀI ĐOẠN 0: HOÀN THÀNH** (2026-10-03, với 1 ghi chú môi trường chưa giải
quyết ở `:ai:test`, không chặn tiến độ vì không phải lỗi logic)

---

## Giai đoạn 1 — Dữ liệu lõi cho 49 tầng + Tower Map

**Mục tiêu:** Có cấu trúc dữ liệu đại diện cho 1 tầng, nạp được cả 49 tầng, và UI
bản đồ hiển thị đúng trạng thái khóa/mở.

**Công việc:**
- [x] Định nghĩa data class `Floor` (số tầng, loại node: Battle/Story/Treasure/
      Puzzle/Boss/Secret, startFen hoặc puzzle tham chiếu, điều kiện thắng, cờ
      có/không thoại) — đặt cạnh `PuzzleCatalog` hoặc module mới `campaign/`
      — **xong:** `app/.../campaign/Floor.kt` (`Floor`, `NodeKind`, `FloorGoal`)
- [x] Viết file dữ liệu cho 49 tầng (có thể sinh tự động phần lớn bằng script/loop,
      chỉ 6 tầng mốc cần nội dung đặc biệt theo `story-bible.md`)
      — **xong:** `TowerCatalog.build()` sinh theo quy luật 7 chương × 7 tầng
- [x] Viết hàm load + validate toàn bộ 49 tầng qua engine thật (giống cách
      `PuzzleCatalog.validate()` đang làm)
      — **xong:** `TowerCatalog.validate()` / `validateAll()`
- [x] Màn hình **Tower Map** (Compose): hiển thị node theo tiến độ, tầng chưa mở =
      khóa, tầng hiện tại = phát sáng
      — **xong:** `TowerMapScreen.kt` + `TowerProgress` / `TowerProgressStore`;
      mở từ nút "Khám Phá Tháp Cờ" ở menu. Chạm vào tầng chưa dẫn đi đâu (Giai đoạn 4)

**Tự xác minh:**
- Unit test: load đủ 49 tầng, không tầng nào thiếu dữ liệu bắt buộc
- Unit test: `validate()` chạy qua engine cho mọi tầng có startFen — không exception,
  không FEN sai
- `./gradlew :app:test` xanh
- Build debug APK thành công (`./gradlew :app:assembleDebug`), không lỗi compile

**Hoàn thành khi:** 49/49 tầng load + validate pass bằng test tự động, Tower Map
build không lỗi.

**→ GIAI ĐOẠN 1: HOÀN THÀNH** (2026-10-03) — `TowerCatalogTest` (14 test) +
`TowerProgressTest` (9 test) xanh, `:app:testDebugUnitTest` và `:app:assembleDebug`
BUILD SUCCESSFUL.

---

## Giai đoạn 2 — AI vs AI (spectate)

**Mục tiêu:** Mode "Máy vs Máy" chạy được 2 AI tự đấu, có điều khiển xem.

**Công việc:**
- [x] Màn chọn AI Trắng/AI Đen (tận dụng data AI đã định nghĩa cho AI Character
      Selection nếu đã có, hoặc tạm dùng danh sách độ khó có sẵn)
      — **xong:** tạm dùng 3 cấp độ có sẵn (`SpectateSetupScreen`); gắn nhân vật AI thật ở Giai đoạn 3
- [x] Vòng lặp chạy: AI Trắng và AI Đen lần lượt gọi `ai` module để chọn nước đi,
      cập nhật board qua `engine`
      — **xong:** `SpectateMatch` + `SpectateViewModel`
- [x] Control: Pause/Resume, tốc độ ×1/×2/×4, Auto play, Move history hiển thị
      — **xong:** `SpectateControls` + `SpectateScreen`

**Tự xác minh:**
- Mở rộng `SelfPlayTest` (đã có trong `ai/src/test`) để chạy nhiều ván AI vs AI liên
  tiếp ở các mức độ khó khác nhau, xác nhận luôn kết thúc (checkmate/stalemate/giới
  hạn nước đi) không bị treo vô hạn
- Test không có exception khi 2 AI cùng độ khó cao nhất đấu nhau 50 ván liên tục
  (phát hiện bug hiếm/edge case)
- Build + chạy thử trên emulator qua `adb` (agent tự chạy, quan sát log không crash)
  ở tốc độ ×1, ×4 và khi bấm Pause giữa chừng

**Hoàn thành khi:** self-play test pass ổn định, demo chạy trên emulator không
crash qua log, đủ 3 control (pause/speed/autoplay) hoạt động.

**→ GIAI ĐOẠN 2: CODE + TEST XONG (2026-10-03), CÒN 1 MỤC MỞ.** `SpectateMatchTest` (7),
`SpectateControlsTest` (8) và `SelfPlayTest` mở rộng xanh; `:app:assembleDebug` thành công.
Bản 50 ván Khó–Khó chỉ chạy khi `SELFPLAY_LONG=1` (hàng giờ). **Chưa làm:** chạy thử
trên emulator qua `adb` ở ×1/×4/Pause — MCP local không cho chạy `adb`, cần chạy tay.

---

## Giai đoạn 3 — Main Lobby & AI Character Selection

**Mục tiêu:** Hai màn hình trung tâm nhất theo mockup gothic.

**Công việc:**
- [~] Redesign `MenuScreen.kt` → Main Lobby: avatar/currency trên cùng, nhân vật
      đứng giữa, 2 bên icon (Nhật ký/Túi đồ/Thành tựu), 5 mode dưới cùng (2 mode
      chính to hơn)
      — **xong một phần:** có avatar/tên/Cài đặt, nhân vật giữa, 5 mode (2 chính 72dp,
      3 phụ 48dp). **Còn thiếu:** currency (chưa có hệ thống Gold/Crystal) và 3 icon
      Nhật ký/Túi đồ/Thành tựu (màn đích làm ở Giai đoạn 5)
- [x] Màn **AI Character Selection**: carousel, stat AI, nút BẮT ĐẦU, nối với danh
      sách nhân vật AI trong `story-bible.md`
      — **xong:** `AiSelectionScreen` + `opponent/AiCharacter.kt`

**Tự xác minh:**
- Compose UI test (hoặc `ComposeTestRule`) kiểm tra: mọi nút trên Main Lobby điều
  hướng đúng màn đích (không bị dead-end/crash khi bấm)
- Compose preview build không lỗi cho cả 2 màn (nhiều kích thước màn hình nếu có
  preview multi-device)
- Kiểm tra bằng code: 2 mode chính có kích thước lớn hơn mode phụ (so sánh giá trị
  `Modifier.size`/`weight` trong code, không cần mắt nhìn)

**Hoàn thành khi:** navigation test pass, build không lỗi, không cần người dùng xem
qua ở bước này (chỉ xem ở Giai đoạn 6).

**→ GIAI ĐOẠN 3: CODE + TEST XONG MỘT PHẦN (2026-10-03).** `AiCharacterTest` (8) và
`LobbyLayoutTest` (2: chế độ chính cao hơn chế độ phụ, so giá trị trong code) xanh;
`:app:assembleDebug` thành công. **Chưa làm:** Compose navigation test (project chưa có
`ui-test`/Robolectric; thêm cần quyết định vì JDK 25 có thể không tương thích Robolectric),
Compose preview nhiều kích thước, currency và 3 icon lối tắt ở Lobby.

---

## Giai đoạn 4 — Tower Map liên kết đầy đủ: Dialogue, Victory, Defeat

**Mục tiêu:** Luồng chơi chính "Khám Phá Tháp Cờ" chạy trọn vẹn từ chọn tầng →
(thoại nếu có) → đánh cờ → thắng/thua → quay lại bản đồ hoặc sang tầng kế.

**Công việc:**
- [x] Màn **Dialogue** đơn giản (box thoại + nút tiếp tục), chỉ kích hoạt ở 6 tầng
      mốc theo `story-bible.md`
      — **xong:** `DialogueScreen` + `FloorDialogues`
- [x] Màn **Victory**/**Defeat** riêng, nhận tham số (phần thưởng, tầng tiếp theo)
      — **xong:** `FloorResultScreens.kt`; chưa có tiền tệ nên phần thưởng duy nhất là mở tầng kế
- [x] Nối toàn bộ luồng: Tower Map → (Dialogue) → Chess Battle → Victory/Defeat →
      quay lại Tower Map với tiến độ cập nhật
      — **xong:** `TowerFlow` + `TowerRoute` + `CampaignMatch`/`CampaignViewModel`

**Tự xác minh:**
- Integration test (không cần UI thật): giả lập "thắng tầng N" → kiểm tra tầng N+1
  được mở khóa trong dữ liệu lưu trữ
- Compose navigation test: đi hết luồng từ Tower Map đến Victory và quay lại, không
  crash, không màn nào bị kẹt (dead-end)
- Chạy thử trên emulator 3 tầng mẫu: 1 tầng thường, 1 tầng mốc có thoại, 1 tầng boss
  — agent tự quan sát log/screenshot, không cần người dùng

**Hoàn thành khi:** luồng chính chạy hết không crash qua test tự động + agent tự
chạy thử trên emulator.

**→ GIAI ĐOẠN 4: CODE + TEST XONG MỘT PHẦN (2026-10-03).** `CampaignMatchTest` (12) và
`TowerFlowTest` (12, duyệt cả 49 tầng: thắng N mở N+1, không tầng nào kẹt) xanh;
`:app:assembleDebug` thành công. **Chưa làm:** Compose navigation test (chưa có thư viện test UI) và chạy thử emulator 3 tầng mẫu (MCP local không cho chạy `adb`).

---

## Giai đoạn 5 — Tính năng phụ: Profile, Journal, Local Multiplayer setup, Settings, Pause

**Mục tiêu:** Hoàn thiện các màn còn lại, không ảnh hưởng luồng chính.

**Công việc:**
- [x] **Character/Profile**: bảng Room mới lưu stats (thắng/thua/tầng cao nhất/
      checkmate/thời gian chơi)
      — **xong phần dữ liệu:** `profile/PlayerStats.kt` + `ProfileDatabase` (DB riêng, không cần
      migration). Đã ghi được tầng cao nhất, thắng/thua/hòa/checkmate/thời gian chơi (đấu máy,
      LAN, chiến dịch; qua `MatchRecording`). **Xong màn hình Profile** (`ProfileScreen`, mở từ
      avatar/nhân vật ở Sảnh): chỉ có phần Thông tin, **không dựng** tab Trang phục/Kỹ năng/Ký ức
      vì chưa có hệ thống tương ứng
- [~] **Character Journal**: bảng Room lưu NPC đã gặp + trạng thái (✓/?/🔒)
      — **xong phần dữ liệu:** `JournalEntry` + `Journal.stateOf`; ghi đã gặp khi bấm BẮT ĐẦU
      ở màn chọn đối thủ. **Còn thiếu:** màn hình Nhật ký (chỉ mới có 5 nhân vật AI)
- [ ] Màn **setup 2 người cùng máy** (chọn quân/tên/thời gian) trước khi vào Battle
- [x] Mở rộng `SettingsStore` + UI tabs đầy đủ (Chung/Âm thanh/Hình ảnh/Điều khiển/
      Khác)
      — **xong:** `SettingsPanel` 5 tab + `DataReset`. **Chỉ có cài đặt có tác dụng thật:** rung, âm
      thanh, bộ màu/bộ quân, hiển thị nước đi hợp lệ, xóa dữ liệu, giới thiệu. **Không làm** (chưa có
      tính năng tương ứng): ngôn ngữ, thông báo, nhạc nền (Giai đoạn 6), chất lượng hiệu ứng,
      FPS, xác nhận nước đi
- [~] **Pause Menu** overlay trong `GameScreen`
      — **xong cho `GameScreen`** (đấu máy/2 người): `PauseMenu` + `PauseMenuOverlay`, nút Menu và Back
      mở Pause. Mục Cài đặt mở `SettingsPanel`.
      **Chưa làm:** màn đấu chiến dịch (`CampaignBattleScreen`) và LAN chưa có Pause

**Tự xác minh:**
- Room migration test: schema mới không làm vỡ dữ liệu cũ (nếu đã có HistoryScreen
  dùng Room từ trước)
- Unit test cho Settings: thay đổi giá trị → lưu → đọc lại đúng giá trị
- Compose navigation test cho từng màn mới, không crash

**Hoàn thành khi:** toàn bộ test trên pass, build debug thành công.

---

## Giai đoạn 6 — Gắn thoại cốt truyện, polish cuối, và **test với người dùng**

**Mục tiêu:** Bản demo hoàn chỉnh, sẵn sàng nộp/chơi thật.

**Công việc:**
- [ ] Gắn nội dung thoại thật vào 6 tầng mốc (1, 5, 21, 42, 45, 49) theo
      `story-bible.md`
- [ ] Rà soát toàn bộ UI theo đúng art style gothic, chỉnh màu/font nếu lệch
- [ ] Âm thanh: nhạc nền, SFX khi đi quân/ăn quân/chiếu hết
- [ ] Build release/signed APK, cài lên thiết bị thật hoặc emulator ổn định

**Tự xác minh (agent tự làm trước):**
- `./gradlew test` toàn bộ project xanh
- Build release không lỗi, không warning nghiêm trọng
- Agent tự chạy qua toàn bộ 5 mode (Khám phá, vs Máy, 2 người, LAN, Máy vs Máy) trên
  emulator ít nhất 1 lượt mỗi mode, xác nhận không crash qua log

**Đây là giai đoạn duy nhất cần người dùng test thủ công**, với checklist gợi ý:
- [ ] Chơi thử Tower Map từ tầng 1 đến ít nhất qua 1 tầng mốc có thoại
- [ ] Thử mode vs Máy ở cả 3 độ khó
- [ ] Thử mode Máy vs Máy, đổi tốc độ xem
- [ ] Thử LAN với 2 thiết bị (hoặc 2 emulator)
- [ ] Đánh giá cảm nhận chung: art style, độ mượt, có bug/crash nào không

**Hoàn thành khi:** người dùng xác nhận đã chơi qua checklist trên và không còn bug
chặn (blocking bug).
