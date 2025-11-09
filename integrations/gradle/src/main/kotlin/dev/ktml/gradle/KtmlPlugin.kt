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
                project.extensions.findByType(KotlinJvmProjectExtension::class.java)?.apply {
                    setupBuild(project, extension.moduleName, extension.templatePackage, sourceSets.toList())
                    configureDevelopmentOnly(project)
                }
            }
        }

        project.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            project.afterEvaluate {
                project.extensions.getByType(KotlinMultiplatformExtension::class.java).apply {
                    setupBuild(project, extension.moduleName, extension.templatePackage, sourceSets.toList())
                    configureDevelopmentOnlyKmp(project)
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

        val sets = sourceSets.mapNotNull { src ->
            val ktmlDir = src.kotlin.srcDirs.firstOrNull()?.resolveSibling("ktml")?.takeIf { it.exists() }
            if (ktmlDir != null) {
                src to SourceInfo(ktmlDir.absolutePath, outputDir.resolve(src.name).absolutePath)
            } else {
                null
            }
        }

        val generateTask = project.tasks.register("generateKtml", KtmlGenerateTask::class.java) {
            it.moduleName = moduleName
            it.templatePackage = templatePackage
            it.dirSets = sets.map { it.second }
        }

        val errorCollectorProvider = project.getErrorCollector()

        project.afterEvaluate {
            sets.forEach { it.first.kotlin.srcDir(it.second.outputDir) }

            // Make all Kotlin compilation tasks depend on generateKtml
            project.tasks.matching { task ->
                task.name.startsWith("compile") && task.name.contains("Kotlin")
            }.configureEach { compileTask ->
                compileTask.dependsOn(generateTask)

                val errorCollector = errorCollectorProvider.get()
                compileTask.logging.addStandardErrorListener(errorCollector)
                compileTask.logging.addStandardOutputListener(errorCollector)

                compileTask.usesService(errorCollectorProvider)
            }
        }
    }
}

fun Project.getErrorCollector() = project.gradle.sharedServices.registerIfAbsent(
    "ktmlErrorCollector-${project.path}",
    KtmlCompilationErrorCollector::class.java
)
