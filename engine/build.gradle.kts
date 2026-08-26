// :engine - luat choi, sinh nuoc di, FEN, perft.
// Kotlin/JVM thuan: KHONG duoc phu thuoc Android (trinh bien dich chan).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(libs.junit)
}

tasks.withType<Test>().configureEach {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
