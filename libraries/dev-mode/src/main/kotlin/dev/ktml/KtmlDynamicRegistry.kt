package dev.ktml

import dev.ktml.compile.KotlinCompile
import dev.ktml.compile.defaultClasspath
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
import dev.ktml.util.ROOT_PACKAGE_PATH
import dev.ktml.util.toKebabCase
import java.io.File
import java.net.URLClassLoader
import kotlin.io.path.createTempDirectory

object KtmlDynamicRegistryFactory : KtmlRegistryFactory {
    override fun create(templateDir: String, outputDir: String): KtmlRegistry =
        KtmlDynamicRegistry(templateDir, outputDir = outputDir)
}

/**
 * This implements the KtmlRegistry interface and will automatically reload templates if they change and recompile them.
 */
class KtmlDynamicRegistry(
    val templateDir: String,
    watchFiles: Boolean = true,
    val onPathsChanged: () -> Unit = {},
    outputDir: String = createTempDirectory().toString(),
) : KtmlRegistry {
    private var _templateRegistry: KtmlRegistry? = null
    private val compileDir: File = File(outputDir).resolve("compiled")
    private val generatedDir: File = File(outputDir).resolve("generated")
    private var exception: Exception? = null

    override val templates: Map<String, Content> get() = ktmlRegistry.templates
    override val tags: List<TagDefinition> get() = ktmlRegistry.tags

    private val processor = KtmlProcessor(outputDirectory = generatedDir.absolutePath)

    private val basePackageName: String get() = processor.basePackageName

    init {
        if (watchFiles) {
            KtmlFileWatcher(templateDir, ::reprocessFile).start()
        }
    }

    private val ktmlRegistry: KtmlRegistry
        get() {
            if (_templateRegistry == null) _templateRegistry = loadTemplateRegistry()
            if (exception != null) throw exception!!
            return _templateRegistry!!
        }

    fun reprocessFile(file: String, itemDeleted: Boolean) {
        val pathsBefore = processor.pagePaths
        if (itemDeleted) {
            // We have to rebuild everything to make sure the removal didn't break anything
            processor.clear()
            _templateRegistry = loadTemplateRegistry()
        } else {
            processor.removeFile(file, templateDir)
            val templates = processor.processFile(file, templateDir)
            templates.forEach { processor.generateTemplateCodeFile(it) }
            processor.generateRegistry()
            _templateRegistry = compileTemplates()
        }
        if (processor.pagePaths != pathsBefore) {
            onPathsChanged()
        }
    }

    private fun loadTemplateRegistry(): KtmlRegistry {
        generatedDir.deleteRecursively()
        generatedDir.mkdirs()
        processor.processRootDirectories(listOf(templateDir))
        processor.generateTemplateCode()
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
            return (type.objectInstance as KtmlRegistry).join(DefaultKtmlRegistry)
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
        compileDir.deleteRecursively()
        val errors = KotlinCompile.compileFilesToDir(generatedDir.toPath(), compileDir.toPath())
        if (errors.isNotEmpty()) {
            val convertedErrors = errors.map {
                val path = it.filePath.substringAfter(ROOT_PACKAGE_PATH)
                val folder = path.substringBeforeLast("/")
                val fileName = path.substringAfterLast("/").substringBeforeLast(".").toKebabCase()
                it.copy(filePath = "$folder/$fileName.ktml".removePrefix("/"))
            }
            exception = CompileException(convertedErrors)
            exception?.printStackTrace()
        }
    }
}
