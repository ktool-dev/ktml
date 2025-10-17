plugins {
    kotlin("jvm")
    signing
    alias(libs.plugins.gradle.publish)
}

kotlin {
    jvmToolchain(22)
}

group = project.property("group").toString()
version = project.property("version").toString()

dependencies {
    implementation(project(":generator"))
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
}

gradlePlugin {
    val repoPath = project.property("scm.repo.path") as String
    val pluginPath = "https://$repoPath/integrations/gradle"
    website = pluginPath
    vcsUrl = pluginPath

    plugins {
        create("ktmlPlugin") {
            id = "$group.gradle"
            displayName = "KTML Gradle Plugin"
            description = "Runs the code generation process for KTML as part of your build"
            tags = listOf("kotlin", "html", "templating", "ktml")
            implementationClass = "dev.ktml.gradle.KtmlPlugin"
        }
    }
}
