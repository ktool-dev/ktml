package dev.ktml.example.ktor.routes

import dev.ktml.example.ktor.data.SampleData
import dev.ktml.example.ktor.models.Priority
import dev.ktml.example.ktor.models.Status
import dev.ktml.example.ktor.plugins.requireAuth
import dev.ktml.ktor.respondKtml
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.configureTaskRoutes() {
    // Dashboard
    get("/") {
        val user = call.requireAuth()
        val stats = SampleData.getStats()
        call.respondKtml(
            path = "pages/dashboard",
            model = mapOf(
                "title" to "Dashboard - Task Manager",
                "user" to user,
                "stats" to stats,
                "todoTasks" to SampleData.getTasksByStatus(Status.TODO),
                "inProgressTasks" to SampleData.getTasksByStatus(Status.IN_PROGRESS),
                "doneTasks" to SampleData.getTasksByStatus(Status.DONE)
            )
        )
    }

    // All tasks page
    get("/tasks") {
        val user = call.requireAuth()
        call.respondKtml(
            path = "pages/tasks",
            model = mapOf(
                "title" to "All Tasks - Task Manager",
                "user" to user,
                "tasks" to SampleData.tasks,
                "users" to SampleData.users
            )
        )
    }

    // Task list fragment (for HTMX)
    get("/tasks/list") {
        val statusParam = call.request.queryParameters["status"]
        val priorityParam = call.request.queryParameters["priority"]
        val assigneeParam = call.request.queryParameters["assignee"]
        val searchParam = call.request.queryParameters["search"]

        val status = statusParam?.takeIf { it.isNotBlank() }?.let { Status.valueOf(it) }
        val priority = priorityParam?.takeIf { it.isNotBlank() }?.let { Priority.valueOf(it) }
        val assigneeId = assigneeParam?.takeIf { it.isNotBlank() }?.toIntOrNull()

        val filteredTasks = SampleData.filterTasks(
            status = status,
            priority = priority,
            assigneeId = assigneeId,
            searchQuery = searchParam
        )

        call.respondKtml(
            path = "fragments/task/task-list",
            model = mapOf(
                "tasks" to filteredTasks,
            )
        )
    }

    // Task detail page
    get("/tasks/{id}") {
        val user = call.requireAuth()
        val taskId = call.parameters["id"]?.toIntOrNull()
        val task = taskId?.let { SampleData.getTaskById(it) }

        if (task == null) {
            call.respondText("Task not found", status = HttpStatusCode.NotFound)
            return@get
        }

        call.respondKtml(
            path = "pages/task-detail",
            model = mapOf(
                "title" to "${task.title} - Task Manager",
                "user" to user,
                "task" to task
            )
        )
    }
}
