plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "dev.ktoola "
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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

    // Native targets
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ksoup.html)
                implementation(libs.ksoup.entities)
                implementation(libs.ktool.kotest.bdd)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.slf4j.simple)
                implementation(libs.kotlin.compiler.embeddable)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation(libs.okio)
            }
        }
    }
}
