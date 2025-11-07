val ktmlVersion = "0.1.3"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.ktml.gradle") version "0.1.3"
}

kotlin {
    applyDefaultHierarchyTemplate()

    linuxX64 {
        binaries {
            executable {
                entryPoint = "dev.ktml.example.ktor.main"
            }
        }
    }

    macosX64 {
        binaries {
            executable {
                entryPoint = "dev.ktml.example.ktor.main"
            }
        }
    }

    macosArm64 {
        binaries {
            executable {
                entryPoint = "dev.ktml.example.ktor.main"
            }
        }
    }

    mingwX64 {
        binaries {
            executable {
                entryPoint = "dev.ktml.example.ktor.main"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation("dev.ktml:ktml-runtime:${ktmlVersion}")
            implementation("dev.ktml:ktml-ktor:${ktmlVersion}")
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
        }
    }
}
