import java.util.Properties

plugins {
    // AGP 9.0+ đã nhúng sẵn Kotlin (built-in Kotlin).
    // KHÔNG khai báo org.jetbrains.kotlin.android ở đây — sẽ xung đột.
    alias(libs.plugins.android.application)
    // Kể từ Kotlin 2.0, plugin Compose Compiler là BẮT BUỘC khi bật compose.
    // Plugin này tương thích với built-in Kotlin (khác kotlin-android).
    alias(libs.plugins.kotlin.compose)
    // KSP: Room sinh mã DAO cho lịch sử ván đấu (mục 7.3).
    alias(libs.plugins.ksp)
}

// Cấu hình ký bản release. Ưu tiên file keystore.properties (máy cá nhân), sau đó
// tới biến môi trường (CI). Cả hai đều không nằm trong git: mất file .jks và mất
// mật khẩu là mất luôn khả năng cập nhật app đã phát hành.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Đọc một giá trị cấu hình ký: file trước, rồi tới biến môi trường.
fun signingValue(key: String, env: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(env)

val releaseStoreFile = signingValue("storeFile", "KEYSTORE_FILE")
val releaseStorePassword = signingValue("storePassword", "KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "KEY_PASSWORD")
val canSignRelease = releaseStoreFile != null &&
    releaseStorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

android {
    namespace = "kma.game.chess2d"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "kma.game.chess2d"
        minSdk = 26
        targetSdk = 37
        // CI truyền vào từ tag và số lần chạy; build tay thì dùng giá trị mặc định.
        // versionCode phải tăng dần, nếu không máy sẽ không cho cài đè lên bản cũ.
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Chỉ tạo khi có đủ thông tin. Thiếu thì build vẫn chạy và ra APK chưa ký,
        // để người khác clone repo vẫn build được mà không cần keystore của mình.
        if (canSignRelease) {
            create("release") {
                storeFile = file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            // Bật R8 (mục 7.1): rút gọn mã và tài nguyên cho bản phát hành.
            // Chỗ dễ vỡ nhất là kotlinx.serialization trong :net, nên keep rule cho
            // nó nằm sẵn trong proguard-rules.pro; đặc biệt là tên các lớp con của
            // NetMessage phải giữ nguyên vì chúng là một phần của giao thức LAN.
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Chỉ :app được biết đến Android và Compose.
    implementation(project(":engine"))
    implementation(project(":ai"))
    implementation(project(":net"))

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // StateFlow. Lifecycle kéo theo sẵn, nhưng khai báo rõ để không phụ thuộc ngầm.
    implementation(libs.kotlinx.coroutines.core)

    // Lưu ván offline đang chơi (mục 7.1). Dùng bản Preferences chứ không Proto: dữ liệu
    // cần lưu chỉ là vài chuỗi và vài cờ, không đáng thêm một bước sinh mã protobuf.
    implementation(libs.datastore.preferences)

    // Lịch sử ván đấu (mục 7.3). Chỉ lưu danh sách nước đi chứ không lưu thế cờ:
    // thế cờ nào cũng dựng lại được từ FEN đầu cộng dãy nước, lưu thêm chỉ tốn chỗ.
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
