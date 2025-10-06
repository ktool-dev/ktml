package dev.ktml.gen

import dev.ktml.parser.HtmlElement
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.ParsedTemplateParameter
import dev.ktml.parser.TemplateDefinitions
import dev.ktool.gen.types.Function
import dev.ktool.gen.types.KotlinFile
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

class KotlinFileGeneratorSpec : BddSpec({
    val kotlinFileGenerator = KotlinFileGenerator(TemplateDefinitions())
    val basePackageName = "my.templates"

    "generate code with basic template" {
        Given
        val template = ParsedTemplate(
            name = "my-button",
            imports = emptyList(),
            parameters = listOf(
                ParsedTemplateParameter("text", "String"),
                ParsedTemplateParameter("onClick", "String")
            ),
            root = HtmlElement.Tag("my-button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.packageName shouldBe basePackageName
        file.imports.size shouldBe 1
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.members.size shouldBe 2
        file.noArgWriterFunction.name shouldBe "writeMyButton"
        file.noArgWriterFunction.parameters.size shouldBe 0
        file.writerFunction.name shouldBe "writeMyButton"
        file.writerFunction.parameters.size shouldBe 2
        file.writerFunction.parameters[0].name shouldBe "text"
        file.writerFunction.parameters[0].type.name shouldBe "String"
        file.writerFunction.parameters[1].name shouldBe "onClick"
        file.writerFunction.parameters[1].type.name shouldBe "String"
    }

    "generate code with imports" {
        Given
        val template = ParsedTemplate(
            name = "custom-component",
            imports = listOf("import kotlinx.datetime.LocalDate", "import kotlin.collections.List"),
            parameters = emptyList(),
            root = HtmlElement.Tag("custom-component", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.imports[1].packagePath shouldBe "kotlin.collections.List"
        file.imports[2].packagePath shouldBe "kotlinx.datetime.LocalDate"
    }

    "generate code with content parameter" {
        Given
        val template = ParsedTemplate(
            name = "card",
            imports = emptyList(),
            parameters = listOf(
                ParsedTemplateParameter("title", "String"),
                ParsedTemplateParameter("content", "Content")
            ),
            root = HtmlElement.Tag("card", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.imports[0].packagePath shouldBe "dev.ktml.Content"
        file.imports[1].packagePath shouldBe "dev.ktml.Context"
        file.writerFunction.name shouldBe "writeCard"
        file.writerFunction.receiver?.name shouldBe "Context"
        file.writerFunction.parameters.size shouldBe 2
        file.writerFunction.parameters[0].name shouldBe "title"
        file.writerFunction.parameters[0].type.name shouldBe "String"
        file.writerFunction.parameters[1].name shouldBe "content"
        file.writerFunction.parameters[1].type.name shouldBe "Content"
    }

    "generate code with default values" {
        Given
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = listOf(
                ParsedTemplateParameter("text", "String", "Click me"),
                ParsedTemplateParameter("disabled", "Boolean", "false"),
                ParsedTemplateParameter("count", "Int", "0")
            ),
            root = HtmlElement.Tag("button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.writerFunction.parameters[0].name shouldBe "text"
        file.writerFunction.parameters[0].type.name shouldBe "String"
        file.writerFunction.parameters[0].defaultValue?.expression shouldBe "\"Click me\""
        file.writerFunction.parameters[1].name shouldBe "disabled"
        file.writerFunction.parameters[1].type.name shouldBe "Boolean"
        file.writerFunction.parameters[1].defaultValue?.expression shouldBe "false"
        file.writerFunction.parameters[2].name shouldBe "count"
        file.writerFunction.parameters[2].type.name shouldBe "Int"
        file.writerFunction.parameters[2].defaultValue?.expression shouldBe "0"
    }

    "generate code with mixed parameters" {
        Given
        val template = ParsedTemplate(
            name = "form-input",
            imports = listOf("import kotlinx.serialization.Serializable"),
            parameters = listOf(
                ParsedTemplateParameter("label", "String"),
                ParsedTemplateParameter("placeholder", "String", "Enter text"),
                ParsedTemplateParameter("required", "Boolean", "true"),
                ParsedTemplateParameter("content", "Content")
            ),
            root = HtmlElement.Tag("form-input", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.imports[0].packagePath shouldBe "dev.ktml.Content"
        file.imports[1].packagePath shouldBe "dev.ktml.Context"
        file.imports[2].packagePath shouldBe "kotlinx.serialization.Serializable"
        file.writerFunction.name shouldBe "writeFormInput"
        file.writerFunction.parameters[0].name shouldBe "label"
        file.writerFunction.parameters[0].defaultValue shouldBe null
        file.writerFunction.parameters[1].name shouldBe "placeholder"
        file.writerFunction.parameters[1].defaultValue?.expression shouldBe "\"Enter text\""
        file.writerFunction.parameters[2].name shouldBe "required"
        file.writerFunction.parameters[2].defaultValue?.expression shouldBe "true"
        file.writerFunction.parameters[3].name shouldBe "content"
        file.writerFunction.parameters[3].defaultValue shouldBe null
    }

    "to camel case conversion" {
        Given
        val template = ParsedTemplate(
            name = "my-custom-button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("my-custom-button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        val function = file.noArgWriterFunction
        function.name shouldBe "writeMyCustomButton"
    }

    "to camel case with single word" {
        Given
        val template = ParsedTemplate(
            name = "button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.noArgWriterFunction.name shouldBe "writeButton"
    }

    "generate code with no parameters" {
        Given
        val template = ParsedTemplate(
            name = "header",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlElement.Tag("header", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.noArgWriterFunction.name shouldBe "writeHeader"
        file.imports.size shouldBe 1
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
    }

    "generate code structure" {
        Given
        val template = ParsedTemplate(
            name = "test-component",
            imports = listOf("import kotlin.String"),
            parameters = listOf(ParsedTemplateParameter("value", "String")),
            root = HtmlElement.Tag(
                "test-component", emptyMap(), mutableListOf(
                    HtmlElement.Text("Hello, World!")
                )
            ),
            externalScriptContent = "val a = 1",
        )

        When
        val file = kotlinFileGenerator.generateCode(basePackageName, template)

        Then
        file.packageName shouldBe "my.templates"
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.imports[1].packagePath shouldBe "kotlin.String"

        file.members.size shouldBe 4
        file.writerFunction.name shouldBe "writeTestComponent"
        file.writerFunction.parameters.size shouldBe 1
        file.writerFunction.parameters[0].name shouldBe "value"
        file.writerFunction.parameters[0].type.name shouldBe "String"
    }
})

private val KotlinFile.noArgWriterFunction: Function get() = members[0] as Function
private val KotlinFile.writerFunction: Function get() = members[1] as Function
