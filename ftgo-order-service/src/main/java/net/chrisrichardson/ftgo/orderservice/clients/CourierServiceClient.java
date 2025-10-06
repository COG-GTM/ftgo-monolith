package net.chrisrichardson.ftgo.orderservice.clients;

import net.chrisrichardson.ftgo.courierservice.api.GetCourierResponse;
import net.chrisrichardson.ftgo.domain.Courier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourierServiceClient {
    private final WebClient webClient;
    
    public CourierServiceClient(@Value("${courier.service.url}") String baseUrl) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }
    
    public List<Courier> findAllAvailable() {
        List<GetCourierResponse> response = webClient.get()
            .uri("/couriers/available")
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<List<GetCourierResponse>>() {})
            .block();
        
        if (response != null) {
            return response.stream()
                .map(cr -> {
                    Courier courier = new Courier(cr.getName(), null);
                    return courier;
                })
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
