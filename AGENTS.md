# AGENTS.md

This file provides guidance to coding agents when working with code in this repository.

## Project Overview

KTML is a Kotlin Multiplatform HTML template engine that transforms `.ktml` templates into optimized Kotlin functions at
compile time. Templates are valid HTML files with special attributes (`if` and `each`) and type-safe parameter
declarations.

**IMPORTANT: This project uses the file at gradle/libs.versions.toml to manage the version of dependencies. So you can
look in that file to find out what version of a library is being used.

## Commands

### Build

```bash
./gradlew build
```

### Testing

```bash
# Run all tests
./gradlew test

# Run JVM tests only
./gradlew jvmTest

# Run tests for a specific module
./gradlew :runtime:test
./gradlew :generator:test

# Run a specific test in a specific module
./gradlew :dev-mode:test --tests "dev.ktml.KotlinCompileSpec"
```

### Development

```bash
# Clean build
./gradlew clean build

# Run example applications
./gradlew :example-ktor:run
./gradlew :example-spring:bootRun
./gradlew :example-javalin:run
```

## Project Structure

The project is organized into three main categories:

### Libraries

- **runtime**: Core runtime library containing `KtmlEngine`, `Context`, `KtmlRegistry`, and HTML encoding utilities
- **generator**: Template parser and Kotlin code generator that transforms `.ktml` files into Kotlin functions
- **dev-mode**: Hot reloading support with file watching and incremental compilation

### Integrations

- **gradle**: Gradle plugin for automatic code generation during builds
- **maven**: Maven plugin support
- **spring**: Spring MVC integration
- **ktor**: Ktor integration
- **javalin**: Javalin integration

### Applications

Example applications demonstrating KTML usage with different frameworks (ktor, spring, javalin).

## Architecture

### Template Processing Pipeline

1. **Parsing** (`TemplateParser` in libraries/generator/src/commonMain/kotlin/dev/ktml/parser/TemplateParser.kt)
    - Parses `.ktml` files using Ksoup HTML parser
    - Extracts template parameters, imports, and structure
    - Handles three template types: full pages (`<html>` root), custom tags (reusable components), and fragments

2. **AST Generation** (`HtmlHandler` and `HtmlElement` in libraries/generator/src/commonMain/kotlin/dev/ktml/parser/)
    - Builds abstract syntax tree from parsed HTML
    - Preserves element structure, attributes, and text content
    - Processes Kotlin expressions embedded in templates (e.g., `${variable}` and `$variable`)

3. **Code Generation** (`KotlinFileGenerator` and `ContentGenerator` in
   libraries/generator/src/commonMain/kotlin/dev/ktml/gen/)
    - Generates type-safe Kotlin functions from parsed templates
    - Each template becomes a `suspend fun writeTemplateName(context: Context, ...params)`
    - Element handlers (in gen/element/) transform HTML elements into Context.write() calls

4. **Registry Creation** (`KtmlRegistryGenerator` in
   libraries/generator/src/commonMain/kotlin/dev/ktml/gen/KtmlRegistryGenerator.kt)
    - Creates `KtmlRegistry` mapping template paths to generated functions
    - Enables runtime template lookup by path

5. **Runtime Execution** (`KtmlEngine` in libraries/runtime/src/commonMain/kotlin/dev/ktml/KtmlEngine.kt)
    - Looks up templates from registry
    - Provides `Context` with model data, path/query params, and HTML writing capabilities

### Key Components

**KtmlProcessor** (libraries/generator/src/commonMain/kotlin/dev/ktml/KtmlProcessor.kt:13)

- Orchestrates the entire template processing pipeline
- Manages template discovery, parsing, and code generation
- Tracks template dependencies and handles incremental updates

**Context** (libraries/runtime/src/commonMain/kotlin/dev/ktml/Context.kt:10)

- Runtime context for template execution
- Provides type-safe access to model data via `required()`, `optional()`, etc.
- Handles HTML encoding and raw output
- Manages path params, query params, and nested content blocks

**TemplateParser** (libraries/generator/src/commonMain/kotlin/dev/ktml/parser/TemplateParser.kt:20)

- Parses template files and extracts metadata
- Handles Kotlin expression replacement (e.g., `${expr}`)
- Validates template structure and parameter declarations

**ContentGenerator** (libraries/generator/src/commonMain/kotlin/dev/ktml/gen/ContentGenerator.kt:24)

- Core code generation logic
- Uses element handlers to process different HTML element types
- Generates optimized string-building code with minimal allocations

### Template Parameter Types

- **Regular parameters**: Passed directly when calling a custom tag (e.g., `userName="String"`)
- **Context parameters**: Prefixed with `@`, pulled from the Context model (e.g., `@userName="String"`)
- **Content parameters**: Special type `Content` for passing HTML blocks (e.g., `content="Content"`)
- **Nullable and defaults**: Supports Kotlin nullable types and default values (e.g., `bio="String? = null"`)

### Element Handlers

Element handlers (libraries/generator/src/commonMain/kotlin/dev/ktml/gen/element/) implement specific rendering logic:

- **DefaultTagHandler**: Standard HTML elements
- **CustomTagHandler**: User-defined custom tags (function calls to other templates)
- **ContextTagHandler**: `<context>` tags for adding context parameters
- **ScriptTagHandler**: `<script type="text/kotlin">` for embedded Kotlin code
- **TextElementHandler**: Text nodes with expression interpolation

## Platform Support

- **JVM**: Java 22+ required
- **Native**: Linux x64, macOS x64/ARM64, Windows x64

## Important Notes

- Templates must be valid HTML
- Custom tag names cannot conflict with existing HTML or SVG tags
- All template parameters must use Kotlin expression syntax (prefixed with `$` in attribute values)
- Context parameters use `@` prefix in parameter names, others don't
- The `if` attribute conditionally renders elements
- The `each` attribute loops over collections
- Generated code is placed in `dev.ktml.templates` package (or `dev.ktml.templates.<moduleName>` for multi-module
  projects)
