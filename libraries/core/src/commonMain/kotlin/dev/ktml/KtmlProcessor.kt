package dev.ktml

import dev.ktml.gen.KotlinFileGenerator
import dev.ktml.gen.TemplateRegistryGenerator
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateDefinitions
import dev.ktml.parser.TemplateParser
import dev.ktml.util.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

open class KtmlProcessor(val basePackageName: String, val outputDirectory: String) {
    private val parser = TemplateParser()
    private val templates = TemplateDefinitions()
    private val parsedTemplates = mutableListOf<ParsedTemplate>()
    private val fileGenerator = KotlinFileGenerator(templates)
    private val basePath = "$outputDirectory/${basePackageName.replace(".", "/")}"

    fun processRootDirectories(dirs: List<String>) = dirs.forEach { processRootDirectory(it) }

    fun processRootDirectory(dir: String) = processDirectory(dir, dir)

    fun processDirectory(dir: String, rootPath: String = dir) {
        val path = dir.toPath()
        require(path.isDirectory) { "Path '${path.absolute}' is not a directory" }

        path.list().forEach {
            if (it.isDirectory) {
                processDirectory(it.toString(), rootPath)
            } else {
                processFile(it.toString(), rootPath)
            }
        }
    }

    fun processFile(file: String, rootPath: String) {
        if (!file.endsWith(".ktml")) return

        val path = file.toPath()
        log.info { "Processing file: $path" }
        val subPath = path.path.substringAfter(rootPath).substringBeforeLast("/").removePrefix("/")
        processTemplate(path.readText(), subPath)
    }

    fun processTemplate(content: String, subPath: String = "") = parser.parseContent(content, subPath).also {
        parsedTemplates.add(it)
        templates.register(it.toTemplateDefinition(basePackageName))
    }

    fun generateTemplateCode() {
        parsedTemplates.map { generateTemplateCodeFile(it) }
        val definitions = parsedTemplates.map { findDefinition(it) }
        val content = TemplateRegistryGenerator.createTemplateRegistry(basePackageName, definitions)
        Path("$basePath/TemplateRegistry.kt").mkDirs().writeText(content)
    }

    private fun findDefinition(template: ParsedTemplate) =
        templates[template.path] ?: error("Could not find template '${template.path}'")

    fun generateTemplateCodeFile(template: ParsedTemplate) {
        log.info { "Generating code for template: ${template.name}" }
        val filePath = "$basePath/${template.pathCamelCaseName}.kt"
        Path(filePath).mkDirs().writeText(generateTemplateCode(template).render())
    }

    fun generateTemplateCode(template: ParsedTemplate) =
        fileGenerator.generateCode(findDefinition(template).packageName, template)
}

internal fun ParsedTemplate.toTemplateDefinition(basePackageName: String) = TemplateDefinition(
    name = name,
    subPath = subPath,
    packageName = if (subPath.isEmpty()) basePackageName else basePackageName + "." +
            subPath.split("/").joinToString(".") { it.replace("_", "-").toPascalCase() },
    parameters = nonContextParameters.map { TemplateParameter(it.name, it.type, it.defaultValue != null) }
)
