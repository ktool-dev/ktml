val ktmlVersion = "0.1.3"

plugins {
    alias(libs.plugins.kotlin.jvm)
    kotlin("plugin.serialization") version "2.1.0"
    application
    id("dev.ktml.gradle") version "0.1.3"
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    implementation("dev.ktml:ktml-ktor:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
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
