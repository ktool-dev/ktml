plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

kotlin {
    jvmToolchain(22)
}

group = project.property("group").toString()
version = project.property("version").toString()
description = "Maven plugin to generated Koltin code from KTML templates"

dependencies {
    implementation(project(":generator"))
    compileOnly(libs.maven.plugin.api)
    compileOnly(libs.maven.core)
    compileOnly(libs.maven.plugin.annotations)
    annotationProcessor(libs.maven.plugin.annotations)
}

signing {
    useInMemoryPgpKeys(System.getenv("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
    sign(publishing.publications)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            val rootName = rootProject.name
            val orgUrl = project.property("scm.org.url") as String
            val repoPath = project.property("scm.repo.path") as String
            val projectUrl = "https://$repoPath"

            groupId = project.group.toString()
            artifactId = "$rootName-${project.name}"
            version = project.version.toString()

            from(components["java"])

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
    }

    repositories {
        maven {
            name = "mavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("CENTRAL_PORTAL_USERNAME")
                password = System.getenv("CENTRAL_PORTAL_PASSWORD")
            }
        }
    }
}
