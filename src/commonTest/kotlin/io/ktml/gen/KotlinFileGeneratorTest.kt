package io.ktml.gen

import io.ktml.parser.HtmlElement
import io.ktml.parser.ParsedTemplate
import io.ktml.parser.TemplateParameter
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinFileGeneratorTest {
    private val kotlinFileGenerator = KotlinFileGenerator()

    @Test
    fun testGenerateCodeWithBasicTemplate() {
        val template = ParsedTemplate(
            name = "my-button",
            imports = emptyList(),
            parameters = listOf(
                TemplateParameter("text", "String"),
                TemplateParameter("onClick", "String")
            ),
            root = HtmlElement.Tag("my-button", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "package io.ktml.templates")
        assertContains(result, "import io.ktml.HtmlWriter")
        assertContains(result, "fun HtmlWriter.writeMyButton(text: String, onClick: String) {")
    }

    @Test
    fun testGenerateCodeWithImports() {
        val template = ParsedTemplate(
            name = "custom-component",
            imports = listOf("import kotlinx.datetime.LocalDate", "import kotlin.collections.List"),
            parameters = emptyList(),
            root = HtmlElement.Tag("custom-component", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        val lines = result.lines()
        val importLines = lines.filter { it.startsWith("import") }
        assertEquals(
            listOf(
                "import io.ktml.HtmlWriter",
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
            root = HtmlElement.Tag("card", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "import io.ktml.Content")
        assertContains(result, "import io.ktml.HtmlWriter")
        assertContains(result, "fun HtmlWriter.writeCard(title: String, content: Content) {")
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
            root = HtmlElement.Tag("button", emptyMap(), emptyList())
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
            root = HtmlElement.Tag("form-input", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "import io.ktml.Content")
        assertContains(result, "import io.ktml.HtmlWriter")
        assertContains(result, "import kotlinx.serialization.Serializable")
        assertContains(
            result,
            "fun HtmlWriter.writeFormInput(label: String, placeholder: String = \"Enter text\", required: Boolean = true, content: Content) {"
        )
    }

    @Test
    fun testToCamelCaseConversion() {
        val template = ParsedTemplate(
            name = "my-custom-button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("my-custom-button", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun HtmlWriter.writeMyCustomButton() {")
    }

    @Test
    fun testToCamelCaseWithSingleWord() {
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("button", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun HtmlWriter.writeButton() {")
    }

    @Test
    fun testGenerateCodeWithNoParameters() {
        val template = ParsedTemplate(
            name = "header",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("header", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)

        assertContains(result, "fun HtmlWriter.writeHeader() {")
        assertContains(result, "import io.ktml.HtmlWriter")
        // Should not import Content if no Content parameters
        assertTrue(!result.contains("import io.ktml.Content"))
    }

    @Test
    fun testGenerateCodeStructure() {
        val template = ParsedTemplate(
            name = "test-component",
            imports = listOf("import kotlin.String"),
            parameters = listOf(TemplateParameter("value", "String")),
            root = HtmlElement.Tag("test-component", emptyMap(), emptyList())
        )

        val result = kotlinFileGenerator.generateCode(template)
        val lines = result.lines()

        // Check structure: package, blank line, imports, blank line, function
        assertTrue(lines[0].startsWith("package"))
        assertEquals("", lines[1])
        assertTrue(lines[2].startsWith("import"))
        assertTrue(lines[3].startsWith("import"))
        assertEquals("", lines[4])
        assertTrue(lines[5].startsWith("fun HtmlWriter.writeTestComponent"))
        assertEquals("", lines[6])
        assertTrue(lines[7].endsWith("}"))
        assertEquals("", lines[8])

    }
}
