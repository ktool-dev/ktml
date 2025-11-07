package dev.ktml.example.ktor.routes

import dev.ktml.example.ktor.data.SampleData
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureAdminRoutes() {
    // Admin page (placeholder, only for admins)
    get("/admin") {
        if (!SampleData.currentUser.isAdmin) {
            call.respondText("Access denied", status = HttpStatusCode.Forbidden)
            return@get
        }
        call.respondText("Admin panel - Coming soon!")
    }
}
