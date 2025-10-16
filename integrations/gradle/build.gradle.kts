plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":generator"))
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
    plugins {
        create("ktmlPlugin") {
            id = "dev.ktml.gradle"
            implementationClass = "dev.ktml.gradle.KtmlPlugin"
        }
    }
}
