package dev.ktml.example.javalin

import dev.ktml.javalin.JavalinKtml
import dev.ktml.javalin.ktml
import io.javalin.Javalin

fun main() {
    JavalinKtml.init()

    Javalin.create().apply {
        configureRouting()
    }.start(8080)

    println("Server started on http://localhost:8080")
}

fun Javalin.configureRouting() {
    get("/") { ctx ->
        ctx.ktml(
            path = "index",
            model = mapOf(
                "title" to "Welcome to KTML with Javalin"
            )
        )
    }

    get("/users/{name}") { ctx ->
        val name = ctx.pathParam("name")
        ctx.ktml(
            path = "user",
            model = mapOf(
                "name" to name,
                "items" to listOf("Item 1", "Item 2", "Item 3")
            )
        )
    }
}
