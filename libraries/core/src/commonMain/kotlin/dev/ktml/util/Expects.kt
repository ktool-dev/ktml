package dev.ktml.util

import io.ktor.client.*

internal expect val Path.absolute: String
internal expect fun Path.readText(): String
internal expect fun Path.writeText(content: String)
internal expect fun Path.list(): List<Path>
internal expect fun Path.mkDirs(): Path
internal expect val Path.isDirectory: Boolean
internal expect val Path.exists: Boolean
internal expect fun Path.remove(): Boolean

internal expect fun createHttpClient(): HttpClient
