package dev.ktml.parser.kotlin

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class ExpressionParserSpec : BddSpec({
    "can parse kotlin expressions out of text" {
        Given
        val content = $$"""
            <div value="something${a} something-else-$b another-${if(a == b) 'blah' else 'stuff'}">
                <div>
                    Anything ${a + b}
                    Another $any
                    Big time ${
                        if(a == b) {
                            "blah"
                        } else {
                            "stuff"
                        }
                    }
                </div>
            </div>
        """.trimIndent()

        When
        val (result, expressions) = content.replaceKotlinExpressions()

        Then
        expressions.size shouldBe 6
        expressions[0].content shouldBe "a"
        expressions[0].start shouldBe Location(0, 21)
        expressions[1].content shouldBe "b"
        expressions[1].start shouldBe Location(0, 41)
        expressions[2].content shouldBe "if(a == b) 'blah' else 'stuff'"
        expressions[2].start shouldBe Location(0, 52)
        expressions[3].content shouldBe "a + b"
        expressions[3].start shouldBe Location(2, 17)
        expressions[4].content shouldBe "any"
        expressions[4].start shouldBe Location(3, 16)
        expressions[5].content shouldBe """
            if(a == b) {
                "blah"
            } else {
                "stuff"
            }
        """.trimIndent().trim()
        expressions[5].start shouldBe Location(4, 17)
        result shouldBe """
            <div value="something${expressions[0].key} something-else-${expressions[1].key} another-${expressions[2].key}">
                <div>
                    Anything ${expressions[3].key}
                    Another ${expressions[4].key}
                    Big time ${expressions[5].key}
                </div>
            </div>
        """.trimIndent()
    }

    "will ignore dollar with a backslash in front" {
        Given
        val escaped = "\\$"
        val content = "<div>$escaped{something} and ${escaped}something</div>"

        When
        val (result, expressions) = content.replaceKotlinExpressions()

        Then
        result shouldBe content.replace(escaped, "$")
        expressions.size shouldBe 0
    }

    "will ignore interpolation inside javascript backtick string if it's escaped" {
        Given
        val content = $$"""
            <button onclick="`\${something}`">Hello</button>
        """.trimIndent()

        When
        val (result, expressions) = content.replaceKotlinExpressions()

        Then
        result shouldBe content.replace("\\$", "$")
        expressions.size shouldBe 0
    }
})
