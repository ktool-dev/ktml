package dev.ktml.example.ktor.models

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val isAdmin: Boolean = false,
    val avatarColor: String = "primary",
    val bio: String? = null,
    val createdAt: LocalDateTime,
    val lastLogin: LocalDateTime? = null
)

@kotlinx.serialization.Serializable
data class UserSession(val userId: Int)

enum class Priority(val displayName: String, val badgeClass: String) {
    LOW("Low", "success"),
    MEDIUM("Medium", "warning"),
    HIGH("High", "danger")
}

enum class Status(val displayName: String, val badgeClass: String) {
    TODO("To Do", "secondary"),
    IN_PROGRESS("In Progress", "primary"),
    DONE("Done", "success")
}

data class Task(
    val id: Int,
    val title: String,
    val description: String,
    val status: Status,
    val priority: Priority,
    val assignee: User?,
    val createdAt: LocalDateTime,
    val dueDate: LocalDateTime?,
    val tags: List<String> = emptyList()
) {
    fun isOverdue(): Boolean {
        return dueDate?.let { it.isBefore(LocalDateTime.now()) && status != Status.DONE } ?: false
    }

    fun formattedDueDate(): String? {
        return dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
    }

    fun formattedCreatedAt(): String {
        return createdAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    }
}

data class TaskStats(
    val total: Int,
    val todo: Int,
    val inProgress: Int,
    val done: Int,
    val overdue: Int
)
