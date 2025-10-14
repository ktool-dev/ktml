plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":runtime"))
    compileOnly(libs.javalin)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.ktool.kotest.bdd)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.javalin)
    testImplementation(libs.mockk)
}
