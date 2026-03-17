# ftgo-common-jpa-lib

JPA utility classes, ORM mappings, and persistence configuration for the FTGO microservices platform.

## Version

**Current version:** `1.0.0`

## Contents

### ORM Mappings (`META-INF/orm.xml`)

Provides JPA `@Embeddable` mappings for shared value objects from `ftgo-common`:

| Class | Description |
|-------|-------------|
| `Money` | Maps `BigDecimal` amount field to a `amount` column via XML ORM mapping. |
| `Address` | Maps 5 address fields (street1, street2, city, state, zip) via XML ORM mapping. |

These XML mappings allow `Money` and `Address` to be embedded in JPA entities across all services without requiring each service to duplicate the mapping configuration.

## Package

```
net.chrisrichardson.ftgo
```

## Dependencies

| Dependency | Purpose |
|------------|---------|
| `ftgo-common` (project) | Value objects (`Money`, `Address`) mapped by ORM |
| `spring-boot-starter-data-jpa` | Spring Data JPA, Hibernate, JPA API |

## Usage

### Gradle dependency (from local Maven repository)

```groovy
dependencies {
    implementation 'net.chrisrichardson.ftgo:ftgo-common-jpa-lib:1.0.0'
}
```

### Intra-project dependency (within the mono-repo)

```groovy
dependencies {
    implementation project(":shared-libraries:ftgo-common-jpa")
}
```

### Publishing to local repository

```bash
./gradlew :shared-libraries:ftgo-common-jpa:publishToMavenLocal
```

Or publish to the project-level repository:

```bash
./gradlew :shared-libraries:ftgo-common-jpa:publish
```
