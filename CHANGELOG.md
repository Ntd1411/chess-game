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

## 2026-10-14 05:28 GMT+7 — Sửa lỗi Compose khi mở ván đấu tầng
- Lint phát hiện remember trả Unit ở mã cũ. Chuyển khởi tạo sang DisposableEffect trước attach AI, chặn render/báo kết quả trước khi khởi tạo, giữ khóa tầng/lượt và detach. Không sửa engine hay luật chiến dịch.
- Kiểm tra cuối: assembleDebug, 277 app unit test và lintDebug qua (vẫn có cảnh báo). Engine 78 test qua, AI 24 qua/1 bỏ qua, LAN 35 qua. Chưa QA thiết bị.

## 2026-10-14 05:20 GMT+7 — Vẽ nốt cửa sổ phong cấp
- Dialog tối viền vàng, bốn ô quân tự vẽ và Hủy. Chỉ hiển thị nước phong cấp engine cung cấp theo thứ tự Hậu/Xe/Tượng/Mã, không tự chọn Hậu và không sửa luật cờ.
- Kiểm tra: assembleDebug và 276 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:18 GMT+7 — Vẽ nốt thông báo trong ván LAN
- Thông báo lỗi/thử lại/đóng đổi sang card và nút tự vẽ. Xác nhận xin hoà/đấu lại/đầu hàng giữ hai callback đồng ý/từ chối, không sửa socket, protocol hoặc đồng hồ.
- Kiểm tra: assembleDebug và 275 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:17 GMT+7 — Vẽ nốt điều khiển Máy vs Máy
- Thay bốn nút ảnh runtime bằng nút tự vẽ trên một hàng vuốt ngang, chữ/touch target dùng dp thật. Giữ bàn cờ, lifecycle AI và quy tắc dừng/tiếp/đi một nước/auto/tốc độ.
- Kiểm tra: assembleDebug và 275 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:14 GMT+7 — Vẽ lại Splash
- Giữ nền/logo sẵn có, thanh tải Compose tự vẽ neo dưới vùng an toàn. Tiến độ vẫn lấy preload thật, giữ fade và thời gian chờ tối thiểu/giữ 100 phần trăm trước khi vào menu.
- Kiểm tra: assembleDebug và 274 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:12 GMT+7 — Vẽ lại Thất bại
- Card tự vẽ đọc rõ tên tầng, lý do và lời động viên, huy hiệu giữ từ asset. Nội dung cuộn với nút Thử lại/Về sảnh neo dưới. Giữ hai lý do thua và callback chơi lại.
- Kiểm tra: assembleDebug và 273 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:10 GMT+7 — Vẽ lại Chiến thắng
- Giữ nền/huy hiệu, card phần thưởng tự vẽ với số liệu thật, nội dung cuộn và hai nút Tiếp tục/Về sảnh neo dưới. Giữ thông báo tầng cuối/tầng mở và luồng ghi tiến độ.
- Kiểm tra: assembleDebug và 272 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:08 GMT+7 — Vẽ lại Hội thoại
- Giữ portrait/cốt truyện, card thoại tự vẽ chữ sáng cuộn được, chỉ số trang và nút Auto/Bỏ qua/Tiếp neo dưới. Giữ kết thúc khi hết thoại, bỏ qua và nhịp Auto 3 giây, không tự bỏ dòng cuối.
- Kiểm tra: assembleDebug và 271 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:05 GMT+7 — Vẽ lại Chọn chế độ
- Năm card tự vẽ với tên/mô tả đủ, nội dung cuộn và header/footer vùng an toàn; giữ điều hướng từng chế độ, bỏ khung ảnh và tọa độ cố định
- Kiểm tra: assembleDebug và 269 app unit test qua; chưa QA thiết bị.

## 2026-10-14 05:00 GMT+7 — Vẽ lại Main menu
- Giữ nền/ảnh đại diện mới, tự vẽ header tên/cấp/tiến độ và số dư; hai mode chính nổi bật, ba mode phụ và lưới sáu shortcut có Shop thay Thành tựu
- Nội dung cuộn, không dùng ảnh làm nút/khung hay tọa độ cố định; vẫn vào đủ năm mode và hồ sơ/nhật ký/túi đồ/lịch sử/cài đặt
- Hộp tiếp tục/bỏ ván dở đổi sang dialog tự vẽ, vẫn không đóng bằng Back/chạm ngoài và giữ xử lý lưu ván; assembleDebug và 269 app unit test qua; chưa QA thiết bị

## 2026-10-14 04:55 GMT+7 — Thêm Shop màu bàn và bộ quân
- Shop tự vẽ có preview bàn/quân thật, hai danh mục, xác nhận mua bằng vàng Tháp Cờ và áp dụng đồ sở hữu; giá tập trung trong ShopCatalog, không thanh toán tiền thật
- Sở hữu và tổng vàng đã tiêu ghi nguyên tử trong DataStore, mua lặp không trừ thêm; menu/túi đồ trừ khoản chi khỏi tổng thưởng, cài đặt khóa lựa chọn chưa mua
- Gothic/bộ quân ảnh miễn phí, lựa chọn đang dùng của bản cũ được giữ; khôi phục cài đặt không mất đồ mua, xoá toàn bộ dữ liệu mới xoá giao dịch
- Thay Thành tựu bằng Shop và nối điều hướng/nhạc/Back; assembleDebug và toàn bộ 271 app unit test qua, gồm số dư/lưu lại/song song/reset/migration; chưa QA thiết bị

## 2026-10-14 04:51 GMT+7 — Vẽ lại Khám phá Tháp Cờ
- Node tròn và đường nối tự vẽ, danh sách tầng cuộn từ 49 xuống 1; ô chi tiết chữ nhật vẫn nằm bên cạnh, cuộn riêng với teaser/boss/Elo/phần thưởng
- Header/footer neo vùng an toàn, tự cuộn tới tầng hiện tại; tầng khóa chỉ xem trước, kiểm tra lại khóa cả lúc bấm vào; giữ 49 tầng và luật chiến dịch
- Thay test tọa độ cũ bằng thứ tự/chỉ số cuộn/khoá tầng; assembleDebug, LobbyLayoutTest và campaign regressions qua; chưa QA thiết bị

## 2026-10-14 04:36 GMT+7 — Vẽ lại Cài đặt
- Dialog toàn màn giữ ván bên dưới; tab ngang cuộn, card tối–vàng và hàng switch/radio tự vẽ bấm cả hàng, tối thiểu 56dp; footer và bàn phím dùng vùng an toàn
- Giữ đủ năm tab và mọi setter thật; tên có IME Done, khôi phục chỉ cài đặt, xoá dữ liệu vẫn cần xác nhận với cảnh báo đầy đủ
- Thay test tọa độ cũ, giữ kiểm tra độc lập với setting_line và thêm test nhãn toàn bộ palette/bộ quân; assembleDebug và toàn bộ 264 app unit test qua; chưa QA hình ảnh/tương tác/bàn phím trên thiết bị

