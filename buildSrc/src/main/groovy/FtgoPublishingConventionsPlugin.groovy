import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * Convention plugin: ftgo.publishing-conventions
 *
 * Configures Maven publishing for FTGO shared libraries.
 * Publishes artifacts to a local repository by default,
 * with support for configuring remote repositories.
 *
 * Published artifact coordinates:
 *   groupId:    com.ftgo
 *   artifactId: <project.name>
 *   version:    <project.version>
 */
class FtgoPublishingConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(FtgoJavaConventionsPlugin)
        project.apply(plugin: 'maven-publish')

        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    from project.components.java

                    groupId = project.group
                    artifactId = project.name
                    version = project.version
                }
            }

            repositories {
                maven {
                    name = 'local'
                    url = "${project.rootProject.buildDir}/repo"
                }
            }
        }
    }
}
