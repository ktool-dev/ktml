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
            KtmlDynamicRegistry(dir.absolutePath, false).templates
        }

        Then
        ex.printStackTrace()
        ex.errors.size shouldBe 1
        ex.errors[0].filePath shouldBe "something.ktml"
        ex.errors[0].message shouldContain $$"""<div class="$missing"></div>"""
    }

    "displays multiline expression" {
        Given
        val dir = createTempDirectory().toFile()
        val template = $$"""
            <something>
                <div>
                    ${
                       if(1 > 4) {
                          b + 2
                       } else if(a == 4) {
                          6
                       }
                    }
                </div>
            </something>
        """.trimIndent()
        File(dir, "something.ktml").writeText(template)

        When
        val ex = shouldThrow<CompileException> {
            KtmlDynamicRegistry(dir.absolutePath, false).templates
        }

        Then
        ex.printStackTrace()
        ex.errors.size shouldBe 1
        ex.errors[0].message shouldContain """} else if(a == 4) {"""
    }

    "handles compiler errors in content above the starting tag" {
        Given
        val dir = createTempDirectory().toFile()
        val template = $$"""
            val a = ""
            a + b
            
            <something>
                <div>$a</div>
            </something>
        """.trimIndent()
        File(dir, "something.ktml").writeText(template)

        When
        val ex = shouldThrow<CompileException> {
            KtmlDynamicRegistry(dir.absolutePath, false).templates
        }

        Then
        ex.printStackTrace()
        ex.errors.size shouldBe 1
        ex.errors[0].message shouldContain "Code above the template tags"
        ex.errors[0].message shouldContain "a + b"
    }

    "handles errors in template parameters" {
        Given
        val dir = createTempDirectory().toFile()
        val template = $$"""
            <something value="${String = null}">
                <div>a</div>
            </something>
        """.trimIndent()
        File(dir, "something.ktml").writeText(template)

        When
        val ex = shouldThrow<CompileException> {
            KtmlDynamicRegistry(dir.absolutePath, false).templates
        }

        Then
        ex.printStackTrace()
        ex.errors.size shouldBe 1
        ex.errors[0].message shouldContain "The expression on line 0 starting at column"
        ex.errors[0].message shouldContain $$"""<something value="${String = null}">"""
    }
})
