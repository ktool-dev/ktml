plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(22)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":runtime"))
    implementation(project(":generator"))
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.reflect)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktool.kotest.bdd)
}
