import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvmToolchain(22)

    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }

    applyDefaultHierarchyTemplate()

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":runtime"))
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.server.core)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
            implementation(libs.ktool.kotest.bdd)
            implementation(libs.kotest.assertions.core)
            implementation(libs.ktor.server.cio)
            implementation(libs.kotlin.test)
            implementation(libs.mockk)
        }
    }
}

description = "Ktor plugin for KTML"

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

mavenPublishing {
    val rootName = rootProject.name
    val orgUrl = project.property("scm.org.url") as String
    val artifactId = "$rootName-${project.name}"
    val repoPath = project.property("scm.repo.path") as String
    val projectUrl = "https://$repoPath"

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("javadocJar"),
            sourcesJar = true,
        )
    )

    publishToMavenCentral()

    signAllPublications()

    coordinates(project.group.toString(), artifactId, project.version.toString())

    pom {
        name = artifactId
        description = project.description
        inceptionYear = project.property("inception.year") as String
        url = projectUrl
        licenses {
            license {
                name = project.property("license.name") as String
                url = project.property("license.url") as String
                distribution = project.property("license.url") as String
            }
        }
        developers {
            developer {
                id = project.property("developer.id") as String
                name = project.property("developer.name") as String
                email = project.property("developer.email") as String
                url = orgUrl
            }
        }
        scm {
            url = projectUrl
            connection = "scm:git:git://$repoPath.git"
            developerConnection = "scm:git:ssh://git@$repoPath.git"
        }
    }
}
