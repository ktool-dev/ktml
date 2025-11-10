plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktml)
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
            implementation(libs.ktml.runtime)
            implementation(libs.ktml.ktor)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
        }
    }
}
