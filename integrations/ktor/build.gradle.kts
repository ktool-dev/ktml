plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(22)

    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    applyDefaultHierarchyTemplate()

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":runtime"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.server.core)
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
