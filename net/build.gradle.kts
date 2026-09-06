// :net - protocol + transport LAN (UDP broadcast discovery, TCP socket).
// Kotlin/JVM thuan de test bang client dong lenh tren PC, khong can 2 dien thoai.
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    // java-library de dung cau hinh `api`: API cong khai cua module nay lo ra
    // cac kieu cua :engine (Move, GameStatus) va StateFlow cua coroutines.
    `java-library`
    // application de chay client dong lenh: ./gradlew :net:run --args="scan"
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    // File LanCli.kt khong co class, nen ten class sinh ra la LanCliKt.
    mainClass.set("kma.game.chess2d.net.LanCliKt")
}

tasks.named<JavaExec>("run") {
    // Mac dinh Gradle khong noi ban phim vao tien trinh con, ma client nay doc lenh
    // tu stdin. Khong co dong nay thi "host"/"join" chay nhung khong nhap duoc nuoc di.
    standardInput = System.`in`
}

dependencies {
    // api chu khong implementation: LanGameState lo ra GameStatus, legalMoves() lo ra Move,
    // nen module nao dung :net cung phai thay duoc :engine va coroutines.
    api(project(":engine"))
    api(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
