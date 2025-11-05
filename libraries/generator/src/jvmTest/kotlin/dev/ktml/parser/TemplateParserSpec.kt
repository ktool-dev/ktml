package dev.ktml.parser

import dev.ktml.DEFAULT_PACKAGE
import dev.ktool.kotest.BddSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class TemplateParserSpec : BddSpec({
    val parser = TemplateParser(DEFAULT_PACKAGE, "mine")

    fun parse(content: String, subPath: String = "mine", fileName: String = "Test.ktml") =
        parser.parseContent(fileName, content, subPath).first()

    fun parseAll(content: String, subPath: String = "mine", fileName: String = "Test.ktml") =
        parser.parseContent(fileName, content, subPath)

    "get template name from tag" {
        Given
        val content = $$"""
            <my-button text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        "my-button" shouldBe result.name
        result.path shouldBe "mine/my-button"
    }

    "can have multiple roots in a file" {
        Given
        val content = $$"""
            <my-button-one text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button-one>
            <my-button-two text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button-two>
        """.trimIndent()

        When
        val result = parseAll(content)

        Then
        result.size shouldBe 2
        result[0].name shouldBe "my-button-one"
        result[1].name shouldBe "my-button-two"
    }

    "path is set correctly" {
        Given
        val content = $$"""
            <my-button text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content, "mine/my/package")

        Then
        result.subPath shouldBe "mine/my/package"
    }

    "get parameters from attributes" {
        Given
        val content = $$"""
            <my-button text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.parameters shouldHaveSize 2
        result.parameters[0].name shouldBe "onClick"
        result.parameters[0].type shouldBe "String"
        result.parameters[1].name shouldBe "text"
        result.parameters[1].type shouldBe "String"
    }

    "get parameters with default values" {
        Given
        val content = $$"""
            <my-button text='${String = "Default Text"}'>
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.parameters[0].name shouldBe "text"
        result.parameters[0].type shouldBe "String"
        result.parameters[0].defaultValue shouldBe "\"Default Text\""
    }

    "allow imports above root element" {
        Given
        val content = $$"""
            import my.app.UserType

            <my-button text="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.name shouldBe "my-button"
        result.imports shouldHaveSize 1
        result.imports[0] shouldBe "import my.app.UserType"
    }

    "should assign root element correctly" {
        Given
        val content = $$"""
            <my-button text="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.name shouldBe "my-button"
        result.root.name shouldBe "my-button"
    }

    "allows special characters in expressions" {
        Given
        val content = $$"""
            <my-button text="$String" onClick="$String">
                <button class="${if(a < b && b < c) {'a'} else {'b'}}">A</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.expressions[2].content shouldBe "if(a < b && b < c) {'a'} else {'b'}"
    }

    "includes external script content" {
        Given
        val content = $$"""
            val a = 1
            val b = 1
            <my-button text="$String" onClick="$String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parse(content)

        Then
        result.externalScriptContent shouldBe "val a = 1\nval b = 1"
    }

    "should parse DOCTYPE is present" {
        Given
        val content = $$"""
            <!DOCTYPE html>
            
            import my.stuff.Here
            
            <html>
            <body>
                <h1>Here</h1>
            </body>
            </html>
        """.trimIndent()

        When
        val template = parse(content, "sub-folder", "my-file")

        Then
        template.imports shouldHaveSize 1
        template.dockTypeDeclaration shouldBe "<!DOCTYPE html>"
        template.name shouldBe "my-file"
        template.subPath shouldBe "sub-folder"
        template.inRegistry shouldBe true
    }

    "two html roots gets an error" {
        Given
        val content = """
            <!DOCTYPE html>
            
            <html>
            <body>
            </body>
            </html>
            <html>
            <body>
            </body>
            </html>
        """.trimIndent()

        Expect
        shouldThrow<IllegalArgumentException> {
            parse(content)
        }
    }

    "should parse self-closing tag properly" {
        Given
        val content = """
            <my-button>
                <some-tag />
                <br>
                <div>Hello</div>
            </my-button>
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.root.children shouldHaveSize 3
    }

    "can handle self-closing and non self-closing tag properly" {
        Given
        val content = """
            <my-button>
                <some-tag value="hello"/>
                <some-tag value="hello"></some-tag>
                <br>
                <div>Hello</div>
            </my-button>
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.root.children shouldHaveSize 4
    }

    "can parse a template with some nested kotlin code" {
        Given
        val content = $$"""
            <my-button text="$String" onClick="$String">
                <button onclick="${onClick}">${if(text.size > a && b < c) "less" else "more"}</button>
            </my-button>
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.inRegistry shouldBe false
        template.root.children shouldHaveSize 1
        (template.root.children[0] as HtmlTag).children shouldHaveSize 1
        template.expressions[3].content shouldBe """if(text.size > a && b < c) "less" else "more""""
    }

    "can put tag in the registry " {
        Given
        val content = """
            <my-tag fragment>
                <span>Hello</span>
            </my-tag
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.inRegistry shouldBe true
    }

    "can put tag in the registry when value is true" {
        Given
        val content = """
            <my-tag fragment="true">
                <span>Hello</span>
            </my-tag
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.inRegistry shouldBe true
    }

    "tag not in registry if value set to false" {
        Given
        val content = """
            <my-tag fragment="false">
                <span>Hello</span>
            </my-tag
        """.trimIndent()

        When
        val template = parse(content)

        Then
        template.inRegistry shouldBe false
    }

    "tag with existing HTML element name throws an exception" {
        Given
        val content = """
            <button>
                Anything
            </button>
        """.trimIndent()

        Expect
        shouldThrow<IllegalArgumentException> {
            parse(content)
        }
    }

    "tag with existing SVG element name throws an exception" {
        Given
        val content = $$"""
            <svg>
                Anything
            </svg>
        """.trimIndent()

        Expect
        shouldThrow<IllegalArgumentException> {
            parse(content)
        }
    }
})
