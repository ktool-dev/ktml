package dev.ktml.ktor

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*

class KtmlConfig {
    lateinit var registry: KtmlRegistry
}

val KtmlPlugin = createApplicationPlugin(name = "KTML", createConfiguration = ::KtmlConfig) {
    application.attributes.put(ktmlEngineKey, KtmlEngine(pluginConfig.registry))
}

private val ktmlEngineKey = AttributeKey<KtmlEngine>("KtmlEngine")

private suspend fun RoutingCall.respondKtml(
    path: String,
    model: Map<String, Any?>,
    status: HttpStatusCode = HttpStatusCode.OK
) {
    respondKtml(
        path = path,
        model = model,
        status = status,
        queryParameters = queryParameters.toMap(),
        pathParameters = pathParameters.toMap()
    )
}

private suspend fun ApplicationCall.respondKtml(
    path: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    model: Map<String, Any?> = mapOf(),
    queryParameters: Map<String, List<String>> = mapOf(),
    pathParameters: Map<String, List<String>> = mapOf(),
) {
    val engine = application.attributes[ktmlEngineKey]

    respondBytesWriter(contentType = ContentType.Text.Html, status = status) {
        val writer = object : ContentWriter {
            override suspend fun write(content: String) {
                writeStringUtf8(content)
            }
        }
        val context = Context(writer, model, queryParameters, pathParameters)
        engine.writePage(context, path)
    }
}
