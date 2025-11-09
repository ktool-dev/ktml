package dev.ktml.example.ktor

import dev.ktml.example.ktor.plugins.configureSessions
import dev.ktml.example.ktor.routes.configureAdminRoutes
import dev.ktml.example.ktor.routes.configureAuthRoutes
import dev.ktml.example.ktor.routes.configureProfileRoutes
import dev.ktml.example.ktor.routes.configureTaskRoutes
import dev.ktml.ktor.KtmlPlugin
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureSessions()
        install(KtmlPlugin)
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        configureTaskRoutes()
        configureAuthRoutes()
        configureProfileRoutes()
        configureAdminRoutes()
    }
}
