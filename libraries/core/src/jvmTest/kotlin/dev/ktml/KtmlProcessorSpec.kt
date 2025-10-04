package dev.ktml

import dev.ktool.kotest.BddSpec

class KtmlProcessorSpec : BddSpec({
    val engine = KtmlProcessor()

    "process file" {
        Given
        val templatesDirs = listOf("src/commonMain/resources/templates", "src/jvmTest/resources/templates")
        val outputDir = "src/jvmTest/kotlin"

        When
        engine.processRootDirectories(templatesDirs)
        engine.generateTemplateCode(outputDir)

        Then
        println("Done generating code")
    }
})

