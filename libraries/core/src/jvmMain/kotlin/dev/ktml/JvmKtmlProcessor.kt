package dev.ktml

import dev.ktml.compile.KotlinCompile
import dev.ktml.compile.defaultClasspath
import java.io.File
import java.net.URLClassLoader
import kotlin.io.path.createTempDirectory

class JvmKtmlProcessor(
    val templateDir: String,
    outputDirectory: String = createTempDirectory("ktml").toString(),
    compiledDirectory: String = createTempDirectory("ktml-compile").toString()
) : KtmlRegistry,
    KtmlProcessor(outputDirectory = outputDirectory) {
    private var _templateRegistry: KtmlRegistry? = null
    private val compileDir: File = File(compiledDirectory)
    private val outputDir: File = File(outputDirectory)
    private var exception: Exception? = null

    override val pages: Map<String, Content> get() = templateRegistry.pages
    override val tags: List<TagDefinition> get() = templateRegistry.tags

    init {
        DirectoryWatcher(templateDir) {
            processFile(it, templateDir, replaceExisting = true)
        }.start()
    }

    private val templateRegistry: KtmlRegistry
        get() {
            if (_templateRegistry == null) _templateRegistry = loadTemplateRegistry()
            if (exception != null) throw exception!!
            return _templateRegistry!!
        }

    override fun replaceTemplate(fileName: String, content: String, subPath: String) =
        super.replaceTemplate(fileName, content, subPath).also {
            _templateRegistry = compileTemplates()
        }

    fun loadTemplateRegistry(): KtmlRegistry {
        outputDir.deleteRecursively()
        outputDir.mkdirs()
        processRootDirectories(listOf(templateDir))
        generateTemplateCode()
        return compileTemplates()
    }

    private fun compileTemplates(): KtmlRegistry {
        compile()

        val className = "$basePackageName.KtmlRegistryImpl"
        try {
            val classLoader = createReloadableClassLoader(listOf(compileDir))
            val type = Class.forName(className, true, classLoader).kotlin
            if (type.objectInstance == null || type.objectInstance !is KtmlRegistry) {
                error("The package $basePackageName does not have a valid KtmlRegistry")
            }
            return type.objectInstance as KtmlRegistry
        } catch (_: ClassNotFoundException) {
            error("The package $basePackageName does not have a valid KtmlRegistry")
        }
    }

    private fun createReloadableClassLoader(classpath: List<File>): ClassLoader =
        URLClassLoader(
            classpath.plus(defaultClasspath().map { it.toFile() }).map { it.toURI().toURL() }.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )

    private fun compile() {
        exception = null
        val errors = KotlinCompile.compileFilesToDir(outputDir.toPath(), compileDir.toPath())
        if (errors.isNotEmpty()) {
            exception = Exception("Failed to compile templates with errors: ${errors.joinToString("\n")}")
            exception?.printStackTrace()
        }
    }
}
