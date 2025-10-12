package dev.ktml.javalin

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import kotlinx.coroutines.runBlocking
import java.io.OutputStream
import java.io.OutputStreamWriter
import io.javalin.http.Context as JavalinContext

object JavalinKtml {
    private lateinit var engine: KtmlEngine

    fun init(ktmlRegistry: KtmlRegistry) {
        engine = KtmlEngine(ktmlRegistry)
    }

    internal fun write(ctx: JavalinContext, path: String, model: Map<String, Any?> = emptyMap()) {
        require(::engine.isInitialized) { "You must initialize KtmlEngine by calling JavalinKtml.init() first!" }
        
        ctx.contentType("text/plain; charset=utf-8")

        val ktmlContext = Context(
            writer = JavalinContentWriter(ctx.outputStream()),
            model = model,
            queryParams = ctx.queryParamMap(),
            pathParams = ctx.pathParamMap().mapValues { listOf(it.value) }
        )

        runBlocking {
            engine.writePage(ktmlContext, path)
        }
    }
}

fun JavalinContext.renderKtml(path: String, model: Map<String, Any?> = emptyMap()) = apply {
    JavalinKtml.write(this, path, model)
}

class JavalinContentWriter(outputStream: OutputStream) : ContentWriter {
    private val writer = OutputStreamWriter(outputStream)

    override suspend fun write(content: String) {
        writer.write(content)
    }
}
