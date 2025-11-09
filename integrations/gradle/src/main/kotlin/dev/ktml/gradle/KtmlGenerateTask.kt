package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.*
import java.io.File
import java.io.Serializable

data class SourceInfo(val ktmlDir: String, val outputDir: String) : Serializable

open class KtmlGenerateTask : DefaultTask() {
    @Input
    lateinit var moduleName: String

    @Input
    lateinit var templatePackage: String

    @Input
    lateinit var dirSets: List<SourceInfo>

    @InputFiles
    @SkipWhenEmpty
    @PathSensitive(PathSensitivity.RELATIVE)
    fun getInputFiles(): FileTree {
        return project.files(dirSets.map { dirSet -> project.fileTree(dirSet.ktmlDir) { it.include("**/*.ktml") } }).asFileTree
    }

    @OutputDirectories
    fun getOutputDirectories(): List<File> {
        return dirSets.map { project.file(it.outputDir) }
    }

    @TaskAction
    open fun generate() {
        dirSets.forEach { (ktmlDir, outputDir) ->
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
