package io.ktml.parser

import kotlin.test.Test
import kotlin.test.assertEquals

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

        assertEquals(1, result.size)
        assertEquals("my-button", result[0].name)
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

        val result = parser.parseContent(content)

        assertEquals(2, result.size)
        assertEquals("my-button-one", result[0].name)
        assertEquals("my-button-two", result[1].name)
    }

    @Test
    fun testPrefixAddedToRootName() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content, "prefix-")

        assertEquals(1, result.size)
        assertEquals("prefix-my-button", result[0].name)
    }

    @Test
    fun testGetParametersFromAttributes() {
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content)

        assertEquals(1, result.size)
        assertEquals(2, result[0].parameters.size)
        assertEquals("text", result[0].parameters[0].name)
        assertEquals("String", result[0].parameters[0].type)
        assertEquals("onClick", result[0].parameters[1].name)
        assertEquals("String", result[0].parameters[1].type)
    }

    @Test
    fun testGetParametersWithDefaultValues() {
        val content = $$"""
            <my-button text="String = 'Default Text'">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        val result = parser.parseContent(content).first()

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

        val result = parser.parseContent(content).first()

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

        val result = parser.parseContent(content).first()

        assertEquals("my-button", result.name)
        assertEquals("my-button", result.root.name)
    }
}
