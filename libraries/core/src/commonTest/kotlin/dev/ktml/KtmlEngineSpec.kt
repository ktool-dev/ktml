package dev.ktml

import dev.ktool.kotest.BddSpec

class KtmlEngineSpec : BddSpec({
    val engine = KtmlEngine("src/commonTest/resources/templates")

    "process file" {
        Given
        val templatesDir = "src/commonTest/resources/templates"
        val outputDir = "src/commonTest/kotlin"

        When
        engine.processDirectory(templatesDir)
        engine.generateTemplateCode(outputDir)

        Then
        println("Done generating code")
    }
})