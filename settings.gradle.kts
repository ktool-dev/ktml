pluginManagement {
    repositories {
        mavenLocal()
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
integration("http4k")

application("example-javalin")
application("example-ktor")
application("example-spring")
application("example-http4k")
application("example-ktor-native")
application("example-js")
application("example-wasm-js")

fun library(name: String) = includeType("libraries", name)
fun integration(name: String) = includeType("integrations", name)
fun application(name: String) = includeType("applications", name)

fun includeType(dir: String, name: String) {
    include(name)
    project(":$name").projectDir = File("$dir/$name")
}