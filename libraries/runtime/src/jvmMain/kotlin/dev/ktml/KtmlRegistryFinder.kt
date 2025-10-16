package dev.ktml

import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.io.File.pathSeparator
import java.io.File.separator

private val log = KotlinLogging.logger {}

fun findKtmlRegistry(basePath: String = determineBasePath()): KtmlRegistry {
    val dynamicRegistryClass = loadDynamicRegistryClass() ?: return findRegistryImpl()

    val ktmlDir = breadthFirstSearchForKtmlDir(basePath)

    log.info { "Running with ktml directory: $ktmlDir" }

    return createDynamicRegistry(dynamicRegistryClass, ktmlDir)
}

private fun determineBasePath(): String {
    val workingDir = System.getProperty("user.dir")
    val projectPath = System.getProperty("java.class.path").split(pathSeparator).find { it.startsWith(workingDir) }

    if (projectPath?.contains(workingDir) == true) {
        return workingDir + projectPath.substringAfter(workingDir)
            .substringBefore("${separator}build$separator")
    }

    return workingDir
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

private fun loadDynamicRegistryClass(): Class<*>? = try {
    Class.forName("dev.ktml.KtmlDynamicRegistry")
} catch (_: ClassNotFoundException) {
    null
}

private fun createDynamicRegistry(dynamicRegistryClass: Class<*>, templateDir: String): KtmlRegistry =
    dynamicRegistryClass.getConstructor(String::class.java).newInstance(templateDir) as KtmlRegistry
