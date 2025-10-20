package dev.ktml.gen

import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class ExpressionParserSpec : BddSpec({
    "isSingleKotlinExpression should return true for valid expressions"(
        row($$"${variable}"),
        row($$"${object.property}"),
        row($$"${function()}"),
        row($$"${user.getName()}"),
        row($$"${count + 1}"),
        row($$"${if (condition) 'yes' else 'no'}"),
        row($$"${ variable }"),
        row($$"${}"),
        row($$"${complex.nested.function(param1, param2)}"),
    ) { (expression) ->
        Expect
        expression.isSingleKotlinExpression() shouldBe true
    }

    "isSingleKotlinExpression should return false for invalid expressions"(
        row("variable"),
        row($$"$variable"),
        row($$"${unclosed"),
        row("unopened}"),
        row("regular text"),
        row(""),
        row("$"),
        row("{variable}"),
        row($$"${"),
        row("}"),
        row($$"Hello ${name}!"),
    ) { (expression) ->
        Expect
        expression.isSingleKotlinExpression() shouldBe false
    }

    "hasKotlinExpression should return true when expressions are present"(
        row($$"Hello ${name}!"),
        row($$"${greeting} ${name}!"),
        row($$"You have ${count} messages"),
        row($$"${variable}"),
        row($$"Before ${middle} after"),
        row($$"${first} and ${second}"),
        row($$"Text with ${nested.property.call()}")
    ) { (text) ->
        Expect
        text.hasKotlinInterpolation() shouldBe true
    }

    "hasKotlinExpression should return false when no expressions are present"(
        row("Hello World!"),
        row("regular text"),
        row(""),
        row("$ without braces"),
        row("} without dollar"),
        row("{ without dollar }"),
        row("$ incomplete"),
        row("incomplete }")
    ) { (text) ->
        Expect
        text.hasKotlinInterpolation() shouldBe false
    }

    "extractSingleExpression should extract expression content correctly"(
        row($$"${name}", "name"),
        row($$"${user.getName()}", "user.getName()"),
        row($$"${count + 1}", "count + 1"),
        row($$"${ variable }", " variable "),
        row($$"${}", ""),
        row($$"${complex.nested.function(param1, param2)}", "complex.nested.function(param1, param2)")
    ) { (input, expected) ->
        Expect
        input.extractAttributeExpression() shouldBe expected
    }

    "extractSingleExpression should decode HTML entities"(
        row($$"${name &amp; title}", "name & title"),
        row($$"${value &lt; max}", "value < max"),
        row($$"${text &gt; min}", "text > min"),
        row($$"${&quot;quoted&quot;}", "\"quoted\""),
        row($$"${a &amp;&amp; b}", "a && b")
    ) { (input, expected) ->
        Expect
        input.extractAttributeExpression() shouldBe expected
    }

    "extractMultipleExpressions should handle mixed content correctly" {
        Given
        val content = $$"Hello ${name}, you have ${count} messages"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 5
        result[0].text shouldBe "Hello "
        result[0].isKotlin shouldBe false
        result[1].text shouldBe "name"
        result[1].isKotlin shouldBe true
        result[2].text shouldBe ", you have "
        result[2].isKotlin shouldBe false
        result[3].text shouldBe "count"
        result[3].isKotlin shouldBe true
        result[4].text shouldBe " messages"
        result[4].isKotlin shouldBe false
    }

    "extractMultipleExpressions should handle only expressions" {
        Given
        val content = $$"${greeting}${name}"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 2
        result[0].text shouldBe "greeting"
        result[0].isKotlin shouldBe true
        result[1].text shouldBe "name"
        result[1].isKotlin shouldBe true
    }

    "extractMultipleExpressions should handle only text" {
        Given
        val content = "Hello World"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 1
        result[0].text shouldBe "Hello World"
        result[0].isKotlin shouldBe false
    }

    "extractMultipleExpressions should handle empty content" {
        Given
        val content = ""

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 0
    }

    "extractMultipleExpressions should handle single expression" {
        Given
        val content = $$"${variable}"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 1
        result[0].text shouldBe "variable"
        result[0].isKotlin shouldBe true
    }

    "extractMultipleExpressions should decode HTML entities in expressions" {
        Given
        val content = $$"Value: ${a &amp; b}"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 2
        result[0].text shouldBe "Value: "
        result[0].isKotlin shouldBe false
        result[1].text shouldBe "a & b"
        result[1].isKotlin shouldBe true
    }

    "extractMultipleExpressions should handle complex nested expressions" {
        Given
        val content = $$"Start ${obj.method(param1, param2)} middle ${another.call()} end"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 5
        result[0].text shouldBe "Start "
        result[0].isKotlin shouldBe false
        result[1].text shouldBe "obj.method(param1, param2)"
        result[1].isKotlin shouldBe true
        result[2].text shouldBe " middle "
        result[2].isKotlin shouldBe false
        result[3].text shouldBe "another.call()"
        result[3].isKotlin shouldBe true
        result[4].text shouldBe " end"
        result[4].isKotlin shouldBe false
    }

    "extractMultipleExpressions should handle empty expressions" {
        Given
        val content = $$"Before ${} after"

        When
        val result = content.extractMultipleExpressions()

        Then
        result.size shouldBe 3
        result[0].text shouldBe "Before "
        result[0].isKotlin shouldBe false
        result[1].text shouldBe ""
        result[1].isKotlin shouldBe true
        result[2].text shouldBe " after"
        result[2].isKotlin shouldBe false
    }
})