package dev.ktml

import dev.ktml.util.CompileException
import dev.ktool.kotest.BddSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.io.File
import kotlin.io.path.createTempDirectory

class MappingCompilerErrorsSpec : BddSpec({
    "maps a compiler error back to the actual line in the template" {
        Given
        val dir = createTempDirectory().toFile()
        val template = $$"""
            <something>
                <div class="$missing"></div>
            </something>
        """.trimIndent()
        File(dir, "something.ktml").writeText(template)

        When
        val ex = shouldThrow<CompileException> {
            KtmlDynamicRegistry(dir.absolutePath, false).initializeRegistry()
        }

        Then
        ex.errors.size shouldBe 1
        ex.errors[0].filePath shouldBe "something.ktml"
        ex.errors[0].message shouldContain $$"""<div class="$missing"></div>"""
    }
})
