package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

/**
 * Convention plugin: ftgo.publishing-conventions
 *
 * Configures Maven publishing for FTGO shared library modules.
 * This plugin:
 * - Applies the maven-publish plugin
 * - Configures a standard Maven publication
 * - Sets up POM metadata
 * - Configures local and remote repository targets
 */
class FtgoPublishingConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('maven-publish')

        project.afterEvaluate {
            project.publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from project.components.java

                        groupId = FtgoVersions.FTGO_GROUP
                        artifactId = project.name
                        version = project.version

                        pom {
                            name = project.name
                            description = "FTGO Microservices - ${project.name}"
                            url = 'https://github.com/COG-GTM/ftgo-monolith'

                            licenses {
                                license {
                                    name = 'The Apache License, Version 2.0'
                                    url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                                }
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        name = 'local'
                        url = "${project.rootDir}/build/repo"
                    }

                    // Remote repository (configure via gradle.properties or environment)
                    // Uncomment and configure when a remote repository is available:
                    // maven {
                    //     name = 'remote'
                    //     url = project.findProperty('publishUrl') ?: 'https://maven.pkg.github.com/COG-GTM/ftgo-monolith'
                    //     credentials {
                    //         username = project.findProperty('publishUsername') ?: System.getenv('PUBLISH_USERNAME')
                    //         password = project.findProperty('publishPassword') ?: System.getenv('PUBLISH_PASSWORD')
                    //     }
                    // }
                }
            }
        }
    }
}
