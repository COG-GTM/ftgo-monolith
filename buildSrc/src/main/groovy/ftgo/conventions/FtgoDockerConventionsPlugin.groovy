package ftgo.conventions

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin: ftgo.docker-conventions
 *
 * Configures Docker image build settings for FTGO microservices.
 * This plugin:
 * - Defines a standard Docker image naming convention
 * - Registers tasks for building Docker images
 * - Configures image labels and tags
 *
 * NOTE: For full Jib integration (Google's container image builder),
 * the Jib Gradle plugin requires Gradle 7.5+. This plugin currently
 * sets up the conventions and a Dockerfile-based build task.
 * When Gradle is upgraded, apply the Jib plugin:
 *   plugins { id 'com.google.cloud.tools.jib' version '3.4.1' }
 */
class FtgoDockerConventionsPlugin implements Plugin<Project> {

    /** Default Docker registry. Override via project property 'dockerRegistry'. */
    static final String DEFAULT_REGISTRY = 'ftgo'

    @Override
    void apply(Project project) {
        // Ensure java plugin is applied
        project.pluginManager.apply('java')

        // Docker image metadata (can be overridden via project properties)
        project.ext.set('dockerImageName', resolveImageName(project))
        project.ext.set('dockerImageTag', resolveImageTag(project))

        // Register Docker build task
        project.task('dockerBuild') {
            group = 'docker'
            description = "Builds a Docker image for ${project.name}"
            dependsOn project.tasks.findByName('jar') ?: project.tasks.findByName('bootJar')

            doLast {
                def imageName = project.ext.get('dockerImageName')
                def imageTag = project.ext.get('dockerImageTag')
                def fullImageName = "${imageName}:${imageTag}"

                project.logger.lifecycle("Docker image name: ${fullImageName}")
                project.logger.lifecycle("To build with Docker CLI:")
                project.logger.lifecycle("  docker build -t ${fullImageName} .")
                project.logger.lifecycle("")
                project.logger.lifecycle("When Gradle is upgraded to 7.5+, apply the Jib plugin for")
                project.logger.lifecycle("containerized builds without a Docker daemon.")

                // Check for Dockerfile
                def dockerfile = project.file('Dockerfile')
                if (dockerfile.exists()) {
                    project.exec {
                        commandLine 'docker', 'build',
                            '-t', fullImageName,
                            '--label', "org.opencontainers.image.source=${project.group}",
                            '--label', "org.opencontainers.image.version=${imageTag}",
                            '.'
                    }
                } else {
                    project.logger.warn("No Dockerfile found in ${project.projectDir}.")
                    project.logger.warn("Create a Dockerfile or upgrade Gradle to use Jib plugin.")
                }
            }
        }

        // Register Docker push task
        project.task('dockerPush') {
            group = 'docker'
            description = "Pushes the Docker image for ${project.name} to the registry"
            dependsOn 'dockerBuild'

            doLast {
                def imageName = project.ext.get('dockerImageName')
                def imageTag = project.ext.get('dockerImageTag')
                def fullImageName = "${imageName}:${imageTag}"

                project.exec {
                    commandLine 'docker', 'push', fullImageName
                }
            }
        }
    }

    /**
     * Resolves the Docker image name from project properties or conventions.
     * Format: {registry}/{project.name}
     */
    private static String resolveImageName(Project project) {
        def registry = project.hasProperty('dockerRegistry')
            ? project.property('dockerRegistry')
            : DEFAULT_REGISTRY
        return "${registry}/${project.name}"
    }

    /**
     * Resolves the Docker image tag from project properties or version.
     */
    private static String resolveImageTag(Project project) {
        if (project.hasProperty('dockerTag')) {
            return project.property('dockerTag')
        }
        return project.version != 'unspecified' ? project.version.toString() : 'latest'
    }
}
