plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":runtime"))
    implementation(project(":spring"))
    implementation(project(":dev-mode"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.webmvc)
    implementation(libs.jakarta.servlet.api)
}
