import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

/**
 * Adds an `integrationTest` source set (src/integration-test/java|resources) plus an
 * `integrationTest` task that runs it, sharing the main and test outputs/classpaths.
 * Uses the Gradle 8 configuration names (…Implementation / …RuntimeOnly).
 */
class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def sourceSets = project.extensions.getByName('sourceSets')
        def main = sourceSets.getByName('main')
        def test = sourceSets.getByName('test')

        def integrationTest = sourceSets.create('integrationTest') {
            java.srcDir project.file('src/integration-test/java')
            resources.srcDir project.file('src/integration-test/resources')
            compileClasspath += main.output + test.output
            runtimeClasspath += main.output + test.output
        }

        // Integration tests see every dependency the unit tests see.
        project.configurations.named('integrationTestImplementation') { it.extendsFrom(project.configurations.testImplementation) }
        project.configurations.named('integrationTestRuntimeOnly') { it.extendsFrom(project.configurations.testRuntimeOnly) }

        project.tasks.register('integrationTest', Test) {
            description = 'Runs the integration tests.'
            group = 'verification'
            testClassesDirs = integrationTest.output.classesDirs
            classpath = integrationTest.runtimeClasspath
        }

        // One HTML report directory per Test task (build/reports/test, build/reports/integrationTest, ...).
        project.tasks.withType(Test).configureEach {
            reports.html.outputLocation = project.layout.buildDirectory.dir("reports/${name}")
        }
    }
}
