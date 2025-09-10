package io.ktml.gen

import io.ktml.Templates
import io.ktml.parser.HtmlElement
import io.ktml.parser.HtmlElement.Tag
import io.ktml.parser.HtmlElement.Text
import io.ktml.parser.ParsedTemplate
import io.ktml.parser.TemplateParameter
import io.ktml.parser.TemplateParser
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentGeneratorTest {
    val parser = TemplateParser()
    val templates = Templates()
    val contentGenerator = ContentGenerator(templates)

    @BeforeTest
    fun setup() {
        parse(
            $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()
        )
        parse(
            $$"""
            <tag-with-content content="Content">
                <div>${content}</div>
            </tag>
        """.trimIndent()
        )
        parse(
            """
            <if inline test="Boolean" content="Content" else="Content? = null">
                <script type="text/kotlin">
                    if (test) write(content) else write(_else)
                </script>
            </if>
            """
        )
    }

    @Test
    fun testBasicContent() {
        val children = mutableListOf(
            Text("Hello, "),
            Tag("b", emptyMap(), mutableListOf(Text("World"))),
            Text("!"),
        )
        val template = parsedTemplate(children = children)

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals("    raw(${RAW_PREFIX}0)", result.functionContent)
    }

    @Test
    fun testTagWithKotlinAttribute() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <button onclick="${onClick}">Hello</button>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                raw(${RAW_PREFIX}0)
                write(onClick)
                raw(${RAW_PREFIX}1)
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testTagWithKotlinText() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <h1>Hello ${text} </h1>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                raw(${RAW_PREFIX}0)
                write(text)
                raw(${RAW_PREFIX}1)
            """.trimIndent(), result.functionContent.trimIndent()
        )

    }

    @Test
    fun testTagWithMultipleKotlinText() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <h1>Hello ${text} a ${text} </h1>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                raw(${RAW_PREFIX}0)
                write(text)
                raw(${RAW_PREFIX}1)
                write(text)
                raw(${RAW_PREFIX}2)
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testTagWithTemplateCall() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <my-button text="Hello" onClick="${onClick}"/>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                io.ktml.templates.writeMyButton(
                    onClick = onClick,
                    text = "Hello",
                )
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testTagWithContentParameter() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <tag-with-content>
                    <h1>Hello ${text} </h1>
                </tag-with-content>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                io.ktml.templates.writeTagWithContent() {
                    raw(${RAW_PREFIX}0)
                    write(text)
                    raw(${RAW_PREFIX}1)
                }
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testContentInNestedTag() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <div><div>
                    <tag-with-content>
                        <div>
                            <h1>Hello ${text} </h1>
                        </div>
                    </tag-with-content>
                </div></div>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                raw(${RAW_PREFIX}0)
                io.ktml.templates.writeTagWithContent() {
                    raw(${RAW_PREFIX}1)
                    write(text)
                    raw(${RAW_PREFIX}2)
                }
                raw(${RAW_PREFIX}3)
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testKotlinScript() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <script type="text/kotlin">
                    val a = 1
                </script>
                <h1>Hello ${text} </h1>
                <script type="text/kotlin">
                    val b = 1
                </script>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                val a = 1
                raw(${RAW_PREFIX}0)
                write(text)
                raw(${RAW_PREFIX}1)
                val b = 1
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testGenerateIfForIfAttribute() {
        val template = parse(
            $$"""
            <tag text="String" onClick="String">
                <h1 if='${text == "Hello"}'>Hello ${text} </h1>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                if (text == "Hello") {
                    raw(${RAW_PREFIX}0)
                    write(text)
                    raw(${RAW_PREFIX}1)
                }
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testGenerateEachForEachAttribute() {
        val template = parse(
            $$"""
            <tag items="List<String>">
                <h1 each="${item in items}">Hello ${item} </h1>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                for (item in items) {
                    raw(${RAW_PREFIX}0)
                    write(item)
                    raw(${RAW_PREFIX}1)
                }
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    @Test
    fun testIfAndEachOnSameTag() {
        val template = parse(
            $$"""
            <tag items="List<String>">
                <h1 if="${items.size > 1}" each="${item in items}">Hello ${item} </h1>
            </tag>
        """.trimIndent()
        )

        val result = contentGenerator.generateTemplateContent(template)

        assertEquals(
            """
                if (items.size > 1) {
                    for (item in items) {
                        raw(${RAW_PREFIX}0)
                        write(item)
                        raw(${RAW_PREFIX}1)
                    }
                }
            """.trimIndent(), result.functionContent.trimIndent()
        )
    }

    private fun parse(content: String) =
        parser.parseContent(content).also { templates.register(it) }

}

private fun parsedTemplate(
    name: String = "test",
    children: MutableList<HtmlElement> = mutableListOf(),
    imports: List<String> = listOf(),
    parameters: List<TemplateParameter> = listOf(),
) = ParsedTemplate(
    name = name,
    imports = imports,
    parameters = parameters,
    root = Tag("root", emptyMap(), children),
)