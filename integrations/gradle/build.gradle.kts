plugins {
    kotlin("jvm")
    signing
    alias(libs.plugins.gradle.publish)
}

kotlin {
    jvmToolchain(22)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

group = project.property("group").toString()
version = project.property("version").toString()

dependencies {
    implementation(project(":generator"))
    compileOnly(gradleApi())
    compileOnly(kotlin("gradle-plugin"))
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    setRequired {
        // Only require signing if not publishing to Maven Local
        gradle.taskGraph.allTasks.none { it.name.contains("ToMavenLocal") }
    }
    sign(publishing.publications)
}

gradlePlugin {
    val repoPath = project.property("scm.repo.path") as String
    val pluginPath = "https://$repoPath"
    website = "$pluginPath/blob/main/integrations/gradle"
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
