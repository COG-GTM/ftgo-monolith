package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CourierClient {
    private final RestTemplate restTemplate;
    private final String courierServiceUrl;

    public CourierClient(RestTemplate restTemplate,
                        @Value("${courier.service.url:http://localhost:8084}") String courierServiceUrl) {
        this.restTemplate = restTemplate;
        this.courierServiceUrl = courierServiceUrl;
    }

    public List<Courier> findAllAvailable() {
        ResponseEntity<List<Courier>> response = restTemplate.exchange(
            courierServiceUrl + "/couriers?available=true",
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<Courier>>() {}
        );
        return response.getBody();
    }
}
