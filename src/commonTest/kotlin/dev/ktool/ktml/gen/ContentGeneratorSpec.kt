package dev.ktool.ktml.gen

import dev.ktool.ktml.Templates
import dev.ktool.ktml.parser.HtmlElement
import dev.ktool.ktml.parser.HtmlElement.Tag
import dev.ktool.ktml.parser.HtmlElement.Text
import dev.ktool.ktml.parser.ParsedTemplate
import dev.ktool.ktml.parser.TemplateParameter
import dev.ktool.ktml.parser.TemplateParser
import dev.ktool.ktml.test.BddSpec
import io.kotest.matchers.shouldBe

private val parser = TemplateParser()
private val templates = Templates()

class ContentGeneratorSpec : BddSpec({
    val contentGenerator = ContentGenerator(templates)

    $$"""
        <my-button text="String" onClick="String">
            <button onclick="${onClick}">${text}</button>
        </my-button>
    """.parse()
    $$"""
        <tag-with-content content="Content">
            <div>${content}</div>
        </tag>
    """.parse()
    """
        <if inline test="Boolean" content="Content" else="Content? = null">
            <script type="text/kotlin">
                if (test) write(content) else write(_else)
            </script>
        </if>
    """.parse()

    "basic content generation" {
        Given
        val children = mutableListOf(
            Text("Hello, "),
            Tag("b", emptyMap(), mutableListOf(Text("World"))),
            Text("!"),
        )
        val template = parsedTemplate(children = children)

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent shouldBe "    raw(${RAW_PREFIX}0)"
    }

    "tag with kotlin attribute" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <button onclick="${onClick}">Hello</button>
            </tag>
        """.trim().parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            raw(${RAW_PREFIX}0)
            write(onClick)
            raw(${RAW_PREFIX}1)
        """.trimIndent()
    }

    "tag with kotlin text" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <h1>Hello ${text} </h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            raw(${RAW_PREFIX}0)
            write(text)
            raw(${RAW_PREFIX}1)
        """.trimIndent()
    }

    "tag with multiple kotlin text" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <h1>Hello ${text} a ${text} </h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            raw(${RAW_PREFIX}0)
            write(text)
            raw(${RAW_PREFIX}1)
            write(text)
            raw(${RAW_PREFIX}2)
        """.trimIndent()
    }

    "tag with template call" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <my-button text="Hello" onClick="${onClick}"/>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            dev.ktool.ktml.templates.writeMyButton(
                onClick = onClick,
                text = "Hello",
            )
        """.trimIndent()
    }

    "tag with content parameter" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <tag-with-content>
                    <h1>Hello ${text} </h1>
                </tag-with-content>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            dev.ktool.ktml.templates.writeTagWithContent() {
                raw(${RAW_PREFIX}0)
                write(text)
                raw(${RAW_PREFIX}1)
            }
        """.trimIndent()
    }

    "content in nested tag" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <div><div>
                    <tag-with-content>
                        <div>
                            <h1>Hello ${text} </h1>
                        </div>
                    </tag-with-content>
                </div></div>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            raw(${RAW_PREFIX}0)
            dev.ktool.ktml.templates.writeTagWithContent() {
                raw(${RAW_PREFIX}1)
                write(text)
                raw(${RAW_PREFIX}2)
            }
            raw(${RAW_PREFIX}3)
        """.trimIndent()
    }

    "kotlin script generation" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <script type="text/kotlin">
                    val a = 1
                </script>
                <h1>Hello ${text} </h1>
                <script type="text/kotlin">
                    val b = 1
                </script>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            val a = 1
            raw(${RAW_PREFIX}0)
            write(text)
            raw(${RAW_PREFIX}1)
            val b = 1
        """.trimIndent()
    }

    "generate if for if attribute" {
        Given
        val template = $$"""
            <tag text="String" onClick="String">
                <h1 if='${text == "Hello"}'>Hello ${text} </h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            if (text == "Hello") {
                raw(${RAW_PREFIX}0)
                write(text)
                raw(${RAW_PREFIX}1)
            }
        """.trimIndent()
    }

    "generate each for each attribute" {
        Given
        val template = $$"""
            <tag items="List<String>">
                <h1 each="${item in items}">Hello ${item} </h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            for (item in items) {
                raw(${RAW_PREFIX}0)
                write(item)
                raw(${RAW_PREFIX}1)
            }
        """.trimIndent()
    }

    "if and each on same tag" {
        Given
        val template = $$"""
            <tag items="List<String>">
                <h1 if="${items.size > 1}" each="${item in items}">Hello ${item} </h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            if (items.size > 1) {
                for (item in items) {
                    raw(${RAW_PREFIX}0)
                    write(item)
                    raw(${RAW_PREFIX}1)
                }
            }
        """.trimIndent()
    }

    "template with content passed as child node" {
        Given
        $$"""
            <tag something="Content">
                ${something}
            </tag>
        """.parse()
        val template = """
            <another-tag>
                <tag>
                    <something>World</something>
                </tag>
            </another-tag>
        """.trimMargin().parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.functionContent.trimIndent() shouldBe """
            dev.ktool.ktml.templates.writeAnotherTag() {
                dev.ktool.ktml.templates.writeTag() {
                    raw(${RAW_PREFIX}0)
                }
            }
        """.trimIndent()
    }
})

private fun String.parse() = parser.parseContent(this.trimIndent()).also { templates.register(it) }

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