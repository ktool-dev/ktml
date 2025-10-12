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

    /**
     * Module name for the templates (optional).
     */
    @Parameter(property = "ktml.moduleName", defaultValue = "")
    private var moduleName: String = ""

    /**
     * List of template directories to process.
     */
    @Parameter(property = "ktml.templateDirectories")
    private var templateDirectories: List<String>? = null

    /**
     * Output directory for generated Kotlin source files.
     */
    @Parameter(
        property = "ktml.outputDirectory",
        defaultValue = $$"${project.build.directory}/generated-sources/ktml"
    )
    private lateinit var outputDirectory: File

    override fun execute() {
        try {
            // Use default template directories if not specified
            val dirs = templateDirectories ?: listOf("src/main/ktml")

            // Ensure output directory exists
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs()
            }

            log.info("Generating KTML code...")
            log.info("Module name: $moduleName")
            log.info("Template directories: $dirs")
            log.info("Output directory: ${outputDirectory.absolutePath}")

            val processor = KtmlProcessor(moduleName = moduleName, outputDirectory = outputDirectory.absolutePath)
            val absoluteDirs = dirs.map { File(project.basedir, it).absolutePath }

            processor.processRootDirectories(absoluteDirs)
            processor.generateTemplateCode()
            project.addCompileSourceRoot(outputDirectory.absolutePath)

            log.info("KTML code generation completed successfully")
        } catch (e: Exception) {
            throw MojoExecutionException("Failed to generate KTML code", e)
        }
    }
}
