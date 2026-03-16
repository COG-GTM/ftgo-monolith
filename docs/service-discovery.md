# Service Discovery

## Overview

Service discovery enables FTGO microservices to locate and communicate with each other without hard-coded addresses. The FTGO platform uses **Kubernetes-native DNS-based service discovery**, which provides a simple, reliable, and infrastructure-native approach.

## Architecture Decision

### Why Kubernetes-Native DNS?

We chose Kubernetes DNS-based service discovery over external registries (Eureka, Consul, Zookeeper) for the following reasons:

| Factor | K8s DNS | External Registry (e.g., Eureka) |
|--------|---------|----------------------------------|
| Infrastructure overhead | None (built-in) | Requires dedicated cluster |
| Operational complexity | Low | High (manage registry HA) |
| Spring Boot compatibility | Works with RestTemplate/WebClient | Requires Spring Cloud Netflix |
| Service mesh ready | Yes | Requires adapter |
| Health check integration | Native (K8s probes) | Application-level |
| Multi-language support | Yes (any HTTP client) | Java-centric |

### ADR Reference

See `docs/adr/` for the architecture decision record on service discovery.

## How It Works

### Kubernetes DNS Resolution

Every Kubernetes Service object gets a DNS entry automatically:

```
<service-name>.<namespace>.svc.cluster.local
```

For FTGO services:

| Service | DNS Name | Port |
|---------|----------|------|
| order-service | `order-service.ftgo-dev.svc.cluster.local` | 8081 |
| consumer-service | `consumer-service.ftgo-dev.svc.cluster.local` | 8082 |
| restaurant-service | `restaurant-service.ftgo-dev.svc.cluster.local` | 8083 |
| courier-service | `courier-service.ftgo-dev.svc.cluster.local` | 8084 |
| api-gateway | `api-gateway.ftgo-dev.svc.cluster.local` | 8080 |
| ftgo-mysql | `ftgo-mysql.ftgo-dev.svc.cluster.local` | 3306 |

### Short DNS Names

Within the same namespace, services can use short names:

```
http://order-service:8081/orders
http://consumer-service:8082/consumers
```

### Cross-Namespace Communication

For cross-namespace communication (e.g., staging service calling a shared database):

```
http://ftgo-mysql.ftgo-shared.svc.cluster.local:3306
```

## Service Configuration

### Application Properties

Each service configures downstream service URLs through environment variables or ConfigMaps:

```properties
# In application.properties or configmap.yaml
ftgo.services.order-service.url=http://order-service:8081
ftgo.services.consumer-service.url=http://consumer-service:8082
ftgo.services.restaurant-service.url=http://restaurant-service:8083
ftgo.services.courier-service.url=http://courier-service:8084
```

### ConfigMap Example

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: order-service-config
  namespace: ftgo-dev
data:
  CONSUMER_SERVICE_URL: "http://consumer-service:8082"
  RESTAURANT_SERVICE_URL: "http://restaurant-service:8083"
  SPRING_DATASOURCE_URL: "jdbc:mysql://ftgo-mysql:3306/ftgo_order_service"
```

### Kubernetes Service Definition

Each microservice has a ClusterIP Service for internal discovery:

```yaml
apiVersion: v1
kind: Service
metadata:
  name: order-service
  namespace: ftgo-dev
  labels:
    app: order-service
spec:
  type: ClusterIP
  selector:
    app: order-service
  ports:
    - port: 8081
      targetPort: 8081
      protocol: TCP
      name: http
```

## Client-Side Communication

### RestTemplate with Service Discovery

Services use Spring's `RestTemplate` with Kubernetes DNS names:

```java
@Configuration
public class ServiceClientConfiguration {

    @Value("${ftgo.services.consumer-service.url}")
    private String consumerServiceUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Usage in a service:
    // restTemplate.getForObject(consumerServiceUrl + "/consumers/{id}", Consumer.class, id);
}
```

### Resilient Service Calls

Combine service discovery with resilience patterns for reliable communication:

```java
@Service
public class OrderServiceClient {

    private final RestTemplate restTemplate;
    private final ResilientServiceCall resilientCall;

    public OrderServiceClient(RestTemplate restTemplate,
                              CircuitBreakerFactory cbFactory,
                              RetryFactory retryFactory,
                              BulkheadFactory bhFactory,
                              RateLimiterFactory rlFactory) {
        this.restTemplate = restTemplate;
        this.resilientCall = new ResilientServiceCall(
            cbFactory.getCircuitBreaker("consumer-service"),
            retryFactory.getRetry("consumer-service"),
            bhFactory.getBulkhead("consumer-service"),
            rlFactory.getRateLimiter("consumer-service")
        );
    }

    public ConsumerResponse getConsumer(long consumerId) {
        return resilientCall.executeWithFallback(
            () -> restTemplate.getForObject(
                consumerServiceUrl + "/consumers/" + consumerId,
                ConsumerResponse.class),
            ConsumerResponse.fallback()
        );
    }
}
```

## Load Balancing

### Kubernetes Service Load Balancing

Kubernetes Services provide built-in load balancing across pod replicas using `iptables` or `IPVS` rules. This is transparent to the application.

```
Client Request
      |
      v
  K8s Service (ClusterIP)
      |
      +---> Pod 1 (order-service replica)
      +---> Pod 2 (order-service replica)
      +---> Pod 3 (order-service replica)
```

### Session Affinity (if needed)

For stateful interactions, configure session affinity:

```yaml
spec:
  sessionAffinity: ClientIP
  sessionAffinityConfig:
    clientIP:
      timeoutSeconds: 600
```

## DNS Caching

### JVM DNS Cache

The JVM caches DNS lookups by default. For Kubernetes environments where pod IPs change frequently, configure the JVM DNS TTL:

```properties
# In JVM system properties or security.properties
networkaddress.cache.ttl=30
networkaddress.cache.negative.ttl=5
```

Or set via environment variable in the Kubernetes deployment:

```yaml
env:
  - name: JAVA_OPTS
    value: "-Dsun.net.inetaddr.ttl=30 -Dsun.net.inetaddr.negative.ttl=5"
```

## Environments

| Environment | Namespace | DNS Suffix |
|-------------|-----------|------------|
| Development | `ftgo-dev` | `.ftgo-dev.svc.cluster.local` |
| Staging | `ftgo-staging` | `.ftgo-staging.svc.cluster.local` |
| Production | `ftgo-prod` | `.ftgo-prod.svc.cluster.local` |

## Future Considerations

### Service Mesh (Istio/Linkerd)

For advanced traffic management, the platform can adopt a service mesh layer which provides:

- Mutual TLS (mTLS) between services
- Advanced load balancing (weighted, canary)
- Traffic mirroring for testing
- Distributed tracing integration

The current Kubernetes-native DNS approach is fully compatible with service mesh adoption. No application code changes are required when introducing a service mesh.
