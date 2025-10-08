package dev.ktml

import dev.ktml.gen.KotlinFileGenerator
import dev.ktml.gen.KtmlRegistryGenerator
import dev.ktml.parser.ParsedTemplate
import dev.ktml.parser.TemplateParser
import dev.ktml.parser.Templates
import dev.ktml.util.*
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

open class KtmlProcessor(private val moduleName: String = "", outputDirectory: String) {
    val basePackageName = if (moduleName.isNotEmpty()) "$ROOT_PACKAGE.$moduleName" else ROOT_PACKAGE
    private val parser = TemplateParser(moduleName)
    private val templates = Templates()
    private val parsedTemplates = mutableMapOf<String, ParsedTemplate>()
    private val fileGenerator = KotlinFileGenerator(templates)
    private val basePath = "$outputDirectory/${basePackageName.replace(".", "/")}"

    fun processRootDirectories(dirs: List<String>) = dirs.forEach { processRootDirectory(it) }

    fun processRootDirectory(dir: String) = processDirectory(dir, dir)

    private fun processDirectory(dir: String, rootPath: String = dir) {
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

    fun processFile(file: String, rootPath: String, replaceExisting: Boolean = false) {
        if (!file.endsWith(".ktml")) return

        val fileName = file.substringAfterLast("/").substringBeforeLast(".ktml")
        val path = file.toPath()
        val modulePath = path.path.substringAfter("$rootPath/").substringBeforeLast("/", "").removeSuffix("/")
        log.info { "Processing file: $path" }
        val subPath = when {
            modulePath.isEmpty() && moduleName.isEmpty() -> ""
            modulePath.isEmpty() -> moduleName
            moduleName.isEmpty() -> modulePath
            else -> "$modulePath/$moduleName"
        }

        if (replaceExisting) {
            replaceTemplate(fileName, path.readText(), subPath)
        } else {
            processTemplate(fileName, path.readText(), subPath)
        }
    }

    private fun processTemplate(fileName: String, content: String, subPath: String) {
        parser.parseContent(fileName, content, subPath).forEach {
            parsedTemplates[it.path] = it
            templates.register(it)
        }
    }

    open fun replaceTemplate(fileName: String, content: String, subPath: String = "") {
        parser.parseContent(fileName, content, subPath).forEach {
            parsedTemplates[it.path] = it
            templates.replace(it)
            generateTemplateCodeFile(it)
            generateRegistry()
        }
    }

    fun generateTemplateCode() {
        parsedTemplates.map { generateTemplateCodeFile(it.value) }
        generateRegistry()
    }

    private fun generateRegistry() {
        val content = KtmlRegistryGenerator.createKtmlRegistry(basePackageName, templates)
        Path("$basePath/KtmlRegistryImpl.kt").mkDirs().writeText(content)
    }

    fun generateTemplateCodeFile(template: ParsedTemplate) {
        log.debug { "Generating code for template: ${template.name}" }
        val filePath = "$basePath/${template.pathCamelCaseName}.kt"
        Path(filePath).mkDirs().writeText(fileGenerator.generateCode(template).render())
    }
}
