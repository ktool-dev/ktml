package io.ktml

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktml.gen.KotlinFileGenerator
import io.ktml.parser.ParsedTemplate
import io.ktml.parser.TemplateParser

private val log = KotlinLogging.logger {}

class KtmlEngine(val basePath: String) {
    private val parser = TemplateParser()
    private val templates = Templates()
    private val fileGenerator = KotlinFileGenerator(templates)

    fun processDirectory(path: String) {
        require(Path(path).isDirectory) { "Path '$path' is not a directory" }

        Path(path).list().forEach {
            if (it.isDirectory) {
                processDirectory(it.toString())
            } else {
                processFile(it.toString())
            }
        }
    }

    fun processFile(path: String) {
        log.info { "Processing file: $path" }
        val packageName = path.substringAfter(basePath).substringBeforeLast("/").replace("/", ".")
        processTemplate(Path(path).readText(), packageName)
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
