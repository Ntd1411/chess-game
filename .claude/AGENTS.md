# AGENTS.md — Tower of Chess (Chess2D)

Hướng dẫn ngắn cho AI agent làm việc trong project này. Đọc file này trước; chỉ mở
thêm tài liệu trong `docs/` khi task thực sự cần (tránh tốn context cho việc không
liên quan).

## Tổng quan

Game cờ vua Android (Kotlin + Jetpack Compose), package `kma.game.chess2d`. Đang bổ
sung mode chiến dịch **"Tower of Chess"** (49 tầng, gothic style, cốt truyện là phần
phụ) lên trên bộ khung chess app đã hoàn chỉnh (engine, AI, LAN đều đã chạy tốt).

## Cấu trúc module

| Module | Vai trò | File/chỗ quan trọng |
|---|---|---|
| `engine/` | Luật cờ đầy đủ, đã test kỹ | `Board`, `MoveGenerator`, `Rules`, `Fen`, `Pgn`; test: `Perft`, `MakeUnmake`, `SpecialMoves`, `DrawRules` |
| `ai/` | Đối thủ máy (minimax/alpha-beta) | `Search`, `Evaluation`, `MoveOrdering`, `TranspositionTable`, `Ai.kt`; test: `SelfPlayTest` |
| `net/` | LAN multiplayer | `LanHost`, `LanGuest`, `Discovery`, `Protocol`, `Clock` |
| `app/` | UI Compose | `MenuScreen`, `GameScreen`, `LanLobbyScreen`, `LanGameScreen`, `HistoryScreen`, `SplashScreen`, `puzzle/PuzzleCatalog.kt` |

## Lệnh thường dùng

- `./gradlew test` — chạy toàn bộ test
- `./gradlew :engine:test` / `:ai:test` / `:net:test` — test riêng từng module
- `./gradlew :app:assembleDebug` — build APK debug

## Quy tắc khi chỉnh sửa

1. **Mỗi lần edit project, thêm 1 dòng có timestamp vào `CHANGELOG.md`** (ở gốc
   project, ngoài `.claude/`) — tóm tắt ngắn gọn đã làm gì. Format xem trong chính
   file đó.
2. Không đụng vào test suite của `engine/` (`Perft`, `MakeUnmake`, `DrawRules`) trừ
   khi được yêu cầu rõ ràng — đây là phần luật cờ đã kiểm chứng kỹ, sai sót ở đây
   rất dễ mất điểm đồ án.
3. UI mới phải theo đúng phong cách gothic (đỏ-đen-vàng, tháp cổ, mặt trăng đỏ) —
   xem `docs/design/ui-screens.md` trước khi code màn hình mới.
4. Mode "Máy vs Máy" = mode xem/spectate đã quyết định dùng, không cần làm thêm mode
   xem riêng.

## Tài liệu chi tiết (đọc khi cần, không đọc mặc định)

- `docs/design/story-bible.md` — cốt truyện, twist, nhân vật (đọc khi làm
  Dialogue/Tower Map/nội dung lore)
- `docs/design/ui-screens.md` — spec đầy đủ 17 màn hình + sơ đồ điều hướng (đọc khi
  làm bất kỳ màn UI nào)
- `docs/context/tower-of-chess.md` — context tổng hợp: tổng quan project, bảng ánh
  xạ việc cần làm vs code đã có, rubric chấm điểm đồ án, asset, roadmap
- `docs/plan/completion-plan.md` — kế hoạch 6 giai đoạn, mỗi giai đoạn có tiêu chí
  tự xác minh riêng; **chỉ yêu cầu người dùng test thủ công ở Giai đoạn 6**, các
  giai đoạn trước agent tự test bằng unit test/build/chạy thử trên emulator, khi hoàn thành giai đoạn nào đánh dấu vào file này luôn
