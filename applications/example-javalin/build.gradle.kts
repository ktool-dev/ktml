val ktmlVersion = "0.1.1"

plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    id("dev.ktml.gradle") version "0.1.1"
}

kotlin {
    jvmToolchain(22)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("dev.ktml:ktml-runtime:$ktmlVersion")
    developmentOnly("dev.ktml:ktml-dev-mode:$ktmlVersion")
    implementation("dev.ktml:ktml-javalin:$ktmlVersion")
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.javalin)
    implementation(libs.slf4j.simple)
}

application {
    mainClass.set("dev.ktml.example.javalin.ApplicationKt")
}
