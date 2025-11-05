val ktmlVersion = "0.1.1"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.spring.boot)
    id("dev.ktml.gradle") version "0.1.1"
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
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation("dev.ktml:ktml-spring:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.webmvc)
    implementation(libs.jakarta.servlet.api)
}
