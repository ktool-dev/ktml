# KTML Gradle Plugin

A Gradle plugin that automatically generates Kotlin code from KTML templates during the build process.

## Installation

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("dev.ktml.gradle") version "<version>"
}
```

## Configuration

The plugin doesn't require any configuration. It will find `ktml` directories in any SourceSet, for instance at:

```
src/main/ktml
src/commonMain/ktml
src/jvmMain/ktml
```

It will then generate code for each SourceSet, and add the generated code as a srcDir on the SourceSet for the
Kotlin compiler. So the compiled code will be included in the output with all your other code.

One of the following Kotlin plugins must be applied:

- `org.jetbrains.kotlin.jvm`
- `org.jetbrains.kotlin.multiplatform`

## Dev Mode

KTML has a dev-mode module that can run on the JVM and hot-reload templates as they are changed. This Gradle plugin adds
a configuration call `developmentOnly` that you use on that dependency which will ensure it's on the class path when you
run locally, but when it won't be there when you run the `build` task, so it won't get included in your server code.
You can use it like this:

```kotlin
dependencies {
    developmentOnly("dev.ktml:ktml-dev-mode:<version>")
}
```

This will also work with KMP projects, but you still define it at the top level of your project, like it's shown and it
will get added to your `jvmMain` classpath locally.

## Tasks

- **`generateKtml`**: Generates Kotlin code from KTML templates

This task runs automatically before Kotlin compilation.

## Example

With Kotlin on the JVM

```kotlin
plugins {
    kotlin("jvm")
    id("dev.ktml.gradle") version "0.1.0"
}
```

With Kotlin and KMP

```kotlin
plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("dev.ktml.gradle")
}
```
