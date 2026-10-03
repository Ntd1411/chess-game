# Context tổng hợp — Tower of Chess (The 49th Floor mode)

> File này tổng hợp ngữ cảnh dự án cho agent/bản thân dùng tiếp. Quyết định mới
> nhất: **không dùng Godot** — phát triển ngay trong project Android Kotlin/Compose
> đã có. Chi tiết cốt truyện xem `docs/design/story-bible.md`; chi tiết UI xem
> `docs/design/ui-screens.md`.

---

## 1. Project nền tảng đã có

**Đường dẫn:** `/d/Android Project/Chess`
**Package:** `kma.game.chess2d`
**Stack:** Kotlin, Jetpack Compose, Gradle (Kotlin DSL), Room (lưu lịch sử ván đấu),
KSP, CI qua `.github/workflows/release.yml`, có cấu hình ký bản release.

### Cấu trúc module

| Module | Nội dung |
|---|---|
| `engine/` | Luật cờ đầy đủ: `Board`, `Move`, `MoveGenerator`, `Rules`, `Fen`, `Pgn`, `San`, `Zobrist`, `Attacks`, `Captures`. Có bộ test `Perft`, `MakeUnmake`, `SpecialMoves`, `DrawRules` — engine đã được kiểm chứng kỹ. |
| `ai/` | Đối thủ máy: `Search` (minimax/alpha-beta), `Evaluation`, `MoveOrdering`, `TranspositionTable`, `Ai.kt`. Có `SelfPlayTest`. |
| `net/` | LAN multiplayer: `LanHost`, `LanGuest`, `Discovery`, `Protocol`, `MessageChannel`, `NetGame`, `Clock`, `LanCli`. Có test loopback. |
| `app/` | UI Compose: `MenuScreen`, `GameScreen`, `LanLobbyScreen`, `LanGameScreen`, `HistoryScreen` (Room), `SplashScreen`, `ChessBoard`, `PromotionDialog`, `SoundEffects`, `SettingsStore`, `puzzle/PuzzleCatalog.kt`. |

### Mode đã có sẵn (trong `MenuScreen`)
- 2 người chơi (local)
- Vs máy (3 độ khó: Easy/Medium/Hard)
- LAN (host/guest)
- Lịch sử ván đấu (Room)
- Tùy chỉnh board palette, piece theme

### Mode còn thiếu, cần thêm
- **Máy vs Máy** (= mode xem/spectate đã chốt dùng)
- **Khám Phá Tháp Cờ** (49 tầng, campaign mode)

---

## 2. Cốt truyện & UI

- Cốt truyện đầy đủ (twist, nhân vật, 3 lớp bí ẩn, tầng mốc gắn thoại): xem
  `docs/design/story-bible.md`
- Thiết kế UI đầy đủ (17 màn hình, nhân vật AI, navigation flow, bảng ánh xạ vào
  code hiện có): xem `docs/design/ui-screens.md`

Tóm tắt quyết định: cốt truyện là **phần phụ** (chỉ gắn thoại ở vài tầng mốc); art
style là **gothic** (đỏ-đen-vàng, tháp cổ); asset phần lớn **đã có sẵn trong
project**.

---

## 3. Tài sản (assets)

- Đã có 1 file ảnh asset pack 2D (background, bàn cờ, chân dung nhân vật, quân cờ,
  VFX, UI, icon, vật phẩm) — cần cắt thành từng PNG riêng.
- Phần lớn asset cần cho 17 màn hình UI **đã có sẵn trong project** — ưu tiên rà
  soát/tái dùng trước khi generate thêm.
- Không có ngân sách — ưu tiên công cụ AI free nếu cần thêm (Leonardo.ai, Scenario
  free tier, Stable Diffusion qua Colab, freesound.org cho SFX).

---

## 4. Bối cảnh đồ án / grading rubric

Đây là đồ án (coursework) game 2D, cũng có khả năng thương mại hóa sau này. Tiêu chí
chấm điểm:
- Cốt truyện hấp dẫn
- Phong cách nghệ thuật chủ đạo
- Cơ chế game đa dạng, logic
- Nhiều nhân vật/đối tượng/vật phẩm
- Thế giới game lôi cuốn
- Đồ họa dễ nhìn
- Gameplay hấp dẫn, độc đáo
- Demo mượt, không giật/lag/crash
- Nhiều cấp độ/bản đồ, độ khó tăng dần
- NPC thông minh dùng AI
- Độ hoàn thiện cao
- Khả năng thương mại

**Ghi chú ánh xạ:** engine + ai + net module đã có sẵn giải quyết phần lớn "cơ chế
logic", "NPC AI thông minh" (AI đối thủ dùng minimax thật), và "nhiều levels" (49
tầng). Phần cần bổ sung nhiều nhất là **UI cho 17 màn hình mới**, **mode Khám Phá
Tháp Cờ**, và **mode Máy vs Máy**.

---

## 5. Việc cần làm tiếp

- [ ] Thiết kế cấu trúc dữ liệu cho 1 "tầng" trong Tower Map — mở rộng pattern của
      `PuzzleCatalog` (startFen + mục tiêu thắng + loại node + cờ có/không thoại)
- [ ] Quyết định mục tiêu thắng mỗi tầng: chiếu hết / sống sót N nước / bắt quân cụ
      thể / giải đố cố định
- [ ] Build **AI vs AI (spectate)**: chạy `ai` module cho cả 2 bên, thêm control
      pause/tốc độ ×1×2×4/auto-play/move history
- [ ] Redesign `MenuScreen.kt` thành Main Lobby theo mockup
- [ ] Làm màn **AI Character Selection** mới, nối vào module `ai` hiện có
- [ ] Làm **Tower Map** (node-based, khóa/mở theo tiến độ)
- [ ] Làm **Dialogue screen** đơn giản
- [ ] Làm **Victory/Defeat** screens riêng
- [ ] Thêm **Character/Profile** + **Journal** (cần bảng Room mới)
- [ ] Thêm màn **setup cho 2 người cùng máy**
- [ ] Mở rộng `SettingsStore` + UI tabs đầy đủ
- [ ] Thêm **Pause Menu** overlay
- [ ] Rà soát asset đã có trong project trước khi generate thêm
- [ ] Gắn thoại cốt truyện vào các tầng mốc (xem `story-bible.md`)

---

*File liên quan: `.claude/AGENTS.md` (hướng dẫn agent), `CHANGELOG.md` (log chỉnh
sửa theo thời gian), `docs/design/story-bible.md`, `docs/design/ui-screens.md`.*
