package dev.ktml.gen

import dev.ktml.parser.*
import dev.ktml.parser.HtmlElement.Tag
import dev.ktml.parser.HtmlElement.Text
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

private val parser = TemplateParser()
private val templates = TemplateDefinitions()

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
    $$"""
        <tag-with-multiple-content first="Content" content="Content">
            <div>${first}</div>
            <div>${content}</div>
        </tag>
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
        result.body.statements.first().toString().trim().trim() shouldBe "raw(${RAW_PREFIX}0)"
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
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
                <h1>Hello</h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.statements.first().toString().trim() shouldBe """
            writeMyButton(
                onClick = onClick,
                text = "Hello",
            )
            raw(${RAW_PREFIX}0)
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
        result.body.statements.first().toString().trim() shouldBe """
            writeTagWithContent {
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
        result.body.statements.first().toString().trim() shouldBe """
            raw(${RAW_PREFIX}0)
            writeTagWithContent {
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
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
        result.body.statements.first().toString().trim() shouldBe """
            writeTag {
                raw(${RAW_PREFIX}0)
            }
        """.trimIndent()
    }

    "doctype tag printed as doctype directive" {
        Given
        val template = $$"""
            <something>
                <!DOCTYPE html>
                <h1>Hello</h1>
            </something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.rawConstants[0].initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """
            <!DOCTYPE html>
            <h1>Hello</h1>
        """.trimIndent()
    }

    "if a tag calls itself it just prints out the raw content" {
        Given
        val template = $$"""
            <tag>
                <tag>World</tag>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.statements.first().toString().trim() shouldBe """
            raw(${RAW_PREFIX}0)
        """.trimIndent()
        result.rawConstants[0].initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """<tag>World</tag>"""
    }

    "can pass multiple content parameters" {
        Given
        val template = """
            <something>
                <tag-with-multiple-content>
                    <first>First</first>
                    <content>Second</content>
                </tag-with-multiple-content>
            </something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.statements.first().toString().trim() shouldBe """
            writeTagWithMultipleContent(
                first = {
                    raw(RAW_CONTENT_0)
                },
            ) {
                raw(RAW_CONTENT_1)
            }
        """.trimIndent()
        result.rawConstants[0].initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """First"""
        result.rawConstants[1].initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """Second"""
    }

    "can pass multiple content parameters without nameing second one" {
        Given
        val template = """
            <something>
                <tag-with-multiple-content>
                    <first>First</first>
                    <div>Second</div>
                </tag-with-multiple-content>
            </something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.statements.first().toString().trim() shouldBe """
            writeTagWithMultipleContent(
                first = {
                    raw(RAW_CONTENT_0)
                },
            ) {
                raw(RAW_CONTENT_1)
            }
        """.trimIndent()
        result.rawConstants[0].initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """First"""
        result.rawConstants[1].initializer?.expression?.replace(TRIPLE_QUOTE, "")
            ?.trim() shouldBe """<div>Second</div>"""
    }

    "can flag tag as no interpolation" {
        Given
        val template = $$"""
            <tag text="String">
                <h1 ignore-kotlin class="${text}">Hello ${text}</h1>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.rawConstants[0].initializer?.expression?.replace(
            TRIPLE_QUOTE,
            ""
        ) shouldBe $$"""<h1 class="${text}">Hello ${text}</h1>"""
        result.body.statements.first().toString().trim() shouldBe """
            raw(${RAW_PREFIX}0)
        """.trimIndent()
    }
})

private fun String.parse() = parser.parseContent(this.trimIndent()).also { templates.replace(it.templateDefinition) }

private fun parsedTemplate(
    name: String = "test",
    children: MutableList<HtmlElement> = mutableListOf(),
    imports: List<String> = listOf(),
    parameters: List<ParsedTemplateParameter> = listOf(),
) = ParsedTemplate(
    name = name,
    imports = imports,
    parameters = parameters,
    root = Tag("root", emptyMap(), children),
)