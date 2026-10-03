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
