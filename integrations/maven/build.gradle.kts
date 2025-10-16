plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":generator"))
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.annotations)
}
