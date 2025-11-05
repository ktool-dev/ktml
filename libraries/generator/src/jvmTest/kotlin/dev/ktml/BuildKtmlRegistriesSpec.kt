package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import java.io.File

private const val RUNTIME_PATH = "../runtime/src/commonMain"

class BuildDefaultKtmlRegistrySpec : BddSpec({
    "build DefaultKtmlRegistry in runtime module" {
        Given
        val outputPath = "$RUNTIME_PATH/kotlin"
        val templatePath = "$RUNTIME_PATH/ktml/default"
        val outputPackage = "$outputPath/dev/ktml/templates"
        val processor = KtmlProcessor(outputDirectory = outputPath)

        When
        File(templatePath).listFiles().forEach { file ->
            processor.processFile(file.path, file.parent)
        }
        processor.generateTemplateCode()

        File("$outputPackage/KtmlRegistry.kt").move("$outputPackage/DefaultKtmlRegistry.kt").modifyText {
            replace("object KtmlRegistry", "object DefaultKtmlRegistry")
        }

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
