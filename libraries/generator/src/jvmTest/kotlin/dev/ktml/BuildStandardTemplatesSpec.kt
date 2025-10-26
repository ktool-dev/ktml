package dev.ktml

import dev.ktml.parser.kotlin.removeContentComments
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import java.io.File

private const val RUNTIME_PATH = "../runtime/src/commonMain"

class BuildStandardTemplatesSpec : BddSpec({
    "build" {
        Given
        val outputPath = "$RUNTIME_PATH/kotlin"
        val templatePath = "$RUNTIME_PATH/ktml"
        val outputPackage = "$outputPath/dev/ktml/templates"
        val processor = KtmlProcessor(outputDirectory = outputPath)

        When
        File(templatePath).listFiles().forEach { file ->
            processor.processFile(file.path, file.parent)
        }
        processor.generateTemplateCode()

        File("$outputPackage/KtmlRegistryImpl.kt").move("$outputPackage/DefaultKtmlRegistry.kt").modifyText {
            replace("KtmlRegistryImpl", "DefaultKtmlRegistry")
        }
        File(outputPackage).listFiles().forEach { it.modifyText { removeContentComments() } }

        Then
        File("$outputPackage/If.kt").exists() shouldBe true
    }
})

private fun File.modifyText(modifier: String.() -> String) {
    writeText(readText().modifier())
}

private fun File.move(newPath: String): File {
    val newFile = File(newPath)
    newFile.writeBytes(readBytes())
    delete()
    return newFile
}
