import java.util.Properties

plugins {
    // AGP 9.0+ da nhung san Kotlin (built-in Kotlin).
    // KHONG khai bao org.jetbrains.kotlin.android o day - se xung dot.
    alias(libs.plugins.android.application)
    // Ke tu Kotlin 2.0, plugin Compose Compiler la BAT BUOC khi bat compose.
    // Plugin nay tuong thich voi built-in Kotlin (khac kotlin-android).
    alias(libs.plugins.kotlin.compose)
}

// Cau hinh ky ban release. Uu tien file keystore.properties (may ca nhan), sau do
// toi bien moi truong (CI). Ca hai deu khong nam trong git: file .jks va mat khau
// mat la mat luon kha nang cap nhat app da phat hanh.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

// Doc mot gia tri cau hinh ky, file truoc roi toi bien moi truong.
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
        // CI truyen vao tu tag va so lan chay; build tay thi dung gia tri mac dinh.
        // versionCode phai tang dan, neu khong may se khong cho cai de len ban cu.
        versionCode = System.getenv("VERSION_CODE")?.toIntOrNull() ?: 1
        versionName = System.getenv("VERSION_NAME") ?: "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Chi tao khi co du thong tin. Thieu thi build van chay va ra APK chua ky,
        // de nguoi khac clone repo van build duoc ma khong can keystore cua minh.
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
            // R8 tam tat: kotlinx.serialization trong :net la cho de vo nhat khi rut
            // gon, nen chi bat sau khi da test tay luong LAN tren ban release.
            optimization {
                enable = false
            }
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
    // Chi :app duoc biet den Android va Compose.
    implementation(project(":engine"))
    implementation(project(":ai"))
    implementation(project(":net"))

    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // StateFlow. Lifecycle keo theo san, nhung khai bao ro de khong phu thuoc ngam.
    implementation(libs.kotlinx.coroutines.core)

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
