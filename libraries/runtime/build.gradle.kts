import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinMultiplatform

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest)
    alias(libs.plugins.ksp)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.dokka)
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(22)

    jvm {
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
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.framework.engine)
            implementation(libs.kotest.assertions.core)
            implementation(libs.ktool.kotest.bdd)
        }

        jvmMain.dependencies {
            implementation(libs.kotlin.logging)
        }

        jvmTest.dependencies {
            implementation(libs.kotest.runner.junit5)
        }
    }
}

object DeployConfig {
    const val ARTIFACT = "ktml-runtime"
    const val NAME = "KTML Runtime Library"
    const val DESCRIPTION = "The runtime library used by KTML"
    const val INCEPTION_YEAR = "2025"
    const val DEV_ID = "aaronfreeman"
    const val DEV_NAME = "Aaron Freeman"
    const val DEV_EMAIL = "aaron@ktool.dev"
}

val javadocJar by tasks.registering(Jar::class) {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

mavenPublishing {
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

    coordinates(groupId, DeployConfig.ARTIFACT, version)

    pom {
        name = DeployConfig.NAME
        description = DeployConfig.DESCRIPTION
        inceptionYear = DeployConfig.INCEPTION_YEAR
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
                id = DeployConfig.DEV_ID
                name = DeployConfig.DEV_NAME
                email = DeployConfig.DEV_EMAIL
                url = "https://github.com/${DeployConfig.DEV_ID}"
            }
        }
        scm {
            url = "https://github.com/$gitHubOrg/$groupId/"
            connection = "scm:git:git://github.com/$gitHubOrg/$groupId.git"
            developerConnection = "scm:git:ssh://git@github.com/$gitHubOrg/$groupId.git"
        }
    }
}

