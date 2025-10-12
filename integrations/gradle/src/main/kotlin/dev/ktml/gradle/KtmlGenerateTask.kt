package dev.ktml.gradle

import dev.ktml.KtmlProcessor
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class KtmlGenerateTask : DefaultTask() {

    @get:Input
    abstract val moduleName: Property<String>

    @get:Input
    abstract val templateDirectories: ListProperty<String>

    @get:OutputDirectory
    val outputDirectory: File = project.file("src/main/kotlin")

    @TaskAction
    fun generate() {
        val processor = KtmlProcessor(
            moduleName = moduleName.get(),
            outputDirectory = outputDirectory.absolutePath
        )

        val dirs = templateDirectories.get().map {
            project.file(it).absolutePath
        }

        processor.processRootDirectories(dirs)
        processor.generateTemplateCode()

        logger.lifecycle("KTML code generation completed")
    }
}
