package dev.ktml.example.ktor

import dev.ktml.KtmlEngine
import dev.ktml.templates.DefaultKtmlRegistry
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.asList

val ktml = KtmlEngine(DefaultKtmlRegistry)
val mainScope = MainScope()

fun main() {
    window.onload = {
        mainScope.launch {
            renderApp()
        }
    }
}

suspend fun renderApp() {
    val root = document.getElementById("root") ?: run {
        console.error("Could not find element with id 'root'")
        return
    }

    root.innerHTML = ktml.renderPage(
        path = "index",
        model = mapOf("title" to "KTML + Kotlin/JS Example")
    )

    // Add click handler for user links
    setupUserLinks()
}

fun setupUserLinks() {
    document.querySelectorAll("a[href^='/users/']").asList().filterIsInstance<HTMLAnchorElement>().forEach {
        it.addEventListener("click", { event ->
            event.preventDefault()
            val href = it.getAttribute("href")
            val name = href?.substringAfterLast("/") ?: "Unknown"
            mainScope.launch {
                showUserProfile(name)
            }
        })
    }
}

suspend fun showUserProfile(name: String) {
    val root = document.getElementById("root") ?: return

    val userHtml = ktml.renderPage(
        path = "user",
        model = mapOf(
            "name" to name,
            "items" to listOf("Item 1", "Item 2", "Item 3", "Item 4")
        )
    )

    root.innerHTML = userHtml

    // Setup back button handler
    val backLink = document.querySelector("a[href='/']") as? HTMLAnchorElement
    backLink?.addEventListener("click", { event ->
        event.preventDefault()
        mainScope.launch {
            renderApp()
        }
    })
}
