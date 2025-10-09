package dev.ktml.web


import dev.ktml.Content
import dev.ktml.ContentWriter
import dev.ktml.Context
import dev.ktml.KtmlRegistry
import dev.ktml.templates.DefaultKtmlRegistry
import dev.ktml.templates.writeCompileException
import dev.ktml.templates.writeDefaultError
import dev.ktml.templates.writeDefaultNotFound
import dev.ktml.util.CompileException
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
    private var application: Application? = null

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
                exception<CompileException> { call, cause ->
                    call.respondKtml(
                        model = mapOf("exception" to cause),
                        function = { writeCompileException() },
                        status = HttpStatusCode.InternalServerError
                    )
                }

                exception<Throwable> { call, cause ->
                    if (ktmlRegistry.hasPage(ERROR_TEMPLATE_PATH)) {
                        call.respondKtml(
                            path = ERROR_TEMPLATE_PATH,
                            model = mapOf("error" to cause),
                            status = HttpStatusCode.InternalServerError
                        )
                    } else {
                        call.respondKtml(
                            function = { writeDefaultError() },
                            model = mapOf("error" to cause),
                            status = HttpStatusCode.InternalServerError
                        )
                    }
                }

                status(HttpStatusCode.NotFound) { call, status ->
                    if (ktmlRegistry.hasPage(NOT_FOUND_TEMPLATE_PATH)) {
                        call.respondKtml(path = NOT_FOUND_TEMPLATE_PATH, status = status)
                    } else {
                        call.respondKtml(function = { writeDefaultNotFound() }, status = HttpStatusCode.NotFound)
                    }
                }
            }
            application = this
            createRoutes(ktmlRegistry)
        }
        server?.start(wait = true)
    }

    fun reloadRoutes() {
        application?.let { app ->
            app.attributes.remove(AttributeKey("RoutingRoot"))
            app.createRoutes(ktmlRegistry)
        }
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
        val function = ktmlRegistry.pages[path] ?: error("Cannot find template at path: $path")
        respondKtml(function, status, model, queryParameters, pathParameters)
    }

    private suspend fun ApplicationCall.respondKtml(
        function: Content,
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
            Context(writer, model, queryParameters, pathParameters).function()
        }
    }

    private fun Application.createRoutes(ktmlRegistry: KtmlRegistry) {
        routing {
            logger.info { "Adding routes" }
            ktmlRegistry.pages.forEach { (path, _) ->
                if (DefaultKtmlRegistry.hasPage(path)) return@forEach

                val route = when {
                    path == "index" -> "/"
                    path.endsWith("/index") -> path.substringBeforeLast("/")
                    else -> path
                }.replacePathVariables()

                logger.info { "Adding rout $route" }

                get(route) {
                    call.respondKtml(
                        path = path,
                        queryParameters = call.queryParameters.toMap(),
                        pathParameters = call.pathParameters.toMap()
                    )
                }
            }
        }
    }

    private fun String.replacePathVariables() = split("/").joinToString("/") {
        if (it.startsWith("_")) "{${it.substring(1)}}" else it
    }
}
