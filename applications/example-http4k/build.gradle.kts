plugins {
    alias(libs.plugins.kotlin.jvm)
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
