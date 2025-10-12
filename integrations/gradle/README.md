# KTML Gradle Plugin

A Gradle plugin that automatically generates Kotlin code from KTML templates during the build process.

## Installation

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("dev.ktml.gradle")
}
```

## Configuration

Configure the plugin using the `ktml` extension:

```kotlin
ktml {
    moduleName.set("my-templates")
    templateDirectories.set(listOf("src/main/ktml"))
}
```

### Options

- **`moduleName`**: Optional module name for generated templates (default: `""`)
- **`templateDirectories`**: List of directories containing KTML templates (default: `["src/main/ktml"]`)

## How It Works

The plugin automatically:

1. Creates a `generateKtml` task that processes KTML templates
2. Configures Kotlin compilation tasks to depend on `generateKtml`
3. Generates Kotlin code from your KTML templates before compilation

## Requirements

One of the following Kotlin plugins must be applied:
- `org.jetbrains.kotlin.jvm`
- `org.jetbrains.kotlin.multiplatform`

## Tasks

- **`generateKtml`**: Generates Kotlin code from KTML templates

This task runs automatically before Kotlin compilation.

## Example

```kotlin
plugins {
    kotlin("jvm")
    id("dev.ktml.gradle")
}

ktml {
    moduleName.set("web-templates")
    templateDirectories.set(listOf(
        "src/main/ktml",
        "src/main/templates"
    ))
}
```
