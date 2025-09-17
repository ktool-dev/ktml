rootProject.name = "ktml"

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

library("core")


fun library(name: String) = includeType("libraries", name)

fun includeType(dir: String, name: String) {
    include(name)
    project(":$name").projectDir = File("$dir/$name")
}