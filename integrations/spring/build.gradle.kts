plugins {
    kotlin("jvm")
    `maven-publish`
}

dependencies {
    implementation(project(":core"))
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.jakarta.servlet.api)
    implementation(libs.kotlinx.coroutines.core)
}
