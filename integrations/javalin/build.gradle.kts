plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.javalin)
    implementation(libs.kotlinx.coroutines.core)
}
