package dev.ktml.example.ktor.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureProfileRoutes() {
    // Profile page (placeholder)
    get("/profile") {
        call.respondText("Profile page - Coming soon!")
    }
}
