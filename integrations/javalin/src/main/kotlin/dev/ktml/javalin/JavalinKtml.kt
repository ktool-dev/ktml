package dev.ktml.javalin

import dev.ktml.*
import kotlinx.coroutines.runBlocking
import java.io.OutputStream
import io.javalin.http.Context as JavalinContext

object JavalinKtml {
    private lateinit var engine: KtmlEngine

    fun init(ktmlRegistry: KtmlRegistry? = null) {
        engine = KtmlEngine(ktmlRegistry ?: findKtmlRegistry())
    }

    internal fun write(ctx: JavalinContext, path: String, model: Map<String, Any?> = emptyMap()) {
        require(::engine.isInitialized) { "You must initialize KtmlEngine by calling JavalinKtml.init() first!" }

        ctx.contentType("text/html; charset=utf-8")
        val out = OutputStreamWriter(ctx.outputStream())

        val ktmlContext = Context(
            writer = out,
            model = model,
            queryParams = ctx.queryParamMap(),
            pathParams = ctx.pathParamMap(),
        )

        runBlocking {
            engine.writePage(ktmlContext, path)
            out.flush()
        }
    }
}

fun JavalinContext.ktml(path: String, model: Map<String, Any?> = emptyMap()) = apply {
    JavalinKtml.write(this, path, model)
}

private class OutputStreamWriter(out: OutputStream) : ContentWriter {
    private val writer = out.bufferedWriter(Charsets.UTF_8)

    override suspend fun write(content: String, offset: Int, length: Int) {
        writer.write(content, offset, length)
    }

    fun flush() {
        writer.flush()
    }
}
