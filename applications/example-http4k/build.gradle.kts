plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.ktml.gradle") version "0.0.16"
}

dependencies {
    implementation(project(":runtime"))
    implementation(project(":dev-mode"))
    implementation(project(":http4k"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.jetty)
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
}
