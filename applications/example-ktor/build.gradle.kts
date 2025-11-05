val ktmlVersion = "0.1.0"

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("dev.ktml.gradle") version "0.1.0"
}

kotlin {
    jvmToolchain(22)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(project(":runtime"))
    developmentOnly(project(":dev-mode"))
    implementation(project(":ktor"))
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
