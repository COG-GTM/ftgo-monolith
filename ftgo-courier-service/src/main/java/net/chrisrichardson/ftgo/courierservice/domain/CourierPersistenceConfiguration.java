package net.chrisrichardson.ftgo.courierservice.domain;

import net.chrisrichardson.ftgo.common.persistence.ServicePersistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * Persistence unit of the courier service, stored in its own ftgo_courier schema.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = CourierRepository.class,
        entityManagerFactoryRef = "courierEntityManagerFactory",
        transactionManagerRef = CourierPersistenceConfiguration.TRANSACTION_MANAGER)
public class CourierPersistenceConfiguration {

  public static final String TRANSACTION_MANAGER = "courierTransactionManager";

  @Bean
  public DataSource courierDataSource(DataSourceProperties dataSourceProperties,
                                  @Value("${ftgo.courier-service.datasource.url:}") String url) {
    return ServicePersistence.migratedDataSource(dataSourceProperties, url,
            "ftgo_courier", "classpath:db/migration/courier");
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean courierEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                        @Qualifier("courierDataSource") DataSource dataSource,
                                                                        JpaProperties jpaProperties) {
    return ServicePersistence.entityManagerFactory(builder, dataSource, jpaProperties, "courier", Courier.class);
  }

  @Bean(TRANSACTION_MANAGER)
  public PlatformTransactionManager courierTransactionManager(@Qualifier("courierEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean
  public WebMvcConfigurer courierOpenEntityManagerInView(@Qualifier("courierEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return ServicePersistence.openEntityManagerInView(entityManagerFactory);
  }
}
