val ktmlVersion = "0.1.3"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.ktml.gradle") version "0.1.3"
}

kotlin {
    applyDefaultHierarchyTemplate()

    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                mainOutputFileName = "example-js.js"
            }
            runTask {
                mainOutputFileName = "example-js.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("dev.ktml:ktml-runtime:${ktmlVersion}")
        }

        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
