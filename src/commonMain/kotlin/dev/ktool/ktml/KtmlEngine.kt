package dev.ktool.ktml

import dev.ktool.ktml.gen.KotlinFileGenerator
import dev.ktool.ktml.parser.ParsedTemplate
import dev.ktool.ktml.parser.TemplateParser
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class KtmlEngine(val basePath: String) {
    private val parser = TemplateParser()
    private val templates = Templates()
    private val fileGenerator = KotlinFileGenerator(templates)

    fun processDirectory(dir: String) {
        val path = dir.toPath()
        require(path.isDirectory) { "Path '${path.absolute}' is not a directory" }

        path.list().forEach {
            if (it.isDirectory) {
                processDirectory(it.toString())
            } else {
                processFile(it.toString())
            }
        }
    }

    fun processFile(file: String) {
        val path = file.toPath()
        log.info { "Processing file: $path" }
        val packageName = path.path.substringAfter(basePath).substringBeforeLast("/").replace("/", ".")
        processTemplate(path.readText(), packageName)
    }

    fun processTemplate(content: String, packageName: String = "") = parser.parseContent(content, packageName).also {
        templates.register(it)
    }

    fun generateTemplateCode(outputBaseDir: String) = templates.all.map { generateTemplateCodeFile(outputBaseDir, it) }

    fun generateTemplateCodeFile(outputBaseDir: String, template: ParsedTemplate) {
        log.info { "Generating code for template: ${template.name}" }
        val filePath = "$outputBaseDir/${template.packageName.replace(".", "/")}/${template.camelCaseName}.kt"
        Path(filePath).mkDirs().writeText(generateTemplateCode(template))
    }

    fun generateTemplateCode(template: ParsedTemplate) = fileGenerator.generateCode(template)
}
