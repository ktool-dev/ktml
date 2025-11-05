package dev.ktml.spring

import dev.ktml.*
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.util.CompileException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.core.Ordered
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
import java.io.OutputStream
import java.util.*

class KtmlViewResolver(templatePackage: String? = null, ktmlRegistry: KtmlRegistry? = null) : ViewResolver, Ordered {
    private val registry = (ktmlRegistry ?: findKtmlRegistry(templatePackage ?: DEFAULT_PACKAGE))
    private val engine = KtmlEngine(registry)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun resolveViewName(viewName: String, locale: Locale): View? {
        return try {
            if (registry.templates.containsKey(viewName)) KtmlView(engine, viewName) else null
        } catch (_: CompileException) {
            // If there's a compiler exception, we need to return the view so it renders the compiler error page
            KtmlView(engine, viewName)
        }
    }
}

class KtmlView(private val engine: KtmlEngine, private val path: String) : View {
    override fun getContentType(): String = "text/html;charset=UTF-8"

    override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = contentType
        val out = OutputStreamWriter(response.outputStream)

        val ktmlContext = Context(
            writer = out,
            model = model ?: emptyMap(),
            queryParams = request.parameterMap.mapValues { it.value.toList() },
            pathParams = request.urlParameters
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

    @Suppress("unchecked_cast")
    private val HttpServletRequest.urlParameters: Map<String, String>
        get() = (getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>) ?: emptyMap()
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