## 2026-10-14 04:32 GMT+7 — Vẽ lại màn Lịch sử và Xem lại
- Thẻ tối viền vàng co theo nội dung, danh sách cuộn có key và thống kê; header/footer ở vùng an toàn, giữ nút xem lại/xuất PGN/xoá và hiện tên đối thủ nếu có
- Xem lại dùng bàn cờ vuông co theo chiều rộng/cao, nút trước/sau tự khoá tại biên; giữ palette/bộ quân từ cài đặt và kiểm tra nước bằng engine
- Thêm xác nhận trước xoá một ván/toàn bộ; giữ DAO/IO và Intent chia sẻ PGN, thêm test dựng thế cờ/chuỗi hỏng/nhãn; assembleDebug và HistoryPresentationTest cùng history regressions qua; chưa QA thiết bị

## 2026-10-14 04:29 GMT+7 — Vẽ lại màn Hồ sơ
- Card chân dung/tên, cấp và thanh tiến độ tự vẽ tối–vàng; tám thống kê hai cột, nội dung cuộn và nút quay lại neo dưới
- Bỏ bố cục tọa độ cũ khiến bảng thống kê vượt đáy màn; giữ tính cấp, tỉ lệ thắng, tầng và thời gian từ PlayerStats
- Thay hai test hình học cũ bằng kiểm tra đủ chỉ số và tiến độ thật; assembleDebug thành công, toàn bộ 259 app unit test qua; chưa QA thiết bị

## 2026-10-14 04:24 GMT+7 — Vẽ lại màn Túi đồ
- Dùng ô vuông tự vẽ tối viền vàng, trạng thái chọn rõ và lưới co giãn 1–4 cột; nội dung/mô tả cuộn, header và Quay lại neo vùng an toàn
- Giữ icon, ba số lượng ví và đủ 13 di vật theo thứ tự cũ, điều kiện mở không đổi; ô khóa xem được tầng mở, mô tả không bị cắt ba dòng
- Thay test lưới tọa độ cũ và thêm test số lượng/biên mở di vật; assembleDebug và các test InventoryPresentationTest/LobbyLayoutTest/TowerRewardsTest thành công; chưa QA trên thiết bị

## 2026-10-14 04:15 GMT+7 — Cân bằng chiều cao tất cả thẻ Nhật ký
- Mọi thẻ chừa hai dòng tên và một dòng tầng mở, kể cả thẻ đã gặp/chưa rõ; badge trạng thái luôn một dòng, dùng line-height chung để không lệch giữa thẻ khóa và thẻ mở
- Giữ portrait vuông, lưới co giãn và khả năng tăng kích thước theo cỡ chữ; không đổi trạng thái/mở khóa hay nội dung chi tiết
- Thêm test dòng tên/footer và điều kiện hiện tầng mở; assembleDebug, JournalPresentationTest và JournalPagesTest đều đạt, git diff --check sạch. Chưa đo/kiểm tra trực quan trên thiết bị

## 2026-10-14 04:00 GMT+7 — Vẽ lại panel Đổi cấp và trang Nhật ký
- Panel Đổi cấp dùng cửa sổ tối bo góc/viền vàng, ba dòng Dễ/Vừa/Khó có trạng thái chọn và ngân sách thời gian thật; chọn áp dụng ngay, Hủy/đóng/Back/chạm ngoài không đổi mức
- Nhật ký dùng card tự vẽ và lưới cuộn co giãn 1–3 cột, header/footer neo mép an toàn, chi tiết cuộn với portrait/sao/hồ sơ/thoại/lịch sử gặp; giữ ID đang xem và vị trí lưới khi quay lại
- Nhân vật chưa gặp vẫn giấu tên và dùng ?/khóa thay vì hiển thị portrait mờ; card khóa ghi tầng mở. Không thay đổi điều kiện mở, dữ liệu Nhật ký hay logic AI/ván chơi
- Thêm test trình bày Đổi cấp/Nhật ký và thay test lưới tọa độ cũ; assembleDebug và 88/88 test liên quan đạt, toàn app 254/255 đạt (còn lỗi ProfileLayout cũ ở profileBlocksDoNotOverlap), git diff --check sạch. Chưa kiểm tra trực quan/cuộn/Back trên thiết bị

## 2026-10-14 03:43 GMT+7 — Thêm hai AI và bảy boss riêng, đồng bộ portrait/cốt truyện/mở khóa
- Màn AI chỉ dùng portrait trong res; thêm Silent Bishop và Golden Queen với ảnh, hồ sơ/tính cách/lời thoại riêng, thay hai boss tái đấu ở tầng 35/42
- Chỉ Pháp Sư Cờ, Hiệp Sĩ Bóng Đêm và Nữ Hoàng Máu mở sẵn; Vua Hắc Ám/Silent Bishop/Golden Queen/nhân vật ẩn mở sau khi thắng tầng 28/35/42/49, giữ tiến độ đã lưu
- Nối Golden Queen với Aria/Nữ Hoàng Ký Ức; sửa lời dẫn chương 5/6 và thoại tầng 21/42/45, bỏ câu tái đấu, giữ twist và kết thúc. Quy luật 6 puzzle + 1 boss và giới hạn tìm kiếm tăng dần không đổi
- Truyền/lưu ID AI qua điều hướng, GameUiState, SavedStateHandle và bản lưu ván; hiển thị đúng tên/portrait kể cả các bot cùng mức Khó, tạo ván mới theo token khi chọn AI và giữ fallback cho bản lưu cũ
- Thêm/cập nhật test bảy boss, biên mở khóa, Nhật ký, cốt truyện và portrait theo ID; assembleDebug và 144/144 test liên quan đạt. Toàn app 247/248 đạt, còn lỗi ProfileLayout cũ ở profileBlocksDoNotOverlap; portrait WebP đọc được, git diff --check sạch. Chưa kiểm tra trực quan/lưu-khôi phục trên thiết bị

## 2026-10-14 03:29 GMT+7 — Vẽ lại cửa sổ menu trong màn chơi
- Thay khung gothic/setting_line và icon ảnh bằng cửa sổ Compose tự vẽ nền tối bo góc, viền vàng mảnh và icon vector; Tiếp tục nổi bật, Rời trận tách riêng bằng đường kẻ/nền đỏ nhẹ
- Chữ căn trái, vùng bấm tối thiểu 56dp tự giãn theo chữ; nội dung cuộn khi màn thấp. Nút đóng, Back và chạm ngoài đều tiếp tục ván như trước
- Giữ nguyên danh sách hành động/callback, logic đồng hồ và xử lý menu của ván thường/chiến dịch; không sửa SettingsPanel hay asset trên bàn chơi
- Cập nhật test nhãn/icon/kích thước và tài liệu UI; assembleDebug, PauseMenuTest, PauseMenuArtworkTest, AssetStateTest, GamePlayAssetTest và LocalClockTest đều đạt, git diff --check sạch. Chưa kiểm tra trực quan trên thiết bị

