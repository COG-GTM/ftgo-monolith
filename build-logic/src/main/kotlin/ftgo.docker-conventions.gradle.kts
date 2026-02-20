plugins {
    id("ftgo.java-conventions")
    id("com.google.cloud.tools.jib")
}

val dockerRegistry = providers.gradleProperty("dockerRegistry").orElse("ftgo")
val dockerTag = providers.gradleProperty("dockerTag").orElse("latest")

jib {
    from {
        image = "eclipse-temurin:17-jre-alpine"
    }
    to {
        image = "${dockerRegistry.get()}/${project.name}:${dockerTag.get()}"
        tags = setOf("latest", project.version.toString())
    }
    container {
        jvmFlags = listOf(
            "-XX:+UseContainerSupport",
            "-XX:MaxRAMPercentage=75.0",
            "-Djava.security.egd=file:/dev/./urandom"
        )
        ports = listOf("8080")
        creationTime.set("USE_CURRENT_TIMESTAMP")
        labels.set(mapOf(
            "org.opencontainers.image.source" to "https://github.com/COG-GTM/ftgo-monolith",
            "org.opencontainers.image.title" to project.name
        ))
    }
}
