# KTML Javalin Integration

Provides KTML template rendering for Javalin applications.

## Installation

Initialize the KTML engine at application startup:

```kotlin
import dev.ktml.javalin.JavalinKtml
import dev.ktml.KtmlRegistry

fun main() {
    JavalinKtml.init(KtmlRegistryImpl)

    // Continue with Javalin setup...
}
```

## Usage

Use the `renderKtml` extension function in your route handlers:

```kotlin
import dev.ktml.javalin.renderKtml
import io.javalin.Javalin

fun main() {
    JavalinKtml.init(KtmlRegistryImpl)

    val app = Javalin.create().start(7070)

    app.get("/") { ctx ->
        ctx.renderKtml(
            path = "/index",
            model = mapOf("title" to "Home Page")
        )
    }

    app.get("/user/{id}") { ctx ->
        ctx.renderKtml(
            path = "/user",
            model = mapOf("userId" to ctx.pathParam("id"))
        )
    }
}
```

### Parameters

- **`path`**: The template path in your registry
- **`model`**: Key-value pairs of data to pass to the template (default: `emptyMap()`)

## Important Notes

⚠️ **You must call `JavalinKtml.init()` before rendering any templates.** The engine will throw an exception if you
attempt to render without initialization.

## Features

The integration automatically:

- Sets the `Content-Type` header to `text/plain; charset=utf-8`
- Passes query parameters to the template context
- Passes path parameters to the template context
- Renders templates in blocking mode (compatible with Javalin's threading model)

## Template Context

Templates receive a `Context` object with:

- **`model`**: The model map you provided
- **`queryParams`**: HTTP query parameters as `Map<String, List<String>>`
- **`pathParams`**: URL path parameters as `Map<String, List<String>>`

## Example

```kotlin
import dev.ktml.javalin.JavalinKtml
import dev.ktml.javalin.renderKtml
import dev.ktml.KtmlRegistry
import io.javalin.Javalin

fun main() {
    JavalinKtml.init(KtmlRegistryImpl)

    val app = Javalin.create().start(7070)

    app.get("/products") { ctx ->
        val products = productService.getAll()
        ctx.renderKtml(
            path = "/products/list",
            model = mapOf(
                "products" to products,
                "count" to products.size
            )
        )
    }

    app.get("/search") { ctx ->
        val query = ctx.queryParam("q")
        ctx.renderKtml(
            path = "/search",
            model = mapOf("query" to query)
        )
    }
}
```
