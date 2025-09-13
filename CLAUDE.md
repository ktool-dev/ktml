# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

KTML is a Kotlin Multiplatform HTML template engine that generates type-safe Kotlin functions from HTML template files. Templates use custom root elements with attributes defining parameters, supporting content composition, conditional rendering, loops, and Kotlin expression interpolation.

## Essential Commands

### Build and Test
```bash
# Build the entire project
./gradlew build

# Run all tests (uses custom BDD framework + Kotest)
./gradlew test

# Run tests for specific platform
./gradlew jvmTest

# Clean build
./gradlew clean

# Build platform-specific artifacts
./gradlew jvmJar
./gradlew linuxX64Binaries
./gradlew macosX64Binaries
```

### Testing Framework
- Uses **Kotest** with a custom BDD framework (src/commonTest/kotlin/dev/ktool/ktml/test/BddSpec.kt)
- BDD syntax: `Given`, `When`, `Then`, `And`, `Expect` blocks
- Test configuration in kotest.properties disables classpath scanning

## Architecture Overview

### Core Components
- **KtmlEngine** (src/commonMain/kotlin/dev/ktool/ktml/KtmlEngine.kt) - Main orchestrator for template processing
- **Templates** registry - Manages template dependencies and composition
- **Context** system - Type-safe parameter binding and HTML content writing

### Processing Pipeline
1. **TemplateParser** - Parses HTML templates using Ksoup, extracts parameters from attributes
2. **KotlinFileGenerator** - Generates complete Kotlin files with type-safe functions
3. **ContentGenerator** - Handles template content rendering logic

### Multiplatform Structure
```
src/
├── commonMain/kotlin/dev/ktool/ktml/    # Core multiplatform logic
├── commonTest/kotlin/dev/ktool/ktml/    # Shared tests with BDD framework
├── jvmMain/kotlin/dev/ktool/ktml/       # JVM-specific implementations
└── nativeMain/kotlin/dev/ktool/ktml/    # Native platform implementations
```

### Platform Abstractions
- **Expects.kt** defines common file operations interface
- Platform-specific **Actuals.kt** files implement file system operations
- JVM uses Java File API, Native uses platform-specific implementations

## Template Format

Templates are HTML files with custom root elements defining parameters as attributes:

```html
<card header="Content" body="Content">
    <div class="card">
        <div class="card-header">${header}</div>
        <div class="card-body">${body}</div>
    </div>
</card>
```

### Template Features
- Type-safe parameters with default values
- Content parameters for nested composition
- Kotlin expression interpolation with `${}`
- Conditional rendering with `if` attributes
- Loop rendering with `each` attributes
- Embedded Kotlin scripts with `<script type="text/kotlin">`

## Key Dependencies

- **kotlin-logging** (7.0.7) - Logging framework
- **ksoup-html/entities** (0.6.0) - HTML parsing and entity handling
- **kotlinx-coroutines-core** (1.7.3) - Coroutines support
- **Kotest** (5.9.1) - Testing framework base

## Development Guidelines

### Code Organization
- Core logic goes in `commonMain` for multiplatform compatibility
- Platform-specific implementations in respective `*Main` directories
- Tests use the custom BDD framework in `commonTest`
- Generated template examples in `src/commonMain/kotlin/dev/ktool/ktml/templates/`

### Template Engine Workflow
1. HTML templates → TemplateParser → ParsedTemplate AST
2. ParsedTemplate → KotlinFileGenerator → Type-safe Kotlin functions
3. Generated functions use Context for type-safe parameter binding
4. Templates registry resolves dependencies and handles composition

### Testing Strategy
- Write BDD-style tests using the custom framework
- Test template parsing, code generation, and rendering separately
- Use example templates in `commonTest/resources` for integration tests
- Verify multiplatform compatibility across JVM and Native targets