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

    // Native targets
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlin.logging)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.ksoup.html)
                implementation(libs.ksoup.entities)
                implementation(libs.ktool.kotest.bdd)
                implementation(libs.ktool.kotlin.gen)
                implementation(libs.ktor.server.core)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.server.cio)
                implementation(libs.ktor.server.status)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.assertions.core)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlin.reflect)
                implementation(libs.slf4j.simple)
                implementation(libs.kotlin.compiler.embeddable)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotest.runner.junit5)
            }
        }

        val nativeMain by getting {
            dependencies {
                implementation(libs.okio)
                implementation(libs.ktor.client.curl)
            }
        }
    }
}
