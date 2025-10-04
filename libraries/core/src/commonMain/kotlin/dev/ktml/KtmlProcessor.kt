package dev.ktml

import dev.ktml.gen.KotlinFileGenerator
import dev.ktml.gen.TemplateRegistryGenerator
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateDefinitions
import dev.ktml.parser.TemplateParser
import dev.ktml.util.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class KtmlProcessor {
    private val parser = TemplateParser()
    private val templates = TemplateDefinitions()
    private val parsedTemplates = mutableListOf<ParsedTemplate>()
    private val fileGenerator = KotlinFileGenerator(templates)

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
        templates.register(it.templateDefinition)
    }

    fun generateTemplateCode(outputBaseDir: String) {
        parsedTemplates.map { generateTemplateCodeFile(outputBaseDir, it) }
        val content = TemplateRegistryGenerator(parsedTemplates).createTemplateRegistry()
        Path("$outputBaseDir/dev/ktml/templates/TemplateRegistry.kt").mkDirs().writeText(content)
    }

    fun generateTemplateCodeFile(outputBaseDir: String, template: ParsedTemplate) {
        log.info { "Generating code for template: ${template.name}" }
        val filePath = "$outputBaseDir/${template.packageName.replace(".", "/")}/${template.camelCaseName}.kt"
        Path(filePath).mkDirs().writeText(generateTemplateCode(template).render())
    }

    fun generateTemplateCode(template: ParsedTemplate) = fileGenerator.generateCode(template)
}
