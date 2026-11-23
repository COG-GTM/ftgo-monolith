package net.chrisrichardson.ftgo.restaurantservice.domain;

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
 * Persistence unit of the restaurant service, stored in its own ftgo_restaurant schema.
 */
@Configuration
@EnableJpaRepositories(basePackageClasses = RestaurantRepository.class,
        entityManagerFactoryRef = "restaurantEntityManagerFactory",
        transactionManagerRef = RestaurantPersistenceConfiguration.TRANSACTION_MANAGER)
public class RestaurantPersistenceConfiguration {

  public static final String TRANSACTION_MANAGER = "restaurantTransactionManager";

  @Bean
  public DataSource restaurantDataSource(DataSourceProperties dataSourceProperties,
                                  @Value("${ftgo.restaurant-service.datasource.url:}") String url) {
    return ServicePersistence.migratedDataSource(dataSourceProperties, url,
            "ftgo_restaurant", "classpath:db/migration/restaurant");
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean restaurantEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                        @Qualifier("restaurantDataSource") DataSource dataSource,
                                                                        JpaProperties jpaProperties) {
    return ServicePersistence.entityManagerFactory(builder, dataSource, jpaProperties, "restaurant", Restaurant.class);
  }

  @Bean(TRANSACTION_MANAGER)
  public PlatformTransactionManager restaurantTransactionManager(@Qualifier("restaurantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  @Bean
  @ConditionalOnProperty(prefix = "spring.jpa", name = "open-in-view", havingValue = "true", matchIfMissing = true)
  public WebMvcConfigurer restaurantOpenEntityManagerInView(@Qualifier("restaurantEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
    return ServicePersistence.openEntityManagerInView(entityManagerFactory);
  }
}
