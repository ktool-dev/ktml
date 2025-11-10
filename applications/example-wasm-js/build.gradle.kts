@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

val ktmlVersion = "0.1.3"

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    id("dev.ktml.gradle") version "0.1.3"
}

kotlin {
    applyDefaultHierarchyTemplate()

    wasmJs {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                mainOutputFileName = "example-wasm-js.js"
            }
            runTask {
                mainOutputFileName = "example-wasm-js.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("dev.ktml:ktml-runtime:${ktmlVersion}")
        }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.browser)
        }
    }
}
