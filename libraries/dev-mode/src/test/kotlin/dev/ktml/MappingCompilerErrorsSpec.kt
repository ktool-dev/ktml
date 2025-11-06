package dev.ktml

import dev.ktool.kotest.BddSpec
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
        val result = renderError(dir)

        Then
        result shouldContain "1 error"
        result shouldContain $$"""<div class="$missing"></div>""".encodeHtml()
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
        val result = renderError(dir)

        Then
        result shouldContain "1 error"
        result shouldContain """} else if(a == 4) {"""
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
        val result = renderError(dir)

        Then
        result shouldContain "1 error"
        result shouldContain "Code above the template tags"
        result shouldContain "a + b"
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
        val result = renderError(dir)

        Then
        result shouldContain "1 error"
        result shouldContain "The expression on line 0 starting at column"
        result shouldContain $$"""<something value="${String = null}">""".encodeHtml()
    }
})

private suspend fun renderError(dir: File) =
    KtmlEngine(KtmlDynamicRegistry(dir.absolutePath, DEFAULT_PACKAGE, false)).renderPage("anything")
