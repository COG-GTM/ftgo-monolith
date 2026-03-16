import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

/**
 * Convention plugin: ftgo.java-conventions
 *
 * Applies standard Java build settings for all FTGO microservices:
 * - Java 8 source/target compatibility
 * - UTF-8 encoding for source files
 * - Compiler warnings enabled
 * - Standard group and repositories
 */
class FtgoJavaConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply(plugin: 'java')

        project.sourceCompatibility = '1.8'
        project.targetCompatibility = '1.8'

        project.group = 'com.ftgo'

        project.repositories {
            mavenCentral()
        }

        project.tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
            options.compilerArgs << '-Xlint:unchecked'
            options.compilerArgs << '-Xlint:deprecation'
        }
    }
}
