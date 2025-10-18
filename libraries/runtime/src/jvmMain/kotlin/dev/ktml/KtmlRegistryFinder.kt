package dev.ktml

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.File.pathSeparator
import java.io.File.separator
import kotlin.io.path.createTempDirectory

private val log = KotlinLogging.logger {}

interface KtmlRegistryFactory {
    fun create(templateDir: String, outputDir: String): KtmlRegistry
}

fun findKtmlRegistry(basePath: String = determineBasePath()): KtmlRegistry {
    val registryFactory = loadDynamicRegistryFactory() ?: return findRegistryImpl()

    val ktmlDir = breadthFirstSearchForKtmlDir(basePath)

    log.info { "Running with ktml directory: $ktmlDir" }

    return registryFactory.create(ktmlDir, determineOutputDir(ktmlDir))
}

/**
 * This will try to determine where the ktml files are located. They should be in a directory like src/main/ktml
 * or maybe src/commonMain/ktml
 */
private fun determineBasePath(): String {
    val workingDir = System.getProperty("user.dir")
    val projectPath = System.getProperty("java.class.path").split(pathSeparator).find { it.startsWith(workingDir) }

    if (projectPath?.contains(workingDir) == true) {
        return workingDir + projectPath.substringAfter(workingDir)
            .substringBefore("${separator}build$separator")
    }

    return workingDir
}

private fun determineOutputDir(ktmlDir: String): String {
    // Usually this is in a src/main/ktml directory. If there is a build or target directory near, use that.
    if (ktmlDir.contains("/src/")) {
        val dir = File(ktmlDir.substringBeforeLast("/src/"))

        val buildDir = dir.resolve("build")
        if (buildDir.exists()) return buildDir.resolve("ktml").absolutePath

        val targetDir = dir.resolve("target")
        if (targetDir.exists()) return targetDir.resolve("ktml").absolutePath
    }

    return createTempDirectory().toString()
}

private fun breadthFirstSearchForKtmlDir(basePath: String): String {
    val userDir = File(basePath)

    val baseDir = if (userDir.name == "build") {
        userDir.parentFile
    } else {
        userDir
    }

    val queue = ArrayDeque<File>()

    val srcDir = baseDir.resolve("src")
    queue.add(if (srcDir.exists()) srcDir else baseDir)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        val files = current.listFiles() ?: continue

        val ktmlDir = files.find { it.isDirectory && it.name == "ktml" }
        if (ktmlDir != null) return ktmlDir.absolutePath

        for (file in files) {
            if (file.isDirectory) {
                queue.add(file)
            }
        }
    }

    return baseDir.absolutePath
}

private fun findRegistryImpl(): KtmlRegistry = try {
    val type = Class.forName("dev.ktml.templates.KtmlRegistryImpl")
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
