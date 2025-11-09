package dev.ktml

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.File.pathSeparator
import java.io.File.separator
import kotlin.io.path.createTempDirectory

private val log = KotlinLogging.logger {}

interface KtmlRegistryFactory {
    fun create(templateDir: String, templatePackage: String, outputDir: String): KtmlRegistry
}

fun findKtmlRegistry(templatePackage: String): KtmlRegistry {
    val registryFactory = loadDynamicRegistryFactory() ?: return findRegistryImpl(templatePackage)

    val projectDir = determineProjectRoot()
    val ktmlDir = determineKtmlDir(projectDir)

    log.info { "Running with ktml directory: $ktmlDir" }

    val outputDir = determineOutputDir(projectDir, ktmlDir)

    return registryFactory.create(ktmlDir.absolutePath, templatePackage, outputDir.absolutePath)
}

private fun determineKtmlDir(projectDir: File): File =
    projectDir.findExisting(listOf("src", "main", "ktml"), listOf("src", "jvmMain", "ktml")) ?: projectDir

private fun File.findExisting(vararg options: List<String>): File? =
    options.map { resolve(it.joinToString(separator)) }.find { it.exists() && it.isDirectory }


private fun determineProjectRoot(): File {
    val buildFolder = "${separator}build${separator}"
    val targetFolder = "${separator}target${separator}"

    // Try to find the actual project directory by looking at the classpath
    // Look for a build/classes or target/classes directory in the classpath
    val classpath = System.getProperty("java.class.path").split(pathSeparator)
    val projectRoot = classpath.firstOrNull {
        it.contains("${buildFolder}classes") || it.contains("${targetFolder}classes")
    }?.let {
        if (it.contains(buildFolder)) it.substringBefore(buildFolder) else it.substringBefore(targetFolder)
    }

    // Fall back to working directory
    return File(projectRoot ?: System.getProperty("user.dir"))
}

private fun determineOutputDir(projectDir: File, ktmlDir: File) = projectDir.findExisting(
    listOf("build", "ktml", "main"),
    listOf("build", "ktml", "jvmMain"),
    listOf("target", "ktml", "main")
) ?: createTempDirectory().toFile()

private fun findRegistryImpl(templatePackage: String): KtmlRegistry = try {
    val type = Class.forName("$templatePackage.KtmlRegistry")
    type.getField("INSTANCE").get(null) as KtmlRegistry
} catch (_: ClassNotFoundException) {
    error("Could not find dev-mode module on the classpath or a generated KtmlRegistry.")
}

private fun loadDynamicRegistryFactory(): KtmlRegistryFactory? = try {
    val type = Class.forName("dev.ktml.KtmlDynamicRegistryFactory")
    type.getField("INSTANCE").get(null) as KtmlRegistryFactory
} catch (_: ClassNotFoundException) {
    null
}
