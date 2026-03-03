# =============================================================================
# FTGO Microservices - Docker Bake Configuration
# =============================================================================
# Defines multi-service build targets for efficient parallel builds.
# Usage: docker buildx bake -f infrastructure/docker/docker-bake.hcl
# =============================================================================

variable "REGISTRY" {
  default = "ghcr.io/cog-gtm"
}

variable "IMAGE_PREFIX" {
  default = "ftgo"
}

variable "TAG" {
  default = "latest"
}

variable "GIT_SHA" {
  default = ""
}

# Common settings shared across all service builds
group "default" {
  targets = [
    "ftgo-order-service",
    "ftgo-consumer-service",
    "ftgo-restaurant-service",
    "ftgo-courier-service"
  ]
}

# ---------------------------------------------------------------------------
# Service Targets
# ---------------------------------------------------------------------------

target "ftgo-order-service" {
  context    = "../.."
  dockerfile = "services/ftgo-order-service/Dockerfile"
  tags = [
    "${REGISTRY}/${IMAGE_PREFIX}-ftgo-order-service:${TAG}",
    notequal("", GIT_SHA) ? "${REGISTRY}/${IMAGE_PREFIX}-ftgo-order-service:${GIT_SHA}" : "",
  ]
  labels = {
    "org.opencontainers.image.source"      = "https://github.com/COG-GTM/ftgo-monolith"
    "org.opencontainers.image.description" = "FTGO Order Service"
  }
  cache-from = ["type=gha"]
  cache-to   = ["type=gha,mode=max"]
}

target "ftgo-consumer-service" {
  context    = "../.."
  dockerfile = "services/ftgo-consumer-service/Dockerfile"
  tags = [
    "${REGISTRY}/${IMAGE_PREFIX}-ftgo-consumer-service:${TAG}",
    notequal("", GIT_SHA) ? "${REGISTRY}/${IMAGE_PREFIX}-ftgo-consumer-service:${GIT_SHA}" : "",
  ]
  labels = {
    "org.opencontainers.image.source"      = "https://github.com/COG-GTM/ftgo-monolith"
    "org.opencontainers.image.description" = "FTGO Consumer Service"
  }
  cache-from = ["type=gha"]
  cache-to   = ["type=gha,mode=max"]
}

target "ftgo-restaurant-service" {
  context    = "../.."
  dockerfile = "services/ftgo-restaurant-service/Dockerfile"
  tags = [
    "${REGISTRY}/${IMAGE_PREFIX}-ftgo-restaurant-service:${TAG}",
    notequal("", GIT_SHA) ? "${REGISTRY}/${IMAGE_PREFIX}-ftgo-restaurant-service:${GIT_SHA}" : "",
  ]
  labels = {
    "org.opencontainers.image.source"      = "https://github.com/COG-GTM/ftgo-monolith"
    "org.opencontainers.image.description" = "FTGO Restaurant Service"
  }
  cache-from = ["type=gha"]
  cache-to   = ["type=gha,mode=max"]
}

target "ftgo-courier-service" {
  context    = "../.."
  dockerfile = "services/ftgo-courier-service/Dockerfile"
  tags = [
    "${REGISTRY}/${IMAGE_PREFIX}-ftgo-courier-service:${TAG}",
    notequal("", GIT_SHA) ? "${REGISTRY}/${IMAGE_PREFIX}-ftgo-courier-service:${GIT_SHA}" : "",
  ]
  labels = {
    "org.opencontainers.image.source"      = "https://github.com/COG-GTM/ftgo-monolith"
    "org.opencontainers.image.description" = "FTGO Courier Service"
  }
  cache-from = ["type=gha"]
  cache-to   = ["type=gha,mode=max"]
}
