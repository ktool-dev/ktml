val ktmlVersion = "0.0.18"

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("dev.ktml.gradle") version "0.0.18"
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation("dev.ktml:ktml-javalin:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
}
