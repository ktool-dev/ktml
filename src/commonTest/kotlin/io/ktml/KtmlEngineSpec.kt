package io.ktml

import io.ktml.test.BddSpec

class KtmlEngineSpec : BddSpec({
    val engine = KtmlEngine("src/commonTest/resources/templates")

    "process file" {
        Given
        val templatesDir = "src/commonTest/resources/templates"
        val outputDir = "src/commonTest/resources/output"

        When
        engine.processDirectory(templatesDir)
        engine.generateTemplateCode(outputDir)

        Then
        println("Done")
    }
})
