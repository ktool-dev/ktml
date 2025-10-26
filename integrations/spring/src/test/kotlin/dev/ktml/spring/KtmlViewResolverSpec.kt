package dev.ktml.spring

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerMapping
import java.io.StringWriter
import java.util.*

class KtmlViewResolverSpec : BddSpec({
    val templates = mutableMapOf<String, Content>()
    val tags = mutableListOf<TagDefinition>()
    val queryParams = mutableMapOf<String, Array<String>>()
    val pathParams = mutableMapOf<String, String>()
    val resolver = KtmlViewResolver(object : KtmlRegistry {
        override val templates: Map<String, Content> get() = templates
        override val tags: List<TagDefinition> get() = tags
    })
    val local = Locale.US
    val request = mockk<HttpServletRequest>()
    val response = mockk<HttpServletResponse>()
    val out = StringOutputStream()

    beforeEach {
        clearMocks(request, response)
        every { request.parameterMap } returns queryParams
        every { request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) } returns pathParams
        every { response.contentType = any<String>() } just Runs
        every { response.outputStream } returns out
        out.clear()
        templates.clear()
        tags.clear()
        queryParams.clear()
        pathParams.clear()
    }

    "should render page using engine" {
        Given
        templates["myTemplate"] = {
            raw("Hello World")
        }

        When
        resolver.resolveViewName("myTemplate", local)?.render(null, request, response)

        Then
        out.toString() shouldBe "Hello World"
        verify { response.contentType = "text/html;charset=UTF-8" }
    }

    "should render page with model data" {
        Given
        templates["greeting"] = {
            val name = required<String>("name")
            raw("Hello $name")
        }

        When
        resolver.resolveViewName("greeting", local)?.render(mutableMapOf("name" to "Test"), request, response)

        Then
        out.toString() shouldBe "Hello Test"
    }

    "passes query params to template" {
        Given
        templates["queryTest"] = {
            val name = queryParam("name")
            raw("Query: $name")
        }
        queryParams["name"] = arrayOf("test")

        When
        resolver.resolveViewName("queryTest", local)?.render(null, request, response)

        Then
        out.toString() shouldBe "Query: test"
    }

    "passes path params to template" {
        Given
        templates["pathTest"] = {
            val id = pathParam("id")
            raw("Path: $id")
        }
        pathParams["id"] = "123"

        When
        resolver.resolveViewName("pathTest", local)?.render(null, request, response)

        Then
        out.toString() shouldBe "Path: 123"
    }
})

private class StringOutputStream() : ServletOutputStream() {
    private var out = StringWriter()

    override fun isReady() = true

    override fun setWriteListener(writeListener: WriteListener?) {
        writeListener?.onWritePossible()
    }

    override fun write(b: Int) {
        out.write(b)
    }

    fun clear() {
        out = StringWriter()
    }

    override fun toString() = out.toString()
}
