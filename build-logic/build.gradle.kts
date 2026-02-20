plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-gradle-plugin:3.2.5")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.4")
    implementation("com.google.cloud.tools:jib-gradle-plugin:3.4.2")
}
