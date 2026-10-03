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
