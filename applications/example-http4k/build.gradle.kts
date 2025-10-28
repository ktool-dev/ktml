val ktmlVersion = "0.1.0"

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.ktml.gradle") version "0.1.0"
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation("dev.ktml:ktml-http4k:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.jetty)
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
}
