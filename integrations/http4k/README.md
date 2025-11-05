# KTML Http4k Integration

Provides KTML template rendering for Http4k applications.

## Installation

Initialize the KTML engine at application startup:

```kotlin
import dev.ktml.http4k.Http4kKtml
import dev.ktml.KtmlRegistry

fun main() {
    Http4kKtml.init()

    // Continue with Http4k setup...
}
```

## Usage

Use the `ktml` extension function on `Request` objects in your handlers:

```kotlin
import dev.ktml.http4k.ktml
import org.http4k.core.*
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Http4kKtml.init()

    val app: HttpHandler = { request: Request ->
        request.ktml(
            path = "index",
            model = mapOf("title" to "Home Page")
        )
    }

    app.asServer(SunHttp(8080)).start()
}
```

### With Routing

```kotlin
import org.http4k.routing.bind
import org.http4k.routing.routes

fun main() {
    Http4kKtml.init(KtmlRegistry)

    val app = routes(
        "/" bind Method.GET to { request ->
            request.ktml(
                path = "index",
                model = mapOf("title" to "Home Page")
            )
        },
        "/user/{id}" bind Method.GET to { request ->
            request.ktml(
                path = "user",
                model = mapOf("userId" to request.path("id"))
            )
        }
    )

    app.asServer(SunHttp(8080)).start()
}
```

### Parameters

- **`path`**: The template path in your registry
- **`model`**: Key-value pairs of data to pass to the template (default: `emptyMap()`)
- **`status`**: HTTP status code (default: `Status.OK`)

## Important Notes

⚠️ **You must call `Http4kKtml.init()` before rendering any templates.** The engine will throw an exception if you
attempt to render without initialization.

## Features

The integration automatically:

- Sets the `Content-Type` header to `text/html; charset=utf-8`
- Passes query parameters to the template context
- Renders templates in blocking mode using coroutines
- Returns an Http4k `Response` object

## Template Context

Templates receive a `Context` object with:

- **`model`**: The model map you provided
- **`queryParams`**: HTTP query parameters as `Map<String, List<String>>`
- **`pathParams`**: Empty map by default (Http4k core doesn't provide automatic path param extraction)

### Path Parameters

Http4k's core library doesn't automatically extract path parameters. You can pass them explicitly via the model:

```kotlin
import org.http4k.lens.Path

val userIdLens = Path.of("id")

"/user/{id}" bind Method.GET to { request ->
    val userId = userIdLens(request)
    request.ktml(
        path = "/user",
        model = mapOf("userId" to userId)
    )
}
```

## Example

```kotlin
import dev.ktml.http4k.Http4kKtml
import dev.ktml.http4k.ktml
import dev.ktml.KtmlRegistry
import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    Http4kKtml.init(KtmlRegistry)

    val app = routes(
        "/products" bind Method.GET to { request ->
            val products = productService.getAll()
            request.ktml(
                path = "/products/list",
                model = mapOf(
                    "products" to products,
                    "count" to products.size
                )
            )
        },
        "/search" bind Method.GET to { request ->
            val query = request.query("q")
            request.ktml(
                path = "/search",
                model = mapOf("query" to query),
                status = Status.OK
            )
        }
    )

    app.asServer(SunHttp(8080)).start()
}
```
