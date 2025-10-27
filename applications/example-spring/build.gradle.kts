plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    id("dev.ktml.gradle") version "0.0.16"
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":runtime"))
    implementation(project(":dev-mode"))
    implementation(project(":spring"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.webmvc)
    implementation(libs.jakarta.servlet.api)
}
