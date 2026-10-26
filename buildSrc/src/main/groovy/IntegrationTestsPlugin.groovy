import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def sourceSets = project.sourceSets

        sourceSets.create('integrationTest') {
            java.srcDir project.file('src/integration-test/java')
            resources.srcDir project.file('src/integration-test/resources')
            compileClasspath += sourceSets.main.output + sourceSets.test.output
            runtimeClasspath += sourceSets.main.output + sourceSets.test.output
        }

        project.configurations {
            integrationTestImplementation.extendsFrom testImplementation
            integrationTestRuntimeOnly.extendsFrom testRuntimeOnly
        }

        project.tasks.register('integrationTest', Test) {
            testClassesDirs = sourceSets.integrationTest.output.classesDirs
            classpath = sourceSets.integrationTest.runtimeClasspath
            useJUnitPlatform()
        }

        project.tasks.withType(Test).configureEach {
            reports.html.outputLocation = project.file("${project.reporting.baseDirectory.get().asFile}/${name}")
        }
    }
}
