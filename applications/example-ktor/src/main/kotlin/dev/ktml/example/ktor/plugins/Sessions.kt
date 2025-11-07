package dev.ktml.example.ktor.plugins

import dev.ktml.example.ktor.data.SampleData
import dev.ktml.example.ktor.models.User
import dev.ktml.example.ktor.models.UserSession
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

/**
 * Configure Ktor Sessions plugin with cookie-based storage
 */
fun Application.configureSessions() {
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            cookie.maxAgeInSeconds = 86400 // 24 hours
        }
    }
}

/**
 * Get the current user session from cookies
 */
fun ApplicationCall.getUserSession(): UserSession? {
    return sessions.get<UserSession>()
}

/**
 * Get the current authenticated user from the session
 */
fun ApplicationCall.getCurrentUser(): User? {
    val session = getUserSession() ?: return null
    return SampleData.findUserById(session.userId)
}

/**
 * Require authentication. Redirects to /login if not authenticated.
 */
suspend fun ApplicationCall.requireAuth(): User {
    val user = getCurrentUser()
    if (user == null) {
        respondRedirect("/login")
        throw AuthenticationRequiredException()
    }
    return user
}

/**
 * Exception thrown when authentication is required but not present
 */
class AuthenticationRequiredException : Exception("Authentication required")
