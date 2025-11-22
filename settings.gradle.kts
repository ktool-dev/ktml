pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ktml"

val projectTypeDirs = listOf("libraries", "applications", "integrations")

projectTypeDirs.forEach { type ->
    file(type).list().forEach { project ->
        include(project)
        project(":$project").projectDir = File("$type/$project")
    }
}
