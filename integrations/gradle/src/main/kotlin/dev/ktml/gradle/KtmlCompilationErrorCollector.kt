package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import dev.ktml.util.CompilerError
import dev.ktml.util.CompilerErrorResolver
import org.gradle.api.logging.Logging
import org.gradle.api.logging.StandardOutputListener
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters


data class ProjectInfo(
    val templatePackage: String,
    val ktmlDir: String,
    val outputDir: String,
    val processor: KtmlProcessor
)

abstract class KtmlCompilationErrorCollector :
    BuildService<BuildServiceParameters.None>,
    StandardOutputListener,
    AutoCloseable {

    private val logger = Logging.getLogger(KtmlCompilationErrorCollector::class.java)
    private val capturedOutput = StringBuilder()
    private val projectInfos = mutableListOf<ProjectInfo>()

    fun addProjectInfo(projectInfo: ProjectInfo) {
        projectInfos.add(projectInfo)
    }

    override fun onOutput(output: CharSequence) {
        capturedOutput.append(output)
    }

    override fun close() {
        val errorText = capturedOutput.toString()

        if (errorText.isEmpty()) return

        val errors = projectInfos.flatMap {
            processSourceSetErrors(errorText, it)
        }

        if (errors.isEmpty()) return

        logger.error("\n" + "=".repeat(80))
        logger.error("KTML Compilation Errors (${errors.size}):")
        logger.error("=".repeat(80))
        for ((filePath, fileErrors) in errors.groupBy { it.filePath }) {
            logger.error("\nFile: $filePath")
            logger.error("—".repeat(80))
            fileErrors.forEach { logger.error(it.message) }
        }
        logger.error("=".repeat(80) + "\n")
    }

    private fun processSourceSetErrors(errorText: String, info: ProjectInfo): List<CompilerError> {
        val errors = parseCompilationErrors(errorText, info.outputDir)

        if (errors.isEmpty()) return errors

        val resolver = CompilerErrorResolver(
            parsedTemplates = info.processor.getParsedTemplates(),
            templatePackage = info.templatePackage,
            generatedDir = info.outputDir,
            templateDir = info.ktmlDir
        )

        return resolver.resolve(errors)
    }

    private fun parseCompilationErrors(message: String, outputDir: String): List<CompilerError> {
        val errors = mutableListOf<CompilerError>()

        message.lines().forEach { line ->
            if (line.startsWith("e: ") && line.contains(outputDir)) {
                val subline = line.substringAfter(outputDir)
                val filePath = subline.removePrefix("/").substringBefore(':')
                val line = subline.substringAfter(':').substringBefore(':').toInt()
                val column = subline.substringAfterLast(':').substringBefore(' ').toInt()
                val message = subline.substringAfter(' ')

                errors.add(CompilerError(message, filePath, line, column))
            }
        }

        return errors
    }
}
