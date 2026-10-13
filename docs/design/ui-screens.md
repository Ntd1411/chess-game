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
2. **Main Lobby** — giữ nền/ảnh đại diện người dùng, header tự vẽ tên/cấp/thanh
   tiến độ và số dư. Hai thẻ chính Tháp Cờ/Người vs Máy, ba thẻ phụ cùng máy/LAN/
   Máy vs Máy; lưới shortcut Hồ sơ/Nhật ký/Túi đồ/Shop/Lịch sử/Cài đặt. Card/nút
   tối–vàng, nội dung cuộn tránh tràn màn, không ảnh nút hay tọa độ cố định.
   Shop thay Thành tựu; hỏi tiếp tục/bỏ ván dở bằng dialog tự vẽ có lựa chọn rõ.
3. **Mode Select** — năm card tự vẽ tối–vàng tên/mô tả, cuộn cùng nội dung;
   header và Quay lại neo vùng an toàn, giữ năm nhánh điều hướng.
4. **AI Character Selection** — card/nút tự vẽ tối viền vàng mảnh, giữ nền và
   portrait AI có sẵn trong res. Header/back sát trên; Bắt đầu/Quay lại neo
   dưới vùng an toàn. Nhân vật, sao độ khó, phong cách, tốc độ nghĩ, tính cách và
   câu thoại nằm trong nội dung cuộn; carousel chân dung vuốt ngang và mũi tên
   chuyển vòng qua bảy AI. Chỉ ba bot đầu mở sẵn; Vua Hắc Ám/Silent Bishop/Golden
   Queen/nhân vật ẩn mở sau khi thắng tầng 28/35/42/49. Bot khóa vẫn xem được,
   hiện điều kiện mở và tắt Bắt đầu. Ván chơi giữ đúng tên/portrait theo ID đã chọn.
5. **Chess Battle** — bàn cờ chiếm 80–90% màn hình; top: avatar/tên/HP-rank/đồng hồ
   đối thủ; bottom: avatar/tên/đồng hồ/quân còn lại của người chơi; hiệu ứng: ô đang
   chọn, ô có thể đi, ô bị tấn công, check, checkmate, hiệu ứng khi ăn quân; thanh
   chức năng gọn: Rút lui / Gợi ý / Lịch sử / Cài đặt — **không nhồi nút**, bàn cờ
   phải là trọng tâm
6. **Tower Map (Exploration)** — tiêu đề "THÁP CỜ — TẦNG N/49"; bản đồ dạng đường đi
   node-to-node trong dải cuộn bên trái; 6 Puzzle rồi 1 Boss mỗi chương. Panel chữ
   nhật bên phải hiện sơ lược cốt truyện, câu thoại/hồ sơ boss, độ khó và phần thưởng
   của node đang chọn. Tầng chưa mở vẫn chọn xem trước nhưng không vào được; tầng
   hiện tại viền vàng. Node/đường nối tự vẽ, tầng 49 ở trên và tầng 1 ở dưới,
   tự cuộn tới tầng hiện tại; chi tiết cuộn riêng. Header và Vào tầng/Quay lại
   neo vùng an toàn. Thưởng đã nhận được đánh dấu khi chọn tầng cũ.
7. **Dialogue** — background cảnh (đại sảnh/phòng ngai vàng/hành lang/đỉnh tháp);
   nhân vật đứng trái/phải; dialogue box lớn phía dưới + nút `▶` tiếp tục; có thể có
   lựa chọn nhánh thoại (vd. `[Tôi sẽ thử.]` / `[Tôi không còn lựa chọn.]`)
8. **Victory** — nền chuyển vàng/đỏ với ánh sáng; chữ "CHIẾN THẮNG"; dòng phụ (vd. đã
   đánh bại ai); phần thưởng Gold/Crystal/Memory; nút `[TIẾP TỤC]` (sang tầng kế nếu
   đang ở Story Mode) / `[VỀ SẢNH]`
9. **Defeat** — nền tối/đỏ, hiệu ứng quân cờ vỡ; chữ "THẤT BẠI"; 1 câu thoại động
   viên; nút `[THỬ LẠI]` / `[VỀ SẢNH]`; có thể bổ sung: số nước đi, thời gian, sai
   lầm lớn nhất, gợi ý cải thiện
10. **Character/Profile** — card tự vẽ tối viền vàng, portrait tròn và tên; card
    cấp có thanh tiến độ Compose lấy điểm thật. Tám thống kê chia hai cột, cuộn
    cùng nội dung; header và Quay lại neo vùng an toàn. Không dùng tọa độ cố định
    vượt mép màn, không dựng tab trang phục/kỹ năng khi chưa có dữ liệu.
11. **Character Journal** — giữ nền gothic, card tự vẽ tối viền vàng và lưới cuộn
    1–3 cột theo chiều rộng (thường 2 trên điện thoại, 3 trên màn rộng). Header/back
    sát trên, Quay lại neo dưới; nhớ vị trí lưới khi mở chi tiết rồi quay về. Nhân
    vật đã gặp hiện tên/portrait thật; chưa rõ/khóa dùng ?/icon khóa, không tải ảnh
    để lộ danh tính. Card khóa ghi tầng mở. Chi tiết cuộn gồm portrait, sao, lời
    thoại, độ khó/phong cách/tính cách/tốc độ và ngày đầu gặp/số lần đối đầu. Back
    từ chi tiết về lưới, rồi mới về menu; dữ liệu gặp và điều kiện mở không đổi.
12. **Inventory** — nền gothic, ô vuông tự vẽ tối viền vàng, lưới co giãn 1–4 cột.
    Giữ icon vật phẩm và số lượng ví; di vật khóa dùng icon khóa, chọn vẫn xem tầng
    mở. Mô tả không giới hạn ba dòng và cuộn cùng lưới. Header/back sát trên, Quay
    lại neo dưới vùng an toàn; thứ tự, số lượng và điều kiện nhận di vật không đổi.
