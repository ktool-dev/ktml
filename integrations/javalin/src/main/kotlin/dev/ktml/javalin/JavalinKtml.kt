package dev.ktml.javalin

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import kotlinx.coroutines.runBlocking
import java.io.OutputStream
import io.javalin.http.Context as JavalinContext

object JavalinKtml {
    private lateinit var engine: KtmlEngine

    fun init(ktmlRegistry: KtmlRegistry) {
        engine = KtmlEngine(ktmlRegistry)
    }

    internal fun write(ctx: JavalinContext, path: String, model: Map<String, Any?> = emptyMap()) {
        require(::engine.isInitialized) { "You must initialize KtmlEngine by calling JavalinKtml.init() first!" }

        ctx.contentType("text/html; charset=utf-8")

        val ktmlContext = Context(
            writer = OutputStreamWriter(ctx.outputStream()),
            model = model,
            queryParams = ctx.queryParamMap(),
            pathParams = ctx.pathParamMap(),
        )

        runBlocking {
            engine.writePage(ktmlContext, path)
        }
    }
}

fun JavalinContext.ktml(path: String, model: Map<String, Any?> = emptyMap()) = apply {
    JavalinKtml.write(this, path, model)
}

private class OutputStreamWriter(private val out: OutputStream) : ContentWriter {
    override suspend fun write(content: String) {
        out.write(content.encodeToByteArray())
    }
}
