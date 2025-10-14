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
        jvmMain.dependencies {
            implementation(project(":runtime"))
            implementation(project(":ktor"))
            implementation(project(":dev-mode"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.cio)
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
