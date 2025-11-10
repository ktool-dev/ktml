plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.1.0"
    application
    alias(libs.plugins.ktml)
}

dependencies {
    implementation(libs.ktml.runtime)
    developmentOnly(libs.ktml.dev.mode)
    implementation(libs.ktml.ktor)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.status)
    implementation("io.ktor:ktor-server-sessions:3.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.mindrot:jbcrypt:0.4")

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.ktool.kotest.bdd)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.server.cio)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

application {
    mainClass.set("dev.ktml.example.ktor.ApplicationKt")
}
