# Story Bible — Tower of Chess (The 49th Floor)

> Lưu ý: cốt truyện hiện là **phần phụ**, không phải trọng tâm. Tài liệu này giữ lại
> đầy đủ ý tưởng gốc để tham khảo, nhưng khi triển khai chỉ cần gắn thoại vào vài
> tầng mốc (xem phần cuối), phần còn lại thuần gameplay.

## Tagline
"Every move leads higher"

## Bối cảnh
Tháp Elysia, 49 tầng, chia 7 chương. Đồng hồ đếm ngược 49 phút (7 phút/chương) tạo
áp lực thời gian xuyên suốt.

## 3 lớp bí ẩn

| Lớp | Tầng | Chủ đề |
|---|---|---|
| 1 | 1–14 | World (Thế giới) |
| 2 | 15–35 | Players (Những người chơi) |
| 3 | 36–49 | The 49th Move (Nước đi thứ 49) |

## Twist chính

Người chơi không phải người thứ 49 bước vào tháp, mà là **Attempt #49 — "Nước đi
thứ 49"**. 48 "người chơi" trước là 48 lần thử trước của cùng một vòng lặp.

- **Aria** — Nữ Hoàng Vàng (Golden Queen), còn được gọi là "Nữ Hoàng Ký Ức",
  hình thành từ ký ức các lần thử trước. Xuất hiện ở tầng 21, tự kiểm tra người
  chơi tại tầng 42, giải thích động cơ ở tầng 45. Nữ Hoàng Máu chỉ giữ cửa ký ức
  của Aria tại tầng 21; hai nữ hoàng là hai nhân vật khác nhau.
- **Player #48** — hồ sơ bị **CORRUPTED**, là đầu mối hé lộ sự thật ở tầng 42.
- **Reveal giả** quanh tầng 20–30 → **reveal thật** tầng 42 → **final reveal** tầng
  49, cái kết cho phép Player bước ra khỏi bàn cờ.

## Câu thoại xuyên suốt
*"Đừng để Vua bước ra khỏi bàn cờ."*

## Nguyên tắc thiết kế (mục 8.22 gốc — Mystery, Foreshadowing & Narrative Payoff)
Mọi câu hỏi lớn gài ở đầu game đều phải có payoff ở phần kết, không bỏ lửng.

## Nhân vật

- **Người Lữ Hành** — nhân vật chính (player)
- **Nữ Hoàng Máu** (Blood Queen) — AI, lối chơi tấn công, hung hãn, boss sớm
- **Hiệp Sĩ Bóng Đêm** (Dark Knight) — AI
- **Pháp Sư Cờ** (Chess Mage) — AI
- **Vua Hắc Ám** (Dark King) — người giữ ngai ở tầng 28
- **Giám Mục Tĩnh Lặng** (Silent Bishop) — người giữ nhà nguyện tầng 35, ít lời,
  điềm tĩnh, coi hành động quan trọng hơn lời hứa
- **Nữ Hoàng Vàng / Aria** (Golden Queen) — boss tầng 42, thử sức người chơi để
  họ đủ khả năng giữ ký ức đến đỉnh tháp; không phải kẻ muốn duy trì vòng lặp
- **??? (locked)** — người giữ bàn cờ cuối tại tầng 49

### Roster và mở khóa

Dùng bảy ảnh `portrait_*` trong `res`, không dùng ảnh toàn thân ở màn chọn AI.
Ba bot đầu mở sẵn: Pháp Sư Cờ, Hiệp Sĩ Bóng Đêm, Nữ Hoàng Máu. Bốn bot sau chỉ
mở trong Người vs Máy khi **đã thắng** tầng boss của chính mình: Vua Hắc Ám 28,
Silent Bishop 35, Golden Queen 42, nhân vật ẩn 49. Chọn xem trước không mở khóa;
thua/hòa/bỏ tầng không mở khóa. Tiến độ cũ được giữ và suy ra quyền mở mới.
Tên và portrait trong ván đấu/lưu ván dùng ID nhân vật, không suy từ mức Khó.

