# Tower of Chess — Cờ vua 2D phong cách gothic cho Android

Game cờ vua 2D viết bằng **Kotlin + Jetpack Compose**, có chế độ chiến dịch **Khám phá Tháp Cờ**
(49 tầng), đấu với AI, hai người cùng máy, đấu qua mạng LAN và xem Máy vs Máy. Toàn bộ luật
cờ, AI và giao thức mạng được tự viết, không dùng thư viện cờ vua bên ngoài.

- **Package:** `kma.game.chess2d`
- **Android:** minSdk 26 (Android 8.0), targetSdk / compileSdk 37
- **Giao diện:** tiếng Việt, phong cách dark fantasy gothic (đỏ – đen – vàng)

---

## Tính năng

### Chế độ chơi

| Chế độ | Mô tả |
|---|---|
| **Khám phá Tháp Cờ** | Leo 49 tầng: đấu cờ, giải thế cờ, kho báu và cốt truyện. Có bản đồ tháp, thoại ở các tầng mốc, màn boss, màn Chiến thắng / Thất bại. Tiến độ được lưu lại. |
| **Người vs Máy** | Chọn một trong các đối thủ AI, mỗi nhân vật có phong cách, tính cách và độ khó riêng (1–3 sao). Một số nhân vật chỉ mở khi vượt đủ tầng Tháp. |
| **Hai người cùng máy** | Thay phiên nhau trên một thiết bị, đặt tên hai bên và chọn thể thức thời gian. |
| **Hai người qua LAN** | Mở phòng hoặc vào phòng trong cùng mạng Wi-Fi (tự tìm phòng bằng UDP broadcast, có nhập địa chỉ thủ công). Hỗ trợ đồng hồ, cầu hòa, xin thua, đấu lại, đồng bộ lại ván. |
| **Máy vs Máy** | Ngồi xem hai AI tự đấu với nhau. |

### Ngoài ván đấu

- **Lịch sử ván đấu:** lưu bằng Room, xem lại từng nước, xuất PGN.
- **Hồ sơ:** cấp, thống kê thắng / thua / hòa, tỉ lệ thắng, tầng cao nhất, thời gian chơi.
- **Nhật ký:** hồ sơ các nhân vật đã gặp.
- **Túi đồ:** vàng, pha lê và di vật thu được từ Tháp.
- **Cài đặt:** âm thanh, nhạc, hiển thị, điều khiển, tên người chơi.
- **Tiếp tục ván dở:** ván offline được lưu (DataStore), mở app lại sẽ hỏi tiếp tục hay bỏ.
- **Âm thanh:** hiệu ứng đi quân, ăn quân, chiếu, kết thúc ván và nhạc nền menu / chiến đấu / boss.

---

## Cấu trúc dự án

Dự án chia thành 4 module Gradle. Chỉ `:app` biết tới Android; ba module còn lại là Kotlin/JVM
thuần nên test chạy nhanh trên máy tính, không cần emulator.

```
Chess/
├── engine/   Luật cờ: Board, Move, MoveGenerator, Rules, Fen, Pgn, San, Zobrist, Perft…
├── ai/       Đối thủ máy: Search (alpha-beta), Evaluation, MoveOrdering, TranspositionTable
├── net/      LAN: LanHost, LanGuest, Discovery (UDP), Protocol, MessageChannel (TCP), Clock, LanCli
├── app/      Ứng dụng Android (Jetpack Compose)
│   └── src/main/java/kma/game/chess2d/
│       ├── campaign/   Tháp Cờ: danh sách tầng, tiến độ, phần thưởng, thoại
│       ├── game/       Ván đấu offline, lưu / tiếp tục ván
│       ├── history/    Lịch sử ván đấu (Room)
│       ├── lan/        ViewModel sảnh và ván LAN
│       ├── opponent/   Nhân vật AI
│       ├── profile/    Hồ sơ, thống kê, nhật ký (Room)
│       ├── puzzle/     Thế cờ (đọc từ assets/puzzles.txt)
│       ├── settings/   Cài đặt
│       ├── sound/      Hiệu ứng âm thanh và nhạc nền
│       ├── spectate/   Máy vs Máy
│       └── ui/         art/ (hệ vẽ ảnh, nút, panel), board/ (bàn cờ), screen/ (các màn hình), theme/
├── docs/     Tài liệu thiết kế (xem bên dưới)
└── .github/workflows/release.yml   CI dựng và phát hành APK
```

### Giao diện

Mọi màn hình được dựng trên một khung thiết kế **1080 x 1920** (`DesignCanvas`): toạ độ lấy thẳng
từ bản thiết kế, khung được co vừa vùng an toàn của màn hình nên bố cục giữ nguyên trên mọi máy.
Ảnh trong `res/drawable` được giải mã thu nhỏ và cache (`ArtCache`), nạp trước ở màn Splash.
Bố cục của từng màn nằm trong các object `*Layout` (ví dụ `LobbyLayout`, `AiSelectLayout`) và có
unit test kiểm tra các khối không chồng nhau và nằm trong vùng an toàn.

