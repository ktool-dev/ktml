package dev.ktml.example.ktor

import dev.ktml.example.ktor.data.SampleData
import dev.ktml.example.ktor.models.Status
import dev.ktml.ktor.KtmlPlugin
import dev.ktml.ktor.respondKtml
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        install(KtmlPlugin)
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        // Dashboard
        get("/") {
            val stats = SampleData.getStats()
            call.respondKtml(
                path = "pages/dashboard",
                model = mapOf(
                    "title" to "Dashboard - Task Manager",
                    "user" to SampleData.currentUser,
                    "stats" to stats,
                    "todoTasks" to SampleData.getTasksByStatus(Status.TODO),
                    "inProgressTasks" to SampleData.getTasksByStatus(Status.IN_PROGRESS),
                    "doneTasks" to SampleData.getTasksByStatus(Status.DONE)
                )
            )
        }

        // All tasks page
        get("/tasks") {
            call.respondKtml(
                path = "pages/tasks",
                model = mapOf(
                    "title" to "All Tasks - Task Manager",
                    "user" to SampleData.currentUser,
                    "tasks" to SampleData.tasks,
                    "users" to SampleData.users
                )
            )
        }

        // Task detail page
        get("/tasks/{id}") {
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
                    "user" to SampleData.currentUser,
                    "task" to task
                )
            )
        }

        // Profile page (placeholder)
        get("/profile") {
            call.respondText("Profile page - Coming soon!")
        }

        // Admin page (placeholder, only for admins)
        get("/admin") {
            if (!SampleData.currentUser.isAdmin) {
                call.respondText("Access denied", status = HttpStatusCode.Forbidden)
                return@get
            }
            call.respondText("Admin panel - Coming soon!")
        }
    }
}
