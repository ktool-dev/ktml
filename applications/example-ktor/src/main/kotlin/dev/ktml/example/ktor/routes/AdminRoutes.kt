package dev.ktml.example.ktor.routes

import dev.ktml.example.ktor.plugins.requireAuth
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureAdminRoutes() {
    // Admin page (only for admins)
    get("/admin") {
        val user = call.requireAuth()
        if (!user.isAdmin) {
            call.respondText("Access denied", status = HttpStatusCode.Forbidden)
            return@get
        }
        call.respondText("Admin panel - Coming soon!")
    }
}
