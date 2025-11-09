package dev.ktml.maven

import dev.ktml.KtmlProcessor
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import java.io.File

/**
 * Maven Mojo for generating Kotlin code from KTML templates.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
class KtmlGenerateMojo : AbstractMojo() {

    /**
     * The Maven project.
     */
    @Parameter(defaultValue = $$"${project}", readonly = true, required = true)
    private lateinit var project: MavenProject

    private val templateDir = "src/main/ktml"
    private val outputDir = "ktml/main"

    override fun execute() {
        try {
            val outputDirectory = "${project.build.directory}/$outputDir"

            log.info("Generating KTML code...")
            log.info("Template directory: $templateDir")
            log.info("Output directory: $outputDirectory")

            KtmlProcessor(outputDirectory = outputDirectory).apply {
                processRootDirectory(File(project.basedir, templateDir).absolutePath)
                generateTemplateCode()
            }

            project.addCompileSourceRoot(outputDirectory)

            log.info("KTML code generation completed successfully")
        } catch (e: Exception) {
            throw MojoExecutionException("Failed to generate KTML code", e)
        }
    }
}
