plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "io.ktml"
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
                implementation("io.github.oshai:kotlin-logging:7.0.7")
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ksoup.html)
                implementation(libs.ksoup.entities)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.slf4j:slf4j-simple:2.0.7")
            }
        }

        val jvmTest by getting {
            dependencies {
                // JVM-specific test dependencies
            }
        }

        val nativeMain by creating {
            dependencies {
                implementation("com.squareup.okio:okio:3.16.0")
            }
        }
    }
}