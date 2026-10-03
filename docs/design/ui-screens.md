# UI Screens — Tower of Chess

Phong cách: gothic, tông đỏ-đen-vàng, tháp cổ, mặt trăng đỏ, sương mù, cánh hoa/
feather bay. Asset phần lớn **đã có sẵn trong project** — ưu tiên dùng lại, chỉ
generate thêm phần còn thiếu.

Có mockup ảnh tham khảo (do người dùng cung cấp, không lưu trong repo này — hỏi lại
nếu cần xem).

## Asset audit (cập nhật 2026-10-03)

Đã rà soát `app/src/main/res/drawable/` — **phần lớn asset cần cho 17 màn hình đã có
sẵn** (chưa commit vào git trước khi agent này kiểm tra, đã commit kèm fix
`pieceThemeLabel`). Chi tiết:

**Đã có đầy đủ:**
- `screen_main_menu`, `screen_floor_select`, `screen_tower_map`, `screen_chess_battle`,
  `screen_dialogue_background`, `screen_character_journal`, `screen_victory`,
  `screen_defeat`, `screen_settings`, `screen_memory_inventory` — nền cho đúng 10/10
  màn chính
- `screen_normal_ending` + `screen_true_ending` — **khớp rất tốt với twist cốt
  truyện** (false reveal / true reveal) — nên dùng cho 2 kết thúc khác nhau ở
  tầng 49 thay vì 1 màn Victory chung chung
- Bộ quân cờ đầy đủ 12 ảnh (`piece_white/black_*`), ô cờ (`tile_*`), hiệu ứng
  (`fx_*`), icon menu (`icon_*`), khung/nút UI (`ui_*`), vật phẩm (`item_*`)
- Nhân vật chính đủ biểu cảm: `char_protagonist_portrait/full_body/determined/
  victory/defeat/sad/shocked`
- AI: `char_chess_queen` (→ Nữ Hoàng Máu), `char_knight_guardian` (→ Hiệp Sĩ Bóng
  Đêm), `char_mysterious_chess_king` (→ Vua Hắc Ám), `char_shadow_opponent` (có
  thể dùng cho nhân vật khóa "???"), `char_tower_keeper` (NPC giữ tháp — có thể
  dùng cho Pháp Sư Cờ hoặc 1 NPC dẫn chuyện riêng)

**Thiếu / cần quyết định:**
- Không có art riêng cho Splash Screen — có thể dùng lại `screen_tower_map` hoặc
  `screen_main_menu` làm nền + overlay logo thay vì tạo mới
- Không có nền riêng cho AI Character Selection — có thể dùng `screen_chess_battle`
  làm backdrop mờ + `ui_character_frame` cho khung nhân vật
- Không có nền riêng cho LAN Lobby / Local Multiplayer setup — có thể dùng
  `ui_main_panel` trên nền `screen_main_menu`
- Chưa rõ asset nào dành riêng cho "Pháp Sư Cờ" — tạm gán `char_tower_keeper`,
  cần xác nhận lại khi làm màn AI Selection

---

## 17 màn hình

1. **Splash Screen** — logo, loading bar, animation nhẹ (logo hiện dần, ánh sáng đỏ
   từ mặt trăng, quân cờ bay quanh logo)
2. **Main Lobby** — trung tâm game: avatar/level/gold/crystal + nút Settings trên
   cùng; nhân vật chính đứng giữa (idle animation, click mở Profile); 2 bên là Nhật
   ký/Túi đồ/Thành tựu; dưới cùng là 5 mode, **Khám Phá Tháp Cờ** và **Người vs Máy**
   để to hơn các mode phụ (2 người cùng máy, LAN, Máy vs Máy)
3. **Mode Select** — có thể gộp vào Lobby hoặc tách riêng
4. **AI Character Selection** — tiêu đề "CHỌN ĐỐI THỦ AI"; nhân vật AI hiển thị lớn ở
   giữa kèm câu thoại giới thiệu; thông tin: độ khó (sao), phong cách, tốc độ, khả
   năng, tính cách; carousel chọn AI phía dưới (mũi tên trái/phải); nút BẮT ĐẦU cuối
   màn hình
5. **Chess Battle** — bàn cờ chiếm 80–90% màn hình; top: avatar/tên/HP-rank/đồng hồ
   đối thủ; bottom: avatar/tên/đồng hồ/quân còn lại của người chơi; hiệu ứng: ô đang
   chọn, ô có thể đi, ô bị tấn công, check, checkmate, hiệu ứng khi ăn quân; thanh
   chức năng gọn: Rút lui / Gợi ý / Lịch sử / Cài đặt — **không nhồi nút**, bàn cờ
   phải là trọng tâm
6. **Tower Map (Exploration)** — tiêu đề "THÁP CỜ — TẦNG N/49"; bản đồ dạng đường đi
   node-to-node; loại node: Battle / Story / Treasure / Puzzle / Boss / Secret; tầng
   chưa mở = khóa; tầng hiện tại = phát sáng vàng
7. **Dialogue** — background cảnh (đại sảnh/phòng ngai vàng/hành lang/đỉnh tháp);
   nhân vật đứng trái/phải; dialogue box lớn phía dưới + nút `▶` tiếp tục; có thể có
   lựa chọn nhánh thoại (vd. `[Tôi sẽ thử.]` / `[Tôi không còn lựa chọn.]`)
