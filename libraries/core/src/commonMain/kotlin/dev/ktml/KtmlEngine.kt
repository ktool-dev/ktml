package dev.ktml

import dev.ktml.gen.KotlinFileGenerator
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateParser
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

class KtmlEngine {
    private val parser = TemplateParser()
    private val templates = Templates()
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
        val packageName = path.path.substringAfter(rootPath).substringBeforeLast("/").replace("/", ".")
        processTemplate(path.readText(), packageName.removePrefix("."))
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
