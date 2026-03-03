# =============================================================================
# Fluentd Docker Image for FTGO Log Aggregation
# =============================================================================
# Custom Fluentd image with Elasticsearch and Kubernetes plugins.
# Used in both Docker Compose (local dev) and as the base for K8s DaemonSet.
# =============================================================================

FROM fluent/fluentd:v1.16-1

USER root

# Install required plugins
RUN gem install fluent-plugin-elasticsearch -v 5.4.3 --no-document && \
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
