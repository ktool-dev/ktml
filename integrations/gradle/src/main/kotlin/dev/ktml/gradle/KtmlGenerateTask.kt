package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

data class SourceInfo(val sourceSet: KotlinSourceSet, val ktmlDir: String, val outputDir: String)

class KtmlGenerateTask : DefaultTask() {
    @Input
    lateinit var moduleName: String

    @Input
    lateinit var dirSets: List<SourceInfo>

    @TaskAction
    fun generate() {
        dirSets.forEach { (_, ktmlDir, outputDir) ->
            KtmlProcessor(moduleName = moduleName, outputDirectory = outputDir).apply {
                processRootDirectory(ktmlDir)
                generateTemplateCode()
            }
        }

        logger.lifecycle("KTML code generation completed")
    }
}
