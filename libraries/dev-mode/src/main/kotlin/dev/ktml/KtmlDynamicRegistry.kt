package dev.ktml

import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
import dev.ktml.util.CompileExceptionRegistry
import dev.ktml.util.CompilerErrorResolver
import java.io.File
import kotlin.io.path.createTempDirectory

object KtmlDynamicRegistryFactory : KtmlRegistryFactory {
    override fun create(templateDir: String, templatePackage: String, outputDir: String): KtmlRegistry =
        KtmlDynamicRegistry(templateDir, templatePackage, outputDir = outputDir).apply { initializeRegistry() }
}

/**
 * This implements the KtmlRegistry interface and will automatically reload templates if they change and recompile them.
 */
class KtmlDynamicRegistry(
    val templateDir: String,
    val templatePackage: String,
    watchFiles: Boolean = true,
    val onPathsChanged: () -> Unit = {},
    outputDir: String = createTempDirectory().toString(),
) : KtmlRegistry {
    private var _templateRegistry: KtmlRegistry? = null
    private val generatedDir: File = File(outputDir)
    private val compileDir: File = generatedDir.parentFile.resolve("classes")

    override operator fun get(path: String): Content? = ktmlRegistry[path]
    override val tags: List<TagDefinition> get() = ktmlRegistry.tags
    override val paths: List<String> get() = ktmlRegistry.paths

    private val processor = KtmlProcessor(outputDirectory = generatedDir.absolutePath, removeContentComments = false)

    private val basePackageName: String get() = processor.basePackageName

    init {
        if (watchFiles) {
            KtmlFileWatcher(templateDir, ::reprocessFile).start()
        }
    }

    private val ktmlRegistry: KtmlRegistry
        get() {
            if (_templateRegistry == null) _templateRegistry = loadTemplateRegistry()
            return _templateRegistry!!
        }

    fun initializeRegistry() {
        _templateRegistry = loadTemplateRegistry()
    }

    fun reprocessFile(file: String, itemDeleted: Boolean) {
        val pathsBefore = processor.pagePaths
        val removed = processor.removeFile(file, templateDir)
        if (itemDeleted) {
            removed.forEach { File(generatedDir, it.codeFile).delete() }
            // We have to rebuild everything to make sure the removal didn't break anything
            processor.clear()
            _templateRegistry = loadTemplateRegistry(false)
        } else {
            val templates = processor.processFile(file, templateDir)
            templates.forEach { processor.generateTemplateCodeFile(it) }
            processor.generateRegistry()
            _templateRegistry = compileTemplates()
        }
        if (processor.pagePaths != pathsBefore) {
            onPathsChanged()
        }
    }

    private fun loadTemplateRegistry(onlyIfNonExisting: Boolean = true): KtmlRegistry {
        generatedDir.mkdirs()
        processor.processRootDirectories(listOf(templateDir))

        // If the registry is already there, then the build must have already generated the code, so we don't need to
        return if (onlyIfNonExisting && File(generatedDir, "KtmlRegistry.kt").exists()) {
            loadRegistry()
        } else {
            processor.generateTemplateCode()
            compileTemplates()
        }
    }

    private fun compileTemplates(): KtmlRegistry {
        val exception = compile()

        if (exception != null) {
            exception.printStackTrace()
            return CompileExceptionRegistry(exception)
        }

        return loadRegistry()
    }

    private fun loadRegistry(): DualKtmlRegistry {
        val className = "$basePackageName.KtmlRegistry"
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
        ChildFirstClassLoader(
            classpath.map { it.toURI().toURL() }.toTypedArray(),
            Thread.currentThread().contextClassLoader
        )

    private fun compile(): CompileException? {
        val errors = KotlinCompile.compileFilesToDir(generatedDir.toPath(), compileDir.toPath())
        val errorResolver =
            CompilerErrorResolver(
                processor.getParsedTemplates(),
                templatePackage,
                generatedDir.absolutePath,
                templateDir
            )
        return if (errors.isNotEmpty()) {
            CompileException(errorResolver.resolve(errors))
        } else {
            null
        }
    }
}
