import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm

plugins {
    alias(libs.plugins.dokka)
    alias(libs.plugins.vanniktech.mavenPublish)
    kotlin("jvm")
}

kotlin {
    jvmToolchain(22)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":runtime"))
    api(project(":generator"))
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.reflect)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.framework.engine)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktool.kotest.bdd)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

description = "Developer mode for KTML"

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
