package net.chrisrichardson.ftgo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "/consumers")
public class ConsumerProxyController {

  private final RestTemplate restTemplate;
  private final String consumerServiceUrl;

  public ConsumerProxyController(
          RestTemplate consumerServiceRestTemplate,
          @Value("${consumer.service.url:http://localhost:8082}") String consumerServiceUrl) {
    this.restTemplate = consumerServiceRestTemplate;
    this.consumerServiceUrl = consumerServiceUrl;
  }

  @RequestMapping(method = RequestMethod.POST)
  public ResponseEntity<String> createConsumer(@RequestBody String body) {
    return forward("/consumers", HttpMethod.POST, body);
  }

  @RequestMapping(method = RequestMethod.GET, path = "/{consumerId}")
  public ResponseEntity<String> getConsumer(@PathVariable String consumerId) {
    return forward("/consumers/" + consumerId, HttpMethod.GET, null);
  }

  private ResponseEntity<String> forward(String path, HttpMethod method, String body) {
    String url = consumerServiceUrl + path;
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> entity = new HttpEntity<>(body, headers);

    try {
      return restTemplate.exchange(url, method, entity, String.class);
    } catch (HttpClientErrorException e) {
      return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
    }
  }
}
