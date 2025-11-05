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
    implementation(project(":http4k"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.jetty)
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
}

application {
    mainClass.set("dev.ktml.example.http4k.ApplicationKt")
}
