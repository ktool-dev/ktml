package dev.ktml.example.http4k

import dev.ktml.http4k.Http4kKtml
import dev.ktml.http4k.ktml
import io.github.oshai.kotlinlogging.KotlinLogging
import org.http4k.core.Method
import org.http4k.lens.Path
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

private val log = KotlinLogging.logger {}

fun main() {
    Http4kKtml.init()

    val app = routes(
        "/" bind Method.GET to { request ->
            request.ktml(
                path = "index",
                model = mapOf(
                    "title" to "Welcome to KTML with Http4k"
                )
            )
        },
        "/users/{name}" bind Method.GET to { request ->
            val nameLens = Path.of("name")
            val name = nameLens(request)
            request.ktml(
                path = "user",
                model = mapOf(
                    "name" to name,
                    "items" to listOf("Item 1", "Item 2", "Item 3")
                )
            )
        }
    )

    app.asServer(Jetty(8080)).start()

    log.info { "Started server on http://localhost:8080" }
}
