package dev.ktml

import dev.ktool.kotest.BddSpec

class KtmlEngineSpec : BddSpec({
    val engine = KtmlEngine()

    "process file" {
        Given
        val templatesDirs = listOf("src/commonMain/resources/templates", "src/commonTest/resources/templates")
        val outputDir = "src/commonTest/kotlin"

        When
        engine.processRootDirectories(templatesDirs)
        engine.generateTemplateCode(outputDir)

        Then
        println("Done generating code")
    }
})