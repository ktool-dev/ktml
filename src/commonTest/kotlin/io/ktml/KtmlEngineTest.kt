package io.ktml

import kotlin.test.Test

class KtmlEngineTest {
    private val engine = KtmlEngine("src/commonTest/resources/templates")

    @Test
    fun testProcessFile() {
        engine.processDirectory("src/commonTest/resources/templates")
        engine.generateTemplateCode("src/commonTest/resources/output")
    }
}