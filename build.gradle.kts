plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotest) apply false
    alias(libs.plugins.ksp) apply false
}

allprojects {
    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}