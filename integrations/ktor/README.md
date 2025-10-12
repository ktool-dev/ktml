# KTML Ktor Plugin

A Ktor plugin that enables KTML template rendering in Ktor applications.

## Installation

Add the dependency to your project and install the plugin:

```kotlin
import dev.ktml.ktor.KtmlPlugin
import dev.ktml.KtmlRegistry
import io.ktor.server.application.*

fun Application.module() {
    install(KtmlPlugin) {
        registry = KtmlRegistry() // Your template registry
    }
}
```

## Configuration

The plugin requires a `KtmlRegistry` instance that contains your compiled templates:

```kotlin
install(KtmlPlugin) {
    registry = myTemplateRegistry
}
```

## Usage

Use the `respondKtml` extension function in your routes:

```kotlin
import io.ktor.server.routing.*
import io.ktor.http.*

routing {
    get("/") {
        call.respondKtml(
            path = "/index",
            model = mapOf("title" to "Home Page")
        )
    }

    get("/user/{id}") {
        call.respondKtml(
            path = "/user",
            model = mapOf("userId" to call.parameters["id"]),
            status = HttpStatusCode.OK
        )
    }
}
```

### Parameters

- **`path`**: The template path in your registry
- **`model`**: Key-value pairs of data to pass to the template (default: `emptyMap()`)
- **`status`**: HTTP status code (default: `HttpStatusCode.OK`)

## Features

The plugin automatically:

- Sets the `Content-Type` header to `text/html`
- Passes query parameters to the template context
- Passes path parameters to the template context
- Handles asynchronous rendering with Ktor's coroutine support
- Uses efficient streaming output

## Template Context

Templates receive a `Context` object with:

- **`model`**: The model map you provided
- **`queryParams`**: HTTP query parameters as `Map<String, List<String>>`
- **`pathParams`**: URL path parameters as `Map<String, List<String>>`

## Example

```kotlin
fun Application.module() {
    install(KtmlPlugin) {
        registry = KtmlRegistry()
    }

    routing {
        get("/products") {
            val products = productService.getAll()
            call.respondKtml(
                path = "/products/list",
                model = mapOf(
                    "products" to products,
                    "count" to products.size
                )
            )
        }
    }
}
```