## 2026-10-14 03:11 GMT+7 — Vẽ lại sảnh LAN và màn chọn đối thủ Người vs Máy
- Thay khung/nút ảnh bằng card tối viền vàng và nút Compose đơn giản; dùng chung header/footer neo mép an toàn, nội dung cuộn và tránh bàn phím ở sảnh LAN
- Giữ địa chỉ/sao chép, discovery, nhập IP, ba thể thức giờ, chờ/huỷ phòng và tự bắt đầu ván LAN; giữ khóa phòng bận/lệch phiên bản, thêm IME Go cho IP
- Giữ năm AI, artwork, độ khó, tính cách/lời thoại và điều kiện mở; carousel vuốt ngang, mũi tên chuyển vòng, AI khóa vẫn xem được nhưng không bắt đầu ván
- Thay test tọa độ AI/LAN cũ bằng test điều hướng, điều kiện mở, nhãn/trạng thái phòng và thể thức; không chỉnh logic engine/AI/network
- Kiểm tra: assembleDebug và các test chọn AI/Local/spectate đạt; toàn bộ app đạt 243/244 test (còn lỗi ProfileLayout cũ ở profileBlocksDoNotOverlap), net có 35/35 test đạt; git diff --check sạch. Chưa kiểm tra trực quan, bàn phím hay kết nối hai thiết bị

## 2026-10-14 02:42 GMT+7 — Vẽ lại màn Hai người cùng máy đồng bộ với Máy vs Máy
- Dùng card tối viền vàng mảnh, avatar tròn, nhãn bên Trắng/Đen và ô tên tự vẽ bằng Compose; nhóm thời gian bốn mức thành lưới 2×2 dễ đọc
- Header/back sát mép trên vùng an toàn; nút Bắt đầu/Quay lại neo dưới, phần nội dung cuộn độc lập và tránh bàn phím. Next chuyển sang tên người chơi 2, Done đóng bàn phím
- Giữ tên mặc định, giới hạn 20 ký tự, chuẩn hóa tên và logic đồng hồ/ván đấu; dùng chung header/nút đơn giản với Máy vs Máy, không thay đổi chức năng spectate
- Kiểm tra: assembleDebug, LocalClockTest, toàn bộ test spectate và git diff --check đều đạt; chưa kiểm tra trực quan/bàn phím trên thiết bị hoặc emulator

## 2026-10-14 02:30 GMT+7 — Làm lại màn chọn Máy vs Máy bằng card và nút tự vẽ đơn giản
- Thay khung/nút ảnh ở bước chọn hai AI bằng card tối viền vàng mảnh, avatar tròn và ba nút Dễ/Vừa/Khó có trạng thái chọn rõ ràng
- Đưa nút quay lại và tiêu đề lên sát mép trên vùng an toàn; neo hai nút Bắt đầu/Quay lại sát mép dưới thực của màn hình thay vì khung 1080×1920 căn giữa
- Nội dung hai AI cuộn độc lập trên màn nhỏ, giữ hai nút dưới luôn hiện; giữ nguyên độ khó được chọn qua xoay màn và không đổi logic ván xem/điều khiển AI
- Kiểm tra: assembleDebug, test spectate và git diff --check đều đạt; chưa kiểm tra trực quan trên thiết bị/emulator

## 2026-10-14 02:20 GMT+7 — Chỉnh ba vấn đề lời thoại và lời chào boss tái đấu
- Thoại Aria tầng 45 nói rõ vòng lặp bắt đầu khi thất bại ở đỉnh tháp, không phải vừa đến đỉnh; đổi câu hỏi của Người Lữ Hành sang cách nói trung tính, không gọi Aria là “bà”
- Hiệp Sĩ tầng 35 và Nữ Hoàng Máu tầng 42 có lời chào tái đấu riêng; giữ lời chào mặc định ở lần đầu và màn chọn AI
- Lời dẫn tầng 20 dùng hình ảnh “dấu tay mờ trên đá” thay cho “dấu tay đã khô”

## 2026-10-14 02:13 GMT+7 — Viết lời dẫn riêng cho đủ 49 tầng, bỏ lặp theo chương
- Thay 7 câu mẫu dùng chung theo chương bằng 49 đoạn giới thiệu riêng, mỗi tầng 1–2 câu ngắn gắn cảnh, nhân vật hoặc di vật; giữ các mốc cốt truyện và không thêm hướng dẫn chơi
- Thêm test kiểm tra 49 lời dẫn khác nhau, không quá hai câu và không chứa câu “Tìm chuỗi”

## 2026-10-14 02:06 GMT+7 — Rút gọn giới thiệu tầng còn 1–2 câu
- Bỏ câu hướng dẫn lặp “Tìm chuỗi chiếu…” và câu yêu cầu thắng ván khỏi lời dẫn tầng
- Panel chọn tầng chỉ giữ lời dẫn ngắn, tên/chân dung boss, nhãn độ khó và phần thưởng; bỏ đoạn hướng dẫn, hồ sơ dài và thoại trùng. Lời chào và tính cách boss trong màn đấu vẫn giữ nguyên

## 2026-10-14 01:54 GMT+7 — Đổi Tháp Cờ sang 42 puzzle tăng dần và 7 boss, thêm bảng xem trước tầng
- Mỗi chương gồm 6 puzzle riêng rồi một ván boss đầy đủ ở tầng chia hết cho 7; Elo puzzle ước lượng bắt đầu từ 800, giữ tiến độ và sáu mốc thoại
- Boss dùng nhân vật, chân dung, tính cách và lời thoại từ danh sách AI; mỗi chương tăng giới hạn tìm kiếm thật, không gán Elo chưa kiểm chứng cho boss
- Bản đồ cho chọn cả tầng khóa để xem mô tả, phần thưởng lần đầu và di vật mở ở tầng đó trong panel bên cạnh; chỉ tầng đã mở mới được vào chơi
- Bộ 42 thế có đáp án chuỗi chiếu liên tiếp chiếu hết trong 1–3 nước; Elo tăng 25 mỗi puzzle đến 1825 nhưng chưa được calibration, hồ sơ AI vẫn dùng tính cách/phong cách nhập vai chứ không đổi thuật toán theo nhân vật
- Tiến độ không reset; số dư suy ra từ các tầng đã qua được tính lại theo bảng thưởng mới
- Kiểm tra: assembleDebug và 64 test campaign/puzzle/layout liên quan đều đạt; AI 24 đạt, 1 self-play dài bỏ qua theo cờ môi trường. Toàn bộ app 233/234 đạt, còn lỗi Profile SubScreenLayoutTest.profileBlocksDoNotOverlap đã có từ trước, không sửa ngoài phạm vi. Chưa kiểm tra giao diện trên thiết bị/emulator

## 2026-10-14 01:01 GMT+7 — Tự vẽ player/timer đơn giản và đưa nước đi lên dải ngang đầu màn chơi
- Thay frame ảnh bằng hai card cùng bố cục, nền tối bo góc và viền mảnh; avatar tròn, tên và quân đã ăn căn trái, timer chữ monospace lớn bên phải
- Nhấn viền/timer vàng cho bên đang tới lượt, cảnh báo đỏ khi ít giờ; giữ nguyên đồng hồ thật và ký hiệu ∞ cho ván không giới hạn, không nạp trước hai frame ảnh cũ
- Chuyển danh sách SAN lên đầu màn thành LazyRow vuốt ngang, nhấn nước offline để xem lại và có nút về thế hiện tại; theo nước mới khi ở cuối nhưng không giật cuộn khi đang đọc nước cũ
- Áp dụng dải nước đi cho offline, campaign, spectate và LAN; dựng SAN của LAN trên bàn cờ riêng, nhớ kết quả theo lịch sử nước đi và giữ số nước đúng với FEN bắt đầu bằng Đen
- Cập nhật test bố cục card, timer, các vùng không chồng nhau, số nước/điều kiện cuộn và SAN LAN; build debug thành công, toàn bộ 230 test còn đúng 1 lỗi Hồ sơ có sẵn ngoài phạm vi

