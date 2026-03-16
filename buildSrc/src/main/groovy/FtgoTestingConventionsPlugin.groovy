import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Convention plugin: ftgo.testing-conventions
 *
 * Configures testing defaults for FTGO microservices:
 * - JUnit 5 (Jupiter) test engine
 * - Rest-Assured for API testing
 * - Spring Boot test support
 * - Test logging and reporting configuration
 *
 * This plugin depends on ftgo.java-conventions and will apply it
 * automatically if not already applied.
 */
class FtgoTestingConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(FtgoJavaConventionsPlugin)

        project.dependencies {
            testCompile FtgoDependencies.SPRING_BOOT_STARTER_TEST
            testCompile FtgoDependencies.JUNIT_JUPITER_API
            testCompile FtgoDependencies.JUNIT_JUPITER_PARAMS
            testCompile FtgoDependencies.REST_ASSURED
            testCompile FtgoDependencies.REST_ASSURED_JSON_PATH

            testRuntime FtgoDependencies.JUNIT_JUPITER_ENGINE
        }

        project.tasks.withType(Test) {
            useJUnitPlatform()

            testLogging {
                events 'passed', 'skipped', 'failed'
                showStandardStreams = false
                exceptionFormat = 'full'
            }

            reports {
                html.enabled = true
                junitXml.enabled = true
            }
        }
    }
}
