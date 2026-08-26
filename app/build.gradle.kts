plugins {
    // AGP 9.0+ da nhung san Kotlin (built-in Kotlin).
    // KHONG khai bao org.jetbrains.kotlin.android o day - se xung dot.
    alias(libs.plugins.android.application)
    // Ke tu Kotlin 2.0, plugin Compose Compiler la BAT BUOC khi bat compose.
    // Plugin nay tuong thich voi built-in Kotlin (khac kotlin-android).
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "kma.game.chess2d"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "kma.game.chess2d"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
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
