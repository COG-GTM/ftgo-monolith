plugins {
    id("ftgo.java-conventions")
    jacoco
}

dependencies {
    "testImplementation"("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    "testImplementation"("org.junit.jupiter:junit-jupiter-api")
    "testImplementation"("org.junit.jupiter:junit-jupiter-params")
    "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine")
    "testImplementation"("org.mockito:mockito-core")
    "testImplementation"("org.mockito:mockito-junit-jupiter")
    "testImplementation"("org.assertj:assertj-core")
    "testImplementation"("io.rest-assured:rest-assured")
    "testImplementation"("io.rest-assured:spring-mock-mvc")

    "testImplementation"("org.testcontainers:junit-jupiter")
    "testImplementation"("org.testcontainers:mysql")
    "testImplementation"("org.testcontainers:kafka")
    "testImplementation"("org.testcontainers:testcontainers")
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading")
    finalizedBy(tasks.withType<JacocoReport>())
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

val integrationTest by tasks.registering(Test::class) {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    shouldRunAfter(tasks.named("test"))
    useJUnitPlatform {
        includeTags("integration")
    }
}

val jacocoIntegrationTestReport by tasks.registering(JacocoReport::class) {
    description = "Generates JaCoCo report for integration tests."
    group = "verification"
    executionData(integrationTest.get())
    sourceDirectories.from(sourceSets["main"].java.srcDirs)
    classDirectories.from(sourceSets["main"].output.classesDirs)
    dependsOn(integrationTest)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.named("check") {
    dependsOn(integrationTest)
}
