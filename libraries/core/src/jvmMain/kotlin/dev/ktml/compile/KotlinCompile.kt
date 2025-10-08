package dev.ktml.compile

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.File
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories

data class CompilerError(val message: String, val filePath: String, val line: Int, val column: Int)

object KotlinCompile {
    /**
     * Compile Kotlin source files on disk to a destination classes directory.
     *
     * @param rootDir Root directory to search for .kt files
     * @param outputDir Where .class files go
     */
    fun compileFilesToDir(
        rootDir: Path,
        outputDir: Path,
        jvmTarget: String = getCurrentJvmTarget()
    ): List<CompilerError> {
        outputDir.createDirectories()

        val sources = Files.walk(rootDir).filter { it.toString().endsWith(".kt") }.toList()

        val args = K2JVMCompilerArguments().apply {
            freeArgs = sources.map { it.toAbsolutePath().toString() }
            this.classpath = defaultClasspath().joinToString(File.pathSeparator) { it.toAbsolutePath().toString() }
            destination = outputDir.toAbsolutePath().toString()
            noReflect = true
            this.jvmTarget = jvmTarget
            this.noStdlib = true
        }

        val messages = mutableListOf<CompilerError>()

        val collector =
            object : MessageCollector {
                override fun clear() = messages.clear()

                override fun hasErrors() = messages.isNotEmpty()

                override fun report(
                    severity: CompilerMessageSeverity,
                    message: String,
                    location: CompilerMessageSourceLocation?
                ) {
                    if (severity == CompilerMessageSeverity.ERROR || severity == CompilerMessageSeverity.EXCEPTION) {
                        messages += CompilerError(
                            message,
                            location?.path?.substringAfter(rootDir.toString())?.trimStart('/') ?: "",
                            location?.line ?: 0,
                            location?.column ?: 0
                        )
                    }
                }
            }

        K2JVMCompiler().exec(collector as MessageCollector, Services.EMPTY, args)

        return messages
    }
}

private fun getCurrentJvmTarget() = System.getProperty("java.specification.version").let { version ->
    when {
        version.startsWith("1.") -> version.substring(2)
        else -> version
    }
}


/**
 * Utility: quick classloader from dirs/jars to load compiled code immediately.
 *
 * @param paths Directories or JAR files to add to the classpath
 * @param parent Optional parent classloader (default: system classloader)
 */
fun newURLClassLoader(vararg paths: Path, parent: ClassLoader = ClassLoader.getSystemClassLoader()): ClassLoader {
    val urls = paths.map { it.toUri().toURL() }.toTypedArray()
    return URLClassLoader.newInstance(urls, parent)
}

/**
 * Utility: build a reasonable default classpath: current process classpath split by path separator.
 */
fun defaultClasspath(): List<Path> =
    System.getProperty("java.class.path")
        .split(File.pathSeparator)
        .filter { it.isNotBlank() }
        .map { Path.of(it) }
