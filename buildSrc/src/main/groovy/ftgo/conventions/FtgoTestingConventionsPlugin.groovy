package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoReport

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
 * - JaCoCo code coverage (target: 70%+)
 * - Parallel test execution for faster feedback
 */
class FtgoTestingConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Ensure java plugin is applied
        project.pluginManager.apply('java')

        // Apply JaCoCo plugin for code coverage
        project.pluginManager.apply(JacocoPlugin)

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

        // Configure JaCoCo
        project.jacoco {
            toolVersion = '0.8.11'
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

            // Parallel test execution for faster feedback
            task.maxParallelForks = Runtime.runtime.availableProcessors().intdiv(2) ?: 1

            // Fork a new JVM every N tests to prevent memory leaks
            task.forkEvery = 100

            // JVM args for tests
            task.jvmArgs = [
                '-XX:+UseG1GC',
                '-Xmx512m'
            ]
        }

        // Configure JaCoCo test report (Gradle 4.x compatible: use 'enabled' not 'required')
        project.tasks.withType(JacocoReport) { JacocoReport reportTask ->
            reportTask.reports {
                xml.enabled = true
                html.enabled = true
                csv.enabled = false
            }
        }

        // Configure jacocoTestReport to run after test
        project.afterEvaluate {
            def jacocoTestReport = project.tasks.findByName('jacocoTestReport')
            if (jacocoTestReport) {
                def testTask = project.tasks.findByName('test')
                if (testTask) {
                    testTask.finalizedBy jacocoTestReport
                }
            }
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

        // Register JaCoCo report for integration tests
        project.task('jacocoIntegrationTestReport', type: JacocoReport) {
            description = 'Generates JaCoCo code coverage report for integration tests.'
            group = 'verification'
            executionData project.tasks.integrationTest
            sourceSets project.sourceSets.main
            reports {
                xml.enabled = true
                html.enabled = true
                csv.enabled = false
            }
        }

        // Wire integrationTest -> jacocoIntegrationTestReport
        project.afterEvaluate {
            def integrationTestTask = project.tasks.findByName('integrationTest')
            def jacocoIntReport = project.tasks.findByName('jacocoIntegrationTestReport')
            if (integrationTestTask && jacocoIntReport) {
                integrationTestTask.finalizedBy jacocoIntReport
            }
        }
    }
}
