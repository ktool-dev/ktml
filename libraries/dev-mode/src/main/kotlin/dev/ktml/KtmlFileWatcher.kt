package dev.ktml

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.*
import java.nio.file.attribute.FileTime

class KtmlFileWatcher(dir: String, val onChange: (String, Boolean) -> Unit) {
    private val dirPath = Paths.get(dir)

    private class WatchedFile(val path: String, val lastModified: FileTime) {
        override fun equals(other: Any?) = other is WatchedFile && other.path == path

        override fun hashCode() = path.hashCode()

        override fun toString() = "WatchedFile(path='$path', lastModified=$lastModified)"
    }

    fun start() {
        val watchService: WatchService = FileSystems.getDefault().newWatchService()
        dirPath.register(
            watchService,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )

        CoroutineScope(Dispatchers.IO).launch {
            var lastFileList = buildFileList()
            while (true) {
                val key = watchService.take()
                if (key.pollEvents().isNotEmpty()) {
                    lastFileList = checkForChanges(lastFileList)
                }
                key.reset()
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
        onChange(file.path, itemDeleted)
    }

    private fun buildFileList() = Files.walk(dirPath)
        .filter { !Files.isDirectory(it) && it.toString().endsWith(".ktml") }
        .map { WatchedFile(it.toString(), Files.getLastModifiedTime(it)) }
        .toList()
}