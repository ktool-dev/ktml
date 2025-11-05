val ktmlVersion = "0.1.2"

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("dev.ktml.gradle") version "0.1.2"
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation("dev.ktml:ktml-javalin:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
}

application {
    mainClass.set("dev.ktml.example.javalin.ApplicationKt")
}
