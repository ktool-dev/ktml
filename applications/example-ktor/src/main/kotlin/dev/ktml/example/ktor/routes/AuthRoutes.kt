package dev.ktml.example.ktor.routes

import dev.ktml.example.ktor.data.SampleData
import dev.ktml.example.ktor.models.UserSession
import dev.ktml.example.ktor.plugins.getCurrentUser
import dev.ktml.ktor.respondKtml
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import java.net.URLEncoder

fun Route.configureAuthRoutes() {
    // GET /login - Show login page
    get("/login") {
        val currentUser = call.getCurrentUser()
        if (currentUser != null) {
            call.respondRedirect("/")
            return@get
        }

        val error = call.request.queryParameters["error"]
        call.respondKtml(
            path = "pages/login",
            model = mapOf(
                "title" to "Login - Task Manager",
                "error" to error
            )
        )
    }

    // POST /login - Process login
    post("/login") {
        val params = call.receiveParameters()
        val email = params["email"]?.trim() ?: ""
        val password = params["password"] ?: ""

        if (email.isBlank() || password.isBlank()) {
            call.respondRedirect("/login?error=${URLEncoder.encode("Email and password are required", "UTF-8")}")
            return@post
        }

        val user = SampleData.validateCredentials(email, password)
        if (user != null) {
            call.sessions.set(UserSession(userId = user.id))
            call.respondRedirect("/")
        } else {
            call.respondRedirect("/login?error=${URLEncoder.encode("Invalid email or password", "UTF-8")}")
        }
    }

    // GET /register - Show registration page
    get("/register") {
        val currentUser = call.getCurrentUser()
        if (currentUser != null) {
            call.respondRedirect("/")
            return@get
        }

        val error = call.request.queryParameters["error"]
        call.respondKtml(
            path = "pages/register",
            model = mapOf(
                "title" to "Register - Task Manager",
                "error" to error
            )
        )
    }

    // POST /register - Process registration
    post("/register") {
        val params = call.receiveParameters()
        val name = params["name"]?.trim() ?: ""
        val email = params["email"]?.trim() ?: ""
        val password = params["password"] ?: ""
        val confirmPassword = params["confirmPassword"] ?: ""

        // Validate input
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            call.respondRedirect("/register?error=${URLEncoder.encode("All fields are required", "UTF-8")}")
            return@post
        }

        if (password != confirmPassword) {
            call.respondRedirect("/register?error=${URLEncoder.encode("Passwords do not match", "UTF-8")}")
            return@post
        }

        if (password.length < 6) {
            call.respondRedirect("/register?error=${URLEncoder.encode("Password must be at least 6 characters", "UTF-8")}")
            return@post
        }

        // Create user
        val user = SampleData.createUser(name, email, password)
        if (user == null) {
            call.respondRedirect("/register?error=${URLEncoder.encode("Email already exists", "UTF-8")}")
            return@post
        }

        // Log in the new user
        call.sessions.set(UserSession(userId = user.id))
        call.respondRedirect("/")
    }

    // POST /logout - Clear session and redirect
    post("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }
}

