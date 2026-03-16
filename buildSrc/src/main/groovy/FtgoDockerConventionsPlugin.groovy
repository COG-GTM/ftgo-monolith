import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin: ftgo.docker-conventions
 *
 * Configures Docker image build tasks for FTGO microservices.
 * Creates a 'buildDockerImage' task that builds a Docker image
 * using the Dockerfile in the service's docker/ directory.
 *
 * Expected directory layout:
 *   services/<service-name>/docker/Dockerfile
 *
 * The image is tagged as: ftgo/<service-name>:<version>
 */
class FtgoDockerConventionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply(FtgoJavaConventionsPlugin)

        project.ext.set('dockerImageName', "ftgo/${project.name}")
        project.ext.set('dockerTag', project.version ?: 'latest')

        project.task('buildDockerImage', type: org.gradle.api.tasks.Exec) {
            group = 'docker'
            description = 'Builds the Docker image for this service'

            def dockerDir = project.file('docker')
            def dockerFile = new File(dockerDir, 'Dockerfile')

            onlyIf { dockerFile.exists() }

            dependsOn project.tasks.findByName('bootRepackage') ?: project.tasks.findByName('jar')

            workingDir project.projectDir
            commandLine 'docker', 'build',
                    '-t', "${project.ext.dockerImageName}:${project.ext.dockerTag}",
                    '-f', dockerFile.absolutePath,
                    '.'
        }

        project.task('pushDockerImage', type: org.gradle.api.tasks.Exec) {
            group = 'docker'
            description = 'Pushes the Docker image to the registry'

            onlyIf { project.file('docker/Dockerfile').exists() }

            dependsOn 'buildDockerImage'

            commandLine 'docker', 'push',
                    "${project.ext.dockerImageName}:${project.ext.dockerTag}"
        }
    }
}
