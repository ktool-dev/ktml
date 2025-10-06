package dev.ktml.gen

import dev.ktml.TemplateDefinition
import dev.ktml.TemplateParameter
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.string.shouldContain

class TemplateRegistryGeneratorSpec : BddSpec({
    val basePackageName = "dev.ktml.templates"

    "generate registry with single template" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "button",
                parameters = listOf(TemplateParameter("text", "String", false)),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "object TemplateRegistryImpl : TemplateRegistry"
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "name = \"button\""
        result shouldContain "TemplateParameter(\"text\", \"String\", false)"
    }

    "generate registry with multiple templates" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "button",
                parameters = listOf(TemplateParameter("text", "String", false)),
            ),
            TemplateDefinition(
                packageName = basePackageName,
                name = "card",
                parameters = listOf(
                    TemplateParameter("title", "String", false),
                    TemplateParameter("content", "Content", false),
                ),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "\"card\" to { writeCard() }"
        result shouldContain "name = \"button\""
        result shouldContain "name = \"card\""
    }

    "generate registry with template with default values" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "button",
                parameters = listOf(
                    TemplateParameter("text", "String", true),
                    TemplateParameter("disabled", "Boolean", true)
                ),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "TemplateParameter(\"text\", \"String\", true)"
        result shouldContain "TemplateParameter(\"disabled\", \"Boolean\", true)"
    }

    "generate registry with template in subpath" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = "$basePackageName.components",
                name = "icon",
                subPath = "components",
                parameters = listOf(TemplateParameter("name", "String", false)),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "\"components/icon\" to { writeComponentsIcon() }"
        result shouldContain "subPath = \"components\""
        result shouldContain "packageName = \"dev.ktml.templates.components\""
        result shouldContain "import dev.ktml.templates.components.writeIcon as writeComponentsIcon"
    }

    "generate registry with template without parameters" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "header",
                parameters = emptyList(),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "\"header\" to { writeHeader() }"
        result shouldContain "name = \"header\""
        result shouldContain "TemplateDefinition(\n            name = \"header\",\n            packageName = \"dev.ktml.templates\",\n        )"
    }

    "generate registry imports" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "button",
                parameters = emptyList(),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "import dev.ktml.Content"
        result shouldContain "import dev.ktml.TemplateDefinition"
        result shouldContain "import dev.ktml.TemplateRegistry"
        result shouldContain "import dev.ktml.TemplateParameter"
    }

    "generate registry with mixed subpaths" {
        Given
        val templates = listOf(
            TemplateDefinition(
                packageName = basePackageName,
                name = "button",
                parameters = emptyList(),
            ),
            TemplateDefinition(
                packageName = "$basePackageName.components",
                name = "icon",
                subPath = "components",
                parameters = emptyList(),
            )
        )

        When
        val result = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, templates)

        Then
        result shouldContain "\"button\" to { writeButton() }"
        result shouldContain "import dev.ktml.templates.components.writeIcon as writeComponentsIcon"
        result shouldContain "\"components/icon\" to { writeComponentsIcon() }"
    }
})
