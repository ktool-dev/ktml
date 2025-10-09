# KTML - Kotlin Multiplatform HTML Template Engine

A type-safe HTML template engine for Kotlin Multiplatform that generates Kotlin functions from `.ktml` template files.

## Overview

KTML transforms HTML templates into type-safe Kotlin functions, enabling compile-time verification of template
parameters and seamless integration with Kotlin code. Templates support parameter binding, conditional rendering, loops,
and composition, which allows you to break templates down into reusable components with no performance overhead since
every template call is just a function call.

## Features

- **Type Safety** - Template parameters are validated at compile time
- **Multiplatform** - Works on JVM and Native platforms
- **Template Composition** - Nest templates and reuse components
- **Kotlin Integration** - Embed Kotlin expressions directly in templates
- **Conditional Rendering** - Use `if` attributes and `<if>` tags for dynamic content
- **Loop Support** - Iterate over collections with `each` attributes
- **Content Parameters** - Pass HTML content blocks as parameters
- **Web Server** - Built-in Ktor server with hot-reloading during development

## Quick Start

### 1. Define a Template

Create a `.ktml` file with a custom root element defining parameters as attributes:

```html
<!-- card.ktml -->
<card header="Content? = null" content="Content">
    <div class="card">
        <div if="${header != null}" class="card-header">
            ${header}
        </div>
        <div class="card-body">
            ${content}
        </div>
    </div>
</card>
```

### 2. Generate and Execute Templates

On JVM, templates are compiled automatically at runtime:

```kotlin
val processor = JvmKtmlProcessor(
    templateDir = "templates/",
    outputDirectory = "build/generated/ktml",
    compiledDirectory = "build/generated/ktml-compiled"
)
val engine = KtmlEngine(processor)

// Render a page
val writer = StringContentWriter()
engine.writePage(Context(writer, mapOf("userName" to "John")), "dashboard")
val html = writer.toString()
```

### 3. Run the Web Server

Start the built-in Ktor server with hot-reloading:

```kotlin
JvmKtmlProcessor("templates/").createWebApp().start(port = 8080)
```

Or use the CLI:

```bash
# Run with current directory as template root
./gradlew run

# Or specify a template directory
./gradlew run --args="/path/to/templates"
```

## Template Syntax

### Parameters

**Basic Parameters:**

```html

<my-button text="String" onClick="String">
    <button onclick="${raw(onClick)}">${text}</button>
</my-button>
```

**Parameters with Default Values:**

```html

<page-layout title='String = "No Title"' header="Content" content="Content">
    <head>
        <title>${title}</title>
    </head>
    <body>
    <div class="header">${header}</div>
    <div class="content">${content}</div>
    </body>
</page-layout>
```

**Context Parameters:**
Use the `ctx-` prefix to pass data from the Context model:

```html

<sidebar ctx-sideBarItems="List<SideBarItem> = listOf()">
    <div class="sidebar">
        <a each="${item in sideBarItems}" href="${item.href}">${item.name}</a>
    </div>
</sidebar>
```

Then pass data via the Context:

```kotlin
val data = mapOf("sideBarItems" to listOf(SideBarItem("Home", "/")))
engine.writePage(Context(writer, data), "sidebar")
```

### Content Parameters

Content parameters allow you to pass HTML blocks as parameters:

**Parameters with Default Values:**

```html

<page-layout title='String = "No Title"' header="Content" content="Content">
    <head>
        <title>${title}</title>
    </head>
    <body>
    <div class="header">${header}</div>
    <div class="content">${content}</div>
    </body>
</page-layout>
```

**Context Parameters:**
Use the `ctx-` prefix to pass data from the Context model:

```html

<card header="Content? = null" content="Content">
    <div class="card">
        <div if="${header != null}" class="card-header">
            ${header}
        </div>
        <div class="card-body">
            ${content}
        </div>
    </div>
</card>
```

Call templates with named content blocks:

