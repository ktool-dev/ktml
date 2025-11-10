plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktml)
}

dependencies {
    implementation(libs.ktml.runtime)
    developmentOnly(libs.ktml.dev.mode)
    implementation(libs.ktml.javalin)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
}

application {
    mainClass.set("dev.ktml.example.javalin.ApplicationKt")
}