## 2026-10-14 00:45 GMT+7 — Chuyển setting_line về đúng Menu/Pause của gameplay
- Hoàn tác toàn bộ thay đổi giao diện Cài đặt từ lượt trước, trả lại panel, đường phân cách và khoảng cách hàng ban đầu
- Dùng setting_line làm khung các dòng Tiếp tục/Khởi động lại/Cài đặt/Rời trận trong Pause Menu; giữ hai đầu hoa văn bằng nine-slice
- Căn icon và chữ trong cùng hàng bấm được, chiều cao 56dp và khoảng cách 8dp; giữ nguyên callback và hành vi Back/chạm ngoài để tiếp tục ván
- Giữ nguyên sửa lỗi frame player, asset nút mới và việc bỏ nút Lật bàn/Cài đặt khỏi hàng thao tác gameplay
- Build debug và các test liên quan đến Pause Menu, Cài đặt, asset và crop frame đều thành công

## 2026-10-14 00:32 GMT+7 — Sửa frame player ẩn, thay asset nút gameplay và thu gọn Cài đặt
- Cắt vùng artwork trong BitmapPainter, ánh xạ tọa độ theo bitmap đã sample thay vì dùng ảnh quá khổ và offset trong Box bị ép kích thước; giữ nguyên vị trí avatar, tên, đồng hồ và quân đã ăn
- Chuyển setting_line và bốn nút Tiếp tục/Đi lại/Đổi cấp AI/Menu sang WebP lossless, xác minh pixel RGBA và lưu PNG gốc trong app/build/gameplay_controls_backup
- Dùng setting_line làm nền từng hàng Cài đặt; giảm chiều cao hàng 200 xuống 144, khoảng cách hàng 8 và nhóm 16, căn đều lề nhãn và cột control
- Bỏ nút Lật bàn và Cài đặt khỏi hàng thao tác màn chơi; Cài đặt vẫn truy cập qua Pause Menu, đồng hồ vẫn dừng khi mở overlay
- Bổ sung test crop theo nhiều mức sample, kiểm tra pixel frame hiển thị, asset và bố cục Cài đặt; build debug và các nhóm test liên quan thành công
- Toàn bộ 224 test còn 1 lỗi có sẵn ở SubScreenLayoutTest.profileBlocksDoNotOverlap; không sửa trang Hồ sơ ngoài phạm vi

## 2026-10-13 23:56 GMT+7 — Căn gameplay theo hai frame player mới và khung bàn cờ mới
- Chuyển hai frame PNG 1536x1024 sang WebP lossless, xác minh từng pixel RGBA; sao lưu trong app/build/player_frames_v2_backup
- Căn avatar tròn, tên sát trái, đồng hồ và hàng quân đã ăn theo tọa độ artwork; bàn cờ đặt đúng vùng rỗng của khung mới, không nine-slice
- Chuyển đồng hồ LAN vào ô thời gian, ánh xạ đúng bên trắng/đen; ván không giới hạn hiện ∞, quân đã ăn và lợi thế vật chất nằm trong từng header
- Build debug và 5 nhóm test gameplay/asset/bố cục thành công; toàn bộ 218 test còn 1 lỗi có sẵn ở SubScreenLayoutTest.profileBlocksDoNotOverlap, không sửa trang Hồ sơ ngoài phạm vi

## 2026-10-13 23:04 GMT+7 — Áp dụng bộ asset gameplay mới và chuyển 20 PNG sang WebP lossless
- Xác minh toàn bộ pixel RGBA và kích thước canvas bằng Pillow; tổng dung lượng 25,127,806 → 16,196,422 byte
- Chuẩn hóa tên quân Tượng Trắng và Tốt Đen, đưa asset ra gốc drawable; sao lưu PNG và bộ quân cũ trong app/build/gameplay_asset_backup
- Tích hợp nền, khung người chơi, nút chơi lại/đầu hàng/hòa/cài đặt/rời trận và bộ 12 quân mới vào giao diện dùng chung, giữ nguyên logic ván đấu; nền Boss giữ riêng
- Căn lại tên, chân dung, HP và đồng hồ theo hai khung mới; thêm nút mở Cài đặt trực tiếp ở ván offline, giữ hành vi Pause và dừng đồng hồ
- Bổ sung 4 test kiểm tra asset, ánh xạ nút, preload và bố cục không chồng nhau; toàn bộ :app:testDebugUnitTest và :app:assembleDebug thành công

## 2026-10-13 22:46 GMT+7 — Chuyển hai ảnh thanh loading Splash sang WebP lossless
- Giữ nguyên canvas 1536x1024 và toàn bộ pixel RGBA, bao gồm vùng trong suốt; xác minh bằng Pillow sau khi giải mã WebP
- Giữ bản PNG gốc trong app/build/splash_png_backup, loại PNG khỏi drawable để tránh trùng resource
- Cập nhật SplashAssetAlignmentTest để đọc WebP qua ImageIO plugin chỉ dành cho unit test; toàn bộ :app:testDebugUnitTest và :app:assembleDebug thành công
- Dung lượng frame giảm 1.266.215 → 651.910 byte (48,52%), fill giảm 74.081 → 27.566 byte (62,79%)

## 2026-10-13 20:09 GMT+7 — Thay bộ font UTM bằng FZ Poppins và chuẩn hóa tên tài nguyên
- Đổi tên 18 file font sang `fz_poppins_*.ttf` đúng chuẩn Android (chữ thường, gạch dưới, không khoảng trắng), bỏ tham chiếu hai font UTM đã được người dùng xóa
- Khai báo đủ 9 độ đậm Thin–Black cùng các bản Italic thật trong `GameFontFamily`; toàn bộ chữ Material, DesignText, nút, hộp thoại và ô nhập dùng bộ font mới
- Phân cấp chữ: Regular cho nội dung/ô nhập, Medium cho nhãn nhỏ, SemiBold cho tiêu đề phụ/nhãn lớn, Bold cho tiêu đề chính và nút; giữ nguyên cỡ chữ và font glyph quân cờ
- Cập nhật test kiểm tra đủ 18 ánh xạ độ đậm/kiểu nghiêng, tên file hợp lệ và hỗ trợ ký tự tiếng Việt; test app và build debug thành công

## 2026-10-13 19:49 GMT+7 — Áp dụng hai font người dùng cung cấp vào toàn bộ chữ giao diện
- Đóng gói `font_normal.ttf` (UTM NguyenHa 02) và `font_bold.ttf` (UTM NguyenHa 01), khai báo `GameFontFamily` với hai độ đậm Normal/Bold
- Cập nhật đủ 15 kiểu chữ Material; nội dung dùng font thường, tiêu đề và nhãn dùng font đậm, giữ nguyên cỡ chữ
- Áp dụng font cho `DesignText`, nút gothic, ô nhập và hộp thoại có TextStyle riêng; không thay font glyph quân cờ và chữ đã nằm trong ảnh
- Bổ sung test cấu hình typography và tính hợp lệ của font, kiểm tra ký tự tiếng Việt

