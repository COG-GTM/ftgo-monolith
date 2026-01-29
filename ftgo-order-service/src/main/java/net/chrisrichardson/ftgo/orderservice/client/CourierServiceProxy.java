package net.chrisrichardson.ftgo.orderservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

public class CourierServiceProxy {

    private static final Logger logger = LoggerFactory.getLogger(CourierServiceProxy.class);

    private final RestTemplate restTemplate;
    private final String courierServiceUrl;

    public CourierServiceProxy(RestTemplate restTemplate, String courierServiceUrl) {
        this.restTemplate = restTemplate;
        this.courierServiceUrl = courierServiceUrl;
    }

    public List<CourierDTO> findAllAvailable() {
        logger.debug("Calling courier service to find all available couriers");
        try {
            ResponseEntity<List<CourierDTO>> response = restTemplate.exchange(
                    courierServiceUrl + "/couriers/available",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<CourierDTO>>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            logger.error("Error calling courier service: {}", e.getMessage());
            throw new CourierServiceException("Failed to get available couriers", e);
        }
    }

    public CourierDTO findById(long courierId) {
        logger.debug("Calling courier service to find courier by id: {}", courierId);
        try {
            ResponseEntity<CourierDTO> response = restTemplate.getForEntity(
                    courierServiceUrl + "/couriers/{courierId}",
                    CourierDTO.class,
                    courierId
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CourierNotFoundException(courierId);
            }
            logger.error("Error calling courier service: {}", e.getMessage());
            throw new CourierServiceException("Failed to get courier", e);
        }
    }

    public void addPickupAction(long courierId, Long orderId) {
        logger.debug("Calling courier service to add pickup action for courier: {} order: {}", courierId, orderId);
        addAction(courierId, orderId, "PICKUP", null);
    }

    public void addDropoffAction(long courierId, Long orderId, LocalDateTime deliveryTime) {
        logger.debug("Calling courier service to add dropoff action for courier: {} order: {}", courierId, orderId);
        addAction(courierId, orderId, "DROPOFF", deliveryTime);
    }

    private void addAction(long courierId, Long orderId, String actionType, LocalDateTime time) {
        try {
            AddActionRequest request = new AddActionRequest(orderId, actionType, time);
            restTemplate.postForEntity(
                    courierServiceUrl + "/couriers/{courierId}/actions",
                    request,
                    String.class,
                    courierId
            );
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new CourierNotFoundException(courierId);
            }
            logger.error("Error calling courier service: {}", e.getMessage());
            throw new CourierServiceException("Failed to add action to courier", e);
        }
    }

    public static class AddActionRequest {
        private Long orderId;
        private String actionType;
        private LocalDateTime time;

        public AddActionRequest() {
        }

        public AddActionRequest(Long orderId, String actionType, LocalDateTime time) {
            this.orderId = orderId;
            this.actionType = actionType;
            this.time = time;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public String getActionType() {
            return actionType;
        }

        public void setActionType(String actionType) {
            this.actionType = actionType;
        }

        public LocalDateTime getTime() {
            return time;
        }

        public void setTime(LocalDateTime time) {
            this.time = time;
        }
    }
}
