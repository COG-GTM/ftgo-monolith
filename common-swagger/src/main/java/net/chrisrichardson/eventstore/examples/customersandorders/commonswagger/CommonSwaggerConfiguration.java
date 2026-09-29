package net.chrisrichardson.eventstore.examples.customersandorders.commonswagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonSwaggerConfiguration {

  @Bean
  public OpenAPI ftgoOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("FTGO Application API")
                    .version("v1")
                    .description("REST API of the FTGO (Food To Go) monolith: consumers, restaurants, orders, couriers and API request tracking. "
                            + "Errors raised by the domain are returned as an `ErrorResponse` body.")
                    .license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
            .addTagsItem(new Tag().name("Consumers").description("Register and look up consumers"))
            .addTagsItem(new Tag().name("Restaurants").description("Register restaurants and their menus"))
            .addTagsItem(new Tag().name("Orders").description("Place, revise and cancel orders and drive them through their lifecycle"))
            .addTagsItem(new Tag().name("Couriers").description("Manage couriers, their availability, location and workload"))
            .addTagsItem(new Tag().name("API Tracking").description("Inspect logged API requests and aggregate statistics"));
  }
}
