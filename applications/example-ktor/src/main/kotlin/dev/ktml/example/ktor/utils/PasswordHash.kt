package dev.ktml.example.ktor.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordHash {
    /**
     * Hash a password using BCrypt
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    /**
     * Verify a password against a BCrypt hash
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}
