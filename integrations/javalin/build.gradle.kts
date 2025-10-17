import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.mavenPublish)
    kotlin("jvm")
}

kotlin {
    jvmToolchain(22)
}

dependencies {
    implementation(project(":runtime"))
    compileOnly(libs.javalin)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.ktool.kotest.bdd)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.javalin)
    testImplementation(libs.mockk)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

description = "Javalin plugin for KTML"

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaGeneratePublicationHtml)
    from(tasks.dokkaGeneratePublicationHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

mavenPublishing {
    val rootName = rootProject.name
    val gitHubOrg = project.property("github.org") as String
    val artifactId = "$rootName-${project.name}"
    val repoPath = "github.com/$gitHubOrg/$rootName"
    val projectUrl = "https://$repoPath"

    configure(
        KotlinJvm(
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
                url = "https://github.com/$gitHubOrg"
            }
        }
        scm {
            url = projectUrl
            connection = "scm:git:git://$repoPath.git"
            developerConnection = "scm:git:ssh://git@$repoPath.git"
        }
    }
}

