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

The plugin doesn't require any configuration. It will find `ktml` directories in any SourceSet, for instance at:

```mermaid
src/main/ktml
src/commonMain/ktml
src/jvmMain/ktml
```

It will then generate code for each SourceSet, and add the generated code as a srcDir on the SourceSet for the
Kotlin compiler. So the compiled code will be included in the output with all your other code.

One of the following Kotlin plugins must be applied:

- `org.jetbrains.kotlin.jvm`
- `org.jetbrains.kotlin.multiplatform`

## Tasks

- **`generateKtml`**: Generates Kotlin code from KTML templates

This task runs automatically before Kotlin compilation.

## Example

With Kotlin on the JVM

```kotlin
plugins {
    kotlin("jvm")
    id("dev.ktml.gradle")
}
```

With Kotlin and KMP

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.ktml.gradle")
}
```
