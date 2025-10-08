package dev.ktml

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.*

class DirectoryWatcher(val dir: String, val onChange: (String) -> Unit) {
    fun start() {
        val watchService = FileSystems.getDefault().newWatchService()

        Files.walk(Paths.get(dir))
            .filter { Files.isDirectory(it) }
            .forEach { dir ->
                dir.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
                )
            }

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val key = watchService.take()
                val dir = key.watchable() as Path

                for (event in key.pollEvents()) {
                    val fileName = event.context().toString()
                    val fullPath = dir.resolve(fileName).toString()

                    if (fileName.endsWith(".ktml")) {
                        runCatching {
                            onChange(fullPath)
                        }
                        break
                    }
                }

                key.reset()
            }
        }
    }
}