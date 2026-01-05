package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileTree
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File
import java.io.Serializable
import javax.inject.Inject

data class SourceInfo(val ktmlDir: String, val outputDir: String) : Serializable

open class KtmlGenerateTask @Inject constructor(private var projectLayout: ProjectLayout) : DefaultTask() {

    @Internal
    lateinit var errorCollectorProvider: Provider<KtmlCompilationErrorCollector>

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
        return projectLayout.files(dirSets.map { it.ktmlDir }).asFileTree.matching { it.include("**/*.ktml") }
    }

    @OutputDirectories
    fun getOutputDirectories(): List<File> {
        return dirSets.flatMap { projectLayout.files(it.outputDir).files }
    }

    @TaskAction
    open fun generate() {
        dirSets.forEach { (ktmlDir, outputDir) ->
            projectLayout.files(outputDir).forEach { it.deleteRecursively() }
            KtmlProcessor(
                moduleName = moduleName,
                templatePackage = templatePackage,
                outputDirectory = outputDir,
                removeContentComments = false,
            ).apply {
                errorCollectorProvider.get().addProjectInfo(ProjectInfo(templatePackage, ktmlDir, outputDir, this))
                processRootDirectory(ktmlDir)
                generateTemplateCode()
            }
        }
    }
}