---

## Yêu cầu

- **JDK 17**
- **Android Studio** bản hỗ trợ Android Gradle Plugin 9.3 và Android SDK 37
- Thiết bị hoặc emulator Android 8.0 trở lên

Phiên bản chính (xem `gradle/libs.versions.toml`): AGP 9.3.2, Kotlin 2.2.20, Compose BOM 2025.09.00,
Room 2.7.2, DataStore 1.1.7, kotlinx.coroutines 1.10.2, kotlinx.serialization 1.9.0.

---

## Bắt đầu

```bash
git clone <url-repo>
cd Chess

# Build bản debug
./gradlew :app:assembleDebug

# Cài lên thiết bị đang kết nối
./gradlew :app:installDebug
```

APK debug nằm ở `app/build/outputs/apk/debug/app-debug.apk`. Có thể mở thư mục dự án bằng Android
Studio và chạy cấu hình `app` như bình thường.

### Chạy test

```bash
# Test JVM của luật cờ, AI và mạng (nhanh, không cần emulator)
./gradlew :engine:test :ai:test :net:test

# Unit test của app (bố cục, logic Tháp, hồ sơ…)
./gradlew :app:testDebugUnitTest
```

Engine có bộ test Perft, make/unmake, nước đi đặc biệt và luật hòa; AI có test tự đấu; `:net` có
test loopback.

### Thử LAN với một điện thoại

Emulator Android không nhận được gói broadcast của máy thật, nên `:net` có sẵn một client dòng lệnh để
chạy trên PC và đấu với một điện thoại thật cùng mạng:

```bash
./gradlew :net:run --args="scan"                          # tìm phòng trong mạng
./gradlew :net:run --args="host Laptop"                   # mở phòng tên Laptop
./gradlew :net:run --args="join Laptop 192.168.1.20:45123" # vào phòng theo địa chỉ
```

Nếu hai máy cùng Wi-Fi mà không thấy nhau, nhiều khả năng router bật AP isolation: hãy dùng điểm phát
Wi-Fi từ một trong hai máy hoặc nhập địa chỉ thủ công.

---

## Bản phát hành (release)

Build release bật R8 (rút gọn mã và tài nguyên). Thông tin ký được đọc theo thứ tự:

1. File `keystore.properties` ở thư mục gốc (không commit lên git):

   ```properties
   storeFile=/duong/dan/toi/release.jks
   storePassword=...
   keyAlias=...
   keyPassword=...
   ```

2. Biến môi trường: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

Thiếu thông tin ký thì build vẫn chạy nhưng ra APK chưa ký.

```bash
./gradlew :app:assembleRelease
```

### CI / CD

Workflow `.github/workflows/release.yml` chạy test JVM, build APK release đã ký và đăng lên GitHub
Release:

- **Push vào `main`:** phiên bản tự tính theo Conventional Commits kể từ tag `v*` gần nhất
  (`feat!:` / `BREAKING CHANGE` → major, `feat:` → minor, còn lại → patch).
- **Push tag `v1.2.3`:** dùng đúng phiên bản trong tag.
- **Chạy tay:** chỉ tạo artifact, không tạo tag hay Release.
- Ghi `[skip release]` trong commit message để bỏ qua phát hành.

Secret cần khai báo trên GitHub: `KEYSTORE_BASE64` (file `.jks` mã hoá base64), `KEYSTORE_PASSWORD`,
`KEY_ALIAS`, `KEY_PASSWORD`.

---

## Tài liệu

| File | Nội dung |
|---|---|
| `docs/context/tower-of-chess.md` | Bối cảnh dự án và các quyết định chính |
| `docs/design/story-bible.md` | Cốt truyện, nhân vật, các tầng mốc có thoại |
| `docs/design/ui-screens.md` | Thiết kế các màn hình và luồng điều hướng |
| `docs/plan/completion-plan.md` | Kế hoạch hoàn thiện |
| `CHANGELOG.md` | Nhật ký thay đổi (mới nhất ở trên) |

---

## Quy ước đóng góp

- Commit message theo **Conventional Commits** bằng tiếng Anh (`feat(ui): …`, `fix(engine): …`), vì CI
  dựa vào đó để tính phiên bản.
- Mỗi lần thay đổi, thêm một mục vào đầu `CHANGELOG.md` theo định dạng
  `## YYYY-MM-DD HH:MM GMT+7 — <tóm tắt>`.
- Chạy `./gradlew :app:testDebugUnitTest` và `./gradlew :app:assembleDebug` trước khi commit.
- Ảnh đặt thẳng trong `app/src/main/res/drawable/` (Android không nhận thư mục con), tên file chỉ
  gồm chữ thường, số và dấu gạch dưới.