13. **LAN Multiplayer** — card/nút tự vẽ tối viền vàng, giữ nền/chân dung. Header
    sát trên; Mở phòng và Về sảnh neo dưới. Nội dung cuộn gồm địa chỉ + sao chép,
    người chơi, ba thể thức giờ, danh sách phòng và nhập IP thủ công có IME Go.
    Khi host, hiện đối thủ chờ + thể thức đã chọn, tắt nút chính và đổi nút dưới
    thành Huỷ phòng; ván tự bắt đầu khi người khác vào. Vẫn cho chọn phòng khác
    khi đang host; phòng bận/lệch phiên bản không vào được. Giữ hướng dẫn cùng
    Wi-Fi, hotspot và nhập IP khi discovery bị mạng chặn.
14. **Local Multiplayer (2 người cùng máy)** — màn setup dùng hai card tự vẽ tối
    viền vàng, avatar tròn và ô tên; Người chơi 1 cầm Trắng, Người chơi 2 cầm Đen.
    Chọn Không giới hạn/5/10/30 phút trong lưới nút 2×2. Header/back neo sát trên;
    Bắt đầu/Quay lại neo dưới vùng an toàn; nội dung cuộn và tránh bàn phím. Next
    chuyển ô tên tiếp theo, Done đóng bàn phím; tên trống dùng mặc định. Gợi ý/hiệu
    ứng vẫn lấy từ cài đặt chung; Bắt đầu mở Chess Battle như trước.
15. **AI vs AI** (= mode xem/spectate) — bước chọn dùng hai card tối viền mảnh,
    avatar tròn/tên AI và ba nút tự vẽ Dễ/Vừa/Khó. Nút back/tiêu đề neo sát mép trên
    vùng an toàn; hai nút Bắt đầu/Quay lại neo sát mép dưới. Nội dung card cuộn trên
    màn nhỏ, footer luôn hiện và không phụ thuộc khung thiết kế căn giữa. Khi chạy,
    bàn cờ vẫn có Pause, tốc độ ×1/×2/×4, Auto play và Move history như trước.
16. **Settings** — dialog toàn màn giữ ván bên dưới, nền gothic và card/nút tự
    vẽ tối–vàng. Năm tab ngang vuốt được; nội dung cuộn, Khôi phục mặc định/Quay
    lại neo dưới và tránh bàn phím. Chung: tên (khi mở từ sảnh), rung; Âm thanh:
    nhạc nền/tiếng quân; Hình ảnh: năm màu bàn/bốn bộ quân; Điều khiển: nước hợp
    lệ; Khác: xoá dữ liệu có xác nhận và giới thiệu. Hàng switch/radio bấm cả
    hàng, tối thiểu 56dp, không dùng setting_line hay control chưa có tác dụng.
17. **Pause Menu** — cửa sổ Compose tự vẽ nền đen, góc bo và viền vàng mảnh, không
    dùng khung/nút ảnh. Tiếp tục là nút vàng nổi bật; Khởi động lại/Cài đặt là dòng
    tối; Rời trận ngăn bằng đường kẻ và nền đỏ nhẹ. Icon vector, chữ căn trái, vùng
    bấm tối thiểu 56dp và tự cao thêm khi chữ lớn. Có nút đóng; đóng/Back/chạm ngoài
    đều Tiếp tục. Nội dung cuộn trên màn thấp, giữ nguyên xử lý ván thường/chiến dịch.
    Panel Đổi cấp cũng tự vẽ tối–vàng: ba dòng Dễ/Vừa/Khó, mức hiện tại có dấu chọn,
    hiển thị thời gian nghĩ tối đa từ Difficulty thật. Chọn dòng áp dụng ngay;
    Hủy/nút đóng/Back/chạm ngoài không đổi cấp. Nội dung cuộn khi màn thấp.

18. **Match History** — thẻ tự vẽ tối–vàng, ngày/chế độ/kết quả/số nước và tên
    đối thủ; danh sách cuộn, nút Xem lại/Xuất PGN/Xoá rõ ràng. Xoá một ván hoặc
    toàn bộ cần xác nhận. Xem lại có bàn vuông co theo vùng trống, nước trước/sau
    khoá tại biên và giữ bộ màu/quân đã chọn. Chia sẻ PGN qua chooser như trước.

19. **Shop** — mua màu bàn/bộ quân hiện có bằng vàng Tháp Cờ, không thanh toán
    tiền thật. Hai danh mục và preview bàn thật; xác nhận mua, chặn thiếu vàng,
    đã sở hữu thì Áp dụng. Gothic và bộ ảnh mặc định miễn phí, giữ lựa chọn đang
    dùng từ bản cũ. Sở hữu/chi tiêu ghi nguyên tử cùng DataStore cài đặt; menu và
    túi đồ hiện số dư sau chi. Khôi phục mặc định không xoá mua hàng; xoá dữ liệu
    toàn bộ mới xoá. Giá tập trung trong ShopCatalog và có thể chỉnh sau.

### Vẽ lại Hội thoại
Hội thoại dùng card tối viền vàng, portrait có sẵn và chữ sáng không bị khung cắt. Nội dung cuộn, footer Auto/Bỏ qua/Tiếp, giữ mapping người nói và nhịp tự động cũ.

### Vẽ lại Chiến thắng
Chiến thắng dùng card phần thưởng tối viền vàng, số vàng/pha lê/sách thật và nút tự vẽ. Không đổi logic cộng thưởng hoặc mở tầng, vẫn Tiếp tục về bản đồ.

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
