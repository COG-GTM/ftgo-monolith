package net.chrisrichardson.ftgo.common.tracking;

import net.chrisrichardson.ftgo.common.persistence.ServicePersistence;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Primary persistence unit, backed by {@code spring.datasource}, which holds the API tracking log.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = ApiRequestLogRepository.class)
public class TrackingPersistenceConfiguration {

  @Bean
  @Primary
  public DataSource dataSource(DataSourceProperties dataSourceProperties) {
    return ServicePersistence.migratedDataSource(dataSourceProperties, dataSourceProperties.determineUrl(),
            null, "classpath:db/migration/tracking");
  }

  @Bean
  @Primary
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                     DataSource dataSource,
                                                                     JpaProperties jpaProperties) {
    return ServicePersistence.entityManagerFactory(builder, dataSource, jpaProperties, "tracking", ApiRequestLog.class);
  }

  @Bean
  @Primary
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }
}
