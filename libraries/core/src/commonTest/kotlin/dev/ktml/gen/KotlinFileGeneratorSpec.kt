package dev.ktml.gen

import dev.ktool.kotest.BddSpec
import dev.ktml.TEMPLATE_PACKAGE
import dev.ktml.Templates
import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateParameter
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

class KotlinFileGeneratorSpec : BddSpec({
    val kotlinFileGenerator = KotlinFileGenerator(Templates())

    "generate code with basic template" {
        Given
        val template = ParsedTemplate(
            name = "my-button",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("text", "String"),
                TemplateParameter("onClick", "String")
            ),
            root = HtmlElement.Tag("my-button", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "package $TEMPLATE_PACKAGE"
        result shouldContain "import dev.ktml.Context"
        result shouldContain """
            fun Context.writeMyButton(
                text: String,
                onClick: String,
            ) {""".trimIndent()
    }

    "generate code with imports" {
        Given
        val template = ParsedTemplate(
            name = "custom-component",
            imports = listOf("import kotlinx.datetime.LocalDate", "import kotlin.collections.List"),
            parameters = emptyList(),
            root = HtmlElement.Tag("custom-component", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        val lines = result.lines()
        val importLines = lines.filter { it.startsWith("import") }
        importLines shouldBe listOf(
            "import dev.ktml.Context",
            "import kotlin.collections.List",
            "import kotlinx.datetime.LocalDate"
        )
    }

    "generate code with content parameter" {
        Given
        val template = ParsedTemplate(
            name = "card",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content")
            ),
            root = HtmlElement.Tag("card", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "import dev.ktml.Content"
        result shouldContain "import dev.ktml.Context"
        result shouldContain """
            fun Context.writeCard(
                title: String,
                content: Content,
            ) {
        """.trimIndent()
    }

    "generate code with default values" {
        Given
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("text", "String", "Click me"),
                TemplateParameter("disabled", "Boolean", "false"),
                TemplateParameter("count", "Int", "0")
            ),
            root = HtmlElement.Tag("button", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "text: String = \"Click me\""
        result shouldContain "disabled: Boolean = false"
        result shouldContain "count: Int = 0"
    }

    "generate code with mixed parameters" {
        Given
        val template = ParsedTemplate(
            name = "form-input",
            imports = listOf("import kotlinx.serialization.Serializable"),
            parameters = listOf(
                TemplateParameter("label", "String"),
                TemplateParameter("placeholder", "String", "Enter text"),
                TemplateParameter("required", "Boolean", "true"),
                TemplateParameter("content", "Content")
            ),
            root = HtmlElement.Tag("form-input", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "import dev.ktml.Content"
        result shouldContain "import dev.ktml.Context"
        result shouldContain "import kotlinx.serialization.Serializable"
        result shouldContain """
            fun Context.writeFormInput(
                label: String,
                placeholder: String = "Enter text",
                required: Boolean = true,
                content: Content,
            ) {""".trimIndent()
    }

    "to camel case conversion" {
        Given
        val template = ParsedTemplate(
            name = "my-custom-button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("my-custom-button", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "fun Context.writeMyCustomButton() {"
    }

    "to camel case with single word" {
        Given
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("button", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "fun Context.writeButton() {"
    }

    "generate code with no parameters" {
        Given
        val template = ParsedTemplate(
            name = "header",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("header", emptyMap())
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        result shouldContain "fun Context.writeHeader() {"
        result shouldContain "import dev.ktml.Context"
        // Should not import Content if no Content parameters
        result shouldNotContain "import dev.ktml.Content"
    }

    "generate code structure" {
        Given
        val template = ParsedTemplate(
            name = "test-component",
            imports = listOf("import kotlin.String"),
            parameters = listOf(TemplateParameter("value", "String")),
            root = HtmlElement.Tag(
                "test-component", emptyMap(), mutableListOf(
                    HtmlElement.Text("Hello, World!")
                )
            ),
            topExternalScriptContent = "val a = 1",
            bottomExternalScriptContent = "val b = 1",
        )

        When
        val result = kotlinFileGenerator.generateCode(template)

        Then
        val expected = """
            package dev.ktml.templates

            import dev.ktml.Context
            import kotlin.String

            val a = 1

            fun Context.writeTestComponent(
                value: String,
            ) {
                raw(RAW_CONTENT_0)
            }

            val b = 1

            private const val RAW_CONTENT_0 = ""${'"'}Hello, World!""${'"'}

        """.trimIndent()
        result shouldBe expected
    }
})
