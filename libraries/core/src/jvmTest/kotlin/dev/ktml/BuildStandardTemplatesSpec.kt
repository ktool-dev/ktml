package dev.ktml

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import java.io.File

class BuildStandardTemplatesSpec : BddSpec({
    "build" {
        Given
        val output = "src/commonMain/kotlin"
        val outputPackage = "$output/dev/ktml/templates"
        val processor = KtmlProcessor(outputDirectory = output)

        When
        File("src/commonMain/resources/templates").listFiles().forEach { file ->
            processor.processFile(file.path, file.parent)
        }
        processor.generateTemplateCode()

        val registryFile = File("$outputPackage/KtmlRegistryImpl.kt")
        File("$outputPackage/DefaultKtmlRegistry.kt").writeText(
            registryFile.readText().replace("KtmlRegistryImpl", "DefaultKtmlRegistry")
        )
        registryFile.delete()

        Then
        File("$outputPackage/DefaultError.kt").exists() shouldBe true
    }
})