plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktml)
}

dependencies {
    implementation(libs.ktml.runtime)
    developmentOnly(libs.ktml.dev.mode)
    implementation(libs.ktml.http4k)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.http4k.core)
    implementation(libs.http4k.server.jetty)
    implementation(libs.slf4j.simple)
    implementation(libs.kotlin.logging)
}

application {
    mainClass.set("dev.ktml.example.http4k.ApplicationKt")
}
