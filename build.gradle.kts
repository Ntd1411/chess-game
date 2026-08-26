// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Luu y: AGP 9.0+ co built-in Kotlin, nen KHONG khai bao org.jetbrains.kotlin.android.
// kotlin.jvm van can cho :engine / :ai / :net (module JVM thuan, khong phai module Android).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
