package dev.ktool.ktml.parser

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import dev.ktool.ktml.test.BddSpec

class ParsedTemplateSpec : BddSpec({
    "ordered parameters with content parameter" {
        Given
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content"),
                TemplateParameter("onClick", "String"),
                TemplateParameter("isVisible", "Boolean")
            )
        )

        When
        val params = template.orderedParameters

        Then
        params shouldHaveSize 4
        params[0].name shouldBe "isVisible"
        params[1].name shouldBe "onClick"
        params[2].name shouldBe "title"
        params[3].name shouldBe "content"
    }

    "ordered parameters without content parameter" {
        Given
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("onClick", "String"),
                TemplateParameter("count", "Int"),
                TemplateParameter("isVisible", "Boolean")
            )
        )

        When
        val params = template.orderedParameters

        Then
        params shouldHaveSize 4
        params[0].name shouldBe "count"
        params[1].name shouldBe "isVisible"
        params[2].name shouldBe "onClick"
        params[3].name shouldBe "title"
    }

    "ordered parameters with multiple content parameters" {
        Given
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content"),
                TemplateParameter("sidebar", "Content"),
                TemplateParameter("onClick", "String"),
            )
        )

        When
        val params = template.orderedParameters

        Then
        params[0].name shouldBe "onClick"
        params[1].name shouldBe "sidebar"
        params[2].name shouldBe "title"
        params[3].name shouldBe "content"
    }

    "ordered parameters with content parameter not named content" {
        Given
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("body", "Content"),
                TemplateParameter("onClick", "String"),
            )
        )

        When
        val params = template.orderedParameters

        Then
        params shouldHaveSize 3
        params[0].name shouldBe "body"
        params[1].name shouldBe "onClick"
        params[2].name shouldBe "title"
    }
})

private fun parsedTemplate(parameters: List<TemplateParameter>) = ParsedTemplate(
    name = "test-component",
    imports = emptyList(),
    parameters = parameters,
    root = HtmlElement.Tag("test-component", emptyMap())
)
