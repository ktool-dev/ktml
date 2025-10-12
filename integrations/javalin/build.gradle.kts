plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":runtime"))
    compileOnly(libs.javalin)
    implementation(libs.kotlinx.coroutines.core)
}
