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
import java.io.PrintWriter
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
            writer = ServletContentWriter(response.writer),
            model = model ?: emptyMap(),
            queryParams = request.parameterMap.mapValues { it.value.toList() },
            pathParams = request.urlParameters
        )

        runBlocking {
            engine.writePage(context, path)
        }
    }

    @Suppress("unchecked_cast")
    private val HttpServletRequest.urlParameters: Map<String, List<String>>
        get() = (getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>)
            ?.mapValues { listOf(it.value) } ?: emptyMap()
}

class ServletContentWriter(private val writer: PrintWriter) : ContentWriter {
    override suspend fun write(content: String) {
        writer.write(content)
    }
}