```html

<card>
    <header>
        <h3>Items</h3>
    </header>
    <ul>
        <li>Item 1</li>
        <li>Item 2</li>
    </ul>
</card>
```

### Conditional Rendering

**Using `if` attribute:**

```html
<h2 if="${user.type == UserType.ADMIN}">You are an admin!</h2>
<h2 if="${user.type != UserType.GUEST}">You are not a guest!</h2>
```

**Using `<if>` tag with `<else>`:**

```html

<if test="${user.type == UserType.ADMIN}">
    <h2>You are an admin!</h2>
    <else>
        <h2>You are not an admin!</h2>
    </else>
</if>
```

### Loops

**Basic iteration:**

```html

<sidebar ctx-sideBarItems="List<SideBarItem> = listOf()">
    <a each="${item in sideBarItems}" href="${item.href}">${item.name}</a>
</sidebar>
```

**Iteration with index:**

```html

<ul>
    <li each="${(index, item) in items.withIndex()}">${item.name} - Item ${index}</li>
</ul>
```

### Embedded Kotlin

You can embed Kotlin code in your templates using the `<script type="text/kotlin">` tag:

```html

<timestamp>
    <script type="text/kotlin">
        val now = java.time.LocalDateTime.now()
    </script>
    <p>Generated at: ${now}</p>
</timestamp>
```

### Importing Types

Import types at the top of your template file:

```html
import dev.ktml.User
import dev.ktml.UserType

<html lang="en" ctx-user="User">
<h1>Hello, ${user.name}!</h1>
<h2 if="${user.type == UserType.ADMIN}">You are an admin!</h2>
</html>
```

### HTML Pages

Root templates with `<html>` as the root element become pages accessible via web routes:

```html
<!DOCTYPE html>

import dev.ktml.*

<html lang="en" ctx-userName="String" ctx-user="User">
<page-layout title="Dashboard - ${user.name}">
    <header>
        <h1>Dashboard</h1>
    </header>
    <content>
        <h1>Hello, ${user.name}!</h1>
    </content>
</page-layout>
</html>
```

File path `templates/dashboard.ktml` becomes accessible at `http://localhost:8080/dashboard`.

### Raw Output

Use `raw()` to output unescaped HTML:

```html

<my-button text="String" onClick="String">
    <button onclick="${raw(onClick)}">${text}</button>
</my-button>
```

## Building

```bash
# Build all platforms
./gradlew build

# Build JVM only
./gradlew jvmJar

# Build native binaries
./gradlew linuxX64Binaries
./gradlew macosX64Binaries
./gradlew macosArm64Binaries
./gradlew mingwX64Binaries
```

## Testing

The project uses Kotest with a custom BDD framework:

```bash
# Run all tests
./gradlew test

# Run JVM tests only
./gradlew jvmTest
```

## Dependencies

- **Kotlin Multiplatform** - Core platform
- **Ksoup** - HTML parsing and entity handling
- **Kotlin Logging** - Logging framework
- **Kotlinx Coroutines** - Async support
- **Kotest** - Testing framework

## Platform Support

- **JVM** - Java 22+ required
- **Native** - Linux x64, macOS x64/ARM64, Windows x64

## License

This project is licensed under the [Apache 2.0 License](LICENSE).

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests for any improvements.

1. Fork the repository
2. Create a feature branch
3. Write tests for your changes
4. Ensure all tests pass with `./gradlew test`
5. Submit a pull request

## Architecture

KTML consists of several key components:

- **KtmlEngine** - Main orchestrator for template processing
- **TemplateParser** - Parses HTML templates using Ksoup
- **KotlinFileGenerator** - Generates type-safe Kotlin functions
- **Templates Registry** - Manages template dependencies and composition
- **Context System** - Handles type-safe parameter binding

Templates are processed through a pipeline: HTML → Parsed AST → Generated Kotlin Functions → Runtime Execution with type
safety.