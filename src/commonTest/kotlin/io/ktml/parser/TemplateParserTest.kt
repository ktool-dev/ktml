package io.ktml.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TemplateParserTest {
    private val parser = TemplateParser()

    @Test
    fun testGetTemplateNameFromTag() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals("my-button", result.name)
    }

    @Test
    fun testGetMultipleRootElements() {
        val content = $$"""
            <my-button-one text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button-one>
            <my-button-two text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button-two>
        """.trimIndent()

        assertFailsWith(IllegalArgumentException::class) {
            parser.parseContent(content)
        }
    }

    @Test
    fun testPackageNameSet() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content, "my.package")

        assertEquals("my.package", result.packageName)
    }

    @Test
    fun testGetParametersFromAttributes() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals(2, result.parameters.size)
        assertEquals("text", result.parameters[0].name)
        assertEquals("String", result.parameters[0].type)
        assertEquals("onClick", result.parameters[1].name)
        assertEquals("String", result.parameters[1].type)
    }

    @Test
    fun testGetParametersWithDefaultValues() {
        val content = $$"""
            <my-button text="String = 'Default Text'">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals("text", result.parameters[0].name)
        assertEquals("String", result.parameters[0].type)
        assertEquals("Default Text", result.parameters[0].defaultValue)
    }

    @Test
    fun testAllowImportsAboveRoot() {
        val content = $$"""
            import my.app.UserType
            
            <my-button text="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals("my-button", result.name)
        assertEquals(1, result.imports.size)
        assertEquals("import my.app.UserType", result.imports[0])
    }

    @Test
    fun testShouldAssignRoot() {
        val content = $$"""
            <my-button text="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals("my-button", result.name)
        assertEquals("my-button", result.root.name)
    }

    @Test
    fun testAllowsSpecialCharactersInExpressions() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button class="${if(a < b && b < c) {'a'} else {'b'}}">A</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        val tag = result.root.children.find { it is HtmlElement.Tag && it.name == "button" } as HtmlElement.Tag
        assertEquals($$"${if(a < b && b < c) {'a'} else {'b'}}", tag.attrs["class"])
    }

    @Test
    fun testIncludesExternalScript() {
        val content = $$"""
            <script type="text/kotlin">
                val a = 1
            </script>
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
            <script type="text/kotlin">
                val b = 1
            </script>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals("val a = 1", result.topExternalScriptContent)
        assertEquals("val b = 1", result.bottomExternalScriptContent)
    }
}
