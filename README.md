# KTML - Kotlin Multiplatform HTML Template Engine

A type-safe HTML template engine for Kotlin Multiplatform that generates Kotlin functions from HTML template files.

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
- **Conditional Rendering** - Use `if` attributes for dynamic content
- **Loop Support** - Iterate over collections with `each` attributes
- **Content Parameters** - Pass HTML content blocks as parameters

## Quick Start

### 1. Define a Template

Create an HTML file with a custom root element defining parameters as attributes:

```html
<!-- card.html -->
<card title="String" content="Content">
    <div class="card">
        <div class="card-header">
            <h3>${title}</h3>
        </div>
        <div class="card-body">
            ${content}
        </div>
    </div>
</card>
```

### 2. Generate Kotlin Functions

Use the KTML engine to process your templates:

```kotlin
val engine = KtmlEngine()
engine.processDirectory("templates/", "output/")
```

### 3. Use Generated Functions

The engine generates type-safe Kotlin functions:

```kotlin
// Generated function signature
fun Context.writeCard(title: String, content: Content)

// Usage
val context = Context()

val html = context.writeCard(
    title = "Welcome",
    content = content { +"Hello, World!" }
)
```

## Template Syntax

### Basic Parameters

```html

<greeting name="String" age="Int">
    <p>Hello ${name}, you are ${age} years old!</p>
</greeting>
```

### Content Parameters

When passing content to a tag, you can use the `content` attribute to define a content parameter. Like this:

```html

<wrapper content="Content">
    <div class="wrapper">
        ${content}
    </div>
</wrapper>
```

Then call that tag, you pass the `content` parameter as children. Like this:

```html

<page>
    <wrapper>
        <h1>Welcome</h1>
        <p>Hello, World!</p>
    </wrapper>
</page>
```

You can also define multiple content parameters with different names.

```html

<layout sidebar="Content" main="Content">
    <div class="container">
        <aside class="sidebar">${sidebar}</aside>
        <main class="content">${main}</main>
    </div>
</layout>
```

To invoke this template and pass content to it, you use nested tags with the name of the content parameter. The content
of the nested tag is passed to the template as the value of the content parameter.

```html

<page>
    <layout>
        <sidebar>
            <ul>
                <li>Home</li>
                <li>About</li>
            </ul>
        </sidebar>
        <main>
            <h1>Welcome</h1>
            <p>Hello, World!</p>
        </main>
    </layout>
</page>
```

### Conditional Rendering

```html

<user-info name="String" isAdmin="Boolean">
    <div class="user">
        <span>${name}</span>
        <span if="${isAdmin}" class="badge">Admin</span>
    </div>
</user-info>
```

### Loops

```html

<item-list items="List<String>">
    <ul>
        <li each="${item in items}">${item}</li>
    </ul>
</item-list>
```

### Embedded Kotlin

You can embed Kotlin code in your templates using the `<script type="text/kotlin">` tag. The code is executed in the
context of the template function at the place the tag is in the flow of the page.

```html

<timestamp>
    <script type="text/kotlin">
        val now = java.time.LocalDateTime.now()
    </script>
    <p>Generated at: ${now}</p>
</timestamp>
```

### Importing Types

You can import types in your templates using the `import` statement. These will go at the top of the generated file, so
the types imported will be available to anything in the template.

```html
import my.app.UserType

<user-info name="String" userType="UserType">
    <div class="user">
        <span>${name}</span>
        <span if="${userType == UserType.ADMIN}" class="badge">Admin</span>
    </div>
</user-info>
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