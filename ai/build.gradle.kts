// :ai - search + eval. Kotlin/JVM thuan, phu thuoc :engine.
plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":engine"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
