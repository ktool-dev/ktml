package dev.ktml.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class KtmlPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            configurePlugin(project)
        }
        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            configurePlugin(project)
        }

        project.afterEvaluate {
            val hasKotlin = project.plugins.hasPlugin("org.jetbrains.kotlin.jvm") ||
                    project.plugins.hasPlugin("org.jetbrains.kotlin.multiplatform")
            if (!hasKotlin) {
                throw IllegalStateException(
                    "KTML Gradle plugin requires the Kotlin plugin to be applied. " +
                            "Please apply either 'org.jetbrains.kotlin.jvm' or 'org.jetbrains.kotlin.multiplatform' plugin."
                )
            }
        }
    }

    private fun configurePlugin(project: Project) {
        val extension = project.extensions.create("ktml", KtmlExtension::class.java)
        extension.moduleName.convention("")
        extension.templateDirectories.convention(listOf("src/main/ktml"))

        val generateTask = project.tasks.register("generateKtml", KtmlGenerateTask::class.java) {
            it.moduleName.set(extension.moduleName)
            it.templateDirectories.set(extension.templateDirectories)
        }

        project.tasks.withType(KotlinCompile::class.java).configureEach {
            it.dependsOn(generateTask)
        }
    }
}
