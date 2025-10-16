import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.registering

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

configure<MavenPublishBaseExtension> {
    val domain = "ktml.dev"
    val gitHubOrg = domain.replace(".", "-")
    val groupId = domain.split(".").reversed().joinToString(".")
    val version = "0.0.0"
    val projectUrl = "https://github.com/$gitHubOrg/${project.name}"

    configure(
        KotlinMultiplatform(
            javadocJar = JavadocJar.Dokka("javadocJar"),
            sourcesJar = true,
            androidVariantsToPublish = listOf("release"),
        )
    )

    publishToMavenCentral()

    signAllPublications()

    coordinates(groupId, project.name, version)

    pom {
        name = project.name
        description = project.description ?: "KTML - Kotlin HTML templating"
        inceptionYear = "2025"
        url = projectUrl
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = findProperty("dev.id") as String? ?: "unknown"
                name = findProperty("dev.name") as String? ?: "KTML Developers"
                email = findProperty("dev.email") as String? ?: ""
                url = "https://github.com/${findProperty("dev.id") ?: "ktml-dev"}"
            }
        }
        scm {
            url = "https://github.com/$gitHubOrg/ktml/"
            connection = "scm:git:git://github.com/$gitHubOrg/ktml.git"
            developerConnection = "scm:git:ssh://git@github.com/$gitHubOrg/ktml.git"
        }
    }
}
