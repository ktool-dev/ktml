package dev.ktml.example.ktor.routes

import dev.ktml.example.ktor.data.SampleData
import dev.ktml.example.ktor.plugins.requireAuth
import dev.ktml.ktor.respondKtml
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureProfileRoutes() {
    // GET /profile - Show current user's profile
    get("/profile") {
        val user = call.requireAuth()
        call.respondKtml(
            path = "pages/profile",
            model = mapOf(
                "title" to "My Profile - Task Manager",
                "user" to user,
                "viewUser" to user
            )
        )
    }

    // GET /profile/edit - Show profile edit form
    get("/profile/edit") {
        val user = call.requireAuth()
        call.respondKtml(
            path = "pages/profile-edit",
            model = mapOf(
                "title" to "Edit Profile - Task Manager",
                "user" to user
            )
        )
    }

    // POST /profile/edit - Update profile (redirect to profile on success)
    post("/profile/edit") {
        val user = call.requireAuth()
        val params = call.receiveParameters()
        
        val name = params["name"]?.trim() ?: ""
        val email = params["email"]?.trim() ?: ""
        val bio = params["bio"]?.trim()

        // Validate input
        if (name.isBlank() || email.isBlank()) {
            call.respondText("Name and email are required", status = HttpStatusCode.BadRequest)
            return@post
        }

        // Check if email is taken by another user
        val existingUser = SampleData.findUserByEmail(email)
        if (existingUser != null && existingUser.id != user.id) {
            call.respondText("Email already in use by another user", status = HttpStatusCode.BadRequest)
            return@post
        }

        // Update user
        val updatedUser = user.copy(
            name = name,
            email = email,
            bio = bio
        )
        
        if (SampleData.updateUser(updatedUser)) {
            call.response.headers.append("HX-Redirect", "/profile")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respondText("Failed to update profile", status = HttpStatusCode.InternalServerError)
        }
    }

    // GET /users/{id} - View any user's public profile
    get("/users/{id}") {
        val currentUser = call.requireAuth()
        val userId = call.parameters["id"]?.toIntOrNull()
        val viewUser = userId?.let { SampleData.findUserById(it) }

        if (viewUser == null) {
            call.respondText("User not found", status = HttpStatusCode.NotFound)
            return@get
        }

        call.respondKtml(
            path = "pages/profile",
            model = mapOf(
                "title" to "${viewUser.name} - Task Manager",
                "user" to currentUser,
                "viewUser" to viewUser
            )
        )
    }
}
