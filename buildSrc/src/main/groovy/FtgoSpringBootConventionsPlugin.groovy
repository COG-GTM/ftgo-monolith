import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin: ftgo.spring-boot-conventions
 *
 * Applies Spring Boot and dependency management plugins, and configures
 * common Spring Boot dependencies for FTGO microservices.
 *
 * This plugin depends on ftgo.java-conventions and will apply it
 * automatically if not already applied.
 */
class FtgoSpringBootConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(FtgoJavaConventionsPlugin)

        project.apply(plugin: 'org.springframework.boot')
        project.apply(plugin: 'io.spring.dependency-management')
    }
}
