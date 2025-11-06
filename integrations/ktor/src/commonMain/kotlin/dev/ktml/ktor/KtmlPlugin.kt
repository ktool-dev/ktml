package dev.ktml.ktor

import dev.ktml.*
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
    var templatePackage: String = DEFAULT_PACKAGE
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
        val context = Context(KtmlWriter(writeBuffer), model, queryParameters, pathParameters)
        application.attributes[ktmlEngineKey].writePage(context, path)
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
