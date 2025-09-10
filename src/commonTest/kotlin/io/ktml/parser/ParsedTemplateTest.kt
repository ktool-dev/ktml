package io.ktml.parser

import kotlin.test.Test
import kotlin.test.assertEquals

class ParsedTemplateTest {
    @Test
    fun testOrderedParametersWithContentParameter() {
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content"),
                TemplateParameter("onClick", "String"),
                TemplateParameter("isVisible", "Boolean")
            )
        )

        val params = template.orderedParameters

        assertEquals(4, params.size)
        assertEquals("isVisible", params[0].name)
        assertEquals("onClick", params[1].name)
        assertEquals("title", params[2].name)
        assertEquals("content", params[3].name)
    }

    @Test
    fun testOrderedParametersWithoutContentParameter() {
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("onClick", "String"),
                TemplateParameter("count", "Int"),
                TemplateParameter("isVisible", "Boolean")
            )
        )

        val params = template.orderedParameters

        assertEquals(4, params.size)
        assertEquals("count", params[0].name)
        assertEquals("isVisible", params[1].name)
        assertEquals("onClick", params[2].name)
        assertEquals("title", params[3].name)
    }

    @Test
    fun testOrderedParametersWithMultipleContentParameters() {
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("content", "Content"),
                TemplateParameter("sidebar", "Content"),
                TemplateParameter("onClick", "String"),
            )
        )

        val params = template.orderedParameters

        assertEquals("onClick", params[0].name)
        assertEquals("sidebar", params[1].name)
        assertEquals("title", params[2].name)
        assertEquals("content", params[3].name)
    }

    @Test
    fun testOrderedParametersWithContentParameterNotNamedContent() {
        val template = parsedTemplate(
            listOf(
                TemplateParameter("title", "String"),
                TemplateParameter("body", "Content"),
                TemplateParameter("onClick", "String"),
            )
        )

        val params = template.orderedParameters

        assertEquals(3, params.size)
        assertEquals("body", params[0].name)
        assertEquals("onClick", params[1].name)
        assertEquals("title", params[2].name)
    }
}

fun parsedTemplate(parameters: List<TemplateParameter>) = ParsedTemplate(
    name = "test-component",
    imports = emptyList(),
    parameters = parameters,
    root = HtmlElement.Tag("test-component", emptyMap())
)
