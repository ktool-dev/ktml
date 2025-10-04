package dev.ktml.gen

import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.ParsedTemplateParameter
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.string.shouldContain

class TemplateRegistryGeneratorSpec : BddSpec({
    "generate registry with single template" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "button",
                imports = emptyList(),
                parameters = listOf(
                    ParsedTemplateParameter("text", "String")
                ),
                root = HtmlElement.Tag("button", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "object TemplateRegistryImpl : TemplateRegistry"
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "name = \"button\""
        result shouldContain "functionName = \"writeButton\""
        result shouldContain "TemplateParameter(\"text\", \"String\", false)"
    }

    "generate registry with multiple templates" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "button",
                imports = emptyList(),
                parameters = listOf(
                    ParsedTemplateParameter("text", "String")
                ),
                root = HtmlElement.Tag("button", emptyMap())
            ),
            ParsedTemplate(
                name = "card",
                imports = emptyList(),
                parameters = listOf(
                    ParsedTemplateParameter("title", "String"),
                    ParsedTemplateParameter("content", "Content")
                ),
                root = HtmlElement.Tag("card", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "\"card\" to { writeCard() }"
        result shouldContain "name = \"button\""
        result shouldContain "name = \"card\""
    }

    "generate registry with template with default values" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "button",
                imports = emptyList(),
                parameters = listOf(
                    ParsedTemplateParameter("text", "String", "Click me"),
                    ParsedTemplateParameter("disabled", "Boolean", "false")
                ),
                root = HtmlElement.Tag("button", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "TemplateParameter(\"text\", \"String\", true)"
        result shouldContain "TemplateParameter(\"disabled\", \"Boolean\", true)"
    }

    "generate registry with template in subpath" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "icon",
                subPath = "components",
                imports = emptyList(),
                parameters = listOf(
                    ParsedTemplateParameter("name", "String")
                ),
                root = HtmlElement.Tag("icon", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "\"components/icon\" to { writeComponentsIcon() }"
        result shouldContain "subPath = \"components\""
        result shouldContain "packageName = \"dev.ktml.templates.components\""
        result shouldContain "import dev.ktml.templates.components.writeIcon as writeComponentsIcon"
    }

    "generate registry with template without parameters" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "header",
                imports = emptyList(),
                parameters = emptyList(),
                root = HtmlElement.Tag("header", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "\"header\" to { writeHeader() }"
        result shouldContain "name = \"header\""
        result shouldContain "functionName = \"writeHeader\""
        result shouldContain "TemplateDefinition(\n            name = \"header\",\n            packageName = \"dev.ktml.templates\",\n            functionName = \"writeHeader\",\n        )"
    }

    "generate registry imports" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "button",
                imports = emptyList(),
                parameters = emptyList(),
                root = HtmlElement.Tag("button", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "import dev.ktml.Content"
        result shouldContain "import dev.ktml.TemplateDefinition"
        result shouldContain "import dev.ktml.TemplateRegistry"
        result shouldContain "import dev.ktml.TemplateParameter"
    }

    "generate registry with mixed subpaths" {
        Given
        val templates = listOf(
            ParsedTemplate(
                name = "button",
                imports = emptyList(),
                parameters = emptyList(),
                root = HtmlElement.Tag("button", emptyMap())
            ),
            ParsedTemplate(
                name = "icon",
                subPath = "components",
                imports = emptyList(),
                parameters = emptyList(),
                root = HtmlElement.Tag("icon", emptyMap())
            )
        )

        When
        val generator = TemplateRegistryGenerator(templates)
        val result = generator.createTemplateRegistry()

        Then
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "import dev.ktml.templates.components.writeIcon as writeComponentsIcon"
        result shouldContain "\"components/icon\" to { writeComponentsIcon() }"
    }
})
