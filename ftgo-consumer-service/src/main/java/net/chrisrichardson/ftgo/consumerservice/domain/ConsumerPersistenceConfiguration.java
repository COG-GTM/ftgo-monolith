package net.chrisrichardson.ftgo.consumerservice.domain;

import net.chrisrichardson.ftgo.common.persistence.ServicePersistence;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
 * Persistence unit of the consumer service, stored in its own ftgo_consumer schema.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = ConsumerRepository.class,
        entityManagerFactoryRef = "consumerEntityManagerFactory",
        transactionManagerRef = ConsumerPersistenceConfiguration.TRANSACTION_MANAGER)
public class ConsumerPersistenceConfiguration {

  public static final String TRANSACTION_MANAGER = "consumerTransactionManager";

  @Bean
  public DataSource consumerDataSource(DataSourceProperties dataSourceProperties,
                                  @Value("${ftgo.consumer-service.datasource.url:}") String url) {
    return ServicePersistence.migratedDataSource(dataSourceProperties, url,
            "ftgo_consumer", "classpath:db/migration/consumer");
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean consumerEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                        @Qualifier("consumerDataSource") DataSource dataSource,
                                                                        JpaProperties jpaProperties) {
    return ServicePersistence.entityManagerFactory(builder, dataSource, jpaProperties, "consumer", Consumer.class);
  }

  @Bean(TRANSACTION_MANAGER)
  public PlatformTransactionManager consumerTransactionManager(@Qualifier("consumerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean
  @ConditionalOnProperty(prefix = "spring.jpa", name = "open-in-view", havingValue = "true", matchIfMissing = true)
  public WebMvcConfigurer consumerOpenEntityManagerInView(@Qualifier("consumerEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return ServicePersistence.openEntityManagerInView(entityManagerFactory);
  }
}
