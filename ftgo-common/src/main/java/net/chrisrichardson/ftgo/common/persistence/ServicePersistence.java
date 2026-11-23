package net.chrisrichardson.ftgo.common.persistence;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Builds the persistence infrastructure of a service that owns its own database schema:
 * a DataSource pointing at the schema, migrated by the service's Flyway scripts, and an
 * EntityManagerFactory that only manages the service's entities.
 */
public class ServicePersistence {

  /**
   * Creates a DataSource for {@code schema} on the same MySQL server and credentials as
   * {@code spring.datasource}, unless {@code schemaUrl} is set, and applies the migrations
   * found in {@code migrationLocation}.
   */
  public static DataSource migratedDataSource(DataSourceProperties dataSourceProperties, String schemaUrl,
                                              String schema, String migrationLocation) {
    String url = schemaUrl == null || schemaUrl.isEmpty()
            ? replaceDatabaseName(dataSourceProperties.determineUrl(), schema)
            : schemaUrl;
    DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().url(url).build();
    Flyway flyway = new Flyway();
    flyway.setDataSource(dataSource);
    flyway.setLocations(migrationLocation);
    flyway.migrate();
    return dataSource;
  }

  static String replaceDatabaseName(String jdbcUrl, String schema) {
    return jdbcUrl.replaceFirst("/[^/?]+(\\?|$)", "/" + schema + "$1");
  }

  public static LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                            DataSource dataSource,
                                                                            JpaProperties jpaProperties,
                                                                            String persistenceUnit,
                                                                            Class<?>... packageClasses) {
    return builder
            .dataSource(dataSource)
            .packages(packageClasses)
            .persistenceUnit(persistenceUnit)
            .properties(jpaProperties.getHibernateProperties(new HibernateSettings()))
            .build();
  }

  public static WebMvcConfigurer openEntityManagerInView(EntityManagerFactory entityManagerFactory) {
    return new WebMvcConfigurer() {
      @Override
      public void addInterceptors(InterceptorRegistry registry) {
        OpenEntityManagerInViewInterceptor interceptor = new OpenEntityManagerInViewInterceptor();
        interceptor.setEntityManagerFactory(entityManagerFactory);
        registry.addWebRequestInterceptor(interceptor);
      }
    };
  }
}
