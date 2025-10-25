package dev.ktml.javalin

import dev.ktml.*
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
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
            try {
                engine.writePage(ktmlContext, path)
            } catch (e: CompileException) {
                e.printStackTrace()
                val context = Context(out, mapOf("exception" to e))
                DefaultKtmlRegistry.templates["compile-exception"]?.invoke(context)
            }
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
