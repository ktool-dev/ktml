plugins {
    kotlin("jvm")
    `maven-publish`
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":runtime"))
    compileOnly(libs.spring.webmvc)
    compileOnly(libs.jakarta.servlet.api)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.ktool.kotest.bdd)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.spring.webmvc)
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
