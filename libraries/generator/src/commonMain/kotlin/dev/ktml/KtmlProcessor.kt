package dev.ktml

import dev.ktml.gen.KotlinFileGenerator
import dev.ktml.gen.createKtmlRegistry
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

    fun clear() {
        templates.clear()
        parsedTemplates.clear()
    }

    val pagePaths: List<String> get() = templates.registryTemplates.map { it.path }.sorted()

    fun getParsedTemplates() = parsedTemplates.values.toList()

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

    fun processFile(file: String, rootPath: String): List<ParsedTemplate> {
        val path = file.toPath()
        if (!file.endsWith(".ktml") || path.isDirectory) return listOf()
        val (fileName, subPath) = parseFilePath(file, rootPath)
        log.info { "Processing file: $subPath/$fileName.ktml" }
        return processTemplate(fileName, path.readText(), subPath)
    }

    private fun parseFilePath(file: String, rootPath: String): Pair<String, String> {
        val fileName = file.substringAfterLast("/").substringBeforeLast(".ktml")
        val modulePath = file.substringAfter("$rootPath/").substringBeforeLast("/", "").removeSuffix("/")

        val subPath = when {
            modulePath.isEmpty() && moduleName.isEmpty() -> ""
            modulePath.isEmpty() -> moduleName
            moduleName.isEmpty() -> modulePath
            else -> "$modulePath/$moduleName"
        }

        return Pair(fileName, subPath)
    }

    private fun processTemplate(fileName: String, content: String, subPath: String) =
        parser.parseContent(fileName, content, subPath).apply {
            forEach {
                parsedTemplates[it.path] = it
                templates.register(it)
            }
        }

    fun removeFile(file: String, rootPath: String): List<ParsedTemplate> {
        val (fileName, subPath) = parseFilePath(file, rootPath)
        val removed = parsedTemplates.values.filter { it.file == fileName && it.subPath == subPath }
        removed.forEach {
            parsedTemplates.remove(it.path)
            templates.remove(it)
            templateCodeFile(it).remove()
        }
        return removed
    }

    fun generateTemplateCode() {
        parsedTemplates.map { generateTemplateCodeFile(it.value) }
        generateRegistry()
    }

    fun generateRegistry() {
        val content = createKtmlRegistry(basePackageName, templates)
        Path("$basePath/KtmlRegistryImpl.kt").mkDirs().writeText(content)
    }

    fun generateTemplateCodeFile(template: ParsedTemplate) {
        log.debug { "Generating code for template: ${template.name}" }
        templateCodeFile(template).mkDirs().writeText(fileGenerator.generateCode(template).render())
    }

    private fun templateCodeFile(template: ParsedTemplate) = Path("$basePath/${template.codeFile}")
}
