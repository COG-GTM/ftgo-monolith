# Kubernetes Liveness and Readiness Probes

## Overview

Kubernetes probes are used to determine the health and readiness of FTGO service pods. Proper probe configuration ensures zero-downtime deployments, automatic recovery from failures, and correct traffic routing.

## Probe Types

### Startup Probe

The startup probe determines when a container application has started. All other probes are disabled until the startup probe succeeds. This is critical for Java/Spring Boot applications with longer startup times.

**Purpose**: Prevent liveness/readiness checks from killing a slow-starting JVM.

### Liveness Probe

The liveness probe determines if a container is running correctly. If the liveness probe fails, Kubernetes kills the container and restarts it.

**Purpose**: Detect and recover from deadlocks, memory leaks, or hung processes.

### Readiness Probe

The readiness probe determines if a container is ready to accept traffic. If the readiness probe fails, the pod is removed from Service endpoints (no traffic is routed to it).

**Purpose**: Ensure traffic only goes to pods that can serve requests.

## Probe Configuration

### Standard Service Probe Template

The following probe configuration is applied to all FTGO microservice deployments:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: <service-name>
spec:
  template:
    spec:
      containers:
        - name: <service-name>
          ports:
            - containerPort: <service-port>
          startupProbe:
            httpGet:
              path: /actuator/health
              port: <service-port>
            initialDelaySeconds: 10
            periodSeconds: 5
            failureThreshold: 12
            # Allows up to 70s (10 + 12*5) for startup
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: <service-port>
            initialDelaySeconds: 60
            periodSeconds: 15
            failureThreshold: 3
            timeoutSeconds: 5
            # Restarts pod after 3 consecutive failures (45s of unhealthy)
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: <service-port>
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 3
            successThreshold: 1
            timeoutSeconds: 5
            # Removes pod from service after 3 consecutive failures (30s)
```

### Per-Service Configuration

| Service | Port | Startup Timeout | Liveness Period | Readiness Period |
|---------|------|-----------------|-----------------|------------------|
| order-service | 8081 | 70s | 15s | 10s |
| consumer-service | 8082 | 70s | 15s | 10s |
| restaurant-service | 8083 | 70s | 15s | 10s |
| courier-service | 8084 | 70s | 15s | 10s |
| api-gateway | 8080 | 70s | 15s | 10s |

### API Gateway Special Considerations

The API Gateway has additional considerations for probe configuration because it depends on downstream services:

```yaml
# API Gateway readiness should check its own health, not downstream health
readinessProbe:
  httpGet:
    path: /actuator/health
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10
  failureThreshold: 3
```

The API Gateway should NOT fail its readiness probe when downstream services are unavailable. Circuit breakers handle downstream failures gracefully.

## Timing Considerations

### JVM Startup Overhead

Spring Boot applications on the JVM have longer startup times compared to native applications. The startup probe configuration accounts for:

- JVM class loading and JIT compilation
- Spring context initialization
- Database connection pool initialization
- Flyway migration execution (first deployment)

### Rolling Update Impact

During a rolling update (`maxSurge: 1`, `maxUnavailable: 0`):

1. New pod starts, startup probe begins checking
2. Once startup probe succeeds, readiness probe starts
3. Once readiness probe succeeds, pod receives traffic
4. Old pod is terminated gracefully

**Timeline**:
```
0s          10s                     70s (max)
|-----------|--------------------------|
  init delay    startup probe checks
                                       readiness begins
                                       ~40s until traffic
```

### Graceful Shutdown

Services should handle `SIGTERM` gracefully by:

1. Failing the readiness probe (stop accepting new requests)
2. Completing in-flight requests
3. Closing database connections
4. Shutting down cleanly

Configure the termination grace period in the deployment:

```yaml
spec:
  template:
    spec:
      terminationGracePeriodSeconds: 30
```

## Troubleshooting

### Common Issues

| Symptom | Likely Cause | Resolution |
|---------|-------------|------------|
| Pod keeps restarting (CrashLoopBackOff) | Startup probe timing too aggressive | Increase `failureThreshold` or `periodSeconds` on startup probe |
| Pod running but not receiving traffic | Readiness probe failing | Check `/actuator/health` response, verify database connectivity |
| Intermittent 503 errors during deploy | Readiness probe too aggressive | Increase `successThreshold` or decrease `periodSeconds` |
| Pod killed during startup | Liveness probe fires before startup complete | Ensure startup probe is configured with sufficient timeout |

### Debugging Commands

```bash
# Check pod probe status
kubectl describe pod <pod-name> -n <namespace>

# View probe events
kubectl get events -n <namespace> --field-selector involvedObject.name=<pod-name>

# Manually test health endpoint
kubectl exec -it <pod-name> -n <namespace> -- curl -s http://localhost:<port>/actuator/health

# View pod logs during startup
kubectl logs <pod-name> -n <namespace> --previous
```
