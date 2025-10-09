package dev.ktml.util

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import java.io.File

private fun Path.toFile() =
    if (path.startsWith("/")) File(path) else File(System.getProperty("user.dir"), path)

internal actual val Path.absolute: String get() = toFile().absolutePath
internal actual fun Path.readText(): String = toFile().readText()
internal actual fun Path.writeText(content: String) = toFile().writeText(content)
internal actual fun Path.list(): List<Path> = toFile().listFiles().map { Path(it.path) }
internal actual val Path.isDirectory: Boolean get() = toFile().isDirectory
internal actual val Path.exists: Boolean get() = toFile().exists()
internal actual fun Path.remove(): Boolean = toFile().delete()

internal actual fun Path.mkDirs(): Path {
    if (exists) return this

    if (isDirectory) {
        toFile().mkdir()
    } else {
        toFile().parentFile?.mkdirs()
    }

    return this
}

internal actual fun createHttpClient() = HttpClient(OkHttp) {
    engine {
        config {
            followRedirects(true)
        }
    }
}

