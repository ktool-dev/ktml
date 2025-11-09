package dev.ktml.example.ktor.data

import dev.ktml.example.ktor.models.*
import dev.ktml.example.ktor.utils.PasswordHash
import java.time.LocalDateTime

object SampleData {
    private var nextUserId = 5

    // Default password for all users is "password123"
    val users = mutableListOf(
        User(
            1, "Alice Johnson", "test@test.com",
            passwordHash = PasswordHash.hashPassword("password123"),
            isAdmin = true, avatarColor = "primary",
            createdAt = LocalDateTime.now().minusMonths(6)
        ),
        User(
            2, "Bob Smith", "bob@example.com",
            passwordHash = PasswordHash.hashPassword("password123"),
            isAdmin = false, avatarColor = "success",
            createdAt = LocalDateTime.now().minusMonths(4)
        ),
        User(
            3, "Charlie Brown", "charlie@example.com",
            passwordHash = PasswordHash.hashPassword("password123"),
            isAdmin = false, avatarColor = "warning",
            createdAt = LocalDateTime.now().minusMonths(3)
        ),
        User(
            4, "Diana Prince", "diana@example.com",
            passwordHash = PasswordHash.hashPassword("password123"),
            isAdmin = false, avatarColor = "info",
            createdAt = LocalDateTime.now().minusMonths(2)
        )
    )

    /**
     * Create a new user. Returns null if email already exists.
     */
    fun createUser(name: String, email: String, password: String, isAdmin: Boolean = false): User? {
        if (users.any { it.email.equals(email, ignoreCase = true) }) {
            return null
        }

        val user = User(
            id = nextUserId++,
            name = name,
            email = email,
            passwordHash = PasswordHash.hashPassword(password),
            isAdmin = isAdmin,
            createdAt = LocalDateTime.now()
        )
        users.add(user)
        return user
    }

    /**
     * Find a user by email (case-insensitive)
     */
    fun findUserByEmail(email: String): User? {
        return users.find { it.email.equals(email, ignoreCase = true) }
    }

    /**
     * Find a user by ID
     */
    fun findUserById(id: Int): User? {
        return users.find { it.id == id }
    }

    /**
     * Validate login credentials
     */
    fun validateCredentials(email: String, password: String): User? {
        val user = findUserByEmail(email) ?: return null
        return if (PasswordHash.verifyPassword(password, user.passwordHash)) {
            // Update last login
            val updatedUser = user.copy(lastLogin = LocalDateTime.now())
            updateUser(updatedUser)
            updatedUser
        } else {
            null
        }
    }

    /**
     * Update an existing user
     */
    fun updateUser(user: User): Boolean {
        val index = users.indexOfFirst { it.id == user.id }
        return if (index != -1) {
            users[index] = user
            true
        } else {
            false
        }
    }

