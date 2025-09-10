package io.ktml.gen

import io.ktml.TEMPLATE_PACKAGE
import io.ktml.Templates
import io.ktml.parser.HtmlElement
import io.ktml.parser.ParsedTemplate
import io.ktml.parser.TemplateParameter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinFileGeneratorTest {
    private val kotlinFileGenerator = KotlinFileGenerator(Templates())

    @Test
    fun testGenerateCodeWithBasicTemplate() {
        val template = ParsedTemplate(
            name = "my-button",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("text", "String"),
                TemplateParameter("onClick", "String")
            ),
            root = HtmlElement.Tag("my-button", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "package $TEMPLATE_PACKAGE")
        assertContains(result, "import io.ktml.Context")
        assertContains(
            result, """
            fun Context.writeMyButton(
                text: String,
                onClick: String,
            ) {""".trimIndent()
        )
    }

    @Test
    fun testGenerateCodeWithImports() {
        val template = ParsedTemplate(
            name = "custom-component",
            imports = listOf("import kotlinx.datetime.LocalDate", "import kotlin.collections.List"),
            parameters = emptyList(),
            root = HtmlElement.Tag("custom-component", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        val lines = result.lines()
        val importLines = lines.filter { it.startsWith("import") }
        assertEquals(
            listOf(
                "import io.ktml.Context",
                "import kotlin.collections.List",
                "import kotlinx.datetime.LocalDate"
            ), importLines
        )
    }

    @Test
    fun testGenerateCodeWithContentParameter() {
        val template = ParsedTemplate(
            name = "card",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content")
            ),
            root = HtmlElement.Tag("card", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "import io.ktml.Content")
        assertContains(result, "import io.ktml.Context")
        assertContains(
            result, """
            fun Context.writeCard(
                title: String,
                content: Content,
            ) {
        """.trimIndent()
        )
    }

    @Test
    fun testGenerateCodeWithDefaultValues() {
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

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "text: String = \"Click me\"")
        assertContains(result, "disabled: Boolean = false")
        assertContains(result, "count: Int = 0")
    }

    @Test
    fun testGenerateCodeWithMixedParameters() {
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

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "import io.ktml.Content")
        assertContains(result, "import io.ktml.Context")
        assertContains(result, "import kotlinx.serialization.Serializable")
        assertContains(
            result,
            """
            fun Context.writeFormInput(
                label: String,
                placeholder: String = "Enter text",
                required: Boolean = true,
                content: Content,
            ) {""".trimIndent()
        )
    }

    @Test
    fun testToCamelCaseConversion() {
        val template = ParsedTemplate(
            name = "my-custom-button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("my-custom-button", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun Context.writeMyCustomButton() {")
    }

    @Test
    fun testToCamelCaseWithSingleWord() {
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("button", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun Context.writeButton() {")
    }

    @Test
    fun testGenerateCodeWithNoParameters() {
        val template = ParsedTemplate(
            name = "header",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("header", emptyMap())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun Context.writeHeader() {")
        assertContains(result, "import io.ktml.Context")
        // Should not import Content if no Content parameters
        assertTrue(!result.contains("import io.ktml.Content"))
    }

    @Test
    fun testGenerateCodeStructure() {
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

        val result = kotlinFileGenerator.generateCode(template)

        val expected = """
            package io.ktml.templates

            import io.ktml.Context
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
        assertEquals(expected, result)
    }
}
