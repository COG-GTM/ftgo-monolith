# =============================================================================
# Fluentd Docker Image for FTGO Log Aggregation
# =============================================================================
# Custom Fluentd image with Elasticsearch and Kubernetes plugins.
# Used in both Docker Compose (local dev) and as the base for K8s DaemonSet.
# =============================================================================

FROM fluent/fluentd:v1.16-debian-1

USER root

# Install build dependencies for native gem extensions
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        build-essential \
        libffi-dev && \
    rm -rf /var/lib/apt/lists/*

# Install required plugins
# Pin elasticsearch gem to 8.x to avoid compatibility issues with fluent-plugin-elasticsearch
RUN gem install elasticsearch -v 8.17.0 --no-document && \
    gem install elastic-transport -v 8.3.5 --no-document && \
    gem install fluent-plugin-elasticsearch -v 5.4.3 --no-document && \
    gem install fluent-plugin-kubernetes_metadata_filter -v 3.4.0 --no-document && \
    gem install fluent-plugin-multi-format-parser -v 1.1.0 --no-document && \
    gem install fluent-plugin-rewrite-tag-filter -v 2.4.0 --no-document

# Create buffer directory
RUN mkdir -p /var/log/fluentd-buffers && \
    chown -R fluent:fluent /var/log/fluentd-buffers

# Copy configuration
COPY fluent.conf /fluentd/etc/fluent.conf

USER fluent

EXPOSE 24224 24224/udp

CMD ["fluentd", "-c", "/fluentd/etc/fluent.conf"]
