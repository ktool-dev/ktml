plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":generator"))
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.annotations)
}
