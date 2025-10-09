package dev.ktml

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.*
import kotlin.io.path.absolutePathString
import kotlin.io.path.name

class DirectoryWatcher(val dir: String, val onChange: (String, Boolean) -> Unit) {
    fun start() {
        val watchService = FileSystems.getDefault().newWatchService()

        fun Path.register() {
            register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_DELETE
            )
        }

        Files.walk(Paths.get(dir))
            .filter { Files.isDirectory(it) && !it.absolutePathString().contains("/.") }
            .forEach { dir ->
                if (dir.name != ".ktml") {
                    dir.register()
                }
            }

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                val key = watchService.take()
                val dir = key.watchable() as Path

                for (event in key.pollEvents()) {
                    val fileName = event.context().toString()
                    val path = dir.resolve(fileName)

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE && Files.isDirectory(path)) {
                        path.register()
                    } else if (fileName.endsWith(".ktml") && !Files.isDirectory(path)) {
                        runCatching {
                            onChange(path.toString(), event.kind() == StandardWatchEventKinds.ENTRY_DELETE)
                        }
                    }
                }

                key.reset()
            }
        }
    }
}