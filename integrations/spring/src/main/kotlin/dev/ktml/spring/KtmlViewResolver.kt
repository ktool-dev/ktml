package dev.ktml.spring

import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlEngine
import dev.ktml.KtmlRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.runBlocking
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.View
import org.springframework.web.servlet.ViewResolver
import java.io.OutputStream
import java.util.*

class KtmlViewResolver(ktmlRegistry: KtmlRegistry) : ViewResolver {
    private val engine = KtmlEngine(ktmlRegistry)

    override fun resolveViewName(viewName: String, locale: Locale) = KtmlView(engine, viewName)
}

class KtmlView(private val engine: KtmlEngine, private val path: String) : View {
    override fun getContentType(): String = "text/html;charset=UTF-8"

    override fun render(model: MutableMap<String, *>?, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = contentType

        val context = Context(
            writer = OutputStreamWriter(response.outputStream),
            model = model ?: emptyMap(),
            queryParams = request.parameterMap.mapValues { it.value.toList() },
            pathParams = request.urlParameters
        )

        runBlocking {
            engine.writePage(context, path)
        }
    }

    @Suppress("unchecked_cast")
    private val HttpServletRequest.urlParameters: Map<String, String>
        get() = (getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>) ?: emptyMap()
}

private class OutputStreamWriter(private val out: OutputStream) : ContentWriter {
    override suspend fun write(content: String) {
        out.write(content.encodeToByteArray())
    }
}
