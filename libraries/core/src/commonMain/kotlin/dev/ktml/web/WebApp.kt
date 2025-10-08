package dev.ktml.web


import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*

private val logger = KotlinLogging.logger { }
const val NOT_FOUND_TEMPLATE_PATH = "errors/not-found"
const val ERROR_TEMPLATE_PATH = "errors/error"

class WebApp(val ktmlRegistry: KtmlRegistry) {
    private var server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration>? = null

    fun start(serverPort: Int = 8080) {
        server = embeddedServer(CIO, environment = applicationEnvironment {
            log = object : Logger by KtorSimpleLogger("App") {
                override fun info(message: String) {
                    logger.info { message }
                }
            }
        }, configure = {
            connector {
                host = "localhost"
                port = serverPort
            }
        }) {
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    if (ktmlRegistry.hasPage(ERROR_TEMPLATE_PATH)) {
                        println("writing error page")
                        call.respondKtml(
                            path = ERROR_TEMPLATE_PATH,
                            status = HttpStatusCode.InternalServerError,
                            model = mapOf("error" to cause)
                        )
                    } else {
                        call.respondText(
                            text = "500: An unexpected error occurred: ${cause.message}",
                            status = HttpStatusCode.InternalServerError
                        )
                    }

                }

                status(HttpStatusCode.NotFound) { call, status ->
                    if (ktmlRegistry.hasPage(NOT_FOUND_TEMPLATE_PATH)) {
                        call.respondKtml(path = NOT_FOUND_TEMPLATE_PATH, status = status)
                    } else {
                        call.respond(status, "Resource not found.")
                    }
                }
            }
            createRoutes(ktmlRegistry)
        }
        logger.info { "Listening at: http://localhost:$serverPort/" }
        server?.start(wait = true)
    }

    fun stop() {
        server?.stop(100, 500)
    }

    private suspend fun RoutingCall.respondKtml(
        path: String,
        model: Map<String, Any?>,
        status: HttpStatusCode = HttpStatusCode.OK
    ) {
        respondKtml(
            path = path,
            model = model,
            status = status,
            queryParameters = queryParameters.toMap(),
            pathParameters = pathParameters.toMap()
        )
    }

    private suspend fun ApplicationCall.respondKtml(
        path: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        model: Map<String, Any?> = mapOf(),
        queryParameters: Map<String, List<String>> = mapOf(),
        pathParameters: Map<String, List<String>> = mapOf(),
    ) {
        respondBytesWriter(contentType = ContentType.Text.Html, status = status) {
            val writer = object : ContentWriter {
                override suspend fun write(content: String) {
                    writeStringUtf8(content)
                }
            }
            val function = ktmlRegistry.pages[path] ?: error("Cannot find template at path: $path")
            Context(writer, model, queryParameters, pathParameters).function()
        }
    }

    private fun Application.createRoutes(ktmlRegistry: KtmlRegistry) {
        routing {
            logger.info { "Adding routes" }
            ktmlRegistry.pages.forEach { (path, _) ->
                val route = when {
                    path == "index" -> "/"
                    path.endsWith("/index") -> path.substringBeforeLast("/")
                    else -> path
                }
                logger.info { "Adding path $path" }

                get(route.replace(pathRegex, "/{$1}")) {
                    call.respondBytesWriter(contentType = ContentType.Text.Html) {
                        val writer = object : ContentWriter {
                            override suspend fun write(content: String) {
                                writeStringUtf8(content)
                            }
                        }
                        val function = ktmlRegistry.pages[path] ?: error("Cannot find template at path: $path")
                        Context(
                            writer,
                            mapOf(),
                            call.queryParameters.toMap(),
                            call.pathParameters.toMap(),
                        ).function()
                    }
                }
            }
        }
    }
}

private val pathRegex = """/_([^/]+)""".toRegex()

