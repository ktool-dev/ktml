plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":runtime"))
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.jakarta.servlet.api)
    implementation(libs.kotlinx.coroutines.core)
}
