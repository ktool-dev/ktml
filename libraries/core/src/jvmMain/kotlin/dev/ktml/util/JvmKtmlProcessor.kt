package dev.ktml.util

import dev.ktml.Content
import dev.ktml.KtmlProcessor
import dev.ktml.TemplateDefinition
import dev.ktml.TemplateRegistry
import dev.ktml.compile.KotlinCompile
import dev.ktml.compile.defaultClasspath
import java.io.File
import java.net.URLClassLoader
import kotlin.io.path.createTempDirectory

class JvmKtmlProcessor(
    basePackageName: String,
    val templatePaths: List<String>,
    outputDirectory: String = createTempDirectory("ktml").toString(),
    compiledDirectory: String = createTempDirectory("ktml-compile").toString()
) : TemplateRegistry,
    KtmlProcessor(basePackageName, outputDirectory) {
    private var _templateRegistry: TemplateRegistry? = null
    private val compileDir: File = File(compiledDirectory)
    private val outputDir: File = File(outputDirectory)

    override val functions: Map<String, Content> get() = templateRegistry.functions
    override val templates: List<TemplateDefinition> get() = templateRegistry.templates

    private val templateRegistry: TemplateRegistry
        get() {
            if (_templateRegistry == null) _templateRegistry = loadTemplateRegistry()
            return _templateRegistry!!
        }

    fun loadTemplateRegistry(): TemplateRegistry {
        processRootDirectories(templatePaths)
        generateTemplateCode()
        compile()
        
        val className = "$basePackageName.TemplateRegistryImpl"
        try {
            val classLoader = createReloadableClassLoader(listOf(compileDir))
            val type = Class.forName(className, true, classLoader).kotlin
            if (type.objectInstance == null || type.objectInstance !is TemplateRegistry) {
                error("The package $basePackageName does not have a valid TemplateRegistry")
            }
            return type.objectInstance as TemplateRegistry
        } catch (_: ClassNotFoundException) {
            error("The package $basePackageName does not have a valid TemplateRegistry")
        }
    }

    private fun createReloadableClassLoader(classpath: List<File>): ClassLoader =
        URLClassLoader(
            classpath.plus(defaultClasspath().map { it.toFile() }).map { it.toURI().toURL() }.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )

    private fun compile() {
        val errors = KotlinCompile.compileFilesToDir(outputDir.toPath(), compileDir.toPath())
        if (errors.isNotEmpty()) {
            throw Exception("Failed to compile templates with errors: ${errors.joinToString("\n") { it.message }}")
        }
    }
}

