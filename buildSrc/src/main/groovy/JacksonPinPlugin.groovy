import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Pins all Jackson artifacts to a version without known CVEs. The Spring Boot
 * BOM applied by io.spring.dependency-management would otherwise manage
 * Jackson down to the vulnerable 2.9.x line (CVE-2020-36518).
 */
class JacksonPinPlugin implements Plugin<Project> {

    static final String JACKSON_VERSION = '2.18.9'

    @Override
    void apply(Project project) {
        project.configurations.all {
            resolutionStrategy.eachDependency { details ->
                if (details.requested.group.startsWith('com.fasterxml.jackson')
                        && details.requested.name != 'jackson-bom') {
                    details.useVersion(JACKSON_VERSION)
                }
            }
        }
    }
}
