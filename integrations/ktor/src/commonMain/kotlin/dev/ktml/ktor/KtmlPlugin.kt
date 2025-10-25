package dev.ktml.ktor

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.io.Sink

val ktmlEngineKey = AttributeKey<KtmlEngine>("KtmlEngine")

class KtmlConfig {
    var registry: KtmlRegistry? = null
}

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
    respondBytesWriter(contentType = ContentType.Text.Html, status = status) {
        try {
            val engine = application.attributes[ktmlEngineKey]
            val context = Context(KtmlWriter(writeBuffer), model, queryParameters, pathParameters)
            engine.writePage(context, path)
        } catch (e: CompileException) {
            val context = Context(KtmlWriter(writeBuffer), mapOf("exception" to e))
            DefaultKtmlRegistry.templates["compile-exception"]?.invoke(context)
        }
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
