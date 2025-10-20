plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":runtime"))
    implementation(project(":dev-mode"))
    implementation(project(":javalin"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
}
