package dev.ktml.http4k

import dev.ktml.*
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
import kotlinx.coroutines.runBlocking
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.io.StringWriter

object Http4kKtml {
    private lateinit var engine: KtmlEngine

    fun init(templatePackage: String = DEFAULT_PACKAGE) = init(findKtmlRegistry(templatePackage))

    fun init(ktmlRegistry: KtmlRegistry) {
        engine = KtmlEngine(ktmlRegistry)
    }

    internal fun write(
        request: Request,
        path: String,
        model: Map<String, Any?> = emptyMap(),
        status: Status = Status.OK
    ): Response {
        require(::engine.isInitialized) { "You must initialize KTML by calling Http4kKtml.init() first!" }

        val writer = KtmlWriter()

        val queryParams = request.uri.query
            .split("&")
            .filter { it.isNotBlank() }
            .mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.isNotEmpty()) {
                    val key = parts[0]
                    val value = if (parts.size > 1) parts[1] else ""
                    key to value
                } else null
            }
            .groupBy({ it.first }, { it.second })

        val ktmlContext = Context(writer = writer, model = model, queryParams = queryParams)

        runBlocking {
            try {
                engine.writePage(ktmlContext, path)
            } catch (e: CompileException) {
                e.printStackTrace()
                val context = Context(writer, mapOf("exception" to e))
                DefaultKtmlRegistry.templates["compile-exception"]?.invoke(context)
            }
            writer.flush()
        }

        return Response(status)
            .header("Content-Type", "text/html; charset=utf-8")
            .body(writer.toString())
    }
}

fun Request.ktml(
    path: String,
    model: Map<String, Any?> = emptyMap(),
    status: Status = Status.OK
): Response = Http4kKtml.write(this, path, model, status)

private class KtmlWriter() : ContentWriter {
    private val writer = StringWriter()

    override suspend fun write(content: String, offset: Int, length: Int) {
        writer.write(content, offset, length)
    }

    fun flush() {
        writer.flush()
    }

    override fun toString() = writer.toString()
}
