package dev.ktml.example.ktor

import dev.ktml.ktor.KtmlPlugin
import dev.ktml.ktor.respondKtml
import dev.ktml.templates.KtmlRegistry
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        install(KtmlPlugin) {
            registry = KtmlRegistry
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondKtml(
                path = "index",
                model = mapOf(
                    "title" to "Welcome to KTML with Ktor Native"
                )
            )
        }
        get("/users/{name}") {
            call.respondKtml(
                path = "user",
                model = mapOf(
                    "name" to call.parameters["name"],
                    "items" to listOf("Item 1", "Item 2", "Item 3")
                )
            )
        }
    }
}
