package dev.ktml.ktor

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import io.ktor.utils.io.core.writeText
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.charsets.Charsets
import kotlinx.io.Sink

class KtmlConfig {
    lateinit var registry: KtmlRegistry
}

val KtmlPlugin = createApplicationPlugin(name = "KTML", createConfiguration = ::KtmlConfig) {
    application.attributes.put(ktmlEngineKey, KtmlEngine(pluginConfig.registry))
}

val ktmlEngineKey = AttributeKey<KtmlEngine>("KtmlEngine")

suspend fun RoutingCall.respondKtml(
    path: String,
    model: Map<String, Any?> = mapOf(),
    status: HttpStatusCode = HttpStatusCode.OK
) {
    respondKtml(
        path = path,
        model = model,
        status = status,
        queryParameters = queryParameters.toMap(),
        pathParameters = pathParameters.toMap().mapValues { it.value.firstOrNull().toString() }
    )
}

@OptIn(InternalAPI::class)
suspend fun ApplicationCall.respondKtml(
    path: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    model: Map<String, Any?> = mapOf(),
    queryParameters: Map<String, List<String>> = mapOf(),
    pathParameters: Map<String, String> = mapOf(),
) {
    val engine = application.attributes[ktmlEngineKey]

    respondBytesWriter(contentType = ContentType.Text.Html, status = status) {
        val context = Context(KtmlWriter(writeBuffer), model, queryParameters, pathParameters)
        engine.writePage(context, path)
    }
}

private class KtmlWriter(val writeBuffer: Sink) : ContentWriter {
    override suspend fun write(content: String) {
        writeBuffer.writeText(content)
    }

    override suspend fun write(content: String, offset: Int, length: Int) {
        writeBuffer.writeText(text = content, fromIndex = offset, toIndex = offset + length)
    }
}