## 2026-10-13 19:36 GMT+7 — Căn ruột thanh loading khớp với lòng khung Splash
- Chỉnh `splash_loading_fill.png` trên canvas gốc 1536x1024: thu ngang còn 85%, thu dọc còn 90%, dịch lên 0,5% chiều cao để ruột không tràn ra hoa văn hai đầu
- Cập nhật vùng cắt tiến độ thành 10,9–89,1%; giữ nguyên khung, vị trí thanh và logic chờ loading hoàn tất
- Bổ sung test kiểm tra kích thước canvas, biên ruột và khoảng hở trong khung

## 2026-10-13 00:35 GMT+7 — Chờ thanh loading chạy đầy trước khi vào menu chính
- `SplashScreen` chờ tiến độ đang hiển thị đạt 100%, thay vì chuyển màn ngay sau khi nạp ảnh và hết thời gian tối thiểu
- Giữ thanh đầy thêm 150 ms để người chơi thấy loading hoàn tất; dùng callback điều hướng mới nhất

## 2026-10-13 00:30 GMT+7 — Làm lại màn Splash bằng bộ 3 ảnh mới, không còn chữ
- Thay `bg_splash.png` bằng nền mới (đã vẽ sẵn logo Tower of Chess); ảnh cũ chuyển ra `app/build/old_res`
- Thêm `splash_loading_frame.png` và `splash_loading_fill.png` (từ thư mục `drawable/splash`, đã chuyển ra `drawable/` vì Android không nhận thư mục con)
- `SplashScreen` chỉ còn nền + thanh tải (bỏ logo, hai hoa văn và nhãn phần trăm), bỏ lớp làm tối nền; `SplashLayout` chỉ còn `Bar` và vùng ruột 4,0–95,8%
- Cập nhật test `SubScreenLayoutTest` cho bố cục splash mới

## 2026-10-12 22:00 GMT+7 — Viết README cho dự án
- README.md mới (tiếng Việt): giới thiệu Tower of Chess, bảng tính năng các chế độ chơi và tính năng phụ
- Mô tả cấu trúc module (:app, :engine, :ai, :net) và hệ thống UI (DesignCanvas, ArtCache, test *Layout)
- Yêu cầu môi trường, lệnh build/cài đặt/test, LAN CLI, ký bản release và workflow CI, bảng tài liệu, quy ước đóng góp

## 2026-10-12 21:50 GMT+7 — Thiết kế lại Splash, Sảnh chính, Chọn chế độ, Chọn đối thủ AI bằng bộ asset mới
- Chuyển 53 ảnh từ `drawable/Splash`, `drawable/MainMenu`, `drawable/SelectModeAndSelectAI` ra `drawable/` (Android không nhận thư mục con); đổi `mode_card_story_art..png` thành `mode_card_story_art.png`; 15 ảnh cũ trùng tên (bg_splash, bg_main_menu, bg_mode_select, icon_back, portrait_protagonist, character_protagonist_fullbody, loading_bar_*, các icon chế độ…) được thay bằng bản mới
- Thêm `ArtWidgets.kt`: `ArtBar` (thanh tiến độ từ cặp ảnh khung/ruột cùng canvas, ruột cắt ngang) và `ArtPressable` (vùng chạm thu nhỏ khi nhấn, mờ khi tắt)
- Splash: logo `logo_chess`, hoa văn `splash_logo_divider`/`splash_loading_divider`, thanh tải mới; thêm `SplashLayout`
- Sảnh chính: avatar `frame_player_avatar` + thanh kinh nghiệm `bar_player_xp_*`, ô tiền `panel_currency_counter` + nút cộng (mở Túi đồ), quầng sáng sau nhân vật, 6 nút tròn `frame_side_button` chia hai bên, thẻ chế độ `panel_mode_card_red/blue/small` (thêm thẻ Máy vs Máy); `MenuScreen` nhận thêm `levelProgress`, `onOpenSpectate`
- Chọn chế độ: khung `frame_gothic_screen`, 5 thẻ tranh `mode_card_*_art` trong `frame_mode_card`, hoa văn `ornament_screen_bottom`; thêm `ModeSelectLayout`
- Chọn đối thủ AI: nền `bg_ai_select`, nhân vật giữa (ảnh toàn thân hoặc chân dung trong `frame_character_thumb`), mũi tên lật, sao độ khó mới, ô khóa `overlay_character_locked`, panel `panel_ai_description`, nút `button_ai_start`; thêm `AiSelectLayout`
- Nữ Hoàng Máu dùng `portrait_ai_blood_queen` + `character_ai_blood_queen`; Pháp Sư Cờ tạm dùng `portrait_ai_blue_queen`
- Test: cập nhật `LobbyLayoutTest`, `SubScreenLayoutTest` cho bố cục mới

## 2026-10-09 21:15 GMT+7 — Hệ panel tối giản, gọn lại Hồ sơ/Cài đặt/Nhật ký/Túi đồ/Lịch sử/LAN/Sảnh
- Thêm `QuietPanel.kt`: `Modifier.quietPanel(PanelTone.BASE/SUNKEN/SELECTED)` (nền tối gradient, viền vàng tối mảnh, góc nhỏ; chỉ SELECTED có ánh đỏ thẫm + viền vàng sáng), `Hairline`, `SectionTitle`; thêm màu `GoldDim`, `PanelTop`, `PanelBottom`, `Crimson`, `Muted`
- `DesignCanvas` thêm `scrim` làm tối nền ở vùng nội dung
- Hồ sơ: bố cục mới `ProfileLayout` (nhân vật, bảng tên, panel cấp + thanh tiến độ, lưới thống kê 2x4) hết chồng chéo
- Cài đặt: tab ngang một hàng trên cùng, nội dung chia nhóm có tiêu đề, hàng có kẻ mảnh, control canh phải — hết khoảng trống
- Nhật ký, Túi đồ, Lịch sử: bỏ `ui_panel_main`/`ui_list_row`, dùng quiet panel; chữ phụ màu `Muted`
- Sảnh LAN: ô địa chỉ, ô người chơi, danh sách phòng, gợi ý trống dùng quiet panel; tiêu đề nhóm rõ ràng, chữ phụ nhỏ và nhạt hơn
- Sảnh chính: bỏ hoa văn trang trí, gom cột icon phải vào một thanh nền tối, ví vàng/pha lê trong một ô
- Test: cập nhật `SubScreenLayoutTest` (Cài đặt, Nhật ký, Hồ sơ, `panelStyle`, thanh icon Sảnh)

