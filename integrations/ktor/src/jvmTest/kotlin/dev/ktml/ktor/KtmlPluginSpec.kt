package dev.ktml.ktor

import dev.ktml.Content
import dev.ktml.KtmlRegistry
import dev.ktml.TagDefinition
import dev.ktool.kotest.BddSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.utils.io.streams.*
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayOutputStream

class KtmlPluginSpec : BddSpec({
    val routingCall = mockk<RoutingCall>()
    val templates = mutableMapOf<String, Content>()
    val tags = mutableListOf<TagDefinition>()
    val queryParams = mutableMapOf<String, List<String>>()
    val pathParams = mutableMapOf<String, List<String>>()
    val out = StringBuilder()

    beforeEach {
        clearMocks(routingCall)
        every { routingCall.queryParameters } answers { ParametersImpl(values = queryParams) }
        every { routingCall.pathParameters } answers { ParametersImpl(values = pathParams) }
        every { routingCall.application.attributes } returns Attributes()
        coEvery {
            routingCall.respond(any<ChannelWriterContent>(), any())
        } coAnswers {
            out.append(ByteArrayOutputStream().also {
                val channel = it.asByteWriteChannel()
                arg<ChannelWriterContent>(0).writeTo(channel)
                channel.flush()
            })
        }
        KtmlPlugin.install(routingCall.application) {
            registry = object : KtmlRegistry {
                override operator fun get(path: String): Content? = templates[path]
                override val tags: List<TagDefinition> get() = tags
            }
        }
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
        routingCall.respondKtml("myTemplate")

        Then
        out.toString() shouldBe "Hello World"
    }

    "should render page with model data" {
        Given
        templates["greeting"] = {
            val name = required<String>("name")
            raw("Hello $name")
        }

        When
        routingCall.respondKtml("greeting", mapOf("name" to "Test"))

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
        routingCall.respondKtml("queryTest")

        Then
        out.toString() shouldBe "Query: test"
    }

    "passes path params to template" {
        Given
        templates["pathTest"] = {
            val id = pathParam("id")
            raw("Path: $id")
        }
        pathParams["id"] = listOf("123")

        When
        routingCall.respondKtml("pathTest")

        Then
        out.toString() shouldBe "Path: 123"
    }
})