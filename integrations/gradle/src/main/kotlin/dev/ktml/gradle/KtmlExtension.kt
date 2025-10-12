package dev.ktml.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface KtmlExtension {
    val moduleName: Property<String>
    val templateDirectories: ListProperty<String>
}
