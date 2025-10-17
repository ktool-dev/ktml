# KTML Maven Plugin

A Maven plugin that generates Kotlin code from KTML templates during the `generate-sources` phase.

## Installation

Add the plugin to your `pom.xml`:

```xml

<build>
    <plugins>
        <plugin>
            <groupId>dev.ktml</groupId>
            <artifactId>ktml-maven-plugin</artifactId>
            <version>${ktml.version}</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## Configuration

The plugin doesn't require any configuration. It will expect a `ktml` directory at `src/main/ktml`. It will then
generate code and put it in the build directory, and add the generated code as a compileSourceRoot for the Kotlin
compiler. So the compiled code will be included in the output with all your other code.

## Goal

- **`ktml:generate`**: Generates Kotlin code from KTML templates

