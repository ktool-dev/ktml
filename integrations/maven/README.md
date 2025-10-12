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

Configure the plugin in the `<configuration>` section:

```xml
<configuration>
    <moduleName>my-templates</moduleName>
    <templateDirectories>
        <directory>src/main/ktml</directory>
        <directory>src/main/templates</directory>
    </templateDirectories>
    <outputDirectory>${project.build.directory}/generated-sources/ktml</outputDirectory>
</configuration>
```

### Options

- **`moduleName`**: Optional module name for templates (default: `""`)
- **`templateDirectories`**: List of directories containing KTML templates (default: `["src/main/ktml"]`)
- **`outputDirectory`**: Output directory for generated Kotlin sources (default: `${project.build.directory}/generated-sources/ktml`)

## How It Works

The plugin automatically:

1. Runs during the `generate-sources` phase
2. Processes KTML templates from specified directories
3. Generates Kotlin code to the output directory
4. Adds the output directory as a compile source root

## Goal

- **`ktml:generate`**: Generates Kotlin code from KTML templates

## Example

```xml
<build>
    <plugins>
        <plugin>
            <groupId>dev.ktml</groupId>
            <artifactId>ktml-maven-plugin</artifactId>
            <version>1.0.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <moduleName>web-templates</moduleName>
                <templateDirectories>
                    <directory>src/main/ktml</directory>
                </templateDirectories>
            </configuration>
        </plugin>
    </plugins>
</build>
```
