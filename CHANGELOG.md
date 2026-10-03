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
