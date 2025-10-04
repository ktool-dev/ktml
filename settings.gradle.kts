rootProject.name = "ktml"

library("core")


fun library(name: String) = includeType("libraries", name)

fun includeType(dir: String, name: String) {
    include(name)
    project(":$name").projectDir = File("$dir/$name")
}