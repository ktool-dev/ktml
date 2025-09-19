package dev.ktml.parser

import dev.ktool.kotest.BddSpec
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class TemplateParserSpec : BddSpec({
    val parser = TemplateParser()

    "get template name from tag" {
        Given
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        "my-button" shouldBe result.name
    }

    "get multiple root elements should fail" {
        Given
        val content = $$"""
            <my-button-one text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button-one>
            <my-button-two text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button-two>
        """.trimIndent()

        Expect
        shouldThrow<IllegalArgumentException> {
            parser.parseContent(content)
        }
    }

    "package name is set correctly" {
        Given
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content, "my.package")

        Then
        "dev.ktml.templates.my.package" shouldBe result.packageName
    }

    "get parameters from attributes" {
        Given
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        result.parameters shouldHaveSize 2
        result.parameters[0].name shouldBe "text"
        result.parameters[0].type shouldBe "String"
        result.parameters[1].name shouldBe "onClick"
        result.parameters[1].type shouldBe "String"
    }

    "get parameters with default values" {
        Given
        val content = $$"""
            <my-button text="String = 'Default Text'">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        result.parameters[0].name shouldBe "text"
        result.parameters[0].type shouldBe "String"
        result.parameters[0].defaultValue shouldBe "Default Text"
    }

    "allow imports above root element" {
        Given
        val content = $$"""
            import my.app.UserType

            <my-button text="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        result.name shouldBe "my-button"
        result.imports shouldHaveSize 1
        result.imports[0] shouldBe "import my.app.UserType"
    }

    "should assign root element correctly" {
        Given
        val content = $$"""
            <my-button text="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        result.name shouldBe "my-button"
        result.root.name shouldBe "my-button"
    }

    "allows special characters in expressions" {
        Given
        val content = $$"""
            <my-button text="String" onClick="String">
                <button class="${if(a < b && b < c) {'a'} else {'b'}}">A</button>
            </my-button>
        """.trimIndent()

        When
        val result = parser.parseContent(content)

        Then
        val tag = result.root.children.find { it is HtmlElement.Tag && it.name == "button" } as HtmlElement.Tag
        tag.attrs["class"] shouldBe $$"${if(a < b && b < c) {'a'} else {'b'}}"
    }

    "includes external script content" {
        Given
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

        When
        val result = parser.parseContent(content)

        Then
        result.topExternalScriptContent shouldBe "val a = 1"
        result.bottomExternalScriptContent shouldBe "val b = 1"
    }

    "should error if DOCTYPE is present" {
        Given
        val content = $$"""
            <!DOCTYPE html>
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${text}</button>
            </my-button>
        """.trimIndent()

        When
        val template = parser.parseContent(content)

        Then
        template.dockTypeDeclaration shouldBe "<!DOCTYPE html>"
    }

    "should parse self closing tag properly" {
        Given
        val content = """
            <my-button>
                <some-tag />
                <br>
                <div>Hello</div>
            </my-button>
        """.trimIndent()

        When
        val template = parser.parseContent(content)

        Then
        template.root.children shouldHaveSize 3
    }

    "can parse a template with some nested kotlin code" {
        Given
        val content = $$"""
            <my-button text="String" onClick="String">
                <button onclick="${onClick}">${if(text.size > a && b < c) "less" else "more"}</button>
            </my-button>
        """.trimIndent()

        When
        val template = parser.parseContent(content)

        Then
        template.root.children shouldHaveSize 1
        (template.root.children[0] as HtmlElement.Tag).children shouldHaveSize 1
        (template.root.children[0] as HtmlElement.Tag).children[0] shouldBe HtmlElement.Text($$"""${if(text.size > a && b < c) "less" else "more"}""")
    }
})
