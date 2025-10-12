plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
}

repositories {
    mavenCentral()
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
                implementation(libs.ktool.kotest.bdd)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }
    }
}
