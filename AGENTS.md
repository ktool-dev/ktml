# AGENTS.md

This file provides guidance to coding agents when working with code in this repository.

## Project Overview

KTML is a Kotlin Multiplatform HTML template engine that transforms `.ktml` template files into type-safe Kotlin
functions. Templates are parsed, converted to Kotlin code, compiled at runtime (JVM) or build-time (Native), and
executed to generate HTML.

## Build & Test Commands

```bash
# Build all platforms
./gradlew build

# Build JVM only
./gradlew jvmJar

# Run all tests
./gradlew test

# Run JVM tests only
./gradlew jvmTest

# Run a single test spec (Kotest)
./gradlew jvmTest --tests "dev.ktml.GeneratedFunctionSpec"
```

**Requirements:** Java 22+ (JVM toolchain is set to 22)

## Template Processing Pipeline

The core workflow follows this pipeline:

1. **Template Files** (`.ktml`) → 2. **TemplateParser** → 3. **ParsedTemplate AST** → 4. **KotlinFileGenerator** → 5. *
   *Generated Kotlin Functions** → 6. **Runtime Compilation** (JVM) → 7. **KtmlRegistry** → 8. **Execution**

### Key Components

**KtmlProcessor** (`KtmlProcessor.kt`)

- Base class that orchestrates template processing
- Scans directories for `.ktml` files and processes them
- Maintains a `Templates` registry of all parsed templates
- Generates Kotlin code files for each template via `KotlinFileGenerator`
- Outputs to `dev.ktml.templates` package (or `dev.ktml.templates.<moduleName>`)

**JvmKtmlProcessor** (`JvmKtmlProcessor.kt`)

- JVM-specific processor that extends `KtmlProcessor`
- Compiles generated Kotlin code at runtime using embedded Kotlin compiler
- Uses custom `URLClassLoader` for hot-reloading templates during development
- Implements `DirectoryWatcher` to automatically recompile templates on file changes
- Loads compiled `KtmlRegistryImpl` object via reflection

**TemplateParser** (`TemplateParser.kt`)

- Parses `.ktml` files using Ksoup HTML parser
- Extracts root element to determine template name and parameters
- Processes template syntax: `${expressions}`, `if="${condition}"`, `each="${item in list}"`
- Handles content parameters (e.g., `content="Content"` attribute)
- Returns `List<ParsedTemplate>` (a single file can define multiple templates)

**KotlinFileGenerator** (`KotlinFileGenerator.kt`)

- Generates Kotlin code from `ParsedTemplate` AST
- Creates extension functions on `Context` (e.g., `suspend fun Context.writeCard(...)`)
- Handles parameter types, conditionals, loops, and nested template calls

**Context** (`Context.kt`)

- Runtime context for template execution
- Holds model data, query params, path params
- Provides `write()` methods for outputting HTML (auto-escapes by default)
- Type-safe parameter access: `required<T>()`, `optional<T>()`

**KtmlRegistry** (`KtmlRegistry.kt`)

- Interface defining `pages: Map<String, Content>` and `tags: List<TagDefinition>`
- Generated `KtmlRegistryImpl` object contains all compiled templates
- `DefaultKtmlRegistry` provides built-in error pages (`default-not-found`, `default-error`, `compile-exception`)

**WebApp** (`WebApp.kt`)

- Ktor-based web server for serving templates as HTTP pages
- Auto-registers routes for all pages in `KtmlRegistry`
- Converts path segments starting with `_` to `{param}` for Ktor path parameters (e.g., `users/_id` → `users/{id}`)
- Handles 404s and exceptions with customizable error templates

## Template Syntax Key Points

- Root element defines the template name and parameters: `<card title="String" content="Content">`
- Use `${variable}` for expressions (HTML-escaped by default)
- Conditional rendering: `<div if="${condition}">...</div>`
- Loops: `<li each="${item in items}">${item}</li>`
- Content parameters allow passing HTML blocks to templates
- Import statements at the top of `.ktml` files add imports to generated Kotlin code
- Embedded Kotlin: `<script type="text/kotlin">val now = LocalDateTime.now()</script>`

## File Structure

```
libraries/core/src/
├── commonMain/kotlin/dev/ktml/          # Core multiplatform code
│   ├── KtmlEngine.kt                     # Main engine
│   ├── KtmlProcessor.kt                  # Base processor
│   ├── Context.kt                        # Runtime context
│   ├── parser/                           # Template parsing
│   ├── gen/                              # Code generation
│   └── web/WebApp.kt                     # Ktor web server
├── jvmMain/kotlin/dev/ktml/
│   ├── JvmKtmlProcessor.kt               # JVM processor with compilation
│   ├── DirectoryWatcher.kt               # File watcher for hot reload
│   ├── compile/KotlinCompile.kt          # Kotlin compiler integration
│   └── Main.kt                           # CLI entry point
└── jvmTest/                              # Tests using Kotest BDD framework
```

## Development Workflow

**Running the dev server:**
The `main()` function in `Main.kt` starts a Ktor server that:

- Watches the template directory for changes
- Auto-recompiles templates on save
- Serves templates as web pages on `http://localhost:8080`

**Running tests:**
Tests use a custom Kotest BDD framework (`dev.ktool.kotest.BddSpec`). Test specs initialize `JvmKtmlProcessor` with test
templates from `src/jvmTest/resources/templates/`.

**Hot Reloading:**
`DirectoryWatcher` monitors `.ktml` files. On changes:

1. `reprocessFile()` removes old template from registry
2. Re-parses and regenerates Kotlin code
3. Recompiles all templates
4. Creates new `URLClassLoader` to load updated classes
5. Reloads web app routes if page paths changed

## Path Parameter Conventions

Template file paths map to web routes:

- `index.ktml` → `/`
- `users/index.ktml` → `/users`
- `users/_id.ktml` → `/users/{id}` (path parameter)
- `blog/_year/_month.ktml` → `/blog/{year}/{month}`

Leading underscores in path segments are converted to `{param}` syntax for Ktor routing.

## Error Handling

**Compile Errors:**
When generated Kotlin code has errors, `CompileException` is thrown with error details. Errors are converted back to
`.ktml` file paths (from generated `.kt` paths) by converting camelCase to kebab-case.

**Runtime Errors:**
The web app's `StatusPages` plugin catches exceptions and renders error templates if available, otherwise uses built-in
defaults.

## Native Platform Support

For Native targets (Linux, macOS, Windows), templates must be compiled at build-time rather than runtime since there's
no embedded compiler available. The compilation step would be integrated into the Gradle build process.
