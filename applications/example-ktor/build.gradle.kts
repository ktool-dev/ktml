val ktmlVersion = "0.1.1"

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("dev.ktml.gradle") version "0.1.1"
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    implementation("dev.ktml:ktml-ktor:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.status)

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
