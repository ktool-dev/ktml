package dev.ktool.ktml.compile

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.*
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.collections.iterator
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

data class CompileResult(
    val exitCode: Int,
    val messages: List<String>,
    val outputDir: Path
) {
    val ok get() = exitCode == 0
}

object Kotlinc {
    /**
     * Compile Kotlin source files on disk to a destination classes directory.
     *
     * @param sources List of .kt files (absolute or project-relative)
     * @param classpath Classpath entries (dirs or jars). If null, uses current java.class.path
     * @param destinationDir Where .class files go
     * @param jvmTarget "17" (default) or your target
     */
    fun compileFilesToDir(
        sources: List<Path>,
        classpath: List<Path>? = null,
        destinationDir: Path,
        jvmTarget: String = "17",
        noStdlib: Boolean = false
    ): CompileResult {
        destinationDir.createDirectories()

        val args = K2JVMCompilerArguments().apply {
            freeArgs = sources.map { it.toAbsolutePath().toString() }
            this.classpath = (classpath ?: defaultClasspath())
                .joinToString(File.pathSeparator) { it.toAbsolutePath().toString() }
            destination = destinationDir.toAbsolutePath().toString()
            noReflect = true
            this.jvmTarget = jvmTarget
            this.noStdlib = noStdlib // usually false if your classpath already has stdlib
            // You can set additional flags here if needed.
        }

        val messages = mutableListOf<String>()
        val collector = PrintingMessageCollector(
            System.err,
            MessageRenderer.PLAIN_FULL_PATHS,
            true
        ).also { mc ->
            // Capture messages, too
            object : MessageCollector {
                override fun clear() = mc.clear()
                override fun hasErrors() = mc.hasErrors()
                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?
                ) {
                    messages += buildString {
                        append(severity).append(": ").append(message)
                        if (location != null) {
                            append(" (")
                            append(location.path).append(':')
                            append(location.line).append(':')
                            append(location.column).append(')')
                        }
                    }
                    mc.report(severity, message, location)
                }
            }
        }

        val exit = K2JVMCompiler().exec(
            collector as MessageCollector, // still prints; we also captured above
            Services.EMPTY,
            args
        )

        return CompileResult(exit.code, messages, destinationDir)
    }

    /**
     * Compile in-memory Kotlin sources (filename -> source) straight into a JAR.
     * Convenience for “generate → compile → load”.
     */
    fun compileStringsToJar(
        sources: Map<String, String>, // e.g. "com/example/Tmpl.kt" to code
        jarPath: Path,
        classpath: List<Path>? = null,
        jvmTarget: String = "17"
    ): CompileResult {
        val tmp = Files.createTempDirectory("ktml-classes-")
        try {
            // materialize sources to temp dir
            val srcRoot = Files.createTempDirectory("ktml-src-")
            for ((rel, code) in sources) {
                val file = srcRoot.resolve(rel)
                file.parent?.createDirectories()
                file.writeText(code)
            }
            val allKt = Files.walk(srcRoot).filter { it.toString().endsWith(".kt") }.toList()

            val result = compileFilesToDir(
                sources = allKt,
                classpath = classpath,
                destinationDir = tmp,
                jvmTarget = jvmTarget
            )
            if (!result.ok) return result

            // package into jar
            jarPath.parent?.createDirectories()
            JarOutputStream(Files.newOutputStream(jarPath)).use { jos ->
                Files.walk(tmp).forEach { p ->
                    if (Files.isRegularFile(p)) {
                        val entryName = tmp.relativize(p).toString().replace(File.separatorChar, '/')
                        jos.putNextEntry(JarEntry(entryName))
                        Files.copy(p, jos)
                        jos.closeEntry()
                    }
                }
            }
            return result.copy(outputDir = jarPath) // reuse field to point to jar
        } finally {
            // best-effort cleanup
            runCatching { Files.walk(tmp).sorted(Comparator.reverseOrder()).forEach(Files::delete) }
        }
    }

    /**
     * Build a reasonable default classpath: current process classpath split by path separator.
     * Append your libs (e.g., template API) as needed.
     */
    fun defaultClasspath(): List<Path> =
        System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .filter { it.isNotBlank() }
            .map { Path.of(it) }
}

/** Utility: quick classloader from dirs/jars to load compiled code immediately. */
fun newURLClassLoader(vararg paths: Path, parent: ClassLoader = ClassLoader.getSystemClassLoader()): ClassLoader {
    val urls = paths.map { it.toUri().toURL() }.toTypedArray()
    return URLClassLoader.newInstance(urls, parent)
}