    val tasks = listOf(
        Task(
            id = 1,
            title = "Implement user authentication",
            description = "Add JWT-based authentication to the API endpoints with proper security measures.",
            status = Status.IN_PROGRESS,
            priority = Priority.HIGH,
            assignee = users[0],
            createdAt = LocalDateTime.now().minusDays(5),
            dueDate = LocalDateTime.now().plusDays(2),
            tags = listOf("backend", "security")
        ),
        Task(
            id = 2,
            title = "Design dashboard UI",
            description = "Create mockups for the main dashboard showing key metrics and user activity.",
            status = Status.TODO,
            priority = Priority.MEDIUM,
            assignee = users[1],
            createdAt = LocalDateTime.now().minusDays(3),
            dueDate = LocalDateTime.now().plusDays(5),
            tags = listOf("design", "frontend")
        ),
        Task(
            id = 3,
            title = "Fix login page responsiveness",
            description = "The login page doesn't display correctly on mobile devices. Need to update CSS.",
            status = Status.TODO,
            priority = Priority.HIGH,
            assignee = users[2],
            createdAt = LocalDateTime.now().minusDays(2),
            dueDate = LocalDateTime.now().plusDays(1),
            tags = listOf("bug", "frontend", "mobile")
        ),
        Task(
            id = 4,
            title = "Write API documentation",
            description = "Document all REST API endpoints with examples and response formats.",
            status = Status.DONE,
            priority = Priority.MEDIUM,
            assignee = users[0],
            createdAt = LocalDateTime.now().minusDays(10),
            dueDate = LocalDateTime.now().minusDays(1),
            tags = listOf("documentation")
        ),
        Task(
            id = 5,
            title = "Set up CI/CD pipeline",
            description = "Configure GitHub Actions for automated testing and deployment.",
            status = Status.IN_PROGRESS,
            priority = Priority.HIGH,
            assignee = users[3],
            createdAt = LocalDateTime.now().minusDays(4),
            dueDate = LocalDateTime.now().plusDays(3),
            tags = listOf("devops", "automation")
        ),
        Task(
            id = 6,
            title = "Optimize database queries",
            description = "Profile slow queries and add proper indexes to improve performance.",
            status = Status.TODO,
            priority = Priority.LOW,
            assignee = users[1],
            createdAt = LocalDateTime.now().minusDays(1),
            dueDate = LocalDateTime.now().plusDays(7),
            tags = listOf("backend", "performance")
        ),
        Task(
            id = 7,
            title = "Update dependencies",
            description = "Update all npm packages to latest stable versions and test for compatibility.",
            status = Status.TODO,
            priority = Priority.LOW,
            assignee = null,
            createdAt = LocalDateTime.now().minusDays(1),
            dueDate = null,
            tags = listOf("maintenance")
        ),
        Task(
            id = 8,
            title = "Implement file upload feature",
            description = "Allow users to upload profile pictures and attachments to tasks.",
            status = Status.DONE,
            priority = Priority.MEDIUM,
            assignee = users[2],
            createdAt = LocalDateTime.now().minusDays(15),
            dueDate = LocalDateTime.now().minusDays(3),
            tags = listOf("feature", "backend")
        ),
        Task(
            id = 9,
            title = "Fix email notification bug",
            description = "Emails are not being sent when tasks are assigned. Investigate SMTP configuration.",
            status = Status.IN_PROGRESS,
            priority = Priority.HIGH,
            assignee = users[0],
            createdAt = LocalDateTime.now().minusDays(1),
            dueDate = LocalDateTime.now(),
            tags = listOf("bug", "critical")
        ),
        Task(
            id = 10,
            title = "Add dark mode support",
            description = "Implement a dark theme toggle and save user preference.",
            status = Status.TODO,
            priority = Priority.MEDIUM,
            assignee = users[1],
            createdAt = LocalDateTime.now(),
            dueDate = LocalDateTime.now().plusDays(10),
            tags = listOf("frontend", "enhancement")
        )
    )

    fun getTaskById(id: Int): Task? = tasks.find { it.id == id }

    fun getTasksByStatus(status: Status): List<Task> = tasks.filter { it.status == status }

    fun filterTasks(
        status: Status? = null,
        priority: Priority? = null,
        assigneeId: Int? = null,
        searchQuery: String? = null
    ): List<Task> {
        return tasks.filter { task ->
            (status == null || task.status == status) &&
                    (priority == null || task.priority == priority) &&
                    (assigneeId == null || task.assignee?.id == assigneeId) &&
                    (searchQuery.isNullOrBlank() ||
                            task.title.contains(searchQuery, ignoreCase = true) ||
                            task.description.contains(searchQuery, ignoreCase = true))
        }
    }

    fun getStats(): TaskStats {
        val overdue = tasks.count { it.isOverdue() }
        return TaskStats(
            total = tasks.size,
            todo = tasks.count { it.status == Status.TODO },
            inProgress = tasks.count { it.status == Status.IN_PROGRESS },
            done = tasks.count { it.status == Status.DONE },
            overdue = overdue
        )
    }
}
