package dev.ktml.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

open class KtmlPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("ktml", KtmlExtension::class.java)
        project.developmentOnly()

        project.plugins.withId("org.jetbrains.kotlin.jvm") {
            project.afterEvaluate {
                val isBuildTask = project.gradle.startParameter.taskNames.any {
                    it.contains("build") || it == ":${project.name}:build"
                }

                project.extensions.findByType(KotlinJvmProjectExtension::class.java)?.apply {
                    if (isBuildTask) {
                        setupBuild(project, extension.moduleName, extension.templatePackage, sourceSets.toList())
                    } else {
                        configureDevelopmentOnly(project)
                    }
                }
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            project.afterEvaluate {
                project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
                    setupBuild(project, extension.moduleName, extension.templatePackage, sourceSets.toList())
                }
            }
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

    private fun Project.developmentOnly() = configurations.findByName("developmentOnly")
        ?: configurations.create("developmentOnly") {
            it.isCanBeConsumed = false
            it.isCanBeResolved = true
        }

    private fun configureDevelopmentOnlyKmp(project: Project) =
        createDevelopmentOnly(project, "jvmCompileClasspath", "jvmRuntimeClasspath", "jvmTestRuntimeClasspath")

    private fun configureDevelopmentOnly(project: Project) =
        createDevelopmentOnly(project, "compileClasspath", "runtimeClasspath", "testRuntimeClasspath")

    private fun createDevelopmentOnly(
        project: Project,
        compileName: String,
        runtimeName: String,
        testRuntimeName: String
    ) {
        val developmentOnly = project.developmentOnly()
        project.configurations.findByName(compileName)?.extendsFrom(developmentOnly)
        project.configurations.findByName(testRuntimeName)?.extendsFrom(developmentOnly)
        project.configurations.findByName(runtimeName)?.extendsFrom(developmentOnly)
    }

    private fun setupBuild(
        project: Project,
        moduleName: String,
        templatePackage: String,
        sourceSets: List<KotlinSourceSet>
    ) {
        val outputDir = project.layout.buildDirectory.asFile.get().resolve("ktml")

        val dirSets = sourceSets.mapNotNull { src ->
            val ktmlDir = src.kotlin.srcDirs.firstOrNull()?.resolveSibling("ktml")?.takeIf { it.exists() }
            if (ktmlDir != null) {
                SourceInfo(src, ktmlDir.absolutePath, outputDir.resolve(src.name).absolutePath)
            } else {
                null
            }
        }

        val generateTask = project.tasks.register("generateKtml", KtmlGenerateTask::class.java) {
            it.moduleName = moduleName
            it.templatePackage = templatePackage
            it.dirSets = dirSets
        }

        project.afterEvaluate {
            dirSets.forEach { it.sourceSet.kotlin.srcDir(it.outputDir) }

            // Make all Kotlin compilation tasks depend on generateKtml
            project.tasks.matching { task ->
                task.name.startsWith("compile") && task.name.contains("Kotlin")
            }.configureEach {
                it.dependsOn(generateTask)
            }
        }
    }
}