## 2026-10-09 20:30 GMT+7 — Gắn bộ asset mới: nút trận, nền, hộp thoại, banner kết quả
- Thêm `BattleButton` (normal/pressed/disabled) cho hàng nút ván đấu; `MatchAction.art` đổi sang `BattleButton?`. Gán restart/level/flip/map/resign/draw/rematch/sync/share/leave cho ván thường, Tháp, LAN, Máy vs Máy
- Banner `ui_result_banner_win/loss/draw` hiện giữa bàn cờ khi ván xong (chạm để cất); `ResultBanner.of` tính theo bên của người chơi
- Thêm `GothicDialog`/`GothicConfirmDialog` (`ui_dialog_panel`, `ui_dialog_small`); thay Pause Menu (icon resume/restart/settings/leave), Phong cấp (`ui_promotion_slot_*` + ảnh quân), Đổi cấp, xác nhận LAN, xóa dữ liệu, tiếp tục ván cũ; ô thông báo LAN vẽ trên `ui_dialog_small`
- Nền mới: Chọn chế độ `bg_mode_select`, Setup hai người `bg_local_setup`, Hồ sơ `bg_profile`, Lịch sử `bg_history`; thẻ Máy vs Máy dùng `icon_spectate`
- Chân dung `portrait_player_two` (người chơi 2), `portrait_lan_opponent` (đối thủ LAN, ô chờ, danh sách phòng)
- `GothicTextField` dùng `ui_text_field_normal/focused`; tab Cài đặt dùng `ui_tab_normal/selected`; dòng Lịch sử và dòng phòng LAN dùng `ui_list_row_normal/pressed` (chạm cả dòng = xem lại / vào phòng)
- Bàn cờ tô ô nước vừa đi bằng `board_tile_last_move` (nạp trước ở splash); màn thoại có dấu `icon_next` nhấp nháy
- Test mới `AssetStateTest` (trạng thái nút, banner, icon Pause). Chưa có font `font_title`/`font_body`

## 2026-10-09 19:45 GMT+7 — Vẽ lại Setup hai người, Hồ sơ và Lịch sử trên khung thiết kế
- Setup hai người cùng máy: hai tấm `ui_panel_sub` có chân dung, quân Vua từng bên, ô nhập `GothicTextField`; bốn nút thời gian gothic; nút Bắt đầu chính
- Hồ sơ: nhân vật toàn thân, tên trên `ui_name_plate`, thanh lên cấp `loading_bar_*`, bảng thống kê hai cột trong `ui_panel_main`
- Lịch sử: danh sách ván trên `ui_panel_sub` kèm huy hiệu kết quả và nút nhỏ; xem lại ván trong `chess_board_frame`, nút Back trong màn xem lại quay về danh sách

## 2026-10-09 19:25 GMT+7 — Vẽ lại sảnh LAN và sửa tỷ lệ khung thiết kế
- Sảnh LAN trên `bg_lan_lobby`: ô địa chỉ `lan_room_code_panel` + nút sao chép `lan_copy_icon`, hai ô `lan_player_slot` (miện vương chủ phòng, đèn sẵn sàng/đang chờ), nút thể thức gothic, danh sách phòng cuộn được trên `ui_panel_sub`, kết nối thủ công, nút Mở phòng / Huỷ phòng — giữ nguyên mọi chức năng cũ
- Sửa lỗi đọc `LocalDesign` bên ngoài `DesignCanvas` (tỷ lệ 1:1 sai) ở khung ván, chọn AI, thoại, kết quả tầng, túi đồ, bản đồ Tháp: khoảng cách, cỡ chữ và cuộn tới tầng hiện tại giờ đúng tỷ lệ
- Thêm kiểm thử bố cục sảnh LAN

## 2026-10-09 19:05 GMT+7 — Vẽ lại Cài đặt và Nhật ký theo bản thiết kế
- Cài đặt phủ toàn màn trên `bg_settings`: panel `ui_panel_main`, cột danh mục bên trái (tab chọn trên `button_secondary_normal`), hàng nhãn + control cuộn được với `toggle_on/off`, `radio_*`, nút "Khôi phục mặc định" (chỉ đặt lại cài đặt)
- Thêm `GothicTextField`, `GothicToggle`, `GothicRadio` dùng chung
- Nhật ký: lưới chân dung 3 cột trong `ui_character_frame`, huy hiệu trạng thái (`badge_complete/new/locked`, `icon_locked`), trang chi tiết có chân dung lớn, `ui_name_plate`, sao độ khó, `ui_separator`
- Thêm `SubScreenLayoutTest`

## 2026-10-09 18:45 GMT+7 — Vẽ lại khung ván đấu theo bản thiết kế Chess Battle
- `MatchScaffold` dựng trên `DesignCanvas`: khung đối thủ/mình (`battle_enemy_frame`/`battle_player_frame`) có chân dung, thanh máu suy từ quân đã mất, đồng hồ trong `battle_timer_frame`; bàn cờ trong `chess_board_frame`; hàng nút tròn (`battle_button_*`) căn giữa
- Hiệu ứng chiếu (`battle_check_indicator`, `fx_check`) và chiếu hết (`fx_checkmate`)
- Đấu máy, hai người, Tháp (boss dùng `bg_battle_boss` + chân dung Nữ hoàng Vàng), LAN và Máy vs Máy đều truyền chân dung/quân/nút mới
- Màn chọn cấp Máy vs Máy vẽ lại bằng panel, chân dung và `GothicButton`; hàng điều khiển xem ván dùng nút gothic
- Thêm `MatchLayoutTest` (vị trí nút, thanh máu)

## 2026-10-09 18:20 GMT+7 — Thiết kế lại các màn chính theo bộ asset mới và khung 1080x1920
- Chuyển sang bộ asset mới (bg_*, button_*, portrait_*, chess_piece_*, board_tile_*, map_*, item_*...), bỏ bộ cũ; đổi tên hai file `.png.png`
- Thêm `DesignCanvas` (khung thiết kế 1080x1920 co vừa màn, nền Crop phủ kín), `SlicedArt` (nine-slice không méo góc), cắt lề trong suốt khi vẽ ảnh, `GothicButton` chỉ vẽ một trạng thái nút
- Vẽ lại Splash, Sảnh chính (avatar, cấp, vàng/pha lê, nhân vật toàn thân, cột icon, 4 nút), Chọn đối thủ AI, Thoại, Chiến thắng, Thất bại, Bản đồ Tháp (node zíc-zắc, xích nối, nút Vào tầng) theo tọa độ bản thiết kế
- Thêm màn Chọn chế độ (5 thẻ, có Máy vs Máy) và Túi đồ (lưới 4x4, tooltip)
- Thêm `TowerRewards`/`Relic`: vàng, pha lê, sách cổ và di vật suy ra từ tiến độ Tháp, hiện ở sảnh, Túi đồ và màn Chiến thắng
- Bàn cờ ghép từ board_tile_* (không dùng chess_board.png), ô gợi ý/ăn/chọn/chiếu bằng tile riêng, fx_selected_piece_glow dưới quân đang chọn
- Test mới: TowerRewardsTest, DesignLayoutTest, LobbyLayoutTest viết lại; `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-04 16:46 GMT+7 — Đổi toàn bộ âm thanh từ wav sang ogg để giảm dung lượng APK
- Thay 9 file trong `res/raw` từ wav sang ogg (khoảng 63 MB xuống khoảng 4 MB), giữ nguyên tên nên code không đổi
- `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-04 02:05 GMT+7 — Nhạc nền theo màn hình, công tắc Nhạc nền và nối file âm thanh wav
- Thêm `MusicPlayer` (MediaPlayer, phát lặp, tạm dừng khi app xuống nền) và `MusicTrack`/`MusicPlan` chọn nhạc menu, chiến đấu, trùm, thắng, thua
- Thêm `MusicHost` ở gốc `MainActivity` và `PlayMusic` trong `AppRoot`, `TowerRoute`, `LanRoute` để mỗi màn khai báo bản nhạc
- Thêm công tắc Nhạc nền (`musicEnabled`) vào `SettingsStore` và tab Âm thanh của `SettingsPanel`
- Thêm 9 file wav vào `res/raw`, `SoundEffects` nhận cả đuôi wav và ogg
- Thêm `MusicPlanTest` và test nhạc nền trong `SettingsStoreTest`

