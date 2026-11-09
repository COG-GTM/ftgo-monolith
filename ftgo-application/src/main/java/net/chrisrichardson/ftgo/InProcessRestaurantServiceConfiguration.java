package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.restaurantservice.RestaurantServiceConfiguration;
import net.chrisrichardson.ftgo.restaurantservice.api.client.RestaurantServiceClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Strangler seam: the restaurant module only runs inside the monolith in in-process mode (the default). In remote
 * mode the monolith neither serves /restaurants nor touches restaurant tables; the standalone service owns both.
 */
@Configuration
@ConditionalOnProperty(name = RestaurantServiceClient.MODE_PROPERTY,
        havingValue = RestaurantServiceClient.MODE_IN_PROCESS, matchIfMissing = true)
@Import(RestaurantServiceConfiguration.class)
public class InProcessRestaurantServiceConfiguration {
}