8. **Victory** — nền chuyển vàng/đỏ với ánh sáng; chữ "CHIẾN THẮNG"; dòng phụ (vd. đã
   đánh bại ai); phần thưởng Gold/Crystal/Memory; nút `[TIẾP TỤC]` (sang tầng kế nếu
   đang ở Story Mode) / `[VỀ SẢNH]`
9. **Defeat** — nền tối/đỏ, hiệu ứng quân cờ vỡ; chữ "THẤT BẠI"; 1 câu thoại động
   viên; nút `[THỬ LẠI]` / `[VỀ SẢNH]`; có thể bổ sung: số nước đi, thời gian, sai
   lầm lớn nhất, gợi ý cải thiện
10. **Character/Profile** — artwork full-body ở giữa; tên + level; stats: số trận
    thắng/thua, tầng cao nhất, số checkmate, thời gian chơi; tabs: Thông tin / Trang
    phục / Kỹ năng / Ký ức
11. **Character Journal** — giao diện như 1 cuốn sách cổ; danh sách nhân vật đã gặp
    (✓ đã gặp / ? chưa rõ / 🔒 khóa); mở 1 nhân vật ra xem: artwork, tên, tiểu sử,
    câu thoại, lịch sử gặp mặt, bí mật đã phát hiện
12. **Inventory** — nêu trong sơ đồ điều hướng, chưa có spec chi tiết riêng (cần hỏi
    thêm khi tới lượt làm)
13. **LAN Multiplayer** — 2 lựa chọn lớn: `TẠO PHÒNG` (chờ người khác kết nối) /
    `THAM GIA PHÒNG` (nhập IP hoặc tìm phòng trong LAN); sau khi kết nối hiện
    Player 1 vs Player 2 → `BẮT ĐẦU TRẬN ĐẤU`; có thể auto-discover phòng trong cùng
    mạng
14. **Local Multiplayer (2 người cùng máy)** — màn setup: Người chơi 1 (chọn quân
    Trắng, tên), Người chơi 2 (chọn quân Đen, tên); tùy chọn thời gian (Không giới
    hạn/5/10/30 phút), bật/tắt gợi ý, hiệu ứng; nút `BẮT ĐẦU` → vào Chess Battle
15. **AI vs AI** (= mode xem/spectate) — chọn AI Trắng vs AI Đen (hiển thị tên + sao
    độ khó mỗi bên); khi chạy, bàn cờ có thêm: Pause, tốc độ ×1/×2/×4, Auto play,
    Move history
16. **Settings** — tabs: Chung (ngôn ngữ, rung, thông báo) / Âm thanh (nhạc nền,
    hiệu ứng, âm quân cờ) / Hình ảnh (chất lượng hiệu ứng, hiệu ứng phép thuật, FPS)
    / Điều khiển (hiển thị nước đi hợp lệ, xác nhận nước đi, gợi ý) / Khác (lưu dữ
    liệu, reset dữ liệu, credits)
17. **Pause Menu** — overlay làm mờ bàn cờ phía sau; `Tiếp tục` / `Khởi động lại` /
    `Cài đặt` / `Rời trận`

## Sơ đồ điều hướng

```
SPLASH → MAIN LOBBY
  ├─ Khám phá → Tower Map → Dialogue → Chess Battle → Victory/Defeat
  │             (+ Character, Journal, Inventory)
  ├─ Người vs Máy → AI Selection → Chess Battle
  ├─ 2 người cùng máy → Setup → Chess Battle
  ├─ 2 người qua LAN → Create/Join Room → Chess Battle
  └─ Máy vs Máy → chọn AI Trắng/Đen → Chess Battle (pause/speed/auto-play)
```

## Ánh xạ vào code Compose hiện có

| Màn hình | Trạng thái | Ghi chú |
|---|---|---|
| Splash Screen | Đã có `SplashScreen.kt` | chỉnh lại theo art style mới |
| Main Lobby | Đã có `MenuScreen.kt` | cần redesign toàn bộ layout |
| Mode Select | — | có thể gộp vào Lobby |
| AI Character Selection | Chưa có | nối tham số độ khó từ module `ai` |
| Chess Battle | Đã có `GameScreen.kt` | chỉnh UI theo mockup (bàn to hơn, gọn thanh chức năng) |
| Tower Map | Chưa có | logic gần giống `PuzzleCatalog.kt`, cần UI node map |
| Dialogue | Chưa có | màn mới, đơn giản |
| Victory/Defeat | Chưa có (riêng) | hiện có thể đang gộp trong `GameScreen` |
| Character/Profile | Chưa có | cần bảng Room mới cho stats |
| Character Journal | Chưa có | cần bảng Room mới cho NPC đã gặp |
| Inventory | Chưa có | chưa rõ nội dung |
| LAN Multiplayer | Đã có `LanLobbyScreen.kt` / `LanGameScreen.kt` | chỉnh UI theo mockup |
| Local Multiplayer setup | Đã có `LocalSetupScreen.kt` | tên 2 bên + thời gian; chưa có bật/tắt gợi ý/hiệu ứng |
| AI vs AI (spectate) | Chưa có | tận dụng `ai` module cho cả 2 bên, thêm control tốc độ/pause |
| Settings | Đã có `SettingsStore` | cần UI tabs đầy đủ hơn |
| Pause Menu | Chưa có | overlay trong `GameScreen` |
