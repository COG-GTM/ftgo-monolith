import org.gradle.api.Plugin
import org.gradle.api.Project

class FtgoServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.apply(plugin: 'org.springframework.boot')
    	project.apply(plugin: "io.spring.dependency-management")

        // Pin Jackson across all configurations: the Spring Boot BOM applied by
        // io.spring.dependency-management would otherwise manage it down to the
        // vulnerable 2.9.x line (CVE-2020-36518).
        project.configurations.all {
            resolutionStrategy.eachDependency { details ->
                if (details.requested.group.startsWith('com.fasterxml.jackson')
                        && details.requested.name != 'jackson-bom') {
                    details.useVersion(details.requested.name == 'jackson-databind'
                            ? project.property('jacksonDatabindVersion')
                            : project.property('jacksonVersion'))
                }
            }
        }
    }
}
