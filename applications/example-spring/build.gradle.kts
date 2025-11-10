plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.ktml)
}

dependencies {
    implementation(libs.ktml.runtime)
    developmentOnly(libs.ktml.dev.mode)
    implementation(libs.ktml.spring)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.webmvc)
    implementation(libs.jakarta.servlet.api)
}