## 2026-10-03 23:27 GMT+7 — Nền screen_chess_battle và chừa inset cho khung ván đấu dùng chung
- MatchScaffold dùng `gothicBackdrop` (mặc định screen_chess_battle, thêm tham số `backdrop`), nên GameScreen, CampaignBattleScreen và LanGameScreen cùng có nền gothic và chừa inset thanh hệ thống. SpectateScreen truyền `backdrop = screen_ai_vs_ai` thay vì tự gắn modifier để không vẽ nền hai lần
- `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-03 23:14 GMT+7 — Nền gothic cho sảnh LAN, Lịch sử, Máy vs Máy; chữ mặc định đọc được trên nền tối
- ui/art/GothicBackdrop.kt: Modifier `gothicBackdrop(res, dim)` vẽ ảnh nền cắt giữa (hàm thuần `centerCrop`) phủ tối, rồi mới chừa inset thanh trạng thái/điều hướng cho nội dung. Gắn vào LanLobbyScreen (screen_lan_lobby), HistoryScreen cả danh sách lẫn xem lại (screen_game_history), SpectateScreen cả bước chọn lẫn màn xem (screen_ai_vs_ai); các màn này trước đó chưa chừa inset. Không nạp trước ở splash để cache 64MB không bị đầy
- Theme: ChessTheme đặt `LocalContentColor` = Parchment vì app không còn Surface/Scaffold gốc (mặc định là đen, chữ đặt thẳng trên nền tối không đọc được)
- Thêm CenterCropTest (5 test). `:app:testDebugUnitTest` và `:app:assembleDebug` thành công

## 2026-10-03 22:46 GMT+7 — Bàn cờ gothic: ô marble, overlay phát sáng, quân ảnh làm mặc định
- BoardPalette thêm GOTHIC (textured) làm mặc định: ChessBoard vẽ tile_marble_light/dark, overlay tile_selected/move/attack/check, viền vàng mảnh; chưa nạp xong thì dùng màu phẳng dự phòng. Các bộ màu cũ giữ nguyên cách vẽ
- PieceTheme.IMAGE làm mặc định, quân Đen dùng piece_black_*_bright, quân nạp qua ArtCache (không còn painterResource 1254px) kèm quầng tối mờ phía sau
- SettingsStore mặc định GOTHIC + IMAGE, thêm nhãn palette_gothic trong SettingsPanel/strings, cập nhật 3 assert trong SettingsStoreTest. `:app:testDebugUnitTest` thành công

## 2026-10-03 22:42 GMT+7 — Splash mới, sau splash vào menu, menu dùng nút ảnh gothic
- AppRoot: SPLASH → MENU (trước đây vào thẳng sảnh LAN). Menu là màn gốc (back thoát app), sảnh LAN back về menu, hộp thoại tiếp tục ván dở chuyển sang menu
- SplashScreen: nền screen_splash_background, logo ui_logo_tower_of_chess, thanh tiến độ thật nạp trước ảnh menu/bàn cờ/quân cờ vào ArtCache (tối thiểu 1,4 giây)
- ui/art: thêm ArtAssets (danh sách nạp trước, ArtSizes) và PlateButton (nút dạng ảnh giữ tỉ lệ, hiệu ứng nhấn)
- MenuScreen: nền/nhân vật nạp qua ArtCache, quầng đỏ phía sau nhân vật, khung avatar, hai nút chính dùng ui_menu_button_primary_wide (+pressed), ba nút phụ dùng secondary_wide. LobbyLayoutTest vẫn pass. `:app:testDebugUnitTest` thành công

## 2026-10-03 22:39 GMT+7 — Thêm 35 asset gothic bổ sung, nền theme gothic cố định và bộ nạp ảnh ArtCache
- Đổi tên 35 ảnh trong asset_adding theo thời gian lưu khớp thứ tự prompt rồi chuyển vào res/drawable (screen_splash_background, ui_logo_tower_of_chess, ui_emblem_chess_tower, ic_launcher_art_*, ui_menu_button_*_wide(+_pressed), piece_black_*_bright, tile_*_overlay, tile_marble_light/dark, icon_* thanh chức năng và tiền tệ, ui_avatar_frame, screen_lan_lobby, screen_game_history, screen_ai_vs_ai)
- Theme gothic cố định (bỏ dynamic color), themes.xml nền tối chống nháy trắng lúc khởi động, MainActivity bỏ Scaffold, thêm ui/art/ArtCache (giải mã downsample có cache). Chưa nối vào các màn hình. `:app:testDebugUnitTest` và `:app:assembleDebug` đều thành công

## 2026-10-03 19:05 GMT+7 — Giai đoạn 6: đối chiếu thoại 6 tầng mốc, thử build release
- Đối chiếu `FloorDialogues` với story-bible: đủ 6 tầng mốc (1, 5, 21, 42, 45, 49) + epilogue tầng 49, đúng mạch Player #48 / Aria / Attempt #49. Đánh dấu xong trong completion-plan
- Thử `./gradlew :app:assembleRelease` qua MCP local hai lần nhưng connector báo server không phản hồi (nhiều khả năng build quá lâu so với giới hạn); chưa xác minh được. Cấu hình ký đã có sẵn trong app/build.gradle.kts (keystore.properties hoặc biến môi trường)

## 2026-10-03 18:41 GMT+7 — Giai đoạn 5 (phần 8): Pause Menu cho ván chiến dịch
- PauseMenu.itemsForCampaign() + test; CampaignBattleScreen thêm nút Menu và PauseMenuOverlay (Tiếp tục / Khởi động lại = chơi lại tầng / Cài đặt / Rời trận = về bản đồ). Back trong ván mở Pause thay vì thoát ngay
- LAN không thêm Pause: không thể tạm dừng đối thủ thật, `LanGameScreen` đã có Đầu hàng/Rời phòng
- Sửa comment lỗi thời trong GameScreen.timeoutLabel. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 18:20 GMT+7 — Giai đoạn 5 (phần 7): màn setup hai người cùng máy + đồng hồ cờ
- Thêm ui/screen/LocalSetupScreen.kt: nhập tên Người chơi 1 (Trắng) / Người chơi 2 (Đen), chọn thời gian Không giới hạn/5/10/30 phút, nút BẮT ĐẦU. Ô tên trống dùng tên mặc định. Chưa làm mục bật/tắt gợi ý và hiệu ứng của spec vì đã có cài đặt hiển thị nước đi hợp lệ toàn cục và chưa có hệ thống hiệu ứng
- AppRoot: thêm Screen.LOCAL_SETUP, menu "2 người cùng máy" đi qua setup rồi mới vào ván; GameScreen nhận `localStart` (token chống mở trùng khi xoay máy) và gọi `GameViewModel.startLocalGame`
- GameScreen/MatchScaffold: hiện tên hai bên, đồng hồ mm:ss ở hàng từng bên (đổi màu khi dưới 30 giây), dòng kết quả hết giờ. Đồng hồ tạm dừng khi mở Pause/Cài đặt, khi app xuống nền và khi rời màn
- Gom phần đã viết dở trước đó nhưng chưa commit: LocalSetup/LocalTimeControl/LocalClock/TimeoutRule, GameViewModel (đồng hồ, khôi phục, hết giờ, khóa Đi lại khi có giờ, ghi tên hai bên vào lịch sử), SavedGameStore lưu tên/thời gian/giờ còn lại
- Sửa LocalClockTest: `mmss(5 phút - 1ms)` phải là 05:00 vì làm tròn lên (kỳ vọng cũ 04:59 sai). Thêm string `local_*`, `status_timeout_*`. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:58 GMT+7 — Giai đoạn 5 (phần 6): màn hình Nhật ký nhân vật
- Thêm profile/Journal.kt (thuần): suy trạng thái ✓ đã gặp / ? chưa rõ / khóa cho từng nhân vật, dùng chung `isUnlocked` với màn chọn đối thủ để không mâu thuẫn. ProfileRepository thêm Flow `journal` (id → bản ghi gặp)
- Thêm ui/screen/JournalScreen.kt: danh sách nhân vật và trang chi tiết. Nhân vật chưa gặp bị che tên/ảnh. Trang đã gặp có câu thoại, độ khó, phong cách, tính cách, lịch sử gặp. Chưa có "bí mật đã phát hiện" vì chưa có hệ thống bí mật
- AppRoot thêm Screen.JOURNAL + JournalRoute (Room + tiến độ Tháp), back về menu. MenuScreen thêm `onOpenJournal` và nút Nhật ký cạnh Lịch sử
- Thêm string `journal_*`, test JournalPagesTest. `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:49 GMT+7 — Giai đoạn 5 (phần 5): màn hình Hồ sơ (Profile)
- Thêm ui/screen/ProfileScreen.kt: ảnh nhân vật giữa, tên + cấp, thanh tiến độ lên cấp, bảng thống kê (thắng/thua/hòa, tổng ván, tỉ lệ thắng, chiếu hết, tầng cao nhất x/49, thời gian chơi). Chỉ có phần Thông tin, chưa dựng tab Trang phục/Kỹ năng/Ký ức vì chưa có hệ thống đằng sau
- PlayerStats: thêm `progressPoints`, `pointsIntoLevel`, `winRatePercent`; thêm profile/PlayTime.kt (tách giờ/phút/giây và chọn đơn vị hiển thị)
- Sảnh: chạm avatar/tên hoặc nhân vật để mở Hồ sơ; AppRoot thêm Screen.PROFILE + ProfileRoute (đọc Room qua Flow), back về menu
- Thêm 20 string `profile_*`; test ProfileDisplayTest (12); `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:42 GMT+7 — AGENTS.md: commit message theo Conventional Commits
- Quy tắc 5 bổ sung: bắt buộc mở đầu bằng type (`feat:` `fix:` `docs:` `refactor:` `test:` `chore:` `style:` `perf:`), tiêu đề mô tả chính xác, các `-m` sau đủ chi tiết để hiểu thay đổi mà không cần xem code; ví dụ trong file đổi sang dạng `feat: ...`

## 2026-10-03 17:40 GMT+7 — AGENTS.md: thêm quy tắc viết commit message
- Thêm quy tắc 5 vào `.claude/AGENTS.md`: commit message bằng tiếng Anh, `-m` đầu là tiêu đề ngắn, nhiều `-m` sau cho từng nhóm thay đổi, cấm `;` `&&` `|` `>` `<` và xuống dòng trực tiếp
- Nếu MCP từ chối command commit thì đưa nguyên câu lệnh đúng dạng cho người dùng tự chạy, không tự đổi sang message sai dạng

## 2026-10-03 17:31 GMT+7 — Giai đoạn 5 (phần 4): ghi thắng/thua/hòa/checkmate/thời gian chơi vào Hồ sơ
- Thêm profile/MatchRecording.kt (luật thuần): chế độ nào tính vào Hồ sơ (chỉ đấu máy; 2 người cùng máy và Máy vs Máy không tính), đo giây chơi (cắt trần 3 giờ/ván), kết quả ván chiến dịch (chỉ tầng đánh bại máy mới tính checkmate)
- GameViewModel (đấu máy): gọi `recordMatch` khi ván kết thúc, mỗi ván đúng 1 lần (Undo rồi thắng lại không cộng 2 lần; xoay máy sau khi ván xong không cộng lại nhờ cờ `replaying`)
- LanViewModel: ghi theo phía máy này; checkmate chỉ tính khi thắng bằng chiếu hết thật (đầu hàng/hết giờ không tính)
- AppRoot/TowerRoute: ghi ở `onWon`/`onLost` của ván chiến dịch, đồng hồ đo từ đầu mỗi lượt chơi (`newAttempt`); tất cả lần ghi chạy NonCancellable
- Test: MatchRecordingTest (10); `:app:testDebugUnitTest` và `:app:assembleDebug` BUILD SUCCESSFUL

## 2026-10-03 17:35 GMT+7 — Giai đoạn 5 (phần 3): Cài đặt dạng tab
- Thêm ui/screen/SettingsPanel.kt (Dialog toàn màn, 5 tab Chung/Âm thanh/Hình ảnh/Điều khiển/Khác) dùng chung cho Sảnh và Pause; settings/DataReset.kt (Xóa dữ liệu có xác nhận: cài đặt, tiến độ Tháp, ván dở, lịch sử, Hồ sơ, Nhật ký)
- SettingsStore nhận DataStore (test được trên JVM), thêm `showLegalMoves` + `resetToDefaults`; nối công tắc này vào bàn cờ GameScreen/CampaignBattle/LAN; TowerProgressStore.clear()
- MenuScreen: gỡ SettingsDialog cũ (chuyển sang SettingsPanel); Pause → Cài đặt mở SettingsPanel, mục này hiện ở mọi chế độ
- Test: SettingsStoreTest (8), PauseMenuTest cập nhật (4)

## 2026-10-03 17:22 GMT+7 — Giai đoạn 5 (phần 2): Pause Menu trong GameScreen
- Thêm game/PauseMenu.kt (luật chọn mục theo chế độ: ẩn Cài đặt khi 2 người), ui/screen/PauseMenuOverlay.kt (Dialog gothic: Tiếp tục / Khởi động lại / Cài đặt / Rời trận) + 5 string `pause_*`
- GameScreen: nút Home đổi thành nút Menu mở Pause; Back trong ván mở Pause thay vì thoát ngay; mục Cài đặt mở hộp chọn cấp máy (chỉ khi đấu máy)
- Thêm test PauseMenuTest (4)

## 2026-10-03 17:15 GMT+7 — Giai đoạn 5 (phần 1): tầng dữ liệu Hồ sơ + Nhật ký (Room), chưa có màn hình
- Thêm profile/PlayerStats.kt (thống kê + luật cộng thắng/thua/hòa/checkmate/thời gian/tầng cao nhất/cấp), Journal.kt (trạng thái ✓/?/🔒 dùng chung điều kiện mở với AiCharacter), ProfileDatabase.kt (Room DB riêng `profile.db`, không đụng `match-history.db`) + ProfileRepository (ghi trong giao dịch)
- Nối AppRoot: vượt tầng → recordFloorCleared; bấm BẮT ĐẦU ở màn chọn đối thủ → recordMet (đều NonCancellable)
- Thêm test ProfileLogicTest (14)

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
