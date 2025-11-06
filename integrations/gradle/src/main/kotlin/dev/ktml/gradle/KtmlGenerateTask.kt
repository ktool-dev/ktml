package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

data class SourceInfo(val sourceSet: KotlinSourceSet, val ktmlDir: String, val outputDir: String)

open class KtmlGenerateTask : DefaultTask() {
    @Input
    lateinit var moduleName: String

    @Input
    lateinit var templatePackage: String

    @Input
    lateinit var dirSets: List<SourceInfo>

    @TaskAction
    open fun generate() {
        dirSets.forEach { (_, ktmlDir, outputDir) ->
            project.file(outputDir).deleteRecursively()
            KtmlProcessor(
                moduleName = moduleName,
                templatePackage = templatePackage,
                outputDirectory = outputDir,
                removeContentComments = false,
            ).apply {
                project.getErrorCollector().get().addProjectInfo(ProjectInfo(templatePackage, ktmlDir, outputDir, this))
                processRootDirectory(ktmlDir)
                generateTemplateCode()
            }
        }
    }
}
