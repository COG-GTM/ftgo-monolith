import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test

class IntegrationTestsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.sourceSets {
            integrationTest {
                java {
                    compileClasspath += main.output + test.output
                    runtimeClasspath += main.output + test.output
                    srcDir project.file('src/integration-test/java')
                }
                resources.srcDir project.file('src/integration-test/resources')
            }
            endToEndTest {
                java {
                    compileClasspath += main.output + test.output
                    runtimeClasspath += main.output + test.output
                    srcDir project.file('src/end-to-end-test/java')
                }
                resources.srcDir project.file('src/end-to-end-test/resources')
            }
        }

        project.configurations {
            integrationTestCompile.extendsFrom testCompile
            integrationTestRuntime.extendsFrom testRuntime
            endToEndTestCompile.extendsFrom testCompile
            endToEndTestRuntime.extendsFrom testRuntime
        }

        project.task("integrationTest", type: Test) {
            testClassesDir = project.sourceSets.integrationTest.output.classesDir
            classpath = project.sourceSets.integrationTest.runtimeClasspath
        }

        project.task("endToEndTest", type: Test) {
            testClassesDir = project.sourceSets.endToEndTest.output.classesDir
            classpath = project.sourceSets.endToEndTest.runtimeClasspath
        }

        project.tasks.withType(Test) {
            reports.html.destination = project.file("${project.reporting.baseDir}/${name}")
        }
    }
}