Mỗi AI có: độ khó (sao), phong cách chơi, tốc độ, khả năng đặc biệt, tính cách, câu
thoại riêng.

## Tài nguyên trong game
Gold (vàng), Crystal, **Memory** (Ký ức — vật phẩm thưởng gắn trực tiếp vào lore,
dùng làm phần thưởng chiến thắng và có thể dùng để "mở khóa" ký ức/lore trong
Journal).

## Quyết định triển khai (đã chốt)

Giữ đủ **49 tầng**, nhưng chỉ gắn thoại cốt truyện ở các **tầng mốc**:
- Tầng 1 — mở đầu
- Tầng 5 — giới thiệu Player #48
- Tầng 21 — gặp Aria, đấu boss Nữ Hoàng Máu
- Tầng 42 — true reveal qua Player #48, sau đó đấu Aria / Nữ Hoàng Vàng
- Tầng 45 — Aria tiết lộ lý do
- Tầng 49 — final reveal + ending

### Quy luật thử thách hiện hành

Mỗi chương có **6 tầng puzzle + 1 tầng boss**. Chỉ các tầng **7, 14, 21, 28, 35,
42, 49** là ván cờ đầy đủ từ thế khởi đầu; phải thắng, hòa không tính qua. Các
loại Treasure/Secret/Story-only không còn dùng trong catalog, nhưng giữ kiểu dữ
liệu cũ cho các fixture tương thích.

- 42 puzzle khác nhau, chuỗi chiếu liên tiếp từ chiếu hết trong 1 đến 3 nước.
- Elo **ước lượng** bắt đầu 800, tăng 25 mỗi puzzle, đến 1825; chưa qua calibration
  với người chơi, không phải rating chính thức. Chuỗi giải dùng đáp án định trước.
- Boss theo thứ tự ở tầng 7/14/21/28/35/42/49: Pháp Sư Cờ → Hiệp Sĩ Bóng Đêm →
  Nữ Hoàng Máu → Vua Hắc Ám → Giám Mục Tĩnh Lặng → Nữ Hoàng Vàng → nhân vật ẩn
  `???`. Mỗi chương có một người giữ cửa riêng, không còn boss tái đấu.
- Boss dùng chung danh mục AI (tên, chân dung, tính cách, câu thoại). Giới hạn tìm
  kiếm tăng theo từng chương: độ sâu 2–8, thời gian 0.5–5 giây/nước, giảm lựa chọn
  ngẫu nhiên và tỷ lệ bỏ nước tốt. Tính cách/phong cách vẫn là phần nhập vai, chưa
  phải một bộ thuật toán chơi riêng cho từng nhân vật.
- Tầng 45 vẫn có thoại Aria, nhưng phải giải puzzle sau thoại để nhận thưởng.
- Khi chọn bất kỳ node, kể cả node khóa, panel hình chữ nhật bên phải hiện lời
  dẫn gắn chương/tầng, mục tiêu, mức ước lượng hoặc hồ sơ boss, và phần thưởng lần
  đầu. Node khóa chỉ xem trước; nút Vào tầng bị vô hiệu hóa.
- Thưởng dùng cùng `TowerRewards.forFloor` như màn chiến thắng, tầng đã qua ghi rõ
  thưởng đã nhận. Tiến độ lưu không bị reset; ví vốn suy ra từ tiến độ sẽ được
  tính lại theo catalog mới, không giữ tổng phần thưởng của quy luật cũ.

Giữ sáu mốc và kết thúc, nhưng chương 5 dẫn vào nhà nguyện của Silent Bishop;
chương 6 dẫn đến vương miện vàng và cuộc thử sức của Aria. Tầng 42 không nhắc
Nữ Hoàng Máu trở lại; tầng 45 giải thích vì sao Aria vừa đấu với người chơi.
Mỗi node vẫn chỉ có 1–2 câu riêng, không tiết lộ vòng lặp trước thoại mốc.
