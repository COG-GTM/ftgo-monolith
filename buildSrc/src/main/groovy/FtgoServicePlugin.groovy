import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Marks a module as a runnable Spring Boot service: applies the Boot plugin (bootJar/bootRun)
 * and the dependency-management plugin. Both plugin ids resolve because the root build.gradle
 * puts them on the buildscript classpath.
 */
class FtgoServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.apply(plugin: 'org.springframework.boot')
    	project.apply(plugin: "io.spring.dependency-management")
    }
}
