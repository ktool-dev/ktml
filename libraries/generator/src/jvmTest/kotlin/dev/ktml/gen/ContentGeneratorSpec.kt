package dev.ktml.gen

import dev.ktml.parser.*
import dev.ktml.parser.HtmlElement.Tag
import dev.ktml.parser.HtmlElement.Text
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

private val parser = TemplateParser("")
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
    $$"""
        <tag-with-multiple-content first="Content" content="Content">
            <div>${first}</div>
            <div>${content}</div>
        </tag>
    """.parse()
    $$"""
        import dev.ktml.User
        
        <script type="text/kotlin">
            val number = 10
            val defaultString = "blah"
            val defaultUser = User("Me")
        </script>
        
        <lots-of-types anInt="Int = number" aString="String = 'a $defaultString'" aBoolean="Boolean = true"
                             aUser="User = defaultUser">
            <div>${anInt}</div>
            <div>${aString}</div>
            <div>${aBoolean}</div>
            <div>${aUser.name}</div>
        </lots-of-types>
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
        result.body.statements.first().toString().trim().trim() shouldBe "raw($TEMPLATE_CONSTANT, 0, 20)"
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
            raw($TEMPLATE_CONSTANT, 0, 17)
            write(onClick)
            raw($TEMPLATE_CONSTANT, 17, 16)
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
            raw($TEMPLATE_CONSTANT, 0, 10)
            write(text)
            raw($TEMPLATE_CONSTANT, 10, 6)
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
            raw($TEMPLATE_CONSTANT, 0, 10)
            write(text)
            raw($TEMPLATE_CONSTANT, 10, 3)
            write(text)
            raw($TEMPLATE_CONSTANT, 13, 6)
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
            raw($TEMPLATE_CONSTANT, 0, 14)
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
                raw($TEMPLATE_CONSTANT, 0, 10)
                write(text)
                raw($TEMPLATE_CONSTANT, 10, 6)
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
            raw($TEMPLATE_CONSTANT, 0, 19)
            writeTagWithContent {
                raw($TEMPLATE_CONSTANT, 19, 32)
                write(text)
                raw($TEMPLATE_CONSTANT, 51, 25)
            }
            raw($TEMPLATE_CONSTANT, 76, 17)
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
            raw($TEMPLATE_CONSTANT, 0, 10)
            write(text)
            raw($TEMPLATE_CONSTANT, 10, 6)
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
                raw($TEMPLATE_CONSTANT, 0, 10)
                write(text)
                raw($TEMPLATE_CONSTANT, 10, 6)
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
                raw($TEMPLATE_CONSTANT, 0, 10)
                write(item)
                raw($TEMPLATE_CONSTANT, 10, 6)
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
                    raw($TEMPLATE_CONSTANT, 0, 10)
                    write(item)
                    raw($TEMPLATE_CONSTANT, 10, 6)
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
                raw($TEMPLATE_CONSTANT, 0, 5)
            }
        """.trimIndent()
    }

    "if a tag calls itself it works fine" {
        Given
        val template = """
            <tag content="Content">
                <tag>World</tag>
            </tag>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.statements.first().toString().trim() shouldBe """
            writeTag {
                raw(TEMPLATE_HTML, 0, 5)
            }
        """.trimIndent()
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
                    raw(TEMPLATE_HTML, 0, 5)
                },
            ) {
                raw(TEMPLATE_HTML, 5, 6)
            }
        """.trimIndent()
        result.templateConstant.initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """FirstSecond"""
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
                    raw(TEMPLATE_HTML, 0, 5)
                },
            ) {
                raw(TEMPLATE_HTML, 5, 17)
            }
        """.trimIndent()
        result.templateConstant.initializer?.expression?.replace(TRIPLE_QUOTE, "") shouldBe """First<div>Second</div>"""
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
        result.templateConstant.initializer?.expression?.replace(
            TRIPLE_QUOTE,
            ""
        ) shouldBe $$"""<h1 class="${text}">Hello ${text}</h1>"""
        result.body.statements.first().toString().trim() shouldBe """
            raw($TEMPLATE_CONSTANT, 0, 38)
        """.trimIndent()
    }

    "can use each on call to custom tag" {
        Given
        val template = $$"""
            <tag-with-multiple-buttons>
                <script type="text/kotlin">
                    val textItems = listOf("One", "Two", "Three")
                </script>
                <my-button each="${text in textItems}" onClick="null" text="${text}" />
            </tag-with-multiple-buttons>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe """
             {
                val textItems = listOf("One", "Two", "Three")
                for (text in textItems) {
                    writeMyButton(
                        onClick = "null",
                        text = text,
                    )
                }
            }
        """.trimIndent()
    }

    "can use if on call to custom tag" {
        Given
        val template = $$"""
            <tag-with-if-buttons test="Int">
                <my-button if="${test == 4}" onClick="null" text="something" />
            </tag-with-if-buttons>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe """
             {
                if (test == 4) {
                    writeMyButton(
                        onClick = "null",
                        text = "something",
                    )
                }
            }
        """.trimIndent()
    }

    "generate lots of types with number" {
        Given
        val template = $$"""
            <tag-something>
                <lots-of-types anInt="${5 * number}"/>
            </tag-something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe """
             {
                writeLotsOfTypes(
                    anInt = 5 * number,
                )
            }
        """.trimIndent()
    }

    "generate lots of types with string concatenation" {
        Given
        val template = $$"""
            <tag-something>
                <lots-of-types aString="something ${value}"/>
            </tag-something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe $$"""
             {
                writeLotsOfTypes(
                    aString = $${TRIPLE_QUOTE}something ${value}$$TRIPLE_QUOTE,
                )
            }
        """.trimIndent()
    }

    "generate lots of types with string and if" {
        Given
        val template = $$"""
            <tag-something>
                <lots-of-types aString="something ${if(number == 10) 'something' else 'not${defaultString}'}"/>
            </tag-something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe $$"""
             {
                writeLotsOfTypes(
                    aString = $${TRIPLE_QUOTE}something ${if(number == 10) "something" else "not${defaultString}"}$$TRIPLE_QUOTE,
                )
            }
        """.trimIndent()
    }

    "generate lots of types with user" {
        Given
        val template = $$"""
            <tag-something>
                <lots-of-types aUser="${User('me')}"/>
            </tag-something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe $$"""
             {
                writeLotsOfTypes(
                    aUser = User("me"),
                )
            }
        """.trimIndent()
    }

    "generate tag with normal html tags and expression parameters" {
        Given
        val template = $$"""
            <something>
                <div class="something ${if(number == 10) 'something' else 'not${defaultString}'}"></div>
                <div if="${defaultUser.name == 'Me'}"></div>
            </something>
        """.parse()

        When
        val result = contentGenerator.generateTemplateContent(template)

        Then
        result.body.render() shouldBe $$"""
             {
                raw(TEMPLATE_HTML, 0, 12)
                write($${TRIPLE_QUOTE}something ${if(number == 10) "something" else "not${defaultString}"}$$TRIPLE_QUOTE)
                raw(TEMPLATE_HTML, 12, 8)
                if (defaultUser.name == "Me") {
                    raw(TEMPLATE_HTML, 20, 11)
                }
            }
        """.trimIndent()
    }
})

private fun String.parse() =
    parser.parseContent("file", this.trimIndent(), "mine").also { parsed -> parsed.forEach { templates.replace(it) } }
        .first()

private fun parsedTemplate(
    name: String = "test",
    children: MutableList<HtmlElement> = mutableListOf(),
    imports: List<String> = listOf(),
    parameters: List<ParsedTemplateParameter> = listOf(),
) = ParsedTemplate(
    file = "testFile",
    name = name,
    subPath = "",
    imports = imports,
    parameters = parameters,
    root = Tag("root", emptyMap(), children),
)
