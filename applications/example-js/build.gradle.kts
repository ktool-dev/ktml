plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktml)
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
            implementation(libs.ktml.runtime)
        }

        jsMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
