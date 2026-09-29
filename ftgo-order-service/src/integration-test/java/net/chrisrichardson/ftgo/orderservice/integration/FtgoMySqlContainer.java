package net.chrisrichardson.ftgo.orderservice.integration;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * A single MySQL container shared by every integration test in the JVM.
 * It is started lazily on first use and removed by Testcontainers (Ryuk) when the JVM exits.
 */
public final class FtgoMySqlContainer {

  public static final String IMAGE_PROPERTY = "ftgo.it.mysql.image";
  private static final String DEFAULT_IMAGE = "mysql:5.7";

  private static final MySQLContainer<?> CONTAINER = new MySQLContainer<>(
          DockerImageName.parse(System.getProperty(IMAGE_PROPERTY, DEFAULT_IMAGE)).asCompatibleSubstituteFor("mysql"))
          .withDatabaseName("ftgo")
          .withUsername("mysqluser")
          .withPassword("mysqlpw");

  static {
    CONTAINER.start();
  }

  private FtgoMySqlContainer() {
  }

  public static MySQLContainer<?> getInstance() {
    return CONTAINER;
  }

  /**
   * Points Spring at the container and makes Hibernate validate the entity mappings
   * against the schema created by the ftgo-flyway migrations instead of generating DDL.
   */
  public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
      TestPropertyValues.of(
              "spring.datasource.url=" + CONTAINER.getJdbcUrl(),
              "spring.datasource.username=" + CONTAINER.getUsername(),
              "spring.datasource.password=" + CONTAINER.getPassword(),
              "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
              "spring.flyway.enabled=true",
              "spring.flyway.locations=classpath:db/migration",
              "spring.jpa.generate-ddl=false",
              "spring.jpa.hibernate.ddl-auto=validate",
              "logging.level.org.hibernate.SQL=INFO"
      ).applyTo(context.getEnvironment());
    }
  }
}
