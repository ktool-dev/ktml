package dev.ktml.gen

import dev.ktml.gen.element.buildHtmlElementHandlers
import dev.ktml.gen.element.process
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.Templates
import dev.ktml.util.replaceTicks
import dev.ktml.util.toImport
import dev.ktool.gen.TRIPLE_QUOTE
import dev.ktool.gen.types.Block
import dev.ktool.gen.types.Import
import dev.ktool.gen.types.Property
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

data class TemplateContent(val imports: List<Import>, val body: Block, val templateConstant: Property) {
    val templateConstantIsNotEmpty = templateConstant.initializer?.expression != "$TRIPLE_QUOTE$TRIPLE_QUOTE"
}

/**
 * Generates HtmlWriter method calls from parsed HTML elements. This class is not thread save.
 */
class ContentGenerator(templates: Templates, private val template: ParsedTemplate) {
    private val contentBuilder = ContentBuilder()
    private val imports = mutableListOf<Import>()
    private val elementHandlers = buildHtmlElementHandlers(template, templates, contentBuilder, imports)

    fun generate(): TemplateContent {
        logger.debug { "Generating content for template: ${template.name}" }
        reset()
        initializeImports()
        generateContextParams()

        if (template.dockTypeDeclaration.isNotBlank()) {
            contentBuilder.doctype(template.dockTypeDeclaration)
        }

        elementHandlers.process(template.root.children)

        val contentAndConstants = contentBuilder.templateContent
        logger.debug { "Finished generating content for template: ${template.name}" }

        return TemplateContent(
            imports = imports.sortedBy { it.packagePath },
            body = Block(contentAndConstants.body.replace("() {", " {")),
            templateConstant = contentAndConstants.templateConstant,
        )
    }

    private fun reset() {
        contentBuilder.clear()
        imports.clear()
    }

    private fun initializeImports() {
        imports.addAll(template.imports.map { it.toImport() })

        if (template.parameters.any { it.isContent }) {
            imports.add(Import("dev.ktml.Content"))
        }

        imports.add(Import("dev.ktml.Context"))
    }

    private fun generateContextParams() {
        template.parameters.filter { it.isContextParam }.forEach { param ->
            logger.debug { "Generating context param: ${param.name}" }
            contentBuilder.kotlin(param.contextParameterDefinition().replaceTicks())
        }
    }
}
