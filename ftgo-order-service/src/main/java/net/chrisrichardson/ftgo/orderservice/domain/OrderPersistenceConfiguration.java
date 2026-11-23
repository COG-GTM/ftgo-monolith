package net.chrisrichardson.ftgo.orderservice.domain;

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
 * Persistence unit of the order service, stored in its own ftgo_order schema.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = OrderRepository.class,
        entityManagerFactoryRef = "orderEntityManagerFactory",
        transactionManagerRef = OrderPersistenceConfiguration.TRANSACTION_MANAGER)
public class OrderPersistenceConfiguration {

  public static final String TRANSACTION_MANAGER = "orderTransactionManager";

  @Bean
  public DataSource orderDataSource(DataSourceProperties dataSourceProperties,
                                  @Value("${ftgo.order-service.datasource.url:}") String url) {
    return ServicePersistence.migratedDataSource(dataSourceProperties, url,
            "ftgo_order", "classpath:db/migration/order");
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean orderEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                        @Qualifier("orderDataSource") DataSource dataSource,
                                                                        JpaProperties jpaProperties) {
    return ServicePersistence.entityManagerFactory(builder, dataSource, jpaProperties, "order", Order.class);
  }

  @Bean(TRANSACTION_MANAGER)
  public PlatformTransactionManager orderTransactionManager(@Qualifier("orderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean
  @ConditionalOnProperty(prefix = "spring.jpa", name = "open-in-view", havingValue = "true", matchIfMissing = true)
  public WebMvcConfigurer orderOpenEntityManagerInView(@Qualifier("orderEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return ServicePersistence.openEntityManagerInView(entityManagerFactory);
  }
}
