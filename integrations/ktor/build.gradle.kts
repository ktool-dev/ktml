plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(22)

    jvm()

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.server.core)
        }
    }
}
