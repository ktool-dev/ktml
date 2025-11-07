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
            call.respondKtml(
                path = "fragments/auth/login-form",
                model = mapOf("error" to "Email and password are required")
            )
            return@post
        }

        val user = SampleData.validateCredentials(email, password)
        if (user != null) {
            call.sessions.set(UserSession(userId = user.id))
            call.response.headers.append("HX-Redirect", "/")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respondKtml(
                path = "fragments/auth/login-form",
                model = mapOf("error" to "Invalid email or password")
            )
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
            call.respondKtml(
                path = "fragments/auth/register-form",
                model = mapOf(
                    "error" to "All fields are required",
                    "name" to name,
                    "email" to email
                )
            )
            return@post
        }

        if (password != confirmPassword) {
            call.respondKtml(
                path = "fragments/auth/register-form",
                model = mapOf(
                    "error" to "Passwords do not match",
                    "name" to name,
                    "email" to email
                )
            )
            return@post
        }

        if (password.length < 6) {
            call.respondKtml(
                path = "fragments/auth/register-form",
                model = mapOf(
                    "error" to "Password must be at least 6 characters",
                    "name" to name,
                    "email" to email
                )
            )
            return@post
        }

        // Create user
        val user = SampleData.createUser(name, email, password)
        if (user == null) {
            call.respondKtml(
                path = "fragments/auth/register-form",
                model = mapOf(
                    "error" to "Email already exists",
                    "name" to name,
                    "email" to email
                )
            )
            return@post
        }

        // Log in the new user
        call.sessions.set(UserSession(userId = user.id))
        call.response.headers.append("HX-Redirect", "/")
        call.respond(HttpStatusCode.OK)
    }

    // POST /logout - Clear session and redirect
    post("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }
}

