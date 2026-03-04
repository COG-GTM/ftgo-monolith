package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Convention plugin: ftgo.quality-gate-conventions
 *
 * Configures static analysis and code coverage tools for FTGO microservices:
 * - Checkstyle: Code style enforcement (Google-based style)
 * - PMD: Static analysis for common bugs and best practices
 * - FindBugs: Bytecode-level bug detection
 * - JaCoCo: Code coverage with 70% minimum threshold
 *
 * Configuration files are located in the root project's config/ directory:
 *   config/checkstyle/checkstyle.xml
 *   config/checkstyle/suppressions.xml
 *   config/pmd/pmd-ruleset.xml
 *   config/spotbugs/spotbugs-exclude.xml
 *
 * Reports are generated in each module's build/reports/ directory.
 *
 * NOTE: This plugin uses Gradle built-in quality plugins compatible with
 * Gradle 4.10.x. When Gradle is upgraded to 7.5+, migrate FindBugs to
 * the SpotBugs Gradle plugin (com.github.spotbugs) and update tool versions:
 *   - Checkstyle -> 10.x (requires Java 11+)
 *   - PMD -> 7.x (requires Java 11+)
 *   - FindBugs -> SpotBugs 4.x
 *   - JaCoCo -> 0.8.12
 */
class FtgoQualityGatePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // Ensure java plugin is applied
        project.pluginManager.apply('java')

        configureCheckstyle(project)
        configurePmd(project)
        configureFindBugs(project)
        configureJacoco(project)

        // Register a composite quality gate task
        project.task('qualityGate') {
            description = 'Runs all quality gate checks (Checkstyle, PMD, FindBugs, JaCoCo).'
            group = 'verification'
            dependsOn 'checkstyleMain', 'pmdMain', 'findbugsMain', 'jacocoTestCoverageVerification'
        }
    }

    private void configureCheckstyle(Project project) {
        project.pluginManager.apply('checkstyle')

        project.checkstyle {
            // Checkstyle 8.45.1 is the last version fully supporting Java 8
            toolVersion = '8.45.1'
            configFile = project.rootProject.file('config/checkstyle/checkstyle.xml')
            configProperties = [
                'suppressionFile': project.rootProject.file('config/checkstyle/suppressions.xml').absolutePath
            ]
            maxWarnings = 0
            maxErrors = 0
            // Set to true during migration to avoid blocking builds on legacy code.
            // The CI quality gate workflow enforces violations separately.
            // Set to false once existing violations are resolved.
            ignoreFailures = true
        }

        // Generate HTML reports alongside XML
        project.tasks.withType(Checkstyle) { Checkstyle task ->
            task.reports {
                xml.enabled = true
                html.enabled = true
            }
        }
    }

    private void configurePmd(Project project) {
        project.pluginManager.apply('pmd')

        project.pmd {
            // PMD 6.55.0 is the last version supporting Java 8
            toolVersion = '6.55.0'
            ruleSetFiles = project.rootProject.files('config/pmd/pmd-ruleset.xml')
            ruleSets = [] // Clear default rulesets, use only our custom one
            consoleOutput = true
            // Set to true during migration to avoid blocking builds on legacy code.
            ignoreFailures = true
        }

        project.tasks.withType(Pmd) { Pmd task ->
            task.reports {
                xml.enabled = true
                html.enabled = true
            }
        }
    }

    private void configureFindBugs(Project project) {
        project.pluginManager.apply('findbugs')

        project.findbugs {
            toolVersion = '3.0.1'
            effort = 'max'
            reportLevel = 'medium'
            excludeFilter = project.rootProject.file('config/spotbugs/spotbugs-exclude.xml')
            // Set to true during migration to avoid blocking builds on legacy code.
            ignoreFailures = true
        }

        // FindBugs in Gradle 4.x supports only one report type at a time
        project.tasks.withType(FindBugs) { FindBugs task ->
            task.reports {
                xml.enabled = false
                html.enabled = true
            }
            // Skip FindBugs if there are no class files to analyze
            // (e.g., modules with only package-info.java)
            task.onlyIf {
                !task.classes.filter { it.name.endsWith('.class') && !it.name.equals('package-info.class') }.isEmpty()
            }
        }
    }

    private void configureJacoco(Project project) {
        project.pluginManager.apply('jacoco')

        project.jacoco {
            // JaCoCo 0.8.8 supports Java 8 bytecode
            toolVersion = '0.8.8'
        }

        // Configure JaCoCo report generation after tests
        project.tasks.withType(JacocoReport) { JacocoReport task ->
            task.dependsOn project.tasks.test
            task.reports {
                xml.enabled = true
                html.enabled = true
                csv.enabled = false
            }
        }

        // Configure coverage verification with 70% minimum line coverage
        project.jacocoTestCoverageVerification {
            dependsOn project.tasks.jacocoTestReport
            violationRules {
                rule {
                    limit {
                        counter = 'LINE'
                        value = 'COVEREDRATIO'
                        minimum = 0.70
                    }
                }
                rule {
                    limit {
                        counter = 'BRANCH'
                        value = 'COVEREDRATIO'
                        minimum = 0.60
                    }
                }
            }
        }

        // Ensure JaCoCo report runs after test
        project.tasks.test.finalizedBy project.tasks.jacocoTestReport
    }
}
