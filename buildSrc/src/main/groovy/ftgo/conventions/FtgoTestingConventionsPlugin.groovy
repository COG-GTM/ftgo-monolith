package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

/**
 * Convention plugin: ftgo.testing-conventions
 *
 * Standardizes test configuration for FTGO microservices:
 * - JUnit 5 (Jupiter) as the test engine
 * - Rest-Assured for REST API testing
 * - Mockito for mocking
 * - AssertJ for fluent assertions
 * - Test logging configuration
 * - Integration test source set
 */
class FtgoTestingConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Ensure java plugin is applied
        project.pluginManager.apply('java')

        // Test dependencies
        project.dependencies {
            // JUnit 5
            testImplementation "org.junit.jupiter:junit-jupiter:${FtgoVersions.JUNIT_JUPITER}"
            testImplementation "org.junit.jupiter:junit-jupiter-api:${FtgoVersions.JUNIT_JUPITER}"
            testRuntimeOnly "org.junit.jupiter:junit-jupiter-engine:${FtgoVersions.JUNIT_JUPITER}"

            // Spring Boot Test
            testImplementation "org.springframework.boot:spring-boot-starter-test:${FtgoVersions.SPRING_BOOT}"

            // Rest-Assured
            testImplementation "io.rest-assured:rest-assured:${FtgoVersions.REST_ASSURED}"
            testImplementation "io.rest-assured:spring-mock-mvc:${FtgoVersions.REST_ASSURED}"

            // Mockito
            testImplementation "org.mockito:mockito-core:${FtgoVersions.MOCKITO}"
            testImplementation "org.mockito:mockito-junit-jupiter:${FtgoVersions.MOCKITO}"

            // AssertJ
            testImplementation "org.assertj:assertj-core:${FtgoVersions.ASSERTJ}"

            // Lombok support in tests
            testCompileOnly "org.projectlombok:lombok:${FtgoVersions.LOMBOK}"
            testAnnotationProcessor "org.projectlombok:lombok:${FtgoVersions.LOMBOK}"
        }

        // Configure test tasks to use JUnit 5
        project.tasks.withType(Test) { Test task ->
            task.useJUnitPlatform()

            // Test logging
            task.testLogging {
                events TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
                exceptionFormat TestExceptionFormat.FULL
                showExceptions true
                showCauses true
                showStackTraces true
            }

            // Fail fast on test failures
            task.failFast = false

            // JVM args for tests
            task.jvmArgs = [
                '-XX:+UseG1GC',
                '-Xmx512m'
            ]
        }

        // Register integration test source set
        project.sourceSets {
            integrationTest {
                java {
                    compileClasspath += project.sourceSets.main.output + project.sourceSets.test.output
                    runtimeClasspath += project.sourceSets.main.output + project.sourceSets.test.output
                    srcDir project.file('src/integration-test/java')
                }
                resources.srcDir project.file('src/integration-test/resources')
            }
        }

        project.configurations {
            integrationTestImplementation.extendsFrom testImplementation
            integrationTestRuntimeOnly.extendsFrom testRuntimeOnly
        }

        // Register integration test task
        project.task('integrationTest', type: Test) {
            description = 'Runs integration tests.'
            group = 'verification'
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
            classpath = project.sourceSets.integrationTest.runtimeClasspath
            shouldRunAfter project.tasks.test

            useJUnitPlatform()

            testLogging {
                events TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
                exceptionFormat TestExceptionFormat.FULL
            }
        }
    }
}
