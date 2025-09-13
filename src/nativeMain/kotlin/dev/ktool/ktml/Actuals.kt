package dev.ktool.ktml

import okio.FileSystem
import okio.Path.Companion.toPath

private fun Path.toOkioPath() = path.toPath()

internal actual val Path.absolute: String get() = toOkioPath().normalized().toString()
internal actual fun Path.readText(): String = FileSystem.SYSTEM.read(this.toOkioPath()) { readUtf8() }
internal actual fun Path.writeText(content: String) {
    FileSystem.SYSTEM.write(this.toOkioPath()) { writeUtf8(content) }
}

internal actual fun Path.list(): List<Path> = FileSystem.SYSTEM.list(this.toOkioPath()).map { Path(it.toString()) }
internal actual val Path.isDirectory: Boolean get() = FileSystem.SYSTEM.metadataOrNull(this.toOkioPath())?.isDirectory == true
internal actual val Path.exists: Boolean get() = FileSystem.SYSTEM.exists(this.toOkioPath())

internal actual fun Path.mkDirs(): Path {
    if (exists) return this

    if (isDirectory) {
        FileSystem.SYSTEM.createDirectories(this.toOkioPath(), true)
    } else {
        FileSystem.SYSTEM.createDirectories(this.toOkioPath().parent!!, true)
    }

    return this
}
