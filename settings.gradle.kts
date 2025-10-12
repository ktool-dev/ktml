pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ktml"

library("runtime")
library("dev-mode")
library("generator")

integration("gradle")
integration("maven")
integration("spring")
integration("ktor")
integration("javalin")


fun library(name: String) = includeType("libraries", name)
fun integration(name: String) = includeType("integrations", name)

fun includeType(dir: String, name: String) {
    include(name)
    project(":$name").projectDir = File("$dir/$name")
}