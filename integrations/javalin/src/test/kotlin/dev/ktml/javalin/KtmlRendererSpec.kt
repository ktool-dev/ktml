package dev.ktml.javalin

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.ServletOutputStream
import jakarta.servlet.WriteListener
import java.io.StringWriter

class KtmlRendererSpec : BddSpec({
    val templates = mutableMapOf<String, Content>()
    val tags = mutableListOf<TagDefinition>()
    val queryParams = mutableMapOf<String, List<String>>()
    val pathParams = mutableMapOf<String, String>()
    JavalinKtml.init(object : KtmlRegistry {
        override val templates: Map<String, Content> get() = templates
        override val tags: List<TagDefinition> get() = tags
    })
    val ctx = mockk<io.javalin.http.Context>()
    val out = StringOutputStream()

    beforeEach {
        clearMocks(ctx)
        every { ctx.queryParamMap() } returns queryParams
        every { ctx.pathParamMap() } returns pathParams
        every { ctx.contentType(any<String>()) } returns ctx
        every { ctx.outputStream() } returns out
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
        ctx.ktml("myTemplate")

        Then
        out.toString() shouldBe "Hello World"
        verify { ctx.contentType("text/plain; charset=utf-8") }
    }

    "should render page with model data" {
        Given
        templates["greeting"] = {
            val name = required<String>("name")
            raw("Hello $name")
        }

        When
        ctx.ktml("greeting", mapOf("name" to "Test"))

        Then
        out.toString() shouldBe "Hello Test"
    }

    "passes query params to template" {
        Given
        templates["queryTest"] = {
            val name = queryParam("name")
            raw("Query: $name")
        }
        queryParams["name"] = listOf("test")

        When
        ctx.ktml("queryTest")

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
        ctx.ktml("pathTest")

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
