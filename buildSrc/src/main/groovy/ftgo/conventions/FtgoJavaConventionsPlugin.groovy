package ftgo.conventions

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Convention plugin: ftgo.java-conventions
 *
 * Establishes standard Java compilation settings for FTGO microservices:
 * - Java 17 source and target compatibility
 * - UTF-8 encoding for source files
 * - Recommended compiler warnings
 * - Standard source layout
 * - Common group and repositories
 */
class FtgoJavaConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply(plugin: 'java')

        project.group = FtgoVersions.FTGO_GROUP

        // Java version configuration
        project.sourceCompatibility = JavaVersion.VERSION_17
        project.targetCompatibility = JavaVersion.VERSION_17

        // Compiler options
        project.tasks.withType(JavaCompile) { JavaCompile task ->
            task.options.encoding = 'UTF-8'
            task.options.compilerArgs.addAll([
                '-parameters',          // Preserve method parameter names
                '-Xlint:unchecked',     // Warn about unchecked operations
                '-Xlint:deprecation'    // Warn about deprecated API usage
            ])
        }

        // Standard repositories
        project.repositories {
            mavenCentral()
        }

        // Common dependencies for all Java modules
        project.dependencies {
            // SLF4J API for logging
            implementation "org.slf4j:slf4j-api:${FtgoVersions.SLF4J}"

            // Lombok (compile-only with annotation processing)
            compileOnly "org.projectlombok:lombok:${FtgoVersions.LOMBOK}"
            annotationProcessor "org.projectlombok:lombok:${FtgoVersions.LOMBOK}"
        }
    }
}
