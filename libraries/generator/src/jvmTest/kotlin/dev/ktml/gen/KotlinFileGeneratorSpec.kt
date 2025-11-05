package dev.ktml.gen

import dev.ktml.DEFAULT_PACKAGE
import dev.ktml.parsedTemplateParameter
import dev.ktml.parser.*
import dev.ktml.parser.kotlin.removeContentComments
import dev.ktool.gen.types.Function
import dev.ktool.gen.types.KotlinFile
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe

private val parser = TemplateParser(DEFAULT_PACKAGE, "")
private val templates = Templates()

class KotlinFileGeneratorSpec : BddSpec({
    val kotlinFileGenerator = KotlinFileGenerator(templates)

    "generate code with basic template" {
        Given
        val template = parsed(
            name = "my-button",
            imports = emptyList(),
            parameters = listOf(
                parsedTemplateParameter("text", "String"),
                parsedTemplateParameter("onClick", "String")
            ),
            root = HtmlTag("my-button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)
        println(file.render())

        Then
        file.packageName shouldBe "dev.ktml.templates"
        file.imports.size shouldBe 1
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.members.size shouldBe 1
        file.writerFunction.name shouldBe "writeMyButton"
        file.writerFunction.parameters.size shouldBe 2
        file.writerFunction.parameters[0].name shouldBe "text"
        file.writerFunction.parameters[0].type.name shouldBe "String"
        file.writerFunction.parameters[1].name shouldBe "onClick"
        file.writerFunction.parameters[1].type.name shouldBe "String"
    }

    "generate code with imports" {
        Given
        val template = parsed(
            name = "custom-component",
            imports = listOf("import kotlinx.datetime.LocalDate", "import kotlin.collections.List"),
            parameters = emptyList(),
            root = HtmlTag("custom-component", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.imports[1].packagePath shouldBe "kotlin.collections.List"
        file.imports[2].packagePath shouldBe "kotlinx.datetime.LocalDate"
    }

    "generate code with content parameter" {
        Given
        val template = parsed(
            name = "card",
            imports = emptyList(),
            parameters = listOf(
                parsedTemplateParameter("title", "String"),
                parsedTemplateParameter("content", "Content")
            ),
            root = HtmlTag("card", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

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
        val template = parsed(
            name = "button",
            imports = emptyList(),
            parameters = listOf(
                parsedTemplateParameter("text", "String", "\"Click me\""),
                parsedTemplateParameter("disabled", "Boolean", "false"),
                parsedTemplateParameter("count", "Int", "0")
            ),
            root = HtmlTag("button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.writerFunction.parameters[0].name shouldBe "text"
        file.writerFunction.parameters[0].type.name shouldBe "String"
        file.writerFunction.parameters[0].defaultValue?.expression?.removeContentComments() shouldBe "\"Click me\""
        file.writerFunction.parameters[1].name shouldBe "disabled"
        file.writerFunction.parameters[1].type.name shouldBe "Boolean"
        file.writerFunction.parameters[1].defaultValue?.expression?.removeContentComments() shouldBe "false"
        file.writerFunction.parameters[2].name shouldBe "count"
        file.writerFunction.parameters[2].type.name shouldBe "Int"
        file.writerFunction.parameters[2].defaultValue?.expression?.removeContentComments() shouldBe "0"
    }

    "generate code with mixed parameters" {
        Given
        val template = parsed(
            name = "form-input",
            imports = listOf("import kotlinx.serialization.Serializable"),
            parameters = listOf(
                parsedTemplateParameter("label", "String"),
                parsedTemplateParameter("placeholder", "String", "\"Enter text\""),
                parsedTemplateParameter("required", "Boolean", "true"),
                parsedTemplateParameter("content", "Content")
            ),
            root = HtmlTag("form-input", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.imports[0].packagePath shouldBe "dev.ktml.Content"
        file.imports[1].packagePath shouldBe "dev.ktml.Context"
        file.imports[2].packagePath shouldBe "kotlinx.serialization.Serializable"
        file.writerFunction.name shouldBe "writeFormInput"
        file.writerFunction.parameters[0].name shouldBe "label"
        file.writerFunction.parameters[0].defaultValue shouldBe null
        file.writerFunction.parameters[1].name shouldBe "placeholder"
        file.writerFunction.parameters[1].defaultValue?.expression?.removeContentComments() shouldBe "\"Enter text\""
        file.writerFunction.parameters[2].name shouldBe "required"
        file.writerFunction.parameters[2].defaultValue?.expression?.removeContentComments() shouldBe "true"
        file.writerFunction.parameters[3].name shouldBe "content"
        file.writerFunction.parameters[3].defaultValue shouldBe null
    }

    "to camel case conversion" {
        Given
        val template = parsed(
            name = "my-custom-button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlTag("my-custom-button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.writerFunction.name shouldBe "writeMyCustomButton"
    }

    "to camel case with single word" {
        Given
        val template = parsed(
            name = "button",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlTag("button", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.writerFunction.name shouldBe "writeButton"
    }

    "generate code with no parameters" {
        Given
        val template = parsed(
            name = "header",
            imports = emptyList(),
            parameters = emptyList(),
            root = HtmlTag("header", emptyMap())
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.writerFunction.name shouldBe "writeHeader"
        file.imports.size shouldBe 1
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
    }

    "generate code structure" {
        Given
        val template = parsed(
            name = "test-component",
            imports = listOf("import kotlin.String"),
            parameters = listOf(parsedTemplateParameter("value", "String")),
            root = HtmlTag(
                "test-component", emptyMap(), mutableListOf(
                    HtmlText("Hello, World!")
                )
            ),
            externalScriptContent = "val a = 1",
        )

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.packageName shouldBe "dev.ktml.templates"
        file.imports[0].packagePath shouldBe "dev.ktml.Context"
        file.imports[1].packagePath shouldBe "kotlin.String"

        file.members.size shouldBe 3
        file.writerFunction.name shouldBe "writeTestComponent"
        file.writerFunction.parameters.size shouldBe 1
        file.writerFunction.parameters[0].name shouldBe "value"
        file.writerFunction.parameters[0].type.name shouldBe "String"
    }

    "can generate functions with default value expressions" {
        Given
        val template = $$"""
            import dev.ktml.User

            val number = 10
            val defaultString = "blah"
            val defaultUser = User("Me")

            <template-with-types anInt="${Int = number}" aString="${String = 'a $defaultString'}" aBoolean="${Boolean = true}"
                                 aUser="${User = defaultUser}">
                <div>${anInt}</div>
                <div>${aString}</div>
                <div>${aBoolean}</div>
                <div>${aUser.name}</div>
            </template-with-types>
        """.trimIndent().parse()

        When
        val file = kotlinFileGenerator.generateCode(template)

        Then
        file.writerFunction.parameters[0].name shouldBe "aBoolean"
        file.writerFunction.parameters[0].defaultValue?.expression?.removeContentComments() shouldBe "true"
        file.writerFunction.parameters[1].name shouldBe "aString"
        file.writerFunction.parameters[1].defaultValue?.expression?.removeContentComments() shouldBe $$""""a $defaultString""""
        file.writerFunction.parameters[2].name shouldBe "aUser"
        file.writerFunction.parameters[2].defaultValue?.expression?.removeContentComments() shouldBe "defaultUser"
        file.writerFunction.parameters[3].name shouldBe "anInt"
        file.writerFunction.parameters[3].defaultValue?.expression?.removeContentComments() shouldBe "number"
    }
})

private val KotlinFile.writerFunction: Function get() = members[0] as Function

private fun parsed(
    name: String = "test",
    subPath: String = "",
    children: MutableList<HtmlElement> = mutableListOf(),
    imports: List<String> = listOf(),
    parameters: List<ParsedTemplateParameter> = listOf(),
    root: HtmlTag = HtmlTag("root", emptyMap(), children),
    externalScriptContent: String = "",
) = ParsedTemplate(
    file = "testFile",
    name = name,
    subPath = subPath,
    imports = imports,
    parameters = parameters,
    root = root,
    expressions = listOf(),
    templatePackage = DEFAULT_PACKAGE,
    externalScriptContent = externalScriptContent,
)

private fun String.parse() =
    parser.parseContent("file", this.trimIndent(), "mine").also { parsed -> parsed.forEach { templates.replace(it) } }
        .first()
