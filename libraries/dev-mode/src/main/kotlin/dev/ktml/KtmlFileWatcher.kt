package dev.ktml

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File.separator
import java.nio.file.*
import java.nio.file.attribute.FileTime

private val log = KotlinLogging.logger {}

class KtmlFileWatcher(dir: String, val onChange: (String, Boolean) -> Unit) {
    private val dirPath = Paths.get(dir)

    private class WatchedFile(val path: String, val lastModified: FileTime) {
        override fun equals(other: Any?) = other is WatchedFile && other.path == path

        override fun hashCode() = path.hashCode()

        override fun toString() = "WatchedFile(path='$path', lastModified=$lastModified)"
    }

    fun start() {
        val watchService: WatchService = FileSystems.getDefault().newWatchService()
        val watchedDirectories = mutableMapOf<WatchKey, Path>()
        registerDirectoryRecursively(dirPath, watchService, watchedDirectories)

        CoroutineScope(Dispatchers.IO).launch {
            var lastFileList = buildFileList()
            while (true) {
                val key = watchService.take()
                val parentDirectory = watchedDirectories[key]
                val events = key.pollEvents()

                if (parentDirectory != null) {
                    registerNewDirectories(events, parentDirectory, watchService, watchedDirectories)
                }

                if (events.isNotEmpty()) {
                    lastFileList = checkForChanges(lastFileList)
                }

                if (!key.reset()) {
                    watchedDirectories.remove(key)
                }
            }
        }
    }

    private fun registerDirectoryRecursively(
        root: Path,
        watchService: WatchService,
        watchedDirectories: MutableMap<WatchKey, Path>
    ) {
        Files.walk(root).use { paths ->
            paths.filter { Files.isDirectory(it) }
                .forEach { directory ->
                    val key = directory.register(
                        watchService,
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY
                    )
                    watchedDirectories[key] = directory
                }
        }
    }

    private fun registerNewDirectories(
        events: List<WatchEvent<*>>,
        parentDirectory: Path,
        watchService: WatchService,
        watchedDirectories: MutableMap<WatchKey, Path>
    ) {
        events.forEach { event ->
            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                val createdPath = parentDirectory.resolve(event.context() as Path)
                if (Files.isDirectory(createdPath)) {
                    registerDirectoryRecursively(createdPath, watchService, watchedDirectories)
                }
            }
        }
    }

    private fun checkForChanges(lastFileList: List<WatchedFile>) = buildFileList().also { fileList ->
        val removedFiles = lastFileList.filter { it !in fileList }
        val updatedFiles = fileList.filter { file ->
            lastFileList.firstOrNull { file == it }?.lastModified != file.lastModified
        }

        removedFiles.forEach { callOnChange(it, true) }
        updatedFiles.forEach { callOnChange(it, false) }
    }

    private fun callOnChange(file: WatchedFile, itemDeleted: Boolean) = runCatching {
        log.info { "Change detected for ${file.path.substringAfter(dirPath.toString()).removePrefix(separator)}." }
        onChange(file.path, itemDeleted)
    }

    private fun buildFileList() = Files.walk(dirPath)
        .filter { !Files.isDirectory(it) && it.toString().endsWith(".ktml") }
        .map { WatchedFile(it.toString(), Files.getLastModifiedTime(it)) }
        .toList()
}
