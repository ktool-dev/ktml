val ktmlVersion = "0.0.18"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.ktml.gradle") version "0.0.18"
}

dependencies {
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
}

kotlin {
    jvmToolchain(22)

    jvm()

    applyDefaultHierarchyTemplate()

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        jvmMain.dependencies {
            implementation("dev.ktml:ktml-runtime:$ktmlVersion")
            implementation("dev.ktml:ktml-ktor:$ktmlVersion")
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.ktor.server.status)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.ktool.kotest.bdd)
            implementation(libs.kotest.assertions.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.kotlin.test)
            implementation(libs.mockk)
        }
    }
}
